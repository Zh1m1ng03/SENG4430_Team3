package com.seng4430.qualitytesttool.staticanalysis.metric.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeakCryptoAnalyserTest {

    private final WeakCryptoAnalyser analyser = new WeakCryptoAnalyser();

    private CompilationUnit parseClass(String classBody) {
        return StaticJavaParser.parse("class Test { " + classBody + " }");
    }

    @Test
    void testMetricMetadata() {
        assertEquals("Weak Crypto APIs", analyser.getMetricName());
        assertEquals("Security", analyser.getQualityAspect());
    }

    @Test
    void testAnalyse_emptyUnits_returnsPerfectScoreAndGood() {
        MetricResult result = analyser.analyse(List.of());

        assertEquals(100.0, result.getScore(), 0.001);
        assertEquals(MetricRating.GOOD, result.getRating());
        assertTrue(result.getDetails().stream().anyMatch(s -> s.contains("No Java sources in scope")));
    }

    @Test
    void testAnalyse_noWeakUsage_stillContainsDetailedBreakdown() {
        CompilationUnit cu = parseClass(
                "void safeMethod() {" +
                        "  java.security.MessageDigest.getInstance(\"SHA-256\");" +
                        "  java.util.ArrayList<String> list = new java.util.ArrayList<>();" +
                        "  int x = 1 + 1;" +
                        "}"
        );

        MetricResult result = analyser.analyse(List.of(cu));

        assertEquals(100.0, result.getScore(), 0.001);
        assertEquals(MetricRating.GOOD, result.getRating());
        assertTrue(result.getDetails().stream().anyMatch(s -> s.contains("Per-pattern analysis (project):")));
        assertTrue(result.getDetails().stream().anyMatch(s -> s.contains("File Summary (sorted by violation count")));
        assertTrue(result.getDetails().stream().anyMatch(s -> s.contains("No weak cryptographic APIs detected")));
    }

    @Test
    void testAnalyse_detectsMethodCallStringPatterns() {
        CompilationUnit cu = parseClass(
                "void weakMethod() {" +
                        "  java.security.MessageDigest.getInstance(\"MD5\");" +
                        "  java.security.MessageDigest.getInstance(\"SHA-1\");" +
                        "  javax.crypto.Cipher.getInstance(\"RC4\");" +
                        "  javax.crypto.Cipher.getInstance(\"DES/ECB/PKCS5Padding\");" +
                        "}"
        );

        MetricResult result = analyser.analyse(List.of(cu));

        // 4 violations -> score 20 -> CRITICAL
        assertEquals(20.0, result.getScore(), 0.001);
        assertEquals(MetricRating.CRITICAL, result.getRating());
        assertTrue(result.getDetails().stream().anyMatch(s -> s.contains("MD5")));
        assertTrue(result.getDetails().stream().anyMatch(s -> s.contains("SHA-1")));
        assertTrue(result.getDetails().stream().anyMatch(s -> s.contains("RC4")));
        assertTrue(result.getDetails().stream().anyMatch(s -> s.contains("DES")));
        assertTrue(result.getDetails().stream().anyMatch(s -> s.contains("Occurrences (file:line")));
    }

    @Test
    void testAnalyse_detectsJavaUtilRandom_whenImported() {
        CompilationUnit cu = StaticJavaParser.parse(
                "import java.util.Random; class Test { void m(){ Random r = new Random(); } }"
        );

        MetricResult result = analyser.analyse(List.of(cu));

        // 1 violation -> score 80 -> GOOD (boundary)
        assertEquals(80.0, result.getScore(), 0.001);
        assertEquals(MetricRating.GOOD, result.getRating());
        assertTrue(result.getDetails().stream().anyMatch(s -> s.contains("java.util.Random")));
    }

    @Test
    void testAnalyse_doesNotFlagRandomWithoutJavaUtilImport() {
        CompilationUnit cu = StaticJavaParser.parse(
                "class Random {} class Test { void m(){ Random r = new Random(); } }"
        );

        MetricResult result = analyser.analyse(List.of(cu));

        assertEquals(100.0, result.getScore(), 0.001);
        assertEquals(MetricRating.GOOD, result.getRating());
        assertTrue(result.getDetails().stream().anyMatch(s -> s.contains("No weak cryptographic APIs detected")));
    }

    @Test
    void testAnalyse_ratingBoundaries_warningAndCritical() {
        // 3 violations -> score 40 -> WARNING boundary
        CompilationUnit warningCu = parseClass(
                "void m(){" +
                        " java.security.MessageDigest.getInstance(\"MD5\");" +
                        " java.security.MessageDigest.getInstance(\"SHA1\");" +
                        " javax.crypto.Cipher.getInstance(\"DES\");" +
                        "}"
        );
        MetricResult warningResult = analyser.analyse(List.of(warningCu));
        assertEquals(40.0, warningResult.getScore(), 0.001);
        assertEquals(MetricRating.WARNING, warningResult.getRating());

        // 4 violations -> score 20 -> CRITICAL
        CompilationUnit criticalCu = parseClass(
                "void m(){" +
                        " java.security.MessageDigest.getInstance(\"MD5\");" +
                        " java.security.MessageDigest.getInstance(\"SHA1\");" +
                        " javax.crypto.Cipher.getInstance(\"DES\");" +
                        " javax.crypto.Cipher.getInstance(\"RC4\");" +
                        "}"
        );
        MetricResult criticalResult = analyser.analyse(List.of(criticalCu));
        assertEquals(20.0, criticalResult.getScore(), 0.001);
        assertEquals(MetricRating.CRITICAL, criticalResult.getRating());
    }
}
