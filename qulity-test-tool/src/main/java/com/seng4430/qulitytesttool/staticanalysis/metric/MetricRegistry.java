package com.seng4430.qulitytesttool.staticanalysis.metric;

import com.seng4430.qulitytesttool.shared.config.AnalysisConfig;
import com.seng4430.qulitytesttool.staticanalysis.metric.impl.CyclomaticComplexityAnalyser;
import com.seng4430.qulitytesttool.staticanalysis.metric.impl.UncaughtExceptionAnalyser;

import java.util.List;

/**
 * Central registry for all static metric analysers.
 * To add a new metric, instantiate it here — no other files need to change.
 */
public class MetricRegistry {

    public static List<MetricAnalyser> getAll(AnalysisConfig config) {
        return List.of(
                new CyclomaticComplexityAnalyser(config.getCyclomaticComplexity()),
                new UncaughtExceptionAnalyser()
        );
    }
}
