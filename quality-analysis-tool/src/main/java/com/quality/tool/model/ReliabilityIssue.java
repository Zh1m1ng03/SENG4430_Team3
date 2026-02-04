package com.quality.tool.model;

public class ReliabilityIssue {
    private String type;
    private String file;
    private int line;
    private String description;
    private String severity;

    public ReliabilityIssue() {}

    public ReliabilityIssue(String type, String file, int line, String description, String severity) {
        this.type = type;
        this.file = file;
        this.line = line;
        this.description = description;
        this.severity = severity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
