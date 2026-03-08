package com.seng4430.qulitytesttool.staticanalysis.engine;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.seng4430.qulitytesttool.shared.config.AnalysisConfig;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricAnalyser;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricResult;

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

    private final List<MetricAnalyser> analysers;
    private final AnalysisConfig config;
    private List<CompilationUnit> parsedUnits = new ArrayList<>();
    private boolean loaded = false;

    public StaticAnalysisEngine(List<MetricAnalyser> analysers, AnalysisConfig config) {
        this.analysers = analysers;
        this.config = config;
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
                        if (!isUniversallyIgnored(cu)) {
                            parsedUnits.add(cu);
                        }
                    } catch (Exception e) {
                        System.out.println("  Skipped (parse error): " + p.getFileName());
                    }
                });
        loaded = true;
        return parsedUnits.size();
    }

    // Universal ignore: interfaces and annotation declarations
    private boolean isUniversallyIgnored(CompilationUnit cu) {
        return cu.findAll(ClassOrInterfaceDeclaration.class)
                .stream()
                .allMatch(ClassOrInterfaceDeclaration::isInterface);
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
