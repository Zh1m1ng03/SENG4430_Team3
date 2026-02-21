package com.team3.analyser;

/**
 * Single interface for both static and dynamic metrics.
 * Static: use context.compilationUnit() and analyze the AST.
 * Dynamic: use context.runConfig() and run/measure the code.
 */
public interface IMetricAnalyser {
    String id();                 // e.g. "CC_AVG"
    String description();        // e.g. "Average cyclomatic complexity per method"

    /**
     * Run this metric. Static analysers use compilationUnit; dynamic use runConfig.
     */
    Result run(MetricContext context);

    /**
     * How to aggregate per-file results into a project summary. Default is AVG.
     * Override for SUM (e.g. total LOC) or MAX (e.g. max nesting depth).
     */
    default ProjectAggregation projectAggregation() {
        return ProjectAggregation.AVG;
    }
}