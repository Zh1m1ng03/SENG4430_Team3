package com.team3.staticMetric.entity;

import java.util.List;

/**
 * Result of running a metric on a project: per-file results and project-level summary.
 */
public record Report(
        String metricId,
        String metricDescription,
        List<FileResult> fileResults,
        double projectValue,
        String projectAggregationLabel
) {
    public record FileResult(String fileName, double value) {}
}
