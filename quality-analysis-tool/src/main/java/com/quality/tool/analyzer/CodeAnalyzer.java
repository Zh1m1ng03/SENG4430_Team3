package com.quality.tool.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.quality.tool.model.QualityReport;
import com.quality.tool.analyzer.metrics.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CodeAnalyzer {
    private final boolean verbose;
    private final JavaParser javaParser;
    private final MaintainabilityAnalyzer maintainabilityAnalyzer;
    private final SecurityAnalyzer securityAnalyzer;
    private final ReliabilityAnalyzer reliabilityAnalyzer;
    private final UsabilityAnalyzer usabilityAnalyzer;

    public CodeAnalyzer(boolean verbose) {
        this.verbose = verbose;
        this.javaParser = new JavaParser();
        this.maintainabilityAnalyzer = new MaintainabilityAnalyzer();
        this.securityAnalyzer = new SecurityAnalyzer();
        this.reliabilityAnalyzer = new ReliabilityAnalyzer();
        this.usabilityAnalyzer = new UsabilityAnalyzer();
    }

    public QualityReport analyze(Path projectPath) throws IOException {
        QualityReport report = new QualityReport();
        List<CompilationUnit> compilationUnits = new ArrayList<>();
        int totalFiles = 0;
        int totalLines = 0;

        if (verbose) {
            System.out.println("Scanning for Java files in: " + projectPath);
        }

        // Collect all Java files
        try (Stream<Path> paths = Files.walk(projectPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java"))
                 .forEach(javaFile -> {
                     try {
                         if (verbose) {
                             System.out.println("  Parsing: " + javaFile);
                         }
                         ParseResult<CompilationUnit> result = javaParser.parse(javaFile);
                         if (result.isSuccessful() && result.getResult().isPresent()) {
                             compilationUnits.add(result.getResult().get());
                             totalFiles++;
                             try {
                                 totalLines += Files.readAllLines(javaFile).size();
                             } catch (IOException e) {
                                 // Ignore
                             }
                         } else {
                             if (verbose) {
                                 System.err.println("  Failed to parse: " + javaFile);
                             }
                         }
                     } catch (Exception e) {
                         if (verbose) {
                             System.err.println("  Error parsing " + javaFile + ": " + e.getMessage());
                         }
                     }
                 });
        }

        report.setTotalFiles(totalFiles);
        report.setTotalLinesOfCode(totalLines);

        if (verbose) {
            System.out.println("\nAnalyzing " + totalFiles + " Java files...");
        }

        // Analyze each quality aspect
        maintainabilityAnalyzer.analyze(compilationUnits, report);
        securityAnalyzer.analyze(compilationUnits, report);
        reliabilityAnalyzer.analyze(compilationUnits, report);
        usabilityAnalyzer.analyze(compilationUnits, report);

        // Calculate overall score (weighted average)
        double overallScore = (
            report.getMaintainabilityScore() * 0.3 +
            report.getSecurityScore() * 0.3 +
            report.getReliabilityScore() * 0.25 +
            report.getUsabilityScore() * 0.15
        );
        report.setOverallScore(overallScore);

        // Generate recommendations
        generateRecommendations(report);

        return report;
    }

    private void generateRecommendations(QualityReport report) {
        List<String> recommendations = new ArrayList<>();

        // Maintainability recommendations
        if (report.getMaintainabilityMetrics().getAverageCyclomaticComplexity() > 10) {
            recommendations.add("Consider refactoring methods with high cyclomatic complexity (>10)");
        }
        if (report.getMaintainabilityMetrics().getCodeDuplicationPercentage() > 20) {
            recommendations.add("Reduce code duplication (currently " + 
                              report.getMaintainabilityMetrics().getCodeDuplicationPercentage() + "%)");
        }
        if (report.getMaintainabilityMetrics().getLongMethods() > 0) {
            recommendations.add("Break down " + report.getMaintainabilityMetrics().getLongMethods() + 
                              " long methods into smaller, focused methods");
        }

        // Security recommendations
        if (report.getSecurityMetrics().getSqlInjectionRisks() > 0) {
            recommendations.add("Fix " + report.getSecurityMetrics().getSqlInjectionRisks() + 
                              " potential SQL injection vulnerabilities");
        }
        if (report.getSecurityMetrics().getHardcodedSecrets() > 0) {
            recommendations.add("Remove " + report.getSecurityMetrics().getHardcodedSecrets() + 
                              " hardcoded secrets and use secure configuration management");
        }

        // Reliability recommendations
        if (report.getReliabilityMetrics().getUnhandledExceptions() > 0) {
            recommendations.add("Add proper exception handling for " + 
                              report.getReliabilityMetrics().getUnhandledExceptions() + " unhandled exceptions");
        }
        if (report.getReliabilityMetrics().getNullPointerRisks() > 0) {
            recommendations.add("Add null checks for " + report.getReliabilityMetrics().getNullPointerRisks() + 
                              " potential null pointer risks");
        }

        // Usability recommendations
        if (report.getUsabilityMetrics().getMissingJavaDocMethods() > 0) {
            recommendations.add("Add JavaDoc comments for " + 
                              report.getUsabilityMetrics().getMissingJavaDocMethods() + " methods");
        }
        if (report.getUsabilityMetrics().getUnclearMethodNames() > 0) {
            recommendations.add("Improve naming clarity for " + 
                              report.getUsabilityMetrics().getUnclearMethodNames() + " methods");
        }

        report.setRecommendations(recommendations);
    }
}
