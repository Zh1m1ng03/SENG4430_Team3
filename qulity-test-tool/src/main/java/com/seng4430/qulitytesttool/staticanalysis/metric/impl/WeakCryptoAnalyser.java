package com.seng4430.qulitytesttool.staticanalysis.metric.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricAnalyser;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WeakCryptoAnalyser implements MetricAnalyser {

    // Simple fixed thresholds for mapping score → rating
    private static final int GOOD_THRESHOLD = 80;    // 0–1 violation(s)
    private static final int WARNING_THRESHOLD = 40; // 2–3 violation(s)

    private static final String[] WEAK_PATTERNS = {
            "MD5",
            "SHA-1",
            "SHA1",
            "DES",
            "RC4",
            "java.util.Random"
    };

    @Override
    public MetricResult analyse(List<CompilationUnit> units) {
        List<String> details = new ArrayList<>();
        int[] violations = {0};
        Map<String, Integer> fileViolations = new LinkedHashMap<>();

        for (CompilationUnit unit : units) {
            String fileName = unit.getStorage()
                    .map(storage -> storage.getFileName())
                    .orElse("Unknown");

            unit.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(ObjectCreationExpr n, Void arg) {
                    checkNodeForWeakCrypto(n.toString(), fileName, n.getBegin().map(p -> p.line).orElse(-1), details, violations, fileViolations);
                    super.visit(n, arg);
                }

                @Override
                public void visit(MethodCallExpr n, Void arg) {
                    checkNodeForWeakCrypto(n.toString(), fileName, n.getBegin().map(p -> p.line).orElse(-1), details, violations, fileViolations);
                    super.visit(n, arg);
                }
            }, null);
        }

        double score = Math.max(0, 100 - violations[0] * 20);

        if (violations[0] == 0) {
            details.add("No weak cryptographic APIs detected.");
        } else {
            details.add(0, String.format("Total weak crypto usages detected: %d", violations[0]));
            details.add(1, "Per-file scores (100 - 20 * violationsInFile):");

            int insertIndex = 2;
            for (Map.Entry<String, Integer> entry : fileViolations.entrySet()) {
                String file = entry.getKey();
                int v = entry.getValue();
                double fileScore = Math.max(0, 100 - v * 20);
                details.add(insertIndex++, String.format("  %s -> score: %.1f (violations: %d)", file, fileScore, v));
            }
        }

        MetricRating rating;
        if (score >= GOOD_THRESHOLD) {
            rating = MetricRating.GOOD;
        } else if (score >= WARNING_THRESHOLD) {
            rating = MetricRating.WARNING;
        } else {
            rating = MetricRating.CRITICAL;
        }

        return new MetricResult(
                getMetricName(),
                getQualityAspect(),
                score,
                rating,
                details
        );
    }

    private void checkNodeForWeakCrypto(String nodeSource,
                                        String fileName,
                                        int line,
                                        List<String> details,
                                        int[] violations,
                                        Map<String, Integer> fileViolations) {
        for (String pattern : WEAK_PATTERNS) {
            if (nodeSource.contains(pattern)) {
                violations[0]++;
                fileViolations.merge(fileName, 1, Integer::sum);
                details.add(String.format("%s:%s - uses weak crypto API: %s", fileName,
                        line > 0 ? line : "?", pattern));
            }
        }
    }

    @Override
    public String getMetricName() {
        return "Weak Crypto APIs";
    }

    @Override
    public String getQualityAspect() {
        return "Security";
    }
}
