package com.team3.staticMetric.analyser.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.team3.staticMetric.analyser.IMetricAnalyser;
import com.team3.staticMetric.analyser.Result;
import com.team3.staticMetric.entity.Report;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.CpdAnalysis;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.lang.document.FileLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DuplicatedCodeRatioMetricAnalyser implements IMetricAnalyser {

    private static final int MIN_TOKENS = 30;

    @Override
    public String id() {
        return "DUP_RATIO";
    }

    @Override
    public String description() {
        return "Duplicated code ratio (CPD, minTokens=30, ignoreIdentifiers=true) for controller/service/dao";
    }

    @Override
    public Result run(CompilationUnit cu) {
        throw new UnsupportedOperationException("Use runOnProject for " + id());
    }

    @Override
    public boolean isProjectLevel() {
        return true;
    }

    @Override
    public Report runOnProject(Path projectRoot, List<Path> javaFiles) {
        if (javaFiles == null || javaFiles.isEmpty()) {
            return new Report(id(), description(), List.of(), 0.0, "ratio");
        }

        Map<String, Path> keyToFile = new HashMap<>();
        Map<Path, Integer> totalLinesByFile = new HashMap<>();
        Map<Path, BitSet> duplicatedLinesByFile = new HashMap<>();

        for (Path file : javaFiles) {
            Path abs = normalize(file);
            keyToFile.put(abs.toString(), abs);
            totalLinesByFile.put(abs, safeCountLines(abs));
            duplicatedLinesByFile.put(abs, new BitSet(Math.max(1, totalLinesByFile.get(abs) + 1)));
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
                    for (Mark mark : match) {
                        FileLocation loc = mark.getLocation();
                        Path file = resolveMarkFile(keyToFile, loc);
                        if (file == null) {
                            continue;
                        }
                        Integer totalLines = totalLinesByFile.get(file);
                        if (totalLines == null || totalLines <= 0) {
                            continue;
                        }

                        int start = Math.max(1, loc.getStartLine());
                        int end = Math.min(totalLines, loc.getEndLine());
                        if (end < start) {
                            continue;
                        }
                        duplicatedLinesByFile.get(file).set(start, end + 1);
                    }
                }
            });
        } catch (Exception e) {
            // If CPD fails entirely, return zeros but still provide per-file totals (all 0 duplication).
        }

        int totalLinesProject = totalLinesByFile.values().stream().mapToInt(Integer::intValue).sum();
        int duplicatedLinesProject = duplicatedLinesByFile.entrySet().stream()
                .mapToInt(e -> e.getValue().cardinality())
                .sum();

        double projectRatio = totalLinesProject == 0 ? 0.0 : ((double) duplicatedLinesProject) / totalLinesProject;

        List<Report.FileResult> fileResults = new ArrayList<>();
        for (Path file : totalLinesByFile.keySet()) {
            int total = totalLinesByFile.getOrDefault(file, 0);
            int dup = duplicatedLinesByFile.getOrDefault(file, new BitSet()).cardinality();
            double ratio = total == 0 ? 0.0 : ((double) dup) / total;
            fileResults.add(new Report.FileResult(file.getFileName().toString(), ratio));
        }

        return new Report(id(), description(), fileResults, projectRatio, "ratio");
    }

    private static Path resolveMarkFile(Map<String, Path> keyToFile, FileLocation loc) {
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

    private static Path normalize(Path p) {
        return p.toAbsolutePath().normalize();
    }

    private static int safeCountLines(Path file) {
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
}

