package com.quality.tool.analyzer.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.quality.tool.model.SecurityIssue;
import com.quality.tool.model.SecurityMetrics;
import com.quality.tool.model.QualityReport;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SecurityAnalyzer {
    private static final Pattern SQL_PATTERN = Pattern.compile(
        "(?i).*(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER).*\\+.*|.*\\+.*(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER)",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern SECRET_PATTERNS = Pattern.compile(
        "(?i).*(password|secret|apikey|apikey|token|credential)\\s*=\\s*[\"'][^\"']+[\"']",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern WEAK_RANDOM = Pattern.compile(
        "(?i).*new\\s+Random\\(\\).*|.*Math\\.random\\(\\).*",
        Pattern.CASE_INSENSITIVE
    );

    public void analyze(List<CompilationUnit> compilationUnits, QualityReport report) {
        SecurityMetrics metrics = new SecurityMetrics();
        List<SecurityIssue> securityIssues = new ArrayList<>();

        for (CompilationUnit cu : compilationUnits) {
            String fileName = cu.getStorage().map(s -> s.getPath().getFileName().toString())
                                  .orElse("Unknown.java");

            // Check for SQL injection risks
            cu.findAll(MethodCallExpr.class).forEach(methodCall -> {
                String methodName = methodCall.getNameAsString();
                if (methodName.equals("executeQuery") || methodName.equals("executeUpdate") || 
                    methodName.equals("prepareStatement")) {
                    // Check if SQL is constructed with string concatenation
                    methodCall.getParentNode().ifPresent(parent -> {
                        String code = parent.toString();
                        if (SQL_PATTERN.matcher(code).find()) {
                            metrics.setSqlInjectionRisks(metrics.getSqlInjectionRisks() + 1);
                            securityIssues.add(new SecurityIssue(
                                "SQL Injection Risk",
                                fileName,
                                methodCall.getBegin().map(b -> b.line).orElse(0),
                                "Potential SQL injection vulnerability detected",
                                "HIGH"
                            ));
                        }
                    });
                }
            });

            // Check for hardcoded secrets
            cu.findAll(StringLiteralExpr.class).forEach(literal -> {
                String value = literal.getValue();
                if (SECRET_PATTERNS.matcher(value).find() || 
                    (value.length() > 20 && looksLikeSecret(value))) {
                    metrics.setHardcodedSecrets(metrics.getHardcodedSecrets() + 1);
                    securityIssues.add(new SecurityIssue(
                        "Hardcoded Secret",
                        fileName,
                        literal.getBegin().map(b -> b.line).orElse(0),
                        "Potential hardcoded secret detected",
                        "HIGH"
                    ));
                }
            });

            // Check for weak random number generation
            cu.findAll(MethodCallExpr.class).forEach(methodCall -> {
                String code = methodCall.toString();
                if (WEAK_RANDOM.matcher(code).find()) {
                    metrics.setInsecureRandomUsage(metrics.getInsecureRandomUsage() + 1);
                    securityIssues.add(new SecurityIssue(
                        "Weak Random Usage",
                        fileName,
                        methodCall.getBegin().map(b -> b.line).orElse(0),
                        "Use SecureRandom instead of Random or Math.random()",
                        "MEDIUM"
                    ));
                }
            });

            // Check for XSS risks (simple pattern matching)
            cu.findAll(MethodCallExpr.class).forEach(methodCall -> {
                String methodName = methodCall.getNameAsString();
                if (methodName.equals("println") || methodName.equals("print") || 
                    methodName.equals("write") || methodName.equals("append")) {
                    // Check if user input is directly output without sanitization
                    if (methodCall.getArguments().size() > 0) {
                        String arg = methodCall.getArgument(0).toString();
                        if (arg.contains("request") || arg.contains("parameter") || 
                            arg.contains("getParameter")) {
                            metrics.setXssRisks(metrics.getXssRisks() + 1);
                            securityIssues.add(new SecurityIssue(
                                "XSS Risk",
                                fileName,
                                methodCall.getBegin().map(b -> b.line).orElse(0),
                                "Potential XSS vulnerability - user input not sanitized",
                                "MEDIUM"
                            ));
                        }
                    }
                }
            });

            // Check for missing input validation
            cu.findAll(MethodDeclaration.class).forEach(method -> {
                if (method.getParameters().size() > 3) {
                    // Methods with many parameters might need validation
                    boolean hasValidation = method.getBody()
                        .map(body -> body.toString().toLowerCase().contains("validate") ||
                                   body.toString().toLowerCase().contains("check") ||
                                   body.toString().toLowerCase().contains("if") && 
                                   body.toString().toLowerCase().contains("null"))
                        .orElse(false);
                    if (!hasValidation) {
                        metrics.setMissingInputValidation(metrics.getMissingInputValidation() + 1);
                        securityIssues.add(new SecurityIssue(
                            "Missing Input Validation",
                            fileName,
                            method.getBegin().map(b -> b.line).orElse(0),
                            "Method with multiple parameters lacks input validation",
                            "LOW"
                        ));
                    }
                }
            });
        }

        metrics.setSecurityIssues(securityIssues);

        // Calculate security score (0-100)
        double score = 100.0;
        score -= metrics.getSqlInjectionRisks() * 15; // High penalty
        score -= metrics.getHardcodedSecrets() * 15; // High penalty
        score -= metrics.getXssRisks() * 10;
        score -= metrics.getInsecureRandomUsage() * 5;
        score -= metrics.getMissingInputValidation() * 2;
        score = Math.max(0, score);

        report.setSecurityMetrics(metrics);
        report.setSecurityScore(score);
    }

    private boolean looksLikeSecret(String value) {
        // Simple heuristic: long strings with mixed case, numbers, and special chars
        return value.length() > 20 && 
               value.matches(".*[A-Z].*") && 
               value.matches(".*[a-z].*") && 
               value.matches(".*[0-9].*");
    }
}
