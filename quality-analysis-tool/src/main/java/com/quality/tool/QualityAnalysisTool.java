package com.quality.tool;

import com.quality.tool.analyzer.CodeAnalyzer;
import com.quality.tool.model.QualityReport;
import com.quality.tool.report.ReportGenerator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(name = "quality-tool", 
         description = "Software Quality Analysis Tool for Java projects",
         mixinStandardHelpOptions = true,
         version = "1.0.0")
public class QualityAnalysisTool implements Callable<Integer> {

    @Parameters(index = "0", description = "Path to the Java project directory or source files")
    private File projectPath;

    @Option(names = {"-o", "--output"}, 
            description = "Output directory for reports (default: ./quality-reports)")
    private File outputDir = new File("./quality-reports");

    @Option(names = {"-f", "--format"}, 
            description = "Report format: json, html, both (default: both)")
    private String format = "both";

    @Option(names = {"-s", "--self-test"}, 
            description = "Run self-test analysis on this tool's own codebase")
    private boolean selfTest = false;

    @Option(names = {"-v", "--verbose"}, 
            description = "Verbose output")
    private boolean verbose = false;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new QualityAnalysisTool()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            System.out.println("=== Software Quality Analysis Tool ===");
            System.out.println("Analyzing: " + projectPath.getAbsolutePath());
            
            Path targetPath = projectPath.toPath();
            
            // Self-test mode: analyze this tool's own codebase
            if (selfTest) {
                System.out.println("\n[Self-Test Mode] Analyzing own codebase...");
                Path selfPath = Paths.get(System.getProperty("user.dir"));
                targetPath = selfPath.resolve("src/main/java");
                System.out.println("Self-test path: " + targetPath);
            }

            if (!targetPath.toFile().exists()) {
                System.err.println("Error: Path does not exist: " + targetPath);
                return 1;
            }

            // Create output directory
            outputDir.mkdirs();

            // Analyze code
            CodeAnalyzer analyzer = new CodeAnalyzer(verbose);
            QualityReport report = analyzer.analyze(targetPath);

            // Generate reports
            ReportGenerator generator = new ReportGenerator(outputDir.toPath());
            
            if (format.equals("json") || format.equals("both")) {
                generator.generateJsonReport(report);
                System.out.println("JSON report generated: " + outputDir + "/quality-report.json");
            }
            
            if (format.equals("html") || format.equals("both")) {
                generator.generateHtmlReport(report);
                System.out.println("HTML report generated: " + outputDir + "/quality-report.html");
            }

            // Print summary
            printSummary(report);

            System.out.println("\nAnalysis complete!");
            return 0;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    private void printSummary(QualityReport report) {
        System.out.println("\n=== Quality Analysis Summary ===");
        System.out.printf("Total Files Analyzed: %d%n", report.getTotalFiles());
        System.out.printf("Total Lines of Code: %d%n", report.getTotalLinesOfCode());
        System.out.println("\n--- Maintainability Score: " + 
                          String.format("%.2f/100", report.getMaintainabilityScore()));
        System.out.println("--- Security Score: " + 
                          String.format("%.2f/100", report.getSecurityScore()));
        System.out.println("--- Reliability Score: " + 
                          String.format("%.2f/100", report.getReliabilityScore()));
        System.out.println("--- Usability Score: " + 
                          String.format("%.2f/100", report.getUsabilityScore()));
        System.out.println("\n--- Overall Quality Score: " + 
                          String.format("%.2f/100", report.getOverallScore()));
    }
}
