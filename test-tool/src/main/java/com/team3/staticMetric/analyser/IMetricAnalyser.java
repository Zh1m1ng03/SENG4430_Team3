package com.team3.staticMetric.analyser;

import com.github.javaparser.ast.CompilationUnit;
import com.team3.staticMetric.entity.Report;

import java.nio.file.Path;
import java.util.List;

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

    /**
     * Whether this metric runs at project-level (needs all files at once).
     * Default is false (per-file metrics).
     */
    default boolean isProjectLevel() {
        return false;
    }

    /**
     * Run this metric at project-level, given the project root and the java files selected for analysis.
     * Implementations must override this when {@link #isProjectLevel()} returns true.
     */
    default Report runOnProject(Path projectRoot, List<Path> javaFiles) {
        throw new UnsupportedOperationException("Not a project-level metric: " + id());
    }
}