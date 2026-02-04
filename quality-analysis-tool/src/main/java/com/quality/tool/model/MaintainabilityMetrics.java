package com.quality.tool.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class MaintainabilityMetrics {
    private double averageCyclomaticComplexity;
    private double averageMethodLength;
    private double averageClassLength;
    private int codeDuplicationPercentage;
    private int totalCodeSmells;
    private int longMethods;
    private int largeClasses;
    private int highComplexityMethods;
    private double couplingScore; // Lower is better
    private double cohesionScore; // Higher is better

    @JsonProperty("codeSmells")
    private List<CodeSmell> codeSmells = new ArrayList<>();

    public double getAverageCyclomaticComplexity() {
        return averageCyclomaticComplexity;
    }

    public void setAverageCyclomaticComplexity(double averageCyclomaticComplexity) {
        this.averageCyclomaticComplexity = averageCyclomaticComplexity;
    }

    public double getAverageMethodLength() {
        return averageMethodLength;
    }

    public void setAverageMethodLength(double averageMethodLength) {
        this.averageMethodLength = averageMethodLength;
    }

    public double getAverageClassLength() {
        return averageClassLength;
    }

    public void setAverageClassLength(double averageClassLength) {
        this.averageClassLength = averageClassLength;
    }

    public int getCodeDuplicationPercentage() {
        return codeDuplicationPercentage;
    }

    public void setCodeDuplicationPercentage(int codeDuplicationPercentage) {
        this.codeDuplicationPercentage = codeDuplicationPercentage;
    }

    public int getTotalCodeSmells() {
        return totalCodeSmells;
    }

    public void setTotalCodeSmells(int totalCodeSmells) {
        this.totalCodeSmells = totalCodeSmells;
    }

    public int getLongMethods() {
        return longMethods;
    }

    public void setLongMethods(int longMethods) {
        this.longMethods = longMethods;
    }

    public int getLargeClasses() {
        return largeClasses;
    }

    public void setLargeClasses(int largeClasses) {
        this.largeClasses = largeClasses;
    }

    public int getHighComplexityMethods() {
        return highComplexityMethods;
    }

    public void setHighComplexityMethods(int highComplexityMethods) {
        this.highComplexityMethods = highComplexityMethods;
    }

    public double getCouplingScore() {
        return couplingScore;
    }

    public void setCouplingScore(double couplingScore) {
        this.couplingScore = couplingScore;
    }

    public double getCohesionScore() {
        return cohesionScore;
    }

    public void setCohesionScore(double cohesionScore) {
        this.cohesionScore = cohesionScore;
    }

    public List<CodeSmell> getCodeSmells() {
        return codeSmells;
    }

    public void setCodeSmells(List<CodeSmell> codeSmells) {
        this.codeSmells = codeSmells;
    }
}
