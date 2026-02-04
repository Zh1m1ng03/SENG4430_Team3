package com.quality.tool.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class SecurityMetrics {
    private int sqlInjectionRisks;
    private int xssRisks;
    private int hardcodedSecrets;
    private int weakEncryptionUsage;
    private int insecureRandomUsage;
    private int missingInputValidation;
    private int exposedSensitiveData;
    private int vulnerableDependencies;

    @JsonProperty("securityIssues")
    private List<SecurityIssue> securityIssues = new ArrayList<>();

    public int getSqlInjectionRisks() {
        return sqlInjectionRisks;
    }

    public void setSqlInjectionRisks(int sqlInjectionRisks) {
        this.sqlInjectionRisks = sqlInjectionRisks;
    }

    public int getXssRisks() {
        return xssRisks;
    }

    public void setXssRisks(int xssRisks) {
        this.xssRisks = xssRisks;
    }

    public int getHardcodedSecrets() {
        return hardcodedSecrets;
    }

    public void setHardcodedSecrets(int hardcodedSecrets) {
        this.hardcodedSecrets = hardcodedSecrets;
    }

    public int getWeakEncryptionUsage() {
        return weakEncryptionUsage;
    }

    public void setWeakEncryptionUsage(int weakEncryptionUsage) {
        this.weakEncryptionUsage = weakEncryptionUsage;
    }

    public int getInsecureRandomUsage() {
        return insecureRandomUsage;
    }

    public void setInsecureRandomUsage(int insecureRandomUsage) {
        this.insecureRandomUsage = insecureRandomUsage;
    }

    public int getMissingInputValidation() {
        return missingInputValidation;
    }

    public void setMissingInputValidation(int missingInputValidation) {
        this.missingInputValidation = missingInputValidation;
    }

    public int getExposedSensitiveData() {
        return exposedSensitiveData;
    }

    public void setExposedSensitiveData(int exposedSensitiveData) {
        this.exposedSensitiveData = exposedSensitiveData;
    }

    public int getVulnerableDependencies() {
        return vulnerableDependencies;
    }

    public void setVulnerableDependencies(int vulnerableDependencies) {
        this.vulnerableDependencies = vulnerableDependencies;
    }

    public List<SecurityIssue> getSecurityIssues() {
        return securityIssues;
    }

    public void setSecurityIssues(List<SecurityIssue> securityIssues) {
        this.securityIssues = securityIssues;
    }
}
