package com.seng4430.qulitytesttool.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "analysis")
public class AnalysisConfig {

    private Ignore ignore = new Ignore();

    public Ignore getIgnore() { return ignore; }
    public void setIgnore(Ignore ignore) { this.ignore = ignore; }

    public static class Ignore {
        private List<String> filePatterns = new ArrayList<>();
        private List<String> packages = new ArrayList<>();

        public List<String> getFilePatterns() { return filePatterns; }
        public void setFilePatterns(List<String> filePatterns) { this.filePatterns = filePatterns; }

        public List<String> getPackages() { return packages; }
        public void setPackages(List<String> packages) { this.packages = packages; }
    }
}
