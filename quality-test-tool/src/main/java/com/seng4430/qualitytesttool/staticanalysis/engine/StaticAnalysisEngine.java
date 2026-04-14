package com.seng4430.qualitytesttool.staticanalysis.engine;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.seng4430.qualitytesttool.shared.config.AnalysisConfig;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricAnalyser;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StaticAnalysisEngine {

    private static final List<String> UNIVERSAL_IGNORE_PATTERNS = List.of(
            "*Test.java",
            "*Tests.java"
    );

    private final AnalysisConfig config;
    private List<CompilationUnit> parsedUnits = new ArrayList<>();
    private boolean loaded = false;

    public StaticAnalysisEngine(AnalysisConfig config) {
        this.config = config;
        StaticJavaParser.getConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
    }

    public int loadPath(Path path) throws IOException {
        parsedUnits = new ArrayList<>();
        Files.walk(path)
                .filter(p -> p.toString().endsWith(".java"))
                .filter(p -> !isIgnoredByPattern(p))
                .filter(p -> !isIgnoredByPackage(p))
                .forEach(p -> {
                    try {
                        CompilationUnit cu = StaticJavaParser.parse(p);
                        parsedUnits.add(cu);
                    } catch (Exception e) {
                        System.out.println("  Skipped (parse error): " + p.getFileName());
                    }
                });
        loaded = true;
        return parsedUnits.size();
    }

    // Ignore files matching hardcoded universal patterns or user-defined patterns from yaml
    private boolean isIgnoredByPattern(Path path) {
        String fileName = path.getFileName().toString();
        return Stream.concat(
                UNIVERSAL_IGNORE_PATTERNS.stream(),
                config.getIgnore().getFilePatterns().stream()
        ).anyMatch(pattern -> matchesWildcard(fileName, pattern));
    }

    // Ignore files whose path contains an excluded package
    private boolean isIgnoredByPackage(Path path) {
        String normalised = path.toString().replace("\\", "/");
        return config.getIgnore().getPackages().stream()
                .anyMatch(pkg -> normalised.contains(pkg.replace(".", "/")));
    }

    private boolean matchesWildcard(String fileName, String pattern) {
        String regex = pattern.replace(".", "\\.").replace("*", ".*");
        return fileName.matches(regex);
    }

    public MetricResult runOne(MetricAnalyser analyser) {
        return analyser.analyse(parsedUnits);
    }

    public List<MetricResult> runAll(List<MetricAnalyser> targets) {
        List<MetricResult> results = new ArrayList<>();
        for (MetricAnalyser analyser : targets) {
            results.add(analyser.analyse(parsedUnits));
        }
        return results;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
