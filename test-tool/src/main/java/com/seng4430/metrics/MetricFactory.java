package com.seng4430.metrics;

import com.seng4430.metrics.impl.CyclomaticComplexityMetric;
// import other metrics...

import java.util.*;

public class MetricFactory {

    public static IMetric create(String metricId) {
        return switch (metricId.toUpperCase()) {
            case "CC_AVG" -> new CyclomaticComplexityMetric();
            // case "LOC" -> new LinesOfCodeMetric();
            // case "NEST" -> new NestingDepthMetric();
            // ...
            default -> throw new IllegalArgumentException("Unknown metric: " + metricId);
        };
    }

    public static List<IMetric> createAll() {
        return List.of(
                new CyclomaticComplexityMetric()
        );
    }
}