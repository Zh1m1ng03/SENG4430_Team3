package com.team3.staticMetric.factory;


// import other metrics...

import com.team3.staticMetric.analyser.IMetricAnalyser;
import com.team3.staticMetric.analyser.impl.CyclomaticComplexityMetricAnalyser;
import com.team3.staticMetric.analyser.impl.DuplicatedCodeRatioMetricAnalyser;

import java.util.*;

public class MetricAnalyserFactory {

    public static IMetricAnalyser create(String metricId) {
        return switch (metricId.toUpperCase()) {
            case "CC_AVG" -> new CyclomaticComplexityMetricAnalyser();
            case "DUP_RATIO" -> new DuplicatedCodeRatioMetricAnalyser();
            // case "LOC" -> new LinesOfCodeMetric();
            // case "NEST" -> new NestingDepthMetric();
            // ...
            default -> throw new IllegalArgumentException("Unknown metric: " + metricId);
        };
    }

    public static List<IMetricAnalyser> createAll() {
        return List.of(
                new CyclomaticComplexityMetricAnalyser(),
                new DuplicatedCodeRatioMetricAnalyser()
        );
    }
}