package com.seng4430.qulitytesttool.shared.cli;

import com.seng4430.qulitytesttool.staticanalysis.engine.StaticAnalysisEngine;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricAnalyser;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricResult;
import com.seng4430.qulitytesttool.shared.report.ReportPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class MenuController {

    private static final int WIDTH   = 50;
    private static final String LINE    = "═".repeat(WIDTH);
    private static final String DIVIDER = "─".repeat(WIDTH);

    private final StaticAnalysisEngine engine;
    private final List<MetricAnalyser> analysers;
    private final Scanner scanner;
    private String currentPath = "(none)";

    public MenuController(StaticAnalysisEngine engine, List<MetricAnalyser> analysers) {
        this.engine    = engine;
        this.analysers = analysers;
        this.scanner   = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            printMenu();
            String input = scanner.nextLine().trim();
            handleInput(input);
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("╔" + LINE + "╗");
        System.out.println("║" + centre("SENG4430 Software Quality Tool", WIDTH) + "║");
        System.out.println("╠" + LINE + "╣");
        System.out.println("║  Current Path: " + padRight(currentPath, WIDTH - 16) + "║");
        System.out.println("╠" + LINE + "╣");
        System.out.println("║  [1] Load / Change Target Path" + padRight("", WIDTH - 31) + "║");
        System.out.println("║" + padRight(DIVIDER, WIDTH) + "║");

        List<MetricAnalyser> active = analysers;
        for (int i = 0; i < active.size(); i++) {
            MetricAnalyser a = active.get(i);
            String option = "  [" + (i + 2) + "] " + a.getMetricName()
                    + "  (" + a.getQualityAspect() + ")";
            System.out.println("║" + padRight(option, WIDTH) + "║");
        }

        int dynamicIndex = active.size() + 2;
        int runAllIndex  = active.size() + 3;

        System.out.println("║" + padRight(DIVIDER, WIDTH) + "║");
        System.out.println("║  [" + dynamicIndex + "] Dynamic Metric (Coming Soon)" + padRight("", WIDTH - 33) + "║");
        System.out.println("║" + padRight(DIVIDER, WIDTH) + "║");
        System.out.println("║  [" + runAllIndex + "] Run All Metrics" + padRight("", WIDTH - 20) + "║");
        System.out.println("║" + padRight(DIVIDER, WIDTH) + "║");
        System.out.println("║  [0] Exit" + padRight("", WIDTH - 10) + "║");
        System.out.println("╚" + LINE + "╝");
        System.out.print("Enter your choice: ");
    }

    private void handleInput(String input) {
        List<MetricAnalyser> active = analysers;
        int dynamicIndex = active.size() + 2;
        int runAllIndex  = active.size() + 3;

        if (input.equals("0")) {
            System.out.println("Goodbye.");
            System.exit(0);
        } else if (input.equals("1")) {
            loadPath();
        } else if (input.equals(String.valueOf(dynamicIndex))) {
            System.out.println("\nThis metric is not yet available.");
            pause();
        } else if (input.equals(String.valueOf(runAllIndex))) {
            runAll();
        } else {
            try {
                int choice      = Integer.parseInt(input);
                int metricIndex = choice - 2;
                if (metricIndex >= 0 && metricIndex < active.size()) {
                    runSingle(active.get(metricIndex));
                } else {
                    System.out.println("Invalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void loadPath() {
        System.out.print("\nEnter target path: ");
        String input = scanner.nextLine().trim();
        Path path = Paths.get(input);
        try {
            int count  = engine.loadPath(path);
            currentPath = input;
            System.out.println("Path loaded. Found " + count + " Java files.");
        } catch (IOException e) {
            System.out.println("Error: could not read path — " + e.getMessage());
        }
        pause();
    }

    private void runSingle(MetricAnalyser analyser) {
        if (!engine.isLoaded()) {
            System.out.println("\nNo path loaded. Please load a target path first.");
            pause();
            return;
        }
        System.out.println("\nAnalysing " + analyser.getMetricName() + "...");
        MetricResult result = engine.runOne(analyser);
        ReportPrinter.printSingle(result);
        pause();
    }

    private void runAll() {
        if (!engine.isLoaded()) {
            System.out.println("\nNo path loaded. Please load a target path first.");
            pause();
            return;
        }
        List<MetricAnalyser> active = analysers;
        if (active.isEmpty()) {
            System.out.println("\nNo metrics are implemented yet.");
            pause();
            return;
        }
        System.out.println("\nRunning all metrics...");
        List<MetricResult> results = engine.runAll(active);
        ReportPrinter.printAll(results);
        pause();
    }

    private void pause() {
        System.out.print("Press Enter to return to menu...");
        scanner.nextLine();
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
