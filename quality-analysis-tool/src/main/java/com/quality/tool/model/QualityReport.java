package com.quality.tool.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class QualityReport {
    private int totalFiles;
    private int totalLinesOfCode;
    private double maintainabilityScore;
    private double securityScore;
    private double reliabilityScore;
    private double usabilityScore;
    private double overallScore;

    @JsonProperty("maintainability")
    private MaintainabilityMetrics maintainabilityMetrics = new MaintainabilityMetrics();
    
    @JsonProperty("security")
    private SecurityMetrics securityMetrics = new SecurityMetrics();
    
    @JsonProperty("reliability")
    private ReliabilityMetrics reliabilityMetrics = new ReliabilityMetrics();
    
    @JsonProperty("usability")
    private UsabilityMetrics usabilityMetrics = new UsabilityMetrics();

    private List<String> issues = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();

    // Getters and Setters
    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public int getTotalLinesOfCode() {
        return totalLinesOfCode;
    }

    public void setTotalLinesOfCode(int totalLinesOfCode) {
        this.totalLinesOfCode = totalLinesOfCode;
    }

    public double getMaintainabilityScore() {
        return maintainabilityScore;
    }

    public void setMaintainabilityScore(double maintainabilityScore) {
        this.maintainabilityScore = maintainabilityScore;
    }

    public double getSecurityScore() {
        return securityScore;
    }

    public void setSecurityScore(double securityScore) {
        this.securityScore = securityScore;
    }

    public double getReliabilityScore() {
        return reliabilityScore;
    }

    public void setReliabilityScore(double reliabilityScore) {
        this.reliabilityScore = reliabilityScore;
    }

    public double getUsabilityScore() {
        return usabilityScore;
    }

    public void setUsabilityScore(double usabilityScore) {
        this.usabilityScore = usabilityScore;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public MaintainabilityMetrics getMaintainabilityMetrics() {
        return maintainabilityMetrics;
    }

    public void setMaintainabilityMetrics(MaintainabilityMetrics maintainabilityMetrics) {
        this.maintainabilityMetrics = maintainabilityMetrics;
    }

    public SecurityMetrics getSecurityMetrics() {
        return securityMetrics;
    }

    public void setSecurityMetrics(SecurityMetrics securityMetrics) {
        this.securityMetrics = securityMetrics;
    }

    public ReliabilityMetrics getReliabilityMetrics() {
        return reliabilityMetrics;
    }

    public void setReliabilityMetrics(ReliabilityMetrics reliabilityMetrics) {
        this.reliabilityMetrics = reliabilityMetrics;
    }

    public UsabilityMetrics getUsabilityMetrics() {
        return usabilityMetrics;
    }

    public void setUsabilityMetrics(UsabilityMetrics usabilityMetrics) {
        this.usabilityMetrics = usabilityMetrics;
    }

    public List<String> getIssues() {
        return issues;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
