package com.quality.tool.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.quality.tool.model.QualityReport;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReportGenerator {
    private final Path outputDir;
    private final ObjectMapper objectMapper;

    public ReportGenerator(Path outputDir) {
        this.outputDir = outputDir;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void generateJsonReport(QualityReport report) throws IOException {
        Files.createDirectories(outputDir);
        Path jsonFile = outputDir.resolve("quality-report.json");
        
        try (FileWriter writer = new FileWriter(jsonFile.toFile())) {
            objectMapper.writeValue(writer, report);
        }
    }

    public void generateHtmlReport(QualityReport report) throws IOException {
        Files.createDirectories(outputDir);
        Path htmlFile = outputDir.resolve("quality-report.html");
        
        String html = generateHtmlContent(report);
        
        try (FileWriter writer = new FileWriter(htmlFile.toFile())) {
            writer.write(html);
        }
    }

    private String generateHtmlContent(QualityReport report) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<title>Software Quality Analysis Report</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
        html.append(".container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append("h1 { color: #333; border-bottom: 3px solid #4CAF50; padding-bottom: 10px; }\n");
        html.append("h2 { color: #555; margin-top: 30px; border-bottom: 2px solid #ddd; padding-bottom: 5px; }\n");
        html.append(".summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin: 20px 0; }\n");
        html.append(".score-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; text-align: center; }\n");
        html.append(".score-card h3 { margin: 0 0 10px 0; font-size: 14px; opacity: 0.9; }\n");
        html.append(".score-card .score { font-size: 36px; font-weight: bold; margin: 10px 0; }\n");
        html.append(".metrics { background: #f9f9f9; padding: 15px; border-radius: 5px; margin: 15px 0; }\n");
        html.append(".metrics h3 { margin-top: 0; color: #667eea; }\n");
        html.append(".metrics ul { list-style-type: none; padding: 0; }\n");
        html.append(".metrics li { padding: 8px; border-bottom: 1px solid #eee; }\n");
        html.append(".metrics li:last-child { border-bottom: none; }\n");
        html.append(".issue { padding: 10px; margin: 5px 0; border-left: 4px solid #ff9800; background: #fff3e0; }\n");
        html.append(".issue.high { border-left-color: #f44336; background: #ffebee; }\n");
        html.append(".issue.medium { border-left-color: #ff9800; background: #fff3e0; }\n");
        html.append(".issue.low { border-left-color: #2196F3; background: #e3f2fd; }\n");
        html.append(".recommendations { background: #e8f5e9; padding: 15px; border-radius: 5px; border-left: 4px solid #4CAF50; margin: 20px 0; }\n");
        html.append(".recommendations ul { margin: 10px 0; padding-left: 20px; }\n");
        html.append("table { width: 100%; border-collapse: collapse; margin: 15px 0; }\n");
        html.append("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        html.append("th { background-color: #667eea; color: white; }\n");
        html.append("tr:hover { background-color: #f5f5f5; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        html.append("<div class=\"container\">\n");
        
        // Header
        html.append("<h1>Software Quality Analysis Report</h1>\n");
        html.append("<p><strong>Generated:</strong> ").append(java.time.LocalDateTime.now()).append("</p>\n");
        html.append("<p><strong>Files Analyzed:</strong> ").append(report.getTotalFiles()).append("</p>\n");
        html.append("<p><strong>Lines of Code:</strong> ").append(report.getTotalLinesOfCode()).append("</p>\n");
        
        // Overall Scores
        html.append("<div class=\"summary\">\n");
        html.append(createScoreCard("Overall Score", report.getOverallScore()));
        html.append(createScoreCard("Maintainability", report.getMaintainabilityScore()));
        html.append(createScoreCard("Security", report.getSecurityScore()));
        html.append(createScoreCard("Reliability", report.getReliabilityScore()));
        html.append(createScoreCard("Usability", report.getUsabilityScore()));
        html.append("</div>\n");
        
        // Maintainability Section
        html.append("<h2>Maintainability Metrics</h2>\n");
        html.append("<div class=\"metrics\">\n");
        html.append("<p><strong>Average Cyclomatic Complexity:</strong> ")
            .append(String.format("%.2f", report.getMaintainabilityMetrics().getAverageCyclomaticComplexity())).append("</p>\n");
        html.append("<p><strong>Average Method Length:</strong> ")
            .append(String.format("%.2f", report.getMaintainabilityMetrics().getAverageMethodLength())).append(" lines</p>\n");
        html.append("<p><strong>Code Duplication:</strong> ")
            .append(report.getMaintainabilityMetrics().getCodeDuplicationPercentage()).append("%</p>\n");
        html.append("<p><strong>Long Methods:</strong> ").append(report.getMaintainabilityMetrics().getLongMethods()).append("</p>\n");
        html.append("<p><strong>Large Classes:</strong> ").append(report.getMaintainabilityMetrics().getLargeClasses()).append("</p>\n");
        html.append("<p><strong>High Complexity Methods:</strong> ")
            .append(report.getMaintainabilityMetrics().getHighComplexityMethods()).append("</p>\n");
        html.append("</div>\n");
        
        // Security Section
        html.append("<h2>Security Metrics</h2>\n");
        html.append("<div class=\"metrics\">\n");
        html.append("<p><strong>SQL Injection Risks:</strong> ")
            .append(report.getSecurityMetrics().getSqlInjectionRisks()).append("</p>\n");
        html.append("<p><strong>XSS Risks:</strong> ").append(report.getSecurityMetrics().getXssRisks()).append("</p>\n");
        html.append("<p><strong>Hardcoded Secrets:</strong> ")
            .append(report.getSecurityMetrics().getHardcodedSecrets()).append("</p>\n");
        html.append("<p><strong>Insecure Random Usage:</strong> ")
            .append(report.getSecurityMetrics().getInsecureRandomUsage()).append("</p>\n");
        html.append("</div>\n");
        
        // Reliability Section
        html.append("<h2>Reliability Metrics</h2>\n");
        html.append("<div class=\"metrics\">\n");
        html.append("<p><strong>Exception Handling Coverage:</strong> ")
            .append(String.format("%.2f", report.getReliabilityMetrics().getExceptionHandlingCoverage())).append("%</p>\n");
        html.append("<p><strong>Unhandled Exceptions:</strong> ")
            .append(report.getReliabilityMetrics().getUnhandledExceptions()).append("</p>\n");
        html.append("<p><strong>Null Pointer Risks:</strong> ")
            .append(report.getReliabilityMetrics().getNullPointerRisks()).append("</p>\n");
        html.append("<p><strong>Resource Leak Risks:</strong> ")
            .append(report.getReliabilityMetrics().getResourceLeakRisks()).append("</p>\n");
        html.append("</div>\n");
        
        // Usability Section
        html.append("<h2>Usability Metrics</h2>\n");
        html.append("<div class=\"metrics\">\n");
        html.append("<p><strong>Documentation Coverage:</strong> ")
            .append(String.format("%.2f", report.getUsabilityMetrics().getDocumentationCoverage())).append("%</p>\n");
        html.append("<p><strong>Missing JavaDoc Methods:</strong> ")
            .append(report.getUsabilityMetrics().getMissingJavaDocMethods()).append("</p>\n");
        html.append("<p><strong>Unclear Method Names:</strong> ")
            .append(report.getUsabilityMetrics().getUnclearMethodNames()).append("</p>\n");
        html.append("<p><strong>Complex Method Signatures:</strong> ")
            .append(report.getUsabilityMetrics().getComplexMethodSignatures()).append("</p>\n");
        html.append("</div>\n");
        
        // Issues
        if (!report.getMaintainabilityMetrics().getCodeSmells().isEmpty() ||
            !report.getSecurityMetrics().getSecurityIssues().isEmpty() ||
            !report.getReliabilityMetrics().getReliabilityIssues().isEmpty() ||
            !report.getUsabilityMetrics().getUsabilityIssues().isEmpty()) {
            html.append("<h2>Issues Found</h2>\n");
            
            // Code Smells
            for (var smell : report.getMaintainabilityMetrics().getCodeSmells()) {
                html.append("<div class=\"issue ").append(smell.getSeverity().toLowerCase()).append("\">\n");
                html.append("<strong>").append(smell.getType()).append("</strong> - ")
                    .append(smell.getFile()).append(" (").append(smell.getLocation()).append(")<br>\n");
                html.append(smell.getDescription()).append("\n");
                html.append("</div>\n");
            }
            
            // Security Issues
            for (var issue : report.getSecurityMetrics().getSecurityIssues()) {
                html.append("<div class=\"issue ").append(issue.getSeverity().toLowerCase()).append("\">\n");
                html.append("<strong>").append(issue.getType()).append("</strong> - ")
                    .append(issue.getFile()).append(" (Line ").append(issue.getLine()).append(")<br>\n");
                html.append(issue.getDescription()).append("\n");
                html.append("</div>\n");
            }
        }
        
        // Recommendations
        if (!report.getRecommendations().isEmpty()) {
            html.append("<div class=\"recommendations\">\n");
            html.append("<h2>Recommendations</h2>\n");
            html.append("<ul>\n");
            for (String rec : report.getRecommendations()) {
                html.append("<li>").append(rec).append("</li>\n");
            }
            html.append("</ul>\n");
            html.append("</div>\n");
        }
        
        html.append("</div>\n</body>\n</html>");
        return html.toString();
    }

    private String createScoreCard(String title, double score) {
        String color = score >= 80 ? "#4CAF50" : score >= 60 ? "#FF9800" : "#F44336";
        return String.format(
            "<div class=\"score-card\" style=\"background: linear-gradient(135deg, %s 0%%, %s 100%%);\">\n" +
            "<h3>%s</h3>\n" +
            "<div class=\"score\">%.1f</div>\n" +
            "</div>\n",
            color, color, title, score
        );
    }
}
