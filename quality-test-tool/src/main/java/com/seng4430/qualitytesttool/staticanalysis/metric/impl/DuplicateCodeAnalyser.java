package com.seng4430.qualitytesttool.staticanalysis.metric.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricAnalyser;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricResult;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.CpdAnalysis;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.lang.document.FileLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DuplicateCodeAnalyser implements MetricAnalyser {

    private static final int MIN_TOKENS = 30;
    private static final double GOOD_THRESHOLD = 80.0;
    private static final double WARNING_THRESHOLD = 50.0;

    @Override
    public MetricResult analyse(List<CompilationUnit> units) {
        List<Path> files = units.stream()
                .map(u -> u.getStorage().map(s -> s.getPath()).orElse(null))
                .filter(p -> p != null)
                .map(this::normalize)
                .toList();

        if (files.isEmpty()) {
            return new MetricResult(
                    getMetricName(),
                    getQualityAspect(),
                    100.0,
                    MetricRating.GOOD,
                    List.of("No valid source files found.")
            );
        }

        Map<String, Path> keyToFile = new HashMap<>();
        Map<Path, Integer> totalLinesByFile = new LinkedHashMap<>();
        Map<Path, Set<Integer>> duplicatedCoverageLinesByFile = new LinkedHashMap<>();
        Map<Path, Set<Integer>> redundantLinesByFile = new LinkedHashMap<>();

        for (Path file : files) {
            keyToFile.put(file.toString(), file);
            int lines = safeCountLines(file);
            totalLinesByFile.put(file, lines);
            duplicatedCoverageLinesByFile.put(file, new java.util.HashSet<>());
            redundantLinesByFile.put(file, new java.util.HashSet<>());
        }

        CPDConfiguration config = new CPDConfiguration();
        config.setMinimumTileSize(MIN_TOKENS);
        config.setIgnoreIdentifiers(true);
        config.setSourceEncoding(StandardCharsets.UTF_8);
        config.setFailOnError(false);
        config.setOnlyRecognizeLanguage(config.getLanguageRegistry().getLanguageById("java"));

        try (CpdAnalysis cpd = CpdAnalysis.create(config)) {
            for (Path file : totalLinesByFile.keySet()) {
                cpd.files().addFile(file);
            }
            cpd.performAnalysis(report -> {
                for (Match match : report.getMatches()) {
                    List<MarkRange> ranges = new ArrayList<>();
                    for (Mark mark : match) {
                        FileLocation loc = mark.getLocation();
                        Path file = resolveMarkFile(keyToFile, loc);
                        if (file == null) {
                            continue;
                        }
                        int total = totalLinesByFile.getOrDefault(file, 0);
                        if (total <= 0) {
                            continue;
                        }

                        int start = Math.max(1, loc.getStartLine());
                        int end = Math.min(total, loc.getEndLine());
                        if (end < start) {
                            continue;
                        }
                        ranges.add(new MarkRange(file, start, end));
                    }

                    for (int i = 0; i < ranges.size(); i++) {
                        MarkRange r = ranges.get(i);
                        Set<Integer> coverage = duplicatedCoverageLinesByFile.get(r.file());
                        for (int line = r.start(); line <= r.end(); line++) {
                            coverage.add(line);
                        }

                        // Redundancy metric: keep one occurrence in each match, count the rest as redundant.
                        if (i > 0) {
                            Set<Integer> redundant = redundantLinesByFile.get(r.file());
                            for (int line = r.start(); line <= r.end(); line++) {
                                redundant.add(line);
                            }
                        }
                    }
                }
            });
        } catch (Exception ignored) {
            // If CPD fails, keep default zero-duplication values.
        }

        int totalLinesProject = totalLinesByFile.values().stream().mapToInt(Integer::intValue).sum();
        int duplicatedCoverageLinesProject = duplicatedCoverageLinesByFile.values().stream().mapToInt(Set::size).sum();
        int redundantLinesProject = redundantLinesByFile.values().stream().mapToInt(Set::size).sum();
        double coverageRatio = totalLinesProject == 0 ? 0.0 : (double) duplicatedCoverageLinesProject / totalLinesProject;
        double redundancyRatio = totalLinesProject == 0 ? 0.0 : (double) redundantLinesProject / totalLinesProject;
        double score = Math.max(0.0, Math.min(100.0, 100.0 * (1.0 - redundancyRatio)));

        MetricRating rating;
        if (score >= GOOD_THRESHOLD) {
            rating = MetricRating.GOOD;
        } else if (score >= WARNING_THRESHOLD) {
            rating = MetricRating.WARNING;
        } else {
            rating = MetricRating.CRITICAL;
        }

        List<String> details = new ArrayList<>();
        details.add(String.format(
                "Total code lines: %d | Redundant code lines: %d | Redundancy ratio: %.2f%%",
                totalLinesProject, redundantLinesProject, redundancyRatio * 100
        ));
        details.add(String.format(
                "Duplicated coverage lines: %d | Coverage ratio: %.2f%%",
                duplicatedCoverageLinesProject, coverageRatio * 100
        ));
        details.add("CPD config: minTokens=30, ignoreIdentifiers=true");
        details.add("");
        details.add("File Summary (sorted by redundancy ratio):");

        totalLinesByFile.entrySet().stream()
                .sorted((a, b) -> {
                    double ratioA = fileRatio(a.getKey(), a.getValue(), redundantLinesByFile);
                    double ratioB = fileRatio(b.getKey(), b.getValue(), redundantLinesByFile);
                    return Double.compare(ratioB, ratioA);
                })
                .forEach(entry -> {
                    int total = entry.getValue();
                    int redundant = redundantLinesByFile.getOrDefault(entry.getKey(), Set.of()).size();
                    int coverage = duplicatedCoverageLinesByFile.getOrDefault(entry.getKey(), Set.of()).size();
                    double ratio = total == 0 ? 0.0 : (double) redundant / total;
                    details.add(String.format(
                            "  %-40s lines: %4d redundant: %4d coverage: %4d ratio: %6.2f%%",
                            entry.getKey().getFileName().toString(), total, redundant, coverage, ratio * 100
                    ));
                });

        return new MetricResult(getMetricName(), getQualityAspect(), score, rating, details);
    }

    @Override
    public String getMetricName() {
        return "Duplicate Code Ratio";
    }

    @Override
    public String getQualityAspect() {
        return "Maintainability";
    }

    private Path resolveMarkFile(Map<String, Path> keyToFile, FileLocation loc) {
        String abs = loc.getFileId().getAbsolutePath();
        if (abs != null && !abs.isBlank()) {
            Path p = normalize(Path.of(abs));
            Path found = keyToFile.get(p.toString());
            if (found != null) {
                return found;
            }
        }

        String original = loc.getFileId().getOriginalPath();
        if (original != null && !original.isBlank()) {
            try {
                Path p = normalize(Path.of(original));
                return keyToFile.get(p.toString());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private double fileRatio(Path file, int total, Map<Path, Set<Integer>> duplicatedLinesByFile) {
        if (total == 0) {
            return 0.0;
        }
        int duplicated = duplicatedLinesByFile.getOrDefault(file, Set.of()).size();
        return (double) duplicated / total;
    }

    private Path normalize(Path p) {
        return p.toAbsolutePath().normalize();
    }

    private int safeCountLines(Path file) {
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            int lines = 0;
            while (br.readLine() != null) {
                lines++;
            }
            return lines;
        } catch (IOException e) {
            return 0;
        }
    }

    private record MarkRange(Path file, int start, int end) {}
}
