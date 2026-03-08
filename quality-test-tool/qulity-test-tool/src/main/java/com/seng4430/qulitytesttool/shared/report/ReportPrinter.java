package com.seng4430.qulitytesttool.shared.report;

import com.seng4430.qulitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricResult;

import java.util.List;

public class ReportPrinter {

    private static final int WIDTH      = 50;
    private static final String LINE    = "═".repeat(WIDTH);

    public static void printSingle(MetricResult result) {
        System.out.println();
        System.out.println("╔" + LINE + "╗");
        System.out.println("║" + centre(result.getMetricName(), WIDTH) + "║");
        System.out.println("║" + centre(result.getQualityAspect(), WIDTH) + "║");
        System.out.println("╠" + LINE + "╣");

        for (String detail : result.getDetails()) {
            System.out.println("║  " + padRight(detail, WIDTH - 2) + "║");
        }

        System.out.println("╠" + LINE + "╣");
        String summary = "Score: " + String.format("%.2f", result.getScore())
                + "   " + ratingLabel(result.getRating());
        System.out.println("║  " + padRight(summary, WIDTH - 2) + "║");
        System.out.println("╚" + LINE + "╝");
        System.out.println();
    }

    public static void printAll(List<MetricResult> results) {
        System.out.println();
        System.out.println("╔" + LINE + "╗");
        System.out.println("║" + centre("FULL QUALITY REPORT", WIDTH) + "║");
        System.out.println("╠" + LINE + "╣");

        double total = 0;
        for (MetricResult r : results) {
            String row = padRight("  " + r.getMetricName(), 30)
                    + padRight(String.format("%.2f", r.getScore()), 10)
                    + ratingLabel(r.getRating());
            System.out.println("║" + padRight(row, WIDTH) + "║");
            total += ratingScore(r.getRating());
        }

        double overall = results.isEmpty() ? 0 : total / results.size();
        System.out.println("╠" + LINE + "╣");
        System.out.println("║  " + padRight("Overall Score:   " + String.format("%.0f", overall) + " / 100", WIDTH - 2) + "║");
        System.out.println("╚" + LINE + "╝");
        System.out.println();
    }

    private static String ratingLabel(MetricRating rating) {
        return switch (rating) {
            case GOOD     -> "GOOD";
            case WARNING  -> "WARNING";
            case CRITICAL -> "CRITICAL";
        };
    }

    private static double ratingScore(MetricRating rating) {
        return switch (rating) {
            case GOOD     -> 100.0;
            case WARNING  -> 50.0;
            case CRITICAL -> 0.0;
        };
    }

    private static String centre(String text, int width) {
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text + " ".repeat(width - text.length() - pad);
    }

    private static String padRight(String text, int width) {
        if (text.length() >= width) return text.substring(0, width);
        return text + " ".repeat(width - text.length());
    }
}
