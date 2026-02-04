package com.quality.tool.model;

public class UsabilityIssue {
    private String type;
    private String file;
    private String location;
    private String description;
    private String severity;

    public UsabilityIssue() {}

    public UsabilityIssue(String type, String file, String location, String description, String severity) {
        this.type = type;
        this.file = file;
        this.location = location;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
