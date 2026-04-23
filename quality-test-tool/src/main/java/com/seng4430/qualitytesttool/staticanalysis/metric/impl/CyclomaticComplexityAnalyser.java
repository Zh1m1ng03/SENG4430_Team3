package com.seng4430.qualitytesttool.staticanalysis.metric.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.seng4430.qualitytesttool.shared.config.AnalysisConfig;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricAnalyser;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricResult;

import java.util.*;
import java.util.stream.Collectors;

public class CyclomaticComplexityAnalyser implements MetricAnalyser {

    private final int goodThreshold;
    private final int warningThreshold;
    private final int topN;

    public CyclomaticComplexityAnalyser(AnalysisConfig.CyclomaticComplexity config) {
        this.goodThreshold    = config.getGoodThreshold();
        this.warningThreshold = config.getWarningThreshold();
        this.topN             = config.getTopN();
    }

    @Override
    public MetricResult analyse(List<CompilationUnit> units) {
        List<MethodComplexity> methods = new ArrayList<>();

        for (CompilationUnit unit : units) {
            String fileName = unit.getStorage()
                    .map(s -> s.getFileName())
                    .orElse("Unknown");

            unit.findAll(MethodDeclaration.class).forEach(method -> {
                String className = method
                        .findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(NodeWithSimpleName::getNameAsString)
                        .orElse(fileName.replace(".java", ""));

                methods.add(new MethodComplexity(fileName, className, method.getNameAsString(), computeCC(method)));
            });
        }

        return buildResult(methods);
    }


    private int computeCC(MethodDeclaration method) {
        int[] count = {1};

        // core
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override public void visit(IfStmt n, Void arg)         { count[0]++; super.visit(n, arg); }
            @Override public void visit(ForStmt n, Void arg)        { count[0]++; super.visit(n, arg); }
            @Override public void visit(ForEachStmt n, Void arg)    { count[0]++; super.visit(n, arg); }
            @Override public void visit(WhileStmt n, Void arg)      { count[0]++; super.visit(n, arg); }
            @Override public void visit(DoStmt n, Void arg)         { count[0]++; super.visit(n, arg); }
            @Override public void visit(CatchClause n, Void arg)    { count[0]++; super.visit(n, arg); }
            @Override public void visit(ConditionalExpr n, Void arg){ count[0]++; super.visit(n, arg); }

            @Override
            public void visit(SwitchEntry n, Void arg) {
                if (!n.getLabels().isEmpty()) count[0]++;
                super.visit(n, arg);
            }

            @Override
            public void visit(BinaryExpr n, Void arg) {
                if (n.getOperator() == BinaryExpr.Operator.AND
                        || n.getOperator() == BinaryExpr.Operator.OR) {
                    count[0]++;
                }
                super.visit(n, arg);
            }
        }, null);

        return count[0];
    }


    private MetricResult buildResult(List<MethodComplexity> methods) {
        if (methods.isEmpty()) {
            return new MetricResult(getMetricName(), getQualityAspect(),
                    0.0, MetricRating.CRITICAL, List.of("No methods found to analyse. Ensure a valid Java source path has been loaded."));
        }

        // overall score
        int totalScore = methods.stream().mapToInt(m -> methodScore(m.cc)).sum();
        double score   = (double) totalScore / methods.size();

        long critical = methods.stream().filter(m -> m.cc > warningThreshold).count();
        long warning  = methods.stream().filter(m -> m.cc > goodThreshold && m.cc <= warningThreshold).count();
        long good     = methods.stream().filter(m -> m.cc <= goodThreshold).count();

        // aggregate by file
        Map<String, List<MethodComplexity>> byFile = methods.stream()
                .collect(Collectors.groupingBy(m -> m.fileName, LinkedHashMap::new, Collectors.toList()));

        List<FileComplexity> files = byFile.entrySet().stream()
                .map(e -> new FileComplexity(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingDouble(FileComplexity::maxCC).reversed())
                .collect(Collectors.toList());

        // top-N methods by CC
        List<MethodComplexity> top = methods.stream()
                .sorted(Comparator.comparingInt((MethodComplexity m) -> m.cc).reversed())
                .limit(topN)
                .collect(Collectors.toList());

        // build details lines
        List<String> details = new ArrayList<>();

        details.add(String.format(
                "Analysed %d method(s) across %d file(s)  —  GOOD: %d  |  WARNING: %d  |  CRITICAL: %d",
                methods.size(), files.size(), good, warning, critical));

        // top-10 methods
        details.add("");
        details.add(String.format("Top %d Most Complex Methods:", Math.min(topN, top.size())));
        int rank = 1;
        for (MethodComplexity m : top) {
            details.add(String.format("  #%-2d [%-8s] %-30s  CC = %d",
                    rank++, ratingLabel(m.cc), m.className + "::" + m.methodName, m.cc));
        }

        // file summary (sorted by max CC desc, skip files with no issues)
        details.add("");
        details.add("File Summary (sorted by highest CC, files with issues only):");
        for (FileComplexity f : files) {
            if (f.maxCC <= goodThreshold) continue;
            details.add(String.format("  %-40s  methods: %2d  avg CC: %4.1f  max CC: %2d  [%s]",
                    f.fileName, f.methodCount, f.avgCC, f.maxCC, ratingLabel(f.maxCC)));
        }

        MetricRating rating = score >= 70 ? MetricRating.GOOD
                            : score >= 40 ? MetricRating.WARNING
                            : MetricRating.CRITICAL;

        return new MetricResult(getMetricName(), getQualityAspect(), score, rating, details);
    }

    // helpers

    private int methodScore(int cc) {
        if (cc <= goodThreshold)    return 100;
        if (cc <= warningThreshold) return 50;
        return 0;
    }

    private String ratingLabel(int cc) {
        if (cc <= goodThreshold)    return "GOOD";
        if (cc <= warningThreshold) return "WARNING";
        return "CRITICAL";
    }

    @Override public String getMetricName()    { return "Cyclomatic Complexity"; }
    @Override public String getQualityAspect() { return "Maintainability"; }

    //  inner classes

    private static class MethodComplexity {
        final String fileName;
        final String className;
        final String methodName;
        final int cc;

        MethodComplexity(String fileName, String className, String methodName, int cc) {
            this.fileName   = fileName;
            this.className  = className;
            this.methodName = methodName;
            this.cc         = cc;
        }
    }

    private static class FileComplexity {
        final String fileName;
        final int methodCount;
        final double avgCC;
        final int maxCC;

        FileComplexity(String fileName, List<MethodComplexity> methods) {
            this.fileName    = fileName;
            this.methodCount = methods.size();
            this.avgCC       = methods.stream().mapToInt(m -> m.cc).average().orElse(0);
            this.maxCC       = methods.stream().mapToInt(m -> m.cc).max().orElse(0);
        }

        int maxCC() { return maxCC; }
    }
}