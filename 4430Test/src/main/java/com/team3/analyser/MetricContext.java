package com.team3.analyser;

import com.github.javaparser.ast.CompilationUnit;

import java.util.Optional;

/**
 * Unified input for running any metric (static or dynamic).
 * Static analysers use {@link #compilationUnit()}; dynamic analysers use {@link #runConfig()}.
 */
public record MetricContext(
        Optional<CompilationUnit> compilationUnit,
        Optional<RunConfig> runConfig
) {
    public static MetricContext forStatic(CompilationUnit cu) {
        return new MetricContext(Optional.of(cu), Optional.empty());
    }

    public static MetricContext forDynamic(RunConfig runConfig) {
        return new MetricContext(Optional.empty(), Optional.of(runConfig));
    }
}
