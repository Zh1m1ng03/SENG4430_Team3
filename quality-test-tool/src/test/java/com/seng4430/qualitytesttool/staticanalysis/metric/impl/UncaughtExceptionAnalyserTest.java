package com.seng4430.qualitytesttool.staticanalysis.metric.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UncaughtExceptionAnalyserTest {

    private UncaughtExceptionAnalyser analyser;

    @BeforeEach
    void setUp() {
        analyser = new UncaughtExceptionAnalyser();
    }

    private CompilationUnit parse(String methodSource) {
        return StaticJavaParser.parse("class Test { " + methodSource + " }");
    }

    private int extractUncaught(MetricResult result) {
        return result.getDetails().stream()
                .filter(line -> line.contains("Uncaught throws:"))
                .mapToInt(line -> {
                    String part = line.substring(line.indexOf("Uncaught throws:") + 16).trim();
                    return Integer.parseInt(part.split("[^0-9]")[0]);
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError("No uncaught throws found: " + result.getDetails()));
    }

    @Test
    void testNoThrow_isZeroUncaught() {
        CompilationUnit cu = parse("void method() { int x = 1; }");
        MetricResult result = analyser.analyse(List.of(cu));
        assertEquals(0, extractUncaught(result));
    }

    @Test
    void testThrowOutsideTry_isUncaught() {
        CompilationUnit cu = parse(
                "void method() { throw new RuntimeException(); }"
        );
        MetricResult result = analyser.analyse(List.of(cu));
        assertEquals(1, extractUncaught(result));
    }

    @Test
    void testThrowInsideTry_isCaught() {
        CompilationUnit cu = parse(
                "void method() {" +
                        "  try { throw new RuntimeException(); }" +
                        "  catch (Exception e) { }" +
                        "}"
        );
        MetricResult result = analyser.analyse(List.of(cu));
        assertEquals(0, extractUncaught(result));
    }

    @Test
    void testThrowInCatchBlock_isUncaught() {
        CompilationUnit cu = parse(
                "void method() {" +
                        "  try { int x = 1; }" +
                        "  catch (Exception e) { throw new RuntimeException(); }" +
                        "}"
        );
        MetricResult result = analyser.analyse(List.of(cu));
        assertEquals(1, extractUncaught(result));
    }

    @Test
    void testMultipleThrowsOutsideTry() {
        CompilationUnit cu = parse(
                "void method(int x) {" +
                        "  if (x < 0) throw new IllegalArgumentException();" +
                        "  if (x > 100) throw new RuntimeException();" +
                        "}"
        );
        MetricResult result = analyser.analyse(List.of(cu));
        assertEquals(2, extractUncaught(result));
    }

    @Test
    void testMixedThrows_oneCaughtOneNot() {
        CompilationUnit cu = parse(
                "void method(int x) {" +
                        "  try { throw new RuntimeException(); }" +
                        "  catch (Exception e) { }" +
                        "  throw new IllegalStateException();" +
                        "}"
        );
        MetricResult result = analyser.analyse(List.of(cu));
        assertEquals(1, extractUncaught(result));
    }

    @Test
    void testScore_allSafeMethods_isGood() {
        CompilationUnit cu = parse(
                "void a() { int x = 1; }" +
                        "void b() { int y = 2; }"
        );
        MetricResult result = analyser.analyse(List.of(cu));
        assertEquals(100.0, result.getScore(), 0.001);
        assertEquals(MetricRating.GOOD, result.getRating());
        assertTrue(result.getDetails().contains("File Summary (sorted by uncaught ratio, all files):"));
        assertTrue(result.getDetails().stream().anyMatch(line ->
                line.matches("^  .*methods:\\s+\\d+ uncaught:\\s+0 ratio:\\s+0\\.00%$")));
    }

    @Test
    void testScore_noSafeMethods_isCritical() {
        CompilationUnit cu = parse(
                "void a() { throw new RuntimeException(); }" +
                        "void b() { throw new IllegalStateException(); }"
        );
        MetricResult result = analyser.analyse(List.of(cu));
        assertEquals(0.0, result.getScore(), 0.001);
        assertEquals(MetricRating.CRITICAL, result.getRating());
    }

    @Test
    void testScore_boundary_good() {
        String safe = "void safe%d() { int x = 1; } ";
        String unsafe = "void unsafe%d() { throw new RuntimeException(); } ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(String.format(safe, i));
        for (int i = 0; i < 2; i++) sb.append(String.format(unsafe, i));
        MetricResult result = analyser.analyse(List.of(parse(sb.toString())));
        assertEquals(80.0, result.getScore(), 0.001);
        assertEquals(MetricRating.GOOD, result.getRating());
    }

    @Test
    void testScore_boundary_warning() {
        String safe = "void safe%d() { int x = 1; } ";
        String unsafe = "void unsafe%d() { throw new RuntimeException(); } ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(String.format(safe, i));
        for (int i = 0; i < 5; i++) sb.append(String.format(unsafe, i));
        MetricResult result = analyser.analyse(List.of(parse(sb.toString())));
        assertEquals(50.0, result.getScore(), 0.001);
        assertEquals(MetricRating.WARNING, result.getRating());
    }

    @Test
    void testScore_belowWarning_isCritical() {
        String safe = "void safe%d() { int x = 1; } ";
        String unsafe = "void unsafe%d() { throw new RuntimeException(); } ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) sb.append(String.format(safe, i));
        for (int i = 0; i < 7; i++) sb.append(String.format(unsafe, i));
        MetricResult result = analyser.analyse(List.of(parse(sb.toString())));
        assertEquals(30.0, result.getScore(), 0.001);
        assertEquals(MetricRating.CRITICAL, result.getRating());
    }

    @Test
    void testEmptyClass_scoreIs100() {
        CompilationUnit cu = StaticJavaParser.parse("class Empty {}");
        MetricResult result = analyser.analyse(List.of(cu));
        assertEquals(100.0, result.getScore(), 0.001);
    }

    @Test
    void testInterface_isSkipped() {
        CompilationUnit cu = StaticJavaParser.parse(
                "interface MyInterface { void method(); }"
        );
        MetricResult result = analyser.analyse(List.of(cu));
        assertEquals(100.0, result.getScore(), 0.001);
    }

    @Test
    void testMetricName() {
        assertEquals("Uncaught Exceptions", analyser.getMetricName());
    }

    @Test
    void testQualityAspect() {
        assertEquals("Reliability", analyser.getQualityAspect());
    }

    @Test
    void testFileSummaryShownWithUpdatedFormatWhenIssuesExist() {
        CompilationUnit cu = parse(
                "void safe() { int x = 1; }" +
                        "void unsafe() { throw new RuntimeException(); }"
        );

        MetricResult result = analyser.analyse(List.of(cu));

        assertTrue(result.getDetails().contains("File Summary (sorted by uncaught ratio, files with issues only):"));
        assertTrue(result.getDetails().stream().anyMatch(line ->
                line.matches("^  .*methods:\\s+\\d+ uncaught:\\s+\\d+ ratio:\\s+\\d+\\.\\d{2}%$")));
    }
}
