package com.quality.tool.analyzer.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.TryStmt;
import com.quality.tool.model.ReliabilityIssue;
import com.quality.tool.model.ReliabilityMetrics;
import com.quality.tool.model.QualityReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReliabilityAnalyzer {
    public void analyze(List<CompilationUnit> compilationUnits, QualityReport report) {
        ReliabilityMetrics metrics = new ReliabilityMetrics();
        List<ReliabilityIssue> reliabilityIssues = new ArrayList<>();

        int totalMethods = 0;
        int methodsWithExceptionHandling = 0;
        int unhandledExceptions = 0;
        int nullPointerRisks = 0;
        int resourceLeakRisks = 0;
        int errorRecoveryMechanisms = 0;

        for (CompilationUnit cu : compilationUnits) {
            String fileName = cu.getStorage().map(s -> s.getPath().getFileName().toString())
                                  .orElse("Unknown.java");

            cu.findAll(MethodDeclaration.class).forEach(method -> {
                totalMethods++;
                String methodBody = method.getBody().map(b -> b.toString()).orElse("");

                // Check for exception handling
                boolean hasTryCatch = cu.findAll(TryStmt.class).stream()
                    .anyMatch(tryStmt -> {
                        Optional<MethodDeclaration> parentMethod = tryStmt.getParentNode()
                            .flatMap(p -> p.findAncestor(MethodDeclaration.class));
                        return parentMethod.isPresent() && parentMethod.get().equals(method);
                    });

                if (hasTryCatch) {
                    methodsWithExceptionHandling++;
                    errorRecoveryMechanisms += cu.findAll(CatchClause.class).size();
                } else {
                    // Check if method throws exceptions or calls methods that might throw
                    boolean mightThrow = method.getThrownExceptions().size() > 0 ||
                                       methodBody.contains("throw") ||
                                       methodBody.contains("Exception") ||
                                       methodBody.contains("Error");
                    if (mightThrow && !methodBody.contains("catch") && !methodBody.contains("try")) {
                        unhandledExceptions++;
                        reliabilityIssues.add(new ReliabilityIssue(
                            "Unhandled Exception",
                            fileName,
                            method.getBegin().map(b -> b.line).orElse(0),
                            "Method may throw exceptions without proper handling",
                            "MEDIUM"
                        ));
                    }
                }

                // Check for null pointer risks
                if (methodBody.contains(".") && 
                    !methodBody.contains("if") && 
                    !methodBody.contains("null") &&
                    !methodBody.contains("Optional")) {
                    // Simple heuristic: method calls without null checks
                    long methodCalls = method.findAll(MethodCallExpr.class).size();
                    if (methodCalls > 3) {
                        nullPointerRisks++;
                        reliabilityIssues.add(new ReliabilityIssue(
                            "Potential Null Pointer",
                            fileName,
                            method.getBegin().map(b -> b.line).orElse(0),
                            "Method has multiple method calls without apparent null checks",
                            "LOW"
                        ));
                    }
                }

                // Check for resource leaks (simplified)
                if (methodBody.contains("new FileInputStream") || 
                    methodBody.contains("new FileOutputStream") ||
                    methodBody.contains("new BufferedReader") ||
                    methodBody.contains("new Connection")) {
                    if (!methodBody.contains("try-with-resources") && 
                        !methodBody.contains("finally") &&
                        !methodBody.contains(".close()")) {
                        resourceLeakRisks++;
                        reliabilityIssues.add(new ReliabilityIssue(
                            "Resource Leak Risk",
                            fileName,
                            method.getBegin().map(b -> b.line).orElse(0),
                            "Resource may not be properly closed",
                            "MEDIUM"
                        ));
                    }
                }
            });
        }

        // Calculate exception handling coverage
        double exceptionHandlingCoverage = totalMethods > 0 ? 
            (double) methodsWithExceptionHandling / totalMethods * 100 : 0;

        metrics.setExceptionHandlingCoverage(exceptionHandlingCoverage);
        metrics.setUnhandledExceptions(unhandledExceptions);
        metrics.setNullPointerRisks(nullPointerRisks);
        metrics.setResourceLeakRisks(resourceLeakRisks);
        metrics.setErrorRecoveryMechanisms(errorRecoveryMechanisms);
        metrics.setReliabilityIssues(reliabilityIssues);

        // Calculate reliability score (0-100)
        double score = 100.0;
        score -= (100 - exceptionHandlingCoverage) * 0.3; // Up to 30 points for exception handling
        score -= Math.min(unhandledExceptions * 5, 25); // Max 25 points deduction
        score -= Math.min(nullPointerRisks * 2, 15); // Max 15 points deduction
        score -= Math.min(resourceLeakRisks * 5, 20); // Max 20 points deduction
        score += Math.min(errorRecoveryMechanisms * 2, 10); // Bonus for error recovery
        score = Math.max(0, Math.min(100, score));

        report.setReliabilityMetrics(metrics);
        report.setReliabilityScore(score);
    }
}
