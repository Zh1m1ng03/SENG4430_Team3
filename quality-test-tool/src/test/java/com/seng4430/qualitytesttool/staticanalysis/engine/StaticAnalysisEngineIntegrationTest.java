package com.seng4430.qualitytesttool.staticanalysis.engine;

import com.seng4430.qualitytesttool.shared.config.AnalysisConfig;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricRegistry;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricResult;
import com.seng4430.qualitytesttool.staticanalysis.metric.impl.CyclomaticComplexityAnalyser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * these tests exercise the full pipeline:
 *   real .java files on disk → loadPath() → parsedUnits → runOne()/runAll() → MetricResult
 */
class StaticAnalysisEngineIntegrationTest {

    @TempDir
    Path tempDir;

    private AnalysisConfig config;
    private StaticAnalysisEngine engine;

    @BeforeEach
    void setUp() {
        config = new AnalysisConfig();
        engine = new StaticAnalysisEngine(config);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void writeFile(String name, String source) throws IOException {
        Files.writeString(tempDir.resolve(name), source, StandardCharsets.UTF_8);
    }

    private void writeFileInSubdir(String subdir, String name, String source) throws IOException {
        Path dir = tempDir.resolve(subdir);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(name), source, StandardCharsets.UTF_8);
    }

    /** Minimal single-method class with no quality issues. */
    private String simpleClass(String className) {
        return "class " + className + " { void method() { int x = 1; } }";
    }

    private AnalysisConfig configWithIgnorePattern(String pattern) {
        AnalysisConfig cfg = new AnalysisConfig();
        cfg.getIgnore().setFilePatterns(List.of(pattern));
        return cfg;
    }

    private AnalysisConfig configWithIgnorePackage(String pkg) {
        AnalysisConfig cfg = new AnalysisConfig();
        cfg.getIgnore().setPackages(List.of(pkg));
        return cfg;
    }

    private CyclomaticComplexityAnalyser defaultCCAnalyser() {
        return new CyclomaticComplexityAnalyser(config.getCyclomaticComplexity());
    }

    // ── Group 1: Engine File Loading ──────────────────────────────────────────

    @Test
    void testLoad_countsJavaFiles() throws IOException {
        writeFile("A.java", simpleClass("A"));
        writeFile("B.java", simpleClass("B"));
        writeFile("C.java", simpleClass("C"));

        int count = engine.loadPath(tempDir);

        assertEquals(3, count);
        assertTrue(engine.isLoaded());
    }

    @Test
    void testLoad_excludesTestSuffix() throws IOException {
        writeFile("Main.java",      simpleClass("Main"));
        writeFile("MainTest.java",  simpleClass("MainTest"));
        writeFile("MainTests.java", simpleClass("MainTests"));

        int count = engine.loadPath(tempDir);

        assertEquals(1, count, "Files ending in Test.java and Tests.java must be excluded");
    }

    @Test
    void testLoad_excludesCustomFilePattern() throws IOException {
        writeFile("Real.java",     simpleClass("Real"));
        writeFile("FakeStub.java", simpleClass("FakeStub"));

        StaticAnalysisEngine eng = new StaticAnalysisEngine(configWithIgnorePattern("*Stub.java"));
        int count = eng.loadPath(tempDir);

        assertEquals(1, count, "*Stub.java pattern should exclude FakeStub.java");
    }

    @Test
    void testLoad_excludesPackage() throws IOException {
        writeFile("Normal.java", simpleClass("Normal"));
        writeFileInSubdir("com/example/generated", "Generated.java", simpleClass("Generated"));

        StaticAnalysisEngine eng = new StaticAnalysisEngine(configWithIgnorePackage("com.example.generated"));
        int count = eng.loadPath(tempDir);

        assertEquals(1, count, "Files under an excluded package path must be skipped");
    }

    // ── Group 2: Engine + runOne() handoff ────────────────────────────────────

    @Test
    void testRunOne_engineHandoffProducesValidResult() throws IOException {
        // Confirms the engine loads a file, parses it, and passes it to an analyser.
        // Per-analyser correctness is covered by unit tests.
        writeFile("A.java", simpleClass("A"));
        engine.loadPath(tempDir);

        MetricResult result = engine.runOne(defaultCCAnalyser());

        assertNotNull(result);
        assertFalse(result.getDetails().isEmpty(), "Engine must pass parsed units through to the analyser");
    }

    // ── Group 3: Engine + runAll() + MetricRegistry ───────────────────────────

    @Test
    void testRunAll_returnsFourResults() throws IOException {
        writeFile("A.java", simpleClass("A"));
        engine.loadPath(tempDir);

        List<MetricResult> results = engine.runAll(MetricRegistry.getAll(config));

        assertEquals(4, results.size(), "MetricRegistry must register exactly 4 analysers");
    }

    // ── Group 4: Cross-Analyser Consistency ──────────────────────────────────

    @Test
    void testRunAll_vs_runOne_ccScoresMatch() throws IOException {
        writeFile("A.java", simpleClass("A"));
        engine.loadPath(tempDir);

        double runOneScore = engine.runOne(defaultCCAnalyser()).getScore();

        double runAllScore = engine.runAll(MetricRegistry.getAll(config)).stream()
                .filter(r -> r.getMetricName().equals("Cyclomatic Complexity"))
                .findFirst().orElseThrow()
                .getScore();

        assertEquals(runOneScore, runAllScore, 0.001,
                "runOne() and runAll() must produce identical CC scores for the same loaded files");
    }
}
