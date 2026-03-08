package com.seng4430.qulitytesttool.staticanalysis.metric;

import java.util.List;

/**
 * Central registry for all static metric analysers.
 * To add a new metric, instantiate it here — no other files need to change.
 * Example: new CyclomaticComplexityAnalyser()
 */
public class MetricRegistry {

    public static List<MetricAnalyser> getAll() {
        return List.of(
                // Add implemented MetricAnalyser instances here
        );
    }
}
