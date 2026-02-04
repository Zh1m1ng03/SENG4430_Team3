package com.quality.tool.analyzer.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.quality.tool.model.UsabilityIssue;
import com.quality.tool.model.UsabilityMetrics;
import com.quality.tool.model.QualityReport;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UsabilityAnalyzer {
    private static final Pattern CLEAR_NAME_PATTERN = Pattern.compile(
        "^[a-z][a-zA-Z0-9]*$|^[A-Z][a-zA-Z0-9]*$"
    );
    private static final Pattern UNCLEAR_PATTERNS = Pattern.compile(
        "(?i).*(temp|tmp|var|data|obj|val|str|num|arr|list|map|set|get|do|run|exec|proc|func|handle|process).*",
        Pattern.CASE_INSENSITIVE
    );

    public void analyze(List<CompilationUnit> compilationUnits, QualityReport report) {
        UsabilityMetrics metrics = new UsabilityMetrics();
        List<UsabilityIssue> usabilityIssues = new ArrayList<>();

        int totalMethods = 0;
        int methodsWithJavaDoc = 0;
        int totalClasses = 0;
        int classesWithJavaDoc = 0;
        int unclearMethodNames = 0;
        int unclearVariableNames = 0;
        int complexMethodSignatures = 0;

        for (CompilationUnit cu : compilationUnits) {
            String fileName = cu.getStorage().map(s -> s.getPath().getFileName().toString())
                                  .orElse("Unknown.java");

            // Analyze classes
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                totalClasses++;
                boolean hasJavaDoc = classDecl.getComment()
                    .filter(c -> c instanceof JavadocComment)
                    .isPresent();
                if (hasJavaDoc) {
                    classesWithJavaDoc++;
                } else {
                    usabilityIssues.add(new UsabilityIssue(
                        "Missing JavaDoc",
                        fileName,
                        classDecl.getNameAsString(),
                        "Class lacks JavaDoc documentation",
                        "LOW"
                    ));
                }

                // Analyze methods
                classDecl.getMethods().forEach(method -> {
                    totalMethods++;
                    
                    // Check for JavaDoc
                    boolean methodHasJavaDoc = method.getComment()
                        .filter(c -> c instanceof JavadocComment)
                        .isPresent();
                    if (methodHasJavaDoc) {
                        methodsWithJavaDoc++;
                    } else if (!method.getNameAsString().startsWith("get") && 
                              !method.getNameAsString().startsWith("set") &&
                              !method.getNameAsString().equals("equals") &&
                              !method.getNameAsString().equals("hashCode") &&
                              !method.getNameAsString().equals("toString")) {
                        usabilityIssues.add(new UsabilityIssue(
                            "Missing JavaDoc",
                            fileName,
                            method.getNameAsString(),
                            "Method lacks JavaDoc documentation",
                            "LOW"
                        ));
                    }

                    // Check method name clarity
                    String methodName = method.getNameAsString();
                    if (UNCLEAR_PATTERNS.matcher(methodName).find() && 
                        methodName.length() < 10) {
                        unclearMethodNames++;
                        usabilityIssues.add(new UsabilityIssue(
                            "Unclear Method Name",
                            fileName,
                            methodName,
                            "Method name may be unclear or too generic",
                            "LOW"
                        ));
                    }

                    // Check for complex method signatures
                    int paramCount = method.getParameters().size();
                    if (paramCount > 5) {
                        complexMethodSignatures++;
                        usabilityIssues.add(new UsabilityIssue(
                            "Complex Method Signature",
                            fileName,
                            methodName,
                            "Method has " + paramCount + " parameters (consider using a parameter object)",
                            "MEDIUM"
                        ));
                    }

                    // Check variable names (simplified - check parameters)
                    method.getParameters().forEach(param -> {
                        String paramName = param.getNameAsString();
                        if (UNCLEAR_PATTERNS.matcher(paramName).find() && 
                            paramName.length() < 4) {
                            unclearVariableNames++;
                        }
                    });
                });
            });
        }

        // Calculate documentation coverage
        double docCoverage = totalMethods > 0 ? 
            (double) methodsWithJavaDoc / totalMethods * 100 : 0;

        metrics.setDocumentationCoverage(docCoverage);
        metrics.setMissingJavaDocMethods(totalMethods - methodsWithJavaDoc);
        metrics.setMissingJavaDocClasses(totalClasses - classesWithJavaDoc);
        metrics.setUnclearMethodNames(unclearMethodNames);
        metrics.setUnclearVariableNames(unclearVariableNames);
        metrics.setComplexMethodSignatures(complexMethodSignatures);
        metrics.setUsabilityIssues(usabilityIssues);

        // Calculate naming convention score
        double namingScore = 100.0;
        namingScore -= Math.min(unclearMethodNames * 3, 30);
        namingScore -= Math.min(unclearVariableNames * 2, 20);
        namingScore = Math.max(0, namingScore);
        metrics.setNamingConventionScore(namingScore);

        // Calculate API design score
        double apiScore = 100.0;
        apiScore -= Math.min(complexMethodSignatures * 5, 30);
        apiScore = Math.max(0, apiScore);
        metrics.setApiDesignScore(apiScore);

        // Calculate usability score (0-100)
        double score = 100.0;
        score -= (100 - docCoverage) * 0.4; // Up to 40 points for documentation
        score -= Math.min(unclearMethodNames * 2, 20); // Max 20 points deduction
        score -= Math.min(complexMethodSignatures * 3, 20); // Max 20 points deduction
        score -= Math.min(unclearVariableNames * 1, 10); // Max 10 points deduction
        score = Math.max(0, score);

        report.setUsabilityMetrics(metrics);
        report.setUsabilityScore(score);
    }
}
