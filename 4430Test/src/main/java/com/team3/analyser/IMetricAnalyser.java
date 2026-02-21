package com.team3.analyser;

import com.github.javaparser.ast.CompilationUnit;

/**
 * Interface for static metrics. Implementations analyse a CompilationUnit (AST).
 */
public interface IMetricAnalyser {
    String id();                 // e.g. "CC_AVG"
    String description();        // e.g. "Average cyclomatic complexity per method"

    /**
     * Run this metric on the given compilation unit.
     */
    Result run(CompilationUnit cu);

    /**
     * How to aggregate per-file results into a project summary. Default is AVG.
     * Override for SUM (e.g. total LOC) or MAX (e.g. max nesting depth).
     */
    default ProjectAggregation projectAggregation() {
        return ProjectAggregation.AVG;
    }
}