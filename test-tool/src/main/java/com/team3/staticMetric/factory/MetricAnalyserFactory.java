package com.team3.factory;


// import other metrics...

import com.team3.analyser.IMetricAnalyser;
import com.team3.analyser.impl.CyclomaticComplexityMetricAnalyser;

import java.util.*;

public class MetricAnalyserFactory {

    public static IMetricAnalyser create(String metricId) {
        return switch (metricId.toUpperCase()) {
            case "CC_AVG" -> new CyclomaticComplexityMetricAnalyser();
            // case "LOC" -> new LinesOfCodeMetric();
            // case "NEST" -> new NestingDepthMetric();
            // ...
            default -> throw new IllegalArgumentException("Unknown metric: " + metricId);
        };
    }

    public static List<IMetricAnalyser> createAll() {
        return List.of(
                new CyclomaticComplexityMetricAnalyser()
        );
    }
}