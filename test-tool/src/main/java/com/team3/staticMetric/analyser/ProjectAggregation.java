package com.team3.analyser;

import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * How to aggregate per-file results into a single project-level value.
 * Each static metric can declare which strategy fits (e.g. AVG for cyclomatic complexity, SUM for LOC).
 */
public enum ProjectAggregation {

    /** Average of per-file values (e.g. average CC per file). */
    AVG(values -> values.stream().mapToDouble(Double::doubleValue).average().orElse(0),
        "average"),

    /** Sum of per-file values (e.g. total lines of code). */
    SUM(values -> values.stream().mapToDouble(Double::doubleValue).sum(),
        "total"),

    /** Maximum of per-file values (e.g. max nesting depth). */
    MAX(values -> values.stream().mapToDouble(Double::doubleValue).max().orElse(0),
        "max");

    private final ToDoubleFunction<List<Double>> aggregator;
    private final String label;

    ProjectAggregation(java.util.function.ToDoubleFunction<List<Double>> aggregator, String label) {
        this.aggregator = aggregator;
        this.label = label;
    }

    public double apply(List<Double> perFileValues) {
        return perFileValues.isEmpty() ? 0 : aggregator.applyAsDouble(perFileValues);
    }

    public String label() {
        return label;
    }
}
