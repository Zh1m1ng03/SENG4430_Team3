package com.seng4430.qualitytesttool.staticanalysis.metric.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DuplicateCodeAnalyserTest {

    @TempDir
    Path tempDir;

    private DuplicateCodeAnalyser analyser;

    @BeforeEach
    void setUp() {
        analyser = new DuplicateCodeAnalyser();
    }

    @Test
    void testAnalyse_emptyInput_returnsDefaultGood() {
        MetricResult result = analyser.analyse(List.of());
        assertEquals(100.0, result.getScore(), 0.001);
        assertEquals(MetricRating.GOOD, result.getRating());
        assertTrue(result.getDetails().get(0).contains("No valid source files found."));
    }

    @Test
    void testAnalyse_unitsWithoutStoragePath_returnsDefaultGood() {
        CompilationUnit inMemory = StaticJavaParser.parse("class InMemory { int x() { return 1; } }");
        MetricResult result = analyser.analyse(List.of(inMemory));

        assertEquals(100.0, result.getScore(), 0.001);
        assertEquals(MetricRating.GOOD, result.getRating());
        assertTrue(result.getDetails().get(0).contains("No valid source files found."));
    }

    @Test
    void testAnalyse_singleSmallFile_noDuplication() throws Exception {
        CompilationUnit cu = writeAndParse("Single.java", """
                class Single {
                    int add(int a, int b) { return a + b; }
                }
                """);

        MetricResult result = analyser.analyse(List.of(cu));
        MetricsSnapshot snapshot = extractMetrics(result);

        assertEquals(0, snapshot.redundantLines);
        assertEquals(0, snapshot.coverageLines);
        assertEquals(100.0, result.getScore(), 0.001);
        assertEquals(MetricRating.GOOD, result.getRating());
    }

    @Test
    void testAnalyse_twoFileClone_hasRedundancy() throws Exception {
        CompilationUnit a = writeAndParse("A.java", classWithCloneBlock("A"));
        CompilationUnit b = writeAndParse("B.java", classWithCloneBlock("B"));

        MetricResult result = analyser.analyse(List.of(a, b));
        MetricsSnapshot snapshot = extractMetrics(result);

        assertTrue(snapshot.coverageLines > 0, "Expected duplicated coverage lines > 0");
        assertTrue(snapshot.redundantLines > 0, "Expected redundant lines > 0");
        assertTrue(snapshot.coverageLines >= snapshot.redundantLines, "Coverage should be >= redundancy");
        assertTrue(snapshot.totalLines >= snapshot.coverageLines, "Coverage should never exceed total lines");
        assertTrue(result.getScore() < 100.0, "Score should drop when redundancy exists");
    }

    @Test
    void testAnalyse_threeFileClone_moreRedundancyThanTwoFileClone() throws Exception {
        CompilationUnit a = writeAndParse("A.java", classWithCloneBlock("A"));
        CompilationUnit b = writeAndParse("B.java", classWithCloneBlock("B"));
        CompilationUnit c = writeAndParse("C.java", classWithCloneBlock("C"));

        MetricsSnapshot twoFile = extractMetrics(analyser.analyse(List.of(a, b)));
        MetricsSnapshot threeFile = extractMetrics(analyser.analyse(List.of(a, b, c)));

        assertTrue(threeFile.redundantLines > twoFile.redundantLines,
                "Redundancy should increase when the same block appears in a third file");
    }

    @Test
    void testAnalyse_threeFileClone_ratingCritical() throws Exception {
        CompilationUnit a = writeAndParse("A.java", classWithCloneBlock("A"));
        CompilationUnit b = writeAndParse("B.java", classWithCloneBlock("B"));
        CompilationUnit c = writeAndParse("C.java", classWithCloneBlock("C"));

        MetricResult result = analyser.analyse(List.of(a, b, c));
        assertEquals(MetricRating.CRITICAL, result.getRating());
        assertTrue(result.getScore() < 50.0, "Three identical files should produce high redundancy");
    }

    @Test
    void testAnalyse_addingUniqueFile_reducesRedundancyRatio() throws Exception {
        CompilationUnit a = writeAndParse("A.java", classWithCloneBlock("A"));
        CompilationUnit b = writeAndParse("B.java", classWithCloneBlock("B"));
        CompilationUnit unique = writeAndParse("Unique.java", classWithUniqueBlock("Unique"));

        MetricsSnapshot cloneOnly = extractMetrics(analyser.analyse(List.of(a, b)));
        MetricsSnapshot withUnique = extractMetrics(analyser.analyse(List.of(a, b, unique)));

        assertTrue(withUnique.redundancyRatioPct < cloneOnly.redundancyRatioPct,
                "Adding unique code should dilute overall redundancy ratio");
    }

    @Test
    void testAnalyse_detailsContainExpectedSections() throws Exception {
        CompilationUnit a = writeAndParse("A.java", classWithCloneBlock("A"));
        CompilationUnit b = writeAndParse("B.java", classWithCloneBlock("B"));

        MetricResult result = analyser.analyse(List.of(a, b));
        String details = String.join("\n", result.getDetails());

        assertTrue(details.contains("CPD config: minTokens=30, ignoreIdentifiers=true"));
        assertTrue(details.contains("File Summary (sorted by redundancy ratio):"));
        assertTrue(details.contains("A.java") || details.contains("B.java"));
    }

    @Test
    void testMetricMetadata() {
        assertEquals("Duplicate Code Ratio", analyser.getMetricName());
        assertEquals("Maintainability", analyser.getQualityAspect());
    }

    private CompilationUnit writeAndParse(String fileName, String source) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, source, StandardCharsets.UTF_8);
        return StaticJavaParser.parse(file);
    }

    private String classWithCloneBlock(String className) {
        return "class " + className + " {\n" +
                "    int calculate(int a, int b, int c) {\n" +
                "        int x1 = a + b;\n" +
                "        int x2 = x1 * c;\n" +
                "        int x3 = x2 - a;\n" +
                "        int x4 = x3 + b;\n" +
                "        int x5 = x4 / 2;\n" +
                "        int x6 = x5 * 3;\n" +
                "        int x7 = x6 - c;\n" +
                "        int x8 = x7 + 10;\n" +
                "        int x9 = x8 - 4;\n" +
                "        int x10 = x9 + 8;\n" +
                "        if (x10 > 0) {\n" +
                "            x10 = x10 + 1;\n" +
                "        }\n" +
                "        return x10;\n" +
                "    }\n" +
                "}\n";
    }

    private String classWithUniqueBlock(String className) {
        return "class " + className + " {\n" +
                "    int uniqueCalc(int a, int b, int c) {\n" +
                "        int p1 = a * 11;\n" +
                "        int p2 = p1 + b * 13;\n" +
                "        int p3 = p2 - c * 17;\n" +
                "        int p4 = (p3 % 19) + 23;\n" +
                "        int p5 = p4 * 29 - a;\n" +
                "        int p6 = p5 / 31 + b;\n" +
                "        int p7 = p6 - 37 + c;\n" +
                "        int p8 = p7 * 41;\n" +
                "        int p9 = p8 / 43;\n" +
                "        int p10 = p9 + 47;\n" +
                "        if (p10 % 2 == 0) {\n" +
                "            p10 = p10 - 53;\n" +
                "        }\n" +
                "        return p10 + 59;\n" +
                "    }\n" +
                "}\n";
    }

    private MetricsSnapshot extractMetrics(MetricResult result) {
        String line1 = result.getDetails().get(0);
        String line2 = result.getDetails().get(1);

        Pattern redundantPattern = Pattern.compile("Total code lines: (\\d+) \\| Redundant code lines: (\\d+) \\| Redundancy ratio: ([0-9.]+)%");
        Pattern coveragePattern = Pattern.compile("Duplicated coverage lines: (\\d+) \\| Coverage ratio: ([0-9.]+)%");

        Matcher m1 = redundantPattern.matcher(line1);
        Matcher m2 = coveragePattern.matcher(line2);

        if (!m1.find() || !m2.find()) {
            throw new AssertionError("Unexpected details format: " + result.getDetails());
        }

        return new MetricsSnapshot(
                Integer.parseInt(m1.group(1)),
                Integer.parseInt(m1.group(2)),
                Double.parseDouble(m1.group(3)),
                Integer.parseInt(m2.group(1)),
                Double.parseDouble(m2.group(2))
        );
    }

    private record MetricsSnapshot(
            int totalLines,
            int redundantLines,
            double redundancyRatioPct,
            int coverageLines,
            double coverageRatioPct
    ) {}
}
