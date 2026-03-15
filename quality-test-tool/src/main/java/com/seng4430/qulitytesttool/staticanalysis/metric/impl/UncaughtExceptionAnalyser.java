package com.seng4430.qulitytesttool.staticanalysis.metric.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricAnalyser;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricResult;

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

        for (CompilationUnit unit : units) {
            String fileName = unit.getStorage()
                    .map(s -> s.getFileName())
                    .orElse("Unknown");

            // 跳过纯接口文件
            boolean isInterface = unit.findAll(ClassOrInterfaceDeclaration.class)
                    .stream().allMatch(ClassOrInterfaceDeclaration::isInterface);
            if (isInterface) {
                details.add(fileName + " | skip: interface (not analysed)");
                continue;
            }

            List<MethodDeclaration> methods = unit.findAll(MethodDeclaration.class);
            if (methods.isEmpty()) continue;

            int fileUncaught = 0;
            int fileSafe     = 0;

            for (MethodDeclaration method : methods) {
                int uncaught = countUncaughtThrows(method);
                fileUncaught += uncaught;
                if (uncaught == 0) {
                    fileSafe++;
                } else {
                    // 列出每个有问题的方法及其未捕获 throw 数量
                    details.add(String.format("  [%s] method: %s() -> uncaught throws: %d",
                            fileName,
                            method.getNameAsString(),
                            uncaught));
                }
            }

            // 该文件汇总
            double fileAvg = (double) fileUncaught / methods.size();
            details.add(String.format("%s | total uncaught: %d, methods: %d, avg: %.4f",
                    fileName, fileUncaught, methods.size(), fileAvg));
            details.add("");  // 空行分隔每个文件

            analysedFiles    += 1;
            totalMethodsAll  += methods.size();
            totalUncaughtAll += fileUncaught;
            safeMethodsAll   += fileSafe;
        }

        // 项目汇总行
        double projectAvg = totalMethodsAll == 0 ? 0.0
                : (double) totalUncaughtAll / totalMethodsAll;
        details.add("---");
        details.add(String.format("Project: %d file(s), %d methods analysed",
                analysedFiles, totalMethodsAll));
        details.add(String.format("Total uncaught throws: %d, average per method: %.4f",
                totalUncaughtAll, projectAvg));

        // 评分：安全方法占比
        double score = totalMethodsAll == 0 ? 100.0
                : 100.0 * safeMethodsAll / totalMethodsAll;

        MetricRating rating;
        if (score >= GOOD_THRESHOLD)         rating = MetricRating.GOOD;
        else if (score >= WARNING_THRESHOLD) rating = MetricRating.WARNING;
        else                                 rating = MetricRating.CRITICAL;

        return new MetricResult(getMetricName(), getQualityAspect(), score, rating, details);
    }

    private int countUncaughtThrows(MethodDeclaration method) {
        if (method.getBody().isEmpty()) return 0;

        var body = method.getBody().get();

        // 统计方法体内所有 throw 语句
        List<ThrowStmt> allThrows     = body.findAll(ThrowStmt.class);
        // 统计方法体内所有 try 块
        List<TryStmt>   tryStatements = body.findAll(TryStmt.class);
        int throwsInTry = 0;

        for (TryStmt tryStmt : tryStatements) {
            // 只统计最外层 try 块，避免嵌套 try 导致重复计数
            boolean isNested = tryStmt.findAncestor(TryStmt.class).isPresent();
            if (!isNested) {
                throwsInTry += tryStmt.getTryBlock().findAll(ThrowStmt.class).size();
            }
        }

        // 未捕获 = 全部 throw - try 块内的 throw
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