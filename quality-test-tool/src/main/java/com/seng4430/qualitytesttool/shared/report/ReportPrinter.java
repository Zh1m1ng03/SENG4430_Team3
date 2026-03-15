package com.seng4430.qualitytesttool.shared.report;

import com.seng4430.qualitytesttool.staticanalysis.metric.MetricRating;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricResult;

import java.util.List;

public class ReportPrinter {

    public static void printSingle(MetricResult result) {
        System.out.println();
        System.out.println(result.getMetricName() + " (" + result.getQualityAspect() + ")");
        System.out.println("Score: " + String.format("%.2f", result.getScore()) + "  [" + ratingLabel(result.getRating()) + "]");
        System.out.println();
        for (String detail : result.getDetails()) {
            System.out.println("  " + detail);
        }
        System.out.println();
    }

    public static void printAll(List<MetricResult> results) {
        System.out.println();
        System.out.println("FULL QUALITY REPORT");
        System.out.println("-------------------");

        double total = 0;
        for (MetricResult r : results) {
            System.out.println("  - " + r.getMetricName() + ": " + String.format("%.2f", r.getScore()) + "  [" + ratingLabel(r.getRating()) + "]");
            total += ratingScore(r.getRating());
        }

        double overall = results.isEmpty() ? 0 : total / results.size();
        System.out.println();
        System.out.println("Overall Score: " + String.format("%.0f", overall) + " / 100");
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
}