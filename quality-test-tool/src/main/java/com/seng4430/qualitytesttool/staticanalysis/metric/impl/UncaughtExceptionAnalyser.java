package com.seng4430.qualitytesttool.staticanalysis.metric.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricAnalyser;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricResult;

import java.util.ArrayList;
import java.util.List;

public class UncaughtExceptionAnalyser implements MetricAnalyser {

    private static final double GOOD_THRESHOLD    = 80.0;
    private static final double WARNING_THRESHOLD = 50.0;

    @Override
    public MetricResult analyse(List<CompilationUnit> units) {
        return analyze(units);
    }

    private MetricResult analyze(List<CompilationUnit> units) {
        List<String> details = new ArrayList<>();

        int analysedFiles    = 0;
        int totalMethodsAll  = 0;
        int totalUncaughtAll = 0;
        int safeMethodsAll   = 0;

        List<String[]> fileRows = new ArrayList<>();

        for (CompilationUnit unit : units) {
            String fileName = unit.getStorage()
                    .map(s -> s.getFileName())
                    .orElse("Unknown");

            boolean isInterface = unit.findAll(ClassOrInterfaceDeclaration.class)
                    .stream().allMatch(ClassOrInterfaceDeclaration::isInterface);
            if (isInterface) continue;

            List<MethodDeclaration> methods = unit.findAll(MethodDeclaration.class);
            if (methods.isEmpty()) continue;

            int fileUncaught = 0;
            int fileSafe     = 0;

            for (MethodDeclaration method : methods) {
                int uncaught = countUncaughtThrows(method);
                fileUncaught += uncaught;
                if (uncaught == 0) fileSafe++;
            }

            double fileRatio = 100.0 * fileUncaught / methods.size();

            fileRows.add(new String[]{
                fileName,
                String.valueOf(methods.size()),
                String.valueOf(fileUncaught),
                String.format("%.2f%%", fileRatio)
            });

            analysedFiles    += 1;
            totalMethodsAll  += methods.size();
            totalUncaughtAll += fileUncaught;
            safeMethodsAll   += fileSafe;
        }

        // Sort by uncaught ratio descending
        fileRows.sort((a, b) -> Double.compare(
            Double.parseDouble(b[3].replace("%", "")),
            Double.parseDouble(a[3].replace("%", ""))
        ));

        double score = totalMethodsAll == 0 ? 100.0
                : 100.0 * safeMethodsAll / totalMethodsAll;

        MetricRating rating;
        if (score >= GOOD_THRESHOLD)         rating = MetricRating.GOOD;
        else if (score >= WARNING_THRESHOLD) rating = MetricRating.WARNING;
        else                                 rating = MetricRating.CRITICAL;

        double projectAvg = totalMethodsAll == 0 ? 0.0
                : (double) totalUncaughtAll / totalMethodsAll;

        details.add(String.format("  Total methods: %d | Uncaught throws: %d | Avg per method: %.4f",
                totalMethodsAll, totalUncaughtAll, projectAvg));
        details.add(String.format("  Safe methods: %d | Safe ratio: %.2f%%",
                safeMethodsAll, score));
        details.add("  File Summary (sorted by uncaught ratio):");

        for (String[] row : fileRows) {
            details.add(String.format("    %-40s methods: %4s  uncaught: %4s  ratio: %s",
                    row[0], row[1], row[2], row[3]));
        }

        return new MetricResult(getMetricName(), getQualityAspect(), score, rating, details);
    }

    private int countUncaughtThrows(MethodDeclaration method) {
        if (method.getBody().isEmpty()) return 0;

        var body = method.getBody().get();

        List<ThrowStmt> allThrows     = body.findAll(ThrowStmt.class);
        List<TryStmt>   tryStatements = body.findAll(TryStmt.class);
        int throwsInTry = 0;

        for (TryStmt tryStmt : tryStatements) {
            boolean isNested = tryStmt.findAncestor(TryStmt.class).isPresent();
            if (!isNested) {
                throwsInTry += tryStmt.getTryBlock().findAll(ThrowStmt.class).size();
            }
        }

        return Math.max(0, allThrows.size() - throwsInTry);
    }

    @Override
    public String getMetricName() {
        return "Uncaught Exceptions";
    }

    @Override
    public String getQualityAspect() {
        return "Reliability";
    }
}