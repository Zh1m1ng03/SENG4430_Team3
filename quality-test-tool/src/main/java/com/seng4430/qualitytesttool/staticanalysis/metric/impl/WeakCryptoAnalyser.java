package com.seng4430.qualitytesttool.staticanalysis.metric.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricAnalyser;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Detects weak cryptographic APIs via {@link ObjectCreationExpr} and {@link MethodCallExpr}.
 * Score = max(0, 100 - violations × 20).
 */
public class WeakCryptoAnalyser implements MetricAnalyser {

    private static final double GOOD_THRESHOLD = 80.0;
    private static final double WARNING_THRESHOLD = 40.0;
    private static final int PENALTY_PER_VIOLATION = 20;

    /** Fixed order for per-pattern reporting (always shown, even when count is 0). */
    private static final String[] PATTERN_LABELS = {
            "MD5", "SHA-1", "SHA1", "DES", "RC4", "java.util.Random"
    };

    private record Violation(String fileName, int line, String api) {}

    private static final class FileCryptoStats {
        final String fileName;
        int lines;
        int objectCreations;
        int methodCalls;
        int violations;

        FileCryptoStats(String fileName, int lines) {
            this.fileName = fileName;
            this.lines = lines;
        }

        int scanSites() {
            return objectCreations + methodCalls;
        }

        double siteDensityPercent() {
            int s = scanSites();
            return s == 0 ? 0.0 : (100.0 * violations) / s;
        }

        double fileScore() {
            return Math.max(0.0, 100.0 - violations * PENALTY_PER_VIOLATION);
        }
    }

    @Override
    public MetricResult analyse(List<CompilationUnit> units) {
        if (units == null || units.isEmpty()) {
            return new MetricResult(
                    getMetricName(),
                    getQualityAspect(),
                    100.0,
                    MetricRating.GOOD,
                    List.of("No Java sources in scope — nothing to analyse."));
        }

        List<Violation> violations = new ArrayList<>();
        Map<String, FileCryptoStats> statsByFile = new LinkedHashMap<>();

        for (CompilationUnit unit : units) {
            String fileKey = unit.getStorage()
                    .map(s -> s.getPath().toAbsolutePath().normalize().toString())
                    .orElse("unknown-" + System.identityHashCode(unit));
            String fileName = unit.getStorage()
                    .map(s -> s.getFileName())
                    .orElse("Unknown");
            int lines = unit.getStorage().map(s -> safeCountLines(s.getPath())).orElse(0);
            FileCryptoStats stats = statsByFile.computeIfAbsent(
                    fileKey,
                    k -> new FileCryptoStats(fileName, lines));
            stats.lines = Math.max(stats.lines, lines);

            unit.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(ObjectCreationExpr n, Void arg) {
                    stats.objectCreations++;
                    if (isJavaUtilRandom(unit, n)) {
                        addViolation(fileName, lineOf(n), "java.util.Random", violations, stats);
                    }
                    Set<String> fromCtorStrings = new LinkedHashSet<>();
                    for (Expression expr : n.getArguments()) {
                        if (expr instanceof StringLiteralExpr sl) {
                            collectWeakFromString(sl.getValue(), fromCtorStrings);
                        }
                    }
                    for (String api : fromCtorStrings) {
                        addViolation(fileName, lineOf(n), api, violations, stats);
                    }
                    super.visit(n, arg);
                }

                @Override
                public void visit(MethodCallExpr n, Void arg) {
                    stats.methodCalls++;
                    for (String api : weakApisInMethodCall(n)) {
                        addViolation(fileName, lineOf(n), api, violations, stats);
                    }
                    super.visit(n, arg);
                }
            }, null);
        }

        int totalViolations = violations.size();
        int totalCtor = statsByFile.values().stream().mapToInt(s -> s.objectCreations).sum();
        int totalCall = statsByFile.values().stream().mapToInt(s -> s.methodCalls).sum();
        int totalSites = totalCtor + totalCall;
        int totalLines = statsByFile.values().stream().mapToInt(s -> s.lines).sum();

        double score = Math.max(0.0, Math.min(100.0, 100.0 - totalViolations * PENALTY_PER_VIOLATION));
        double projectDensity = totalSites == 0 ? 0.0 : (100.0 * totalViolations) / totalSites;

        MetricRating rating;
        if (score >= GOOD_THRESHOLD) {
            rating = MetricRating.GOOD;
        } else if (score >= WARNING_THRESHOLD) {
            rating = MetricRating.WARNING;
        } else {
            rating = MetricRating.CRITICAL;
        }

        Map<String, Integer> countByPattern = new LinkedHashMap<>();
        for (String p : PATTERN_LABELS) {
            countByPattern.put(p, 0);
        }
        for (Violation v : violations) {
            countByPattern.merge(v.api(), 1, Integer::sum);
        }

        List<String> details = buildDetails(
                units.size(),
                totalLines,
                totalCtor,
                totalCall,
                totalSites,
                totalViolations,
                projectDensity,
                score,
                rating,
                violations,
                statsByFile,
                countByPattern);

        return new MetricResult(getMetricName(), getQualityAspect(), score, rating, details);
    }

    private static void addViolation(
            String fileName,
            int line,
            String api,
            List<Violation> violations,
            FileCryptoStats stats) {
        violations.add(new Violation(fileName, line, api));
        stats.violations++;
    }

    private static int lineOf(com.github.javaparser.ast.Node n) {
        return n.getBegin().map(p -> p.line).orElse(-1);
    }

    private static int safeCountLines(Path file) {
        if (file == null || !Files.isRegularFile(file)) {
            return 0;
        }
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            int n = 0;
            while (br.readLine() != null) {
                n++;
            }
            return n;
        } catch (IOException e) {
            return 0;
        }
    }

    private static boolean isJavaUtilRandom(CompilationUnit cu, ObjectCreationExpr n) {
        String typeStr = n.getType().asString();
        if (typeStr.equals("java.util.Random")) {
            return true;
        }
        if (!typeStr.equals("Random")) {
            return false;
        }
        return cu.getImports().stream().anyMatch(imp -> {
            if (imp.isStatic()) {
                return false;
            }
            String name = imp.getNameAsString();
            if (name.equals("java.util.Random")) {
                return true;
            }
            return imp.isAsterisk() && name.equals("java.util");
        });
    }

    private static Set<String> weakApisInMethodCall(MethodCallExpr n) {
        Set<String> found = new LinkedHashSet<>();
        for (StringLiteralExpr lit : n.findAll(StringLiteralExpr.class)) {
            collectWeakFromString(lit.getValue(), found);
        }
        String callUpper = n.toString().toUpperCase();
        if (callUpper.contains("MD5") && !found.contains("MD5")) {
            String name = n.getNameAsString().toUpperCase();
            if (name.contains("MD5") || callUpper.contains(".MD5")) {
                found.add("MD5");
            }
        }
        return found;
    }

    private static void collectWeakFromString(String value, Set<String> out) {
        if (value == null || value.isEmpty()) {
            return;
        }
        String u = value.toUpperCase();
        if (u.contains("MD5")) {
            out.add("MD5");
        }
        if (u.contains("SHA-1") || u.contains("SHA_1")) {
            out.add("SHA-1");
        } else if (u.contains("SHA1") && !u.contains("SHA256") && !u.contains("SHA384") && !u.contains("SHA512")) {
            out.add("SHA1");
        }
        if (u.contains("RC4")) {
            out.add("RC4");
        }
        if (u.contains("DES")) {
            out.add("DES");
        }
    }

    private List<String> buildDetails(
            int fileCount,
            int totalLines,
            int totalCtor,
            int totalCall,
            int totalSites,
            int totalViolations,
            double projectDensity,
            double score,
            MetricRating rating,
            List<Violation> violations,
            Map<String, FileCryptoStats> statsByFile,
            Map<String, Integer> countByPattern) {

        List<String> details = new ArrayList<>();

        // Project roll-up (mirrors DuplicateCodeAnalyser first lines)
        details.add(String.format(
                "Java files analysed: %d | Total source lines: %d | ObjectCreationExpr: %d | MethodCallExpr: %d | Scan sites: %d",
                fileCount, totalLines, totalCtor, totalCall, totalSites));
        details.add(String.format(
                "Weak crypto usages: %d | Flagged-site ratio: %.2f%% | Project score: %.1f | Rating: %s",
                totalViolations, projectDensity, score, rating.name()));
        details.add(String.format(
                "Scoring: max(0, 100 - %d × violations) | Bands: GOOD ≥%.0f | WARNING ≥%.0f | else CRITICAL",
                PENALTY_PER_VIOLATION, GOOD_THRESHOLD, WARNING_THRESHOLD));
        details.add("Patterns: MD5, SHA-1, SHA1, DES, RC4, java.util.Random (ObjectCreationExpr + MethodCallExpr)");
        details.add("");

        // Per-pattern row: always every category with count and share of violations
        details.add("Per-pattern analysis (project):");
        for (String pattern : PATTERN_LABELS) {
            int c = countByPattern.getOrDefault(pattern, 0);
            double pctViol = totalViolations == 0 ? 0.0 : (100.0 * c) / totalViolations;
            details.add(String.format(
                    "  %-22s hits: %4d  |  share of violations: %6.2f%%  |  status: %s",
                    pattern, c, pctViol, c == 0 ? "clean" : "flagged"));
        }
        details.add("");

        // File table: only files with violations
        details.add("File Summary (sorted by violation count, files with issues only):");
        statsByFile.values().stream()
                .filter(s -> s.violations > 0)
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.violations, a.violations);
                    return cmp != 0 ? cmp : a.fileName.compareToIgnoreCase(b.fileName);
                })
                .forEach(s -> {
                    int sites = s.scanSites();
                    double dens = s.siteDensityPercent();
                    details.add(String.format(
                            "  %-40s lines: %4d  ctors: %5d  calls: %6d  sites: %5d  viol: %3d  site%%: %6.2f  score: %6.1f",
                            trimFileName(s.fileName, 40),
                            s.lines,
                            s.objectCreations,
                            s.methodCalls,
                            sites,
                            s.violations,
                            dens,
                            s.fileScore()));
                });

        if (totalViolations > 0) {
            details.add("");
            details.add("Occurrences (file:line → flagged API):");
            for (Violation v : violations) {
                String lineStr = v.line > 0 ? String.valueOf(v.line) : "?";
                details.add(String.format("  %-42s line: %4s  %s", v.fileName, lineStr, v.api));
            }
        } else {
            details.add("");
            details.add("No weak cryptographic APIs detected — all listed patterns are clean for scanned sites.");
        }

        return details;
    }

    private static String trimFileName(String name, int maxLen) {
        if (name == null || name.length() <= maxLen) {
            return name != null ? name : "";
        }
        return "…" + name.substring(name.length() - maxLen + 1);
    }

    @Override
    public String getMetricName() {
        return "Weak Crypto APIs";
    }

    @Override
    public String getQualityAspect() {
        return "Security";
    }
}
