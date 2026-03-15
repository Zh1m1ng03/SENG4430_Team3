package com.seng4430.qualitytesttool.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "analysis")
public class AnalysisConfig {

    private Ignore ignore = new Ignore();
    private CyclomaticComplexity cyclomaticComplexity = new CyclomaticComplexity();

    public Ignore getIgnore() { return ignore; }
    public void setIgnore(Ignore ignore) { this.ignore = ignore; }

    public CyclomaticComplexity getCyclomaticComplexity() { return cyclomaticComplexity; }
    public void setCyclomaticComplexity(CyclomaticComplexity cyclomaticComplexity) { this.cyclomaticComplexity = cyclomaticComplexity; }

    public static class Ignore {
        private List<String> filePatterns = new ArrayList<>();
        private List<String> packages = new ArrayList<>();

        public List<String> getFilePatterns() { return filePatterns; }
        public void setFilePatterns(List<String> filePatterns) { this.filePatterns = filePatterns; }

        public List<String> getPackages() { return packages; }
        public void setPackages(List<String> packages) { this.packages = packages; }
    }

    public static class CyclomaticComplexity {
        private int goodThreshold = 10;
        private int warningThreshold = 20;
        private int topN = 10;

        public int getGoodThreshold() { return goodThreshold; }
        public void setGoodThreshold(int goodThreshold) { this.goodThreshold = goodThreshold; }

        public int getWarningThreshold() { return warningThreshold; }
        public void setWarningThreshold(int warningThreshold) { this.warningThreshold = warningThreshold; }

        public int getTopN() { return topN; }
        public void setTopN(int topN) { this.topN = topN; }
    }
}
