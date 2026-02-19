package com.seng4430.metrics;

import com.github.javaparser.ast.CompilationUnit;
import com.seng4430.result.MetricResult;

public interface IMetric {
    String id();                 // e.g. "CC"
    String description();        // e.g. "Cyclomatic Complexity"
    MetricResult analyze(CompilationUnit cu);
}