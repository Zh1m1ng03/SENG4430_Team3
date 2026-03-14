package com.seng4430.qulitytesttool.staticanalysis.metric;

import com.github.javaparser.ast.CompilationUnit;

import java.util.List;

public interface MetricAnalyser {
    MetricResult analyse(List<CompilationUnit> units);
    String getMetricName();
    String getQualityAspect();


}
