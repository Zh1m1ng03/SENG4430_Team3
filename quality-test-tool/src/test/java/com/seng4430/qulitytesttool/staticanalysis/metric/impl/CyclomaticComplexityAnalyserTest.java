package com.seng4430.qulitytesttool.staticanalysis.metric.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.seng4430.qulitytesttool.shared.config.AnalysisConfig;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CyclomaticComplexityAnalyserTest {

    private CyclomaticComplexityAnalyser analyser;

    /**
     * Thresholds are set very high so every method always scores GOOD,
     * letting us focus purely on the raw CC value in Group 1 tests.
     */
    @BeforeEach
    void setUp() {
        AnalysisConfig.CyclomaticComplexity config = new AnalysisConfig.CyclomaticComplexity();
        config.setGoodThreshold(1000);
        config.setWarningThreshold(2000);
        config.setTopN(10);
        analyser = new CyclomaticComplexityAnalyser(config);
    }

    // helpers

    /** Wraps a method source string into a minimal class so JavaParser can parse it. */
    private CompilationUnit parse(String methodSource) {
        return StaticJavaParser.parse("class Test { " + methodSource + " }");
    }

    /**
     * Reads the CC value directly from the details line produced by buildResult().
     * Format: "  #1  [GOOD    ] Test::method                    CC = 2"
     */
    private int extractCC(MetricResult result) {
        return result.getDetails().stream()
                .filter(line -> line.contains("CC ="))
                .mapToInt(line -> {
                    String afterEq = line.substring(line.lastIndexOf("CC =") + 4).trim();
                    return Integer.parseInt(afterEq);
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError("No CC value found in details: " + result.getDetails()));
    }

    // Decision-point counting

    @Test
    void testBaseCC_noDecisions() {
        // A method with no branches should have CC = 1 (base path only)
        CompilationUnit cu = parse("void method() { int x = 1; }");
        assertEquals(1, extractCC(analyser.analyse(List.of(cu))));
    }

    @Test
    void testCC_ifStatement() {
        // Each if adds 1
        CompilationUnit cu = parse("void method(int x) { if (x > 0) { x++; } }");
        assertEquals(2, extractCC(analyser.analyse(List.of(cu))));
    }

    @Test
    void testCC_forLoop() {
        // Each for-loop adds 1
        CompilationUnit cu = parse("void method() { for (int i = 0; i < 10; i++) { } }");
        assertEquals(2, extractCC(analyser.analyse(List.of(cu))));
    }

    @Test
    void testCC_forEachLoop() {
        // Each enhanced for-loop adds 1
        CompilationUnit cu = parse("void method(java.util.List<String> items) { for (String item : items) { } }");
        assertEquals(2, extractCC(analyser.analyse(List.of(cu))));
    }

    @Test
    void testCC_whileLoop() {
        // Each while adds 1
        CompilationUnit cu = parse("void method(int x) { while (x > 0) { x--; } }");
        assertEquals(2, extractCC(analyser.analyse(List.of(cu))));
    }

    @Test
    void testCC_doWhileLoop() {
        // Each do-while adds 1
        CompilationUnit cu = parse("void method(int x) { do { x--; } while (x > 0); }");
        assertEquals(2, extractCC(analyser.analyse(List.of(cu))));
    }

    @Test
    void testCC_catchClause() {
        // Each catch clause adds 1
        CompilationUnit cu = parse("void method() { try { int x = 1; } catch (Exception e) { } }");
        assertEquals(2, extractCC(analyser.analyse(List.of(cu))));
    }

    @Test
    void testCC_ternaryOperator() {
        // Ternary (conditional expression) adds 1
        CompilationUnit cu = parse("int method(int x) { return x > 0 ? x : -x; }");
        assertEquals(2, extractCC(analyser.analyse(List.of(cu))));
    }

    @Test
    void testCC_switchEntry() {
        // Each case label adds 1; default has no label so adds 0
        // 2 case labels → CC = 1 (base) + 2 = 3
        CompilationUnit cu = parse(
                "void method(int x) { switch (x) { case 1: break; case 2: break; default: break; } }"
        );
        assertEquals(3, extractCC(analyser.analyse(List.of(cu))));
    }

    @Test
    void testCC_logicalAnd() {
        // && operator adds 1
        CompilationUnit cu = parse("boolean method(boolean a, boolean b) { return a && b; }");
        assertEquals(2, extractCC(analyser.analyse(List.of(cu))));
    }

    @Test
    void testCC_logicalOr() {
        // || operator adds 1
        CompilationUnit cu = parse("boolean method(boolean a, boolean b) { return a || b; }");
        assertEquals(2, extractCC(analyser.analyse(List.of(cu))));
    }

    @Test
    void testCC_combined() {
        // if(+1) + forEach(+1) + catch(+1) = base 1 + 3 = 4
        CompilationUnit cu = parse(
                "void method(int x, java.util.List<Integer> list) {" +
                "  if (x > 0) {" +
                "    for (int i : list) { }" +
                "  }" +
                "  try { int y = 1; } catch (Exception e) { }" +
                "}"
        );
        assertEquals(4, extractCC(analyser.analyse(List.of(cu))));
    }

    // Threshold logic (score & rating)

    /** Builds an analyser with explicit good/warning thresholds. */
    private CyclomaticComplexityAnalyser analyserWithThresholds(int good, int warning) {
        AnalysisConfig.CyclomaticComplexity config = new AnalysisConfig.CyclomaticComplexity();
        config.setGoodThreshold(good);
        config.setWarningThreshold(warning);
        config.setTopN(10);
        return new CyclomaticComplexityAnalyser(config);
    }

    /** Builds a method with exactly (1 + ifCount) CC by stacking if-statements. */
    private CompilationUnit methodWithCC(int ifCount) {
        StringBuilder sb = new StringBuilder("void method(int x) {");
        for (int i = 0; i < ifCount; i++) {
            sb.append(" if (x > ").append(i).append(") {}");
        }
        sb.append(" }");
        return parse(sb.toString());
    }

    @Test
    void testScore_good() {
        // CC = 2 (1 if), goodThreshold = 3 → methodScore = 100 → overall score = 100.0
        CyclomaticComplexityAnalyser a = analyserWithThresholds(3, 6);
        MetricResult result = a.analyse(List.of(methodWithCC(1)));
        assertEquals(100.0, result.getScore(), 0.001);
        assertEquals(MetricRating.GOOD, result.getRating());
    }

    @Test
    void testScore_atGoodBoundary() {
        // CC = 3 exactly (2 ifs), goodThreshold = 3 → still GOOD (cc <= goodThreshold)
        CyclomaticComplexityAnalyser a = analyserWithThresholds(3, 6);
        MetricResult result = a.analyse(List.of(methodWithCC(2)));
        assertEquals(100.0, result.getScore(), 0.001);
        assertEquals(MetricRating.GOOD, result.getRating());
    }

    @Test
    void testScore_warning() {
        // CC = 5 (4 ifs), 3 < 5 <= 6 → methodScore = 50 → overall score = 50.0 → WARNING
        CyclomaticComplexityAnalyser a = analyserWithThresholds(3, 6);
        MetricResult result = a.analyse(List.of(methodWithCC(4)));
        assertEquals(50.0, result.getScore(), 0.001);
        assertEquals(MetricRating.WARNING, result.getRating());
    }

    @Test
    void testScore_atWarningBoundary() {
        // CC = 6 exactly (5 ifs), cc <= warningThreshold → methodScore = 50 → WARNING
        CyclomaticComplexityAnalyser a = analyserWithThresholds(3, 6);
        MetricResult result = a.analyse(List.of(methodWithCC(5)));
        assertEquals(50.0, result.getScore(), 0.001);
        assertEquals(MetricRating.WARNING, result.getRating());
    }

    @Test
    void testScore_critical() {
        // CC = 8 (7 ifs), 8 > warningThreshold 6 → methodScore = 0 → CRITICAL
        CyclomaticComplexityAnalyser a = analyserWithThresholds(3, 6);
        MetricResult result = a.analyse(List.of(methodWithCC(7)));
        assertEquals(0.0, result.getScore(), 0.001);
        assertEquals(MetricRating.CRITICAL, result.getRating());
    }

    // Aggregate score & rating (buildResult)

    /** Wraps multiple method source strings into one class for multi-method scenarios. */
    private CompilationUnit parseClass(String... methodSources) {
        return StaticJavaParser.parse("class Test { " + String.join(" ", methodSources) + " }");
    }

    /** CC = 1, always GOOD regardless of threshold. */
    private String goodMethod(String name) {
        return "void " + name + "() { int x = 1; }";
    }

    /** CC = 8 (7 ifs), always CRITICAL with thresholds (3, 6). */
    private String criticalMethod(String name) {
        return "void " + name + "(int x) { if(x>0){} if(x>1){} if(x>2){} if(x>3){} if(x>4){} if(x>5){} if(x>6){} }";
    }

    @Test
    void testResult_allGoodMethods() {
        // 3 methods all CC = 1 → score = (100+100+100)/3 = 100.0 → GOOD
        CyclomaticComplexityAnalyser a = analyserWithThresholds(3, 6);
        CompilationUnit cu = parseClass(goodMethod("a"), goodMethod("b"), goodMethod("c"));
        MetricResult result = a.analyse(List.of(cu));
        assertEquals(100.0, result.getScore(), 0.001);
        assertEquals(MetricRating.GOOD, result.getRating());
    }

    @Test
    void testResult_allCriticalMethods() {
        // 3 methods all CC = 8 → score = (0+0+0)/3 = 0.0 → CRITICAL
        CyclomaticComplexityAnalyser a = analyserWithThresholds(3, 6);
        CompilationUnit cu = parseClass(criticalMethod("a"), criticalMethod("b"), criticalMethod("c"));
        MetricResult result = a.analyse(List.of(cu));
        assertEquals(0.0, result.getScore(), 0.001);
        assertEquals(MetricRating.CRITICAL, result.getRating());
    }

    @Test
    void testResult_mixedMethods() {
        // 2 GOOD (score 100) + 1 CRITICAL (score 0) = 200/3 = 66.67 → WARNING (<70, >=40)
        CyclomaticComplexityAnalyser a = analyserWithThresholds(3, 6);
        CompilationUnit cu = parseClass(goodMethod("a"), goodMethod("b"), criticalMethod("c"));
        MetricResult result = a.analyse(List.of(cu));
        assertEquals(200.0 / 3.0, result.getScore(), 0.001);
        assertEquals(MetricRating.WARNING, result.getRating());
    }

    @Test
    void testResult_emptyUnit() {
        // Class with no methods → buildResult short-circuits to score=0.0, CRITICAL
        CyclomaticComplexityAnalyser a = analyserWithThresholds(3, 6);
        CompilationUnit cu = StaticJavaParser.parse("class Empty {}");
        MetricResult result = a.analyse(List.of(cu));
        assertEquals(0.0, result.getScore(), 0.001);
        assertEquals(MetricRating.CRITICAL, result.getRating());
    }

    @Test
    void testResult_scoreBoundary_good() {
        // 7 GOOD + 3 CRITICAL = (7×100 + 3×0) / 10 = 70.0 → exactly at GOOD boundary (score >= 70)
        CyclomaticComplexityAnalyser a = analyserWithThresholds(3, 6);
        String[] methods = new String[10];
        for (int i = 0; i < 7; i++) methods[i] = goodMethod("good" + i);
        for (int i = 7; i < 10; i++) methods[i] = criticalMethod("crit" + i);
        MetricResult result = a.analyse(List.of(parseClass(methods)));
        assertEquals(70.0, result.getScore(), 0.001);
        assertEquals(MetricRating.GOOD, result.getRating());
    }

    @Test
    void testResult_scoreBoundary_warning() {
        // 4 GOOD + 6 CRITICAL = (4×100 + 6×0) / 10 = 40.0 → exactly at WARNING boundary (score >= 40)
        CyclomaticComplexityAnalyser a = analyserWithThresholds(3, 6);
        String[] methods = new String[10];
        for (int i = 0; i < 4; i++) methods[i] = goodMethod("good" + i);
        for (int i = 4; i < 10; i++) methods[i] = criticalMethod("crit" + i);
        MetricResult result = a.analyse(List.of(parseClass(methods)));
        assertEquals(40.0, result.getScore(), 0.001);
        assertEquals(MetricRating.WARNING, result.getRating());
    }
}