package com.team3.service;


// import other metrics...

import com.team3.service.impl.CyclomaticComplexityMetric;

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