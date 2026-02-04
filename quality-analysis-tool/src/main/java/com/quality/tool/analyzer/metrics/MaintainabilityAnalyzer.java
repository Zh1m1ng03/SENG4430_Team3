package com.quality.tool.analyzer.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.quality.tool.model.CodeSmell;
import com.quality.tool.model.MaintainabilityMetrics;
import com.quality.tool.model.QualityReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaintainabilityAnalyzer {
    private static final int LONG_METHOD_THRESHOLD = 50; // lines
    private static final int LARGE_CLASS_THRESHOLD = 500; // lines
    private static final int HIGH_COMPLEXITY_THRESHOLD = 10;

    public void analyze(List<CompilationUnit> compilationUnits, QualityReport report) {
        MaintainabilityMetrics metrics = new MaintainabilityMetrics();
        List<CodeSmell> codeSmells = new ArrayList<>();
        
        int totalMethods = 0;
        int totalComplexity = 0;
        int totalMethodLines = 0;
        int totalClassLines = 0;
        int longMethods = 0;
        int largeClasses = 0;
        int highComplexityMethods = 0;

        for (CompilationUnit cu : compilationUnits) {
            String fileName = cu.getStorage().map(s -> s.getPath().getFileName().toString())
                                  .orElse("Unknown.java");

            // Analyze classes
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                if (classDecl.isInterface()) {
                    return;
                }

                int classLines = classDecl.getEnd().map(end -> end.line)
                                          .orElse(0) - 
                                classDecl.getBegin().map(begin -> begin.line)
                                         .orElse(0) + 1;

                if (classLines > LARGE_CLASS_THRESHOLD) {
                    largeClasses++;
                    codeSmells.add(new CodeSmell(
                        "Large Class",
                        fileName,
                        classDecl.getNameAsString(),
                        "Class has " + classLines + " lines (threshold: " + LARGE_CLASS_THRESHOLD + ")",
                        "MEDIUM"
                    ));
                }

                // Analyze methods
                classDecl.getMethods().forEach(method -> {
                    totalMethods++;
                    int complexity = calculateCyclomaticComplexity(method);
                    totalComplexity += complexity;

                    int methodLines = method.getEnd().map(end -> end.line)
                                            .orElse(0) - 
                                     method.getBegin().map(begin -> begin.line)
                                            .orElse(0) + 1;
                    totalMethodLines += methodLines;

                    if (methodLines > LONG_METHOD_THRESHOLD) {
                        longMethods++;
                        codeSmells.add(new CodeSmell(
                            "Long Method",
                            fileName,
                            method.getNameAsString(),
                            "Method has " + methodLines + " lines (threshold: " + LONG_METHOD_THRESHOLD + ")",
                            "MEDIUM"
                        ));
                    }

                    if (complexity > HIGH_COMPLEXITY_THRESHOLD) {
                        highComplexityMethods++;
                        codeSmells.add(new CodeSmell(
                            "High Complexity",
                            fileName,
                            method.getNameAsString(),
                            "Method has cyclomatic complexity of " + complexity + 
                            " (threshold: " + HIGH_COMPLEXITY_THRESHOLD + ")",
                            "HIGH"
                        ));
                    }
                });
            });
        }

        // Calculate averages
        if (totalMethods > 0) {
            metrics.setAverageCyclomaticComplexity((double) totalComplexity / totalMethods);
            metrics.setAverageMethodLength((double) totalMethodLines / totalMethods);
        }

        metrics.setLongMethods(longMethods);
        metrics.setLargeClasses(largeClasses);
        metrics.setHighComplexityMethods(highComplexityMethods);
        metrics.setTotalCodeSmells(codeSmells.size());
        metrics.setCodeSmells(codeSmells);

        // Simple coupling/cohesion estimation
        metrics.setCouplingScore(estimateCoupling(compilationUnits));
        metrics.setCohesionScore(estimateCohesion(compilationUnits));

        // Calculate maintainability score (0-100)
        double score = 100.0;
        score -= Math.min(metrics.getAverageCyclomaticComplexity() * 2, 30); // Max 30 points deduction
        score -= Math.min(longMethods * 2, 20); // Max 20 points deduction
        score -= Math.min(largeClasses * 3, 20); // Max 20 points deduction
        score -= Math.min(highComplexityMethods * 1.5, 20); // Max 20 points deduction
        score -= Math.min(metrics.getCouplingScore() * 2, 10); // Max 10 points deduction
        score = Math.max(0, score);

        report.setMaintainabilityMetrics(metrics);
        report.setMaintainabilityScore(score);
    }

    private int calculateCyclomaticComplexity(MethodDeclaration method) {
        int complexity = 1; // Base complexity

        // Count decision points
        complexity += method.findAll(com.github.javaparser.ast.stmt.IfStmt.class).size();
        complexity += method.findAll(com.github.javaparser.ast.stmt.ForStmt.class).size();
        complexity += method.findAll(com.github.javaparser.ast.stmt.ForEachStmt.class).size();
        complexity += method.findAll(com.github.javaparser.ast.stmt.WhileStmt.class).size();
        complexity += method.findAll(com.github.javaparser.ast.stmt.DoStmt.class).size();
        complexity += method.findAll(com.github.javaparser.ast.stmt.SwitchStmt.class).size();
        complexity += method.findAll(com.github.javaparser.ast.expr.ConditionalExpr.class).size();
        complexity += method.findAll(com.github.javaparser.ast.expr.BinaryExpr.class)
                            .stream()
                            .filter(expr -> expr.getOperator() == com.github.javaparser.ast.expr.BinaryExpr.Operator.AND ||
                                          expr.getOperator() == com.github.javaparser.ast.expr.BinaryExpr.Operator.OR)
                            .count();

        return complexity;
    }

    private double estimateCoupling(List<CompilationUnit> compilationUnits) {
        // Simple estimation: count imports per file
        long totalImports = compilationUnits.stream()
            .mapToLong(cu -> cu.getImports().size())
            .sum();
        return compilationUnits.isEmpty() ? 0 : (double) totalImports / compilationUnits.size();
    }

    private double estimateCohesion(List<CompilationUnit> compilationUnits) {
        // Simple estimation: average methods per class
        long totalMethods = compilationUnits.stream()
            .flatMap(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream())
            .filter(c -> !c.isInterface())
            .mapToLong(c -> c.getMethods().size())
            .sum();
        long totalClasses = compilationUnits.stream()
            .flatMap(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream())
            .filter(c -> !c.isInterface())
            .count();
        return totalClasses == 0 ? 0 : (double) totalMethods / totalClasses;
    }
}
