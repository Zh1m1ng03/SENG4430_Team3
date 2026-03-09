package com.seng4430.qulitytesttool.staticanalysis.metric.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricAnalyser;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DuplicateCodeAnalyser implements MetricAnalyser {

    private static final double GOOD_THRESHOLD = 80.0;
    private static final double WARNING_THRESHOLD = 50.0;
    private static final int MAX_DETAIL_LINES = 30;

    @Override
    public MetricResult analyse(List<CompilationUnit> units) {
        List<MethodFingerprint> methods = new ArrayList<>();

        for (CompilationUnit unit : units) {
            String fileName = unit.getStorage()
                    .map(s -> s.getFileName())
                    .orElse("Unknown");

            for (MethodDeclaration method : unit.findAll(MethodDeclaration.class)) {
                if (method.getBody().isEmpty()) {
                    continue;
                }

                String className = method
                        .findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(NodeWithSimpleName::getNameAsString)
                        .orElse(fileName.replace(".java", ""));

                String methodName = className + "::" + method.getNameAsString();
                String normalized = normalizeMethodBody(method.getBody().get().toString());

                if (!normalized.isBlank()) {
                    methods.add(new MethodFingerprint(fileName, methodName, normalized));
                }
            }
        }

        if (methods.isEmpty()) {
            return new MetricResult(
                    getMetricName(),
                    getQualityAspect(),
                    100.0,
                    MetricRating.GOOD,
                    List.of("No concrete methods found to analyse.")
            );
        }

        Map<String, List<MethodFingerprint>> grouped = methods.stream()
                .collect(Collectors.groupingBy(
                        MethodFingerprint::fingerprint,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<List<MethodFingerprint>> duplicateGroups = grouped.values().stream()
                .filter(group -> group.size() > 1)
                .collect(Collectors.toList());

        int duplicatedMethods = duplicateGroups.stream()
                .mapToInt(List::size)
                .sum();
        int totalMethods = methods.size();
        double duplicateRatio = (double) duplicatedMethods / totalMethods;
        double score = Math.max(0.0, Math.min(100.0, 100.0 * (1.0 - duplicateRatio)));

        MetricRating rating;
        if (score >= GOOD_THRESHOLD) {
            rating = MetricRating.GOOD;
        } else if (score >= WARNING_THRESHOLD) {
            rating = MetricRating.WARNING;
        } else {
            rating = MetricRating.CRITICAL;
        }

        List<String> details = buildDetails(totalMethods, duplicatedMethods, duplicateRatio, duplicateGroups);

        return new MetricResult(
                getMetricName(),
                getQualityAspect(),
                score,
                rating,
                details
        );
    }

    @Override
    public String getMetricName() {
        return "Duplicate Code Ratio";
    }

    @Override
    public String getQualityAspect() {
        return "Maintainability";
    }

    private List<String> buildDetails(
            int totalMethods,
            int duplicatedMethods,
            double duplicateRatio,
            List<List<MethodFingerprint>> duplicateGroups
    ) {
        List<String> details = new ArrayList<>();
        details.add(String.format(
                "Total methods: %d | Duplicated methods: %d | Duplicate ratio: %.2f%%",
                totalMethods, duplicatedMethods, duplicateRatio * 100
        ));

        if (duplicateGroups.isEmpty()) {
            details.add("No duplicated method bodies detected.");
            return details;
        }

        details.add("Duplicated method groups:");
        int lines = 0;
        int groupIndex = 1;
        for (List<MethodFingerprint> group : duplicateGroups.stream()
                .sorted(Comparator.comparingInt((List<MethodFingerprint> g) -> g.size()).reversed())
                .toList()) {
            if (lines >= MAX_DETAIL_LINES) {
                break;
            }

            details.add(String.format("  Group %d (%d methods):", groupIndex++, group.size()));
            lines++;

            for (MethodFingerprint method : group) {
                if (lines >= MAX_DETAIL_LINES) {
                    break;
                }
                details.add("    - " + method.fileName() + " :: " + method.methodName());
                lines++;
            }
        }

        if (lines >= MAX_DETAIL_LINES) {
            details.add("  ...more duplicated methods omitted");
        }
        return details;
    }

    private String normalizeMethodBody(String body) {
        String normalized = body;
        normalized = normalized.replaceAll("/\\*.*?\\*/", " ");
        normalized = normalized.replaceAll("//.*", " ");
        normalized = normalized.replaceAll("\"(?:\\\\.|[^\"\\\\])*\"", "\"STR\"");
        normalized = normalized.replaceAll("'(?:\\\\.|[^'\\\\])*'", "'CHR'");
        normalized = normalized.replaceAll("\\b\\d+(?:\\.\\d+)?\\b", "NUM");
        normalized = normalized.replaceAll("\\s+", "");
        return normalized;
    }

    private record MethodFingerprint(String fileName, String methodName, String fingerprint) {}
}
