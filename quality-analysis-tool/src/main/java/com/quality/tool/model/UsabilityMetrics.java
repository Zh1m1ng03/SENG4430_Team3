package com.quality.tool.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class UsabilityMetrics {
    private double documentationCoverage;
    private int missingJavaDocMethods;
    private int missingJavaDocClasses;
    private double namingConventionScore;
    private int unclearMethodNames;
    private int unclearVariableNames;
    private double apiDesignScore;
    private int complexMethodSignatures;

    @JsonProperty("usabilityIssues")
    private List<UsabilityIssue> usabilityIssues = new ArrayList<>();

    public double getDocumentationCoverage() {
        return documentationCoverage;
    }

    public void setDocumentationCoverage(double documentationCoverage) {
        this.documentationCoverage = documentationCoverage;
    }

    public int getMissingJavaDocMethods() {
        return missingJavaDocMethods;
    }

    public void setMissingJavaDocMethods(int missingJavaDocMethods) {
        this.missingJavaDocMethods = missingJavaDocMethods;
    }

    public int getMissingJavaDocClasses() {
        return missingJavaDocClasses;
    }

    public void setMissingJavaDocClasses(int missingJavaDocClasses) {
        this.missingJavaDocClasses = missingJavaDocClasses;
    }

    public double getNamingConventionScore() {
        return namingConventionScore;
    }

    public void setNamingConventionScore(double namingConventionScore) {
        this.namingConventionScore = namingConventionScore;
    }

    public int getUnclearMethodNames() {
        return unclearMethodNames;
    }

    public void setUnclearMethodNames(int unclearMethodNames) {
        this.unclearMethodNames = unclearMethodNames;
    }

    public int getUnclearVariableNames() {
        return unclearVariableNames;
    }

    public void setUnclearVariableNames(int unclearVariableNames) {
        this.unclearVariableNames = unclearVariableNames;
    }

    public double getApiDesignScore() {
        return apiDesignScore;
    }

    public void setApiDesignScore(double apiDesignScore) {
        this.apiDesignScore = apiDesignScore;
    }

    public int getComplexMethodSignatures() {
        return complexMethodSignatures;
    }

    public void setComplexMethodSignatures(int complexMethodSignatures) {
        this.complexMethodSignatures = complexMethodSignatures;
    }

    public List<UsabilityIssue> getUsabilityIssues() {
        return usabilityIssues;
    }

    public void setUsabilityIssues(List<UsabilityIssue> usabilityIssues) {
        this.usabilityIssues = usabilityIssues;
    }
}
