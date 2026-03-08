package com.seng4430.qulitytesttool.staticanalysis.metric.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricAnalyser;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricResult;

import java.util.List;

public class UncaughtExceptionAnalyser implements MetricAnalyser {

    @Override
    public MetricResult analyse(List<CompilationUnit> units) {
        // TODO: implement uncaught exception analysis
        return new MetricResult(
                getMetricName(),
                getQualityAspect(),
                0.0,
                MetricRating.GOOD,
                List.of("Not yet implemented")
        );
    }

    @Override
    public String getMetricName() {
        return "Uncaught Exceptions";
    }

    @Override
    public String getQualityAspect() {
        return "Reliability";
    }
}
