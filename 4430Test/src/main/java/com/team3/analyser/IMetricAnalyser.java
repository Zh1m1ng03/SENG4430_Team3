package com.team3.analyser;

import com.github.javaparser.ast.CompilationUnit;


public interface IMetric {
    String id();                 // e.g. "CC"
    String description();        // e.g. "Cyclomatic Complexity"
    MetricResult analyze(CompilationUnit cu);
}