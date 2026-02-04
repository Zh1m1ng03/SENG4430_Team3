package com.quality.tool.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class ReliabilityMetrics {
    private double exceptionHandlingCoverage;
    private int unhandledExceptions;
    private int nullPointerRisks;
    private int resourceLeakRisks;
    private int errorRecoveryMechanisms;
    private double errorHandlingScore;

    @JsonProperty("reliabilityIssues")
    private List<ReliabilityIssue> reliabilityIssues = new ArrayList<>();

    public double getExceptionHandlingCoverage() {
        return exceptionHandlingCoverage;
    }

    public void setExceptionHandlingCoverage(double exceptionHandlingCoverage) {
        this.exceptionHandlingCoverage = exceptionHandlingCoverage;
    }

    public int getUnhandledExceptions() {
        return unhandledExceptions;
    }

    public void setUnhandledExceptions(int unhandledExceptions) {
        this.unhandledExceptions = unhandledExceptions;
    }

    public int getNullPointerRisks() {
        return nullPointerRisks;
    }

    public void setNullPointerRisks(int nullPointerRisks) {
        this.nullPointerRisks = nullPointerRisks;
    }

    public int getResourceLeakRisks() {
        return resourceLeakRisks;
    }

    public void setResourceLeakRisks(int resourceLeakRisks) {
        this.resourceLeakRisks = resourceLeakRisks;
    }

    public int getErrorRecoveryMechanisms() {
        return errorRecoveryMechanisms;
    }

    public void setErrorRecoveryMechanisms(int errorRecoveryMechanisms) {
        this.errorRecoveryMechanisms = errorRecoveryMechanisms;
    }

    public double getErrorHandlingScore() {
        return errorHandlingScore;
    }

    public void setErrorHandlingScore(double errorHandlingScore) {
        this.errorHandlingScore = errorHandlingScore;
    }

    public List<ReliabilityIssue> getReliabilityIssues() {
        return reliabilityIssues;
    }

    public void setReliabilityIssues(List<ReliabilityIssue> reliabilityIssues) {
        this.reliabilityIssues = reliabilityIssues;
    }
}
