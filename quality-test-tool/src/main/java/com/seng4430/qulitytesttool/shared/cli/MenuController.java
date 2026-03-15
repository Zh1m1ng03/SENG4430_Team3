package com.seng4430.qulitytesttool.shared.cli;

import com.seng4430.qulitytesttool.staticanalysis.engine.StaticAnalysisEngine;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricAnalyser;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricResult;
import com.seng4430.qulitytesttool.shared.report.ReportPrinter;
import com.seng4430.qulitytesttool.dynamicanalysis.service.RunTimeTestService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class MenuController {

    private final StaticAnalysisEngine engine;
    private final List<MetricAnalyser> analysers;
    private final RunTimeTestService throughputService;
    private final Scanner scanner;
    private String currentPath = "(none)";

    public MenuController(StaticAnalysisEngine engine, List<MetricAnalyser> analysers, RunTimeTestService throughputService) {
        this.engine            = engine;
        this.analysers         = analysers;
        this.throughputService = throughputService;
        this.scanner           = new Scanner(System.in);
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
        System.out.println("SENG4430 Software Quality Tool");
        System.out.println("Current Path: " + currentPath);
        System.out.println();


        System.out.println("  1. Load / Change Target Path");
        for (int i = 0; i < analysers.size(); i++) {
            MetricAnalyser a = analysers.get(i);
            System.out.println("  " + (i + 2) + ". " + a.getMetricName() + " (" + a.getQualityAspect() + ")");
        }


        int throughputIndex = analysers.size() + 2;
        int runAllIndex     = analysers.size() + 3;


        System.out.println("  " + throughputIndex + ". Throughput (Reusability)");
        System.out.println("  " + runAllIndex  + ". Run All Metrics");
        System.out.println("  0. Exit");
        System.out.println();
        System.out.print("Enter your choice: ");
    }

    private void handleInput(String input) {
        int dynamicIndex = analysers.size() + 2;
        int runAllIndex  = analysers.size() + 3;

        if (input.equals("0")) {
            System.out.println("Goodbye.");
            System.exit(0);
        } else if (input.equals("1")) {
            loadPath();
        } else if (input.equals(String.valueOf(dynamicIndex))) {
            runThroughput();
        } else if (input.equals(String.valueOf(runAllIndex))) {
            runAll();
        } else {
            try {
                int choice      = Integer.parseInt(input);
                int metricIndex = choice - 2;
                if (metricIndex >= 0 && metricIndex < analysers.size()) {
                    runSingle(analysers.get(metricIndex));
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
            int count = engine.loadPath(path);
            currentPath = input;
            System.out.println("Path loaded. Found " + count + " Java files.");
        } catch (IOException e) {
            System.out.println("Error: could not read path - " + e.getMessage());
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
        if (analysers.isEmpty()) {
            System.out.println("\nNo metrics are implemented yet.");
            pause();
            return;
        }
        System.out.println("\nRunning all metrics...");
        List<MetricResult> results = engine.runAll(analysers);
        ReportPrinter.printAll(results);
        pause();
    }

    private void runThroughput() {
        System.out.println("\nRunning Throughput (Reusability) tests...");
        throughputService.testAllThroughput();
        pause();
    }

    private void pause() {
        System.out.print("Press Enter to return to menu...");
        scanner.nextLine();
    }
}