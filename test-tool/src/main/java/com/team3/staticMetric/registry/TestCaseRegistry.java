package com.team3.staticMetric.registry;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.team3.staticMetric.analyser.IMetricAnalyser;
import com.team3.staticMetric.factory.MetricAnalyserFactory;
import com.team3.staticMetric.analyser.Result;
import com.team3.staticMetric.entity.Report;
import com.team3.staticMetric.report.ReportGenerator;
import com.team3.staticMetric.report.ReportProperties;
import com.team3.staticMetric.io.DefaultSourcePathHolder;
import com.team3.staticMetric.io.JavaFileFinder;
import com.team3.staticMetric.entity.TestCase;
import com.team3.staticMetric.io.JavaSourceReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Registers all CLI test cases. Add or remove entries here to change the menu.
 * Names are used as menu labels; order is preserved.
 */
@Configuration
public class TestCaseRegistry {

    private final JavaSourceReader javaSourceReader;
    private final JavaFileFinder javaFileFinder;
    private final DefaultSourcePathHolder defaultPathHolder;
    private final ReportGenerator reportGenerator;
    private final ReportProperties reportProperties;

    public TestCaseRegistry(JavaSourceReader javaSourceReader, JavaFileFinder javaFileFinder,
                            DefaultSourcePathHolder defaultPathHolder, ReportGenerator reportGenerator,
                            ReportProperties reportProperties) {
        this.javaSourceReader = javaSourceReader;
        this.javaFileFinder = javaFileFinder;
        this.defaultPathHolder = defaultPathHolder;
        this.reportGenerator = reportGenerator;
        this.reportProperties = reportProperties;
    }

    @Bean
    public List<TestCase> testCases() {
        List<TestCase> cases = new ArrayList<>();

        // Metric-based tests: one menu option per metric (name comes from metric description)
        for (IMetricAnalyser metric : MetricAnalyserFactory.createAll()) {
            cases.add(new TestCase("run " + metric.description(), () -> runMetric(metric)));
        }

        // Option to change default file path (shown on home menu)
        cases.add(new TestCase("change default file path", this::changeDefaultPath));

        return cases;
    }

    private void runMetric(IMetricAnalyser metric) {
        System.out.println("run " + metric.description());
        Scanner sc = new Scanner(System.in);
        Path path = resolvePath(sc);
        if (path == null) {
            System.out.println("No path given.");
            return;
        }
        try {
            List<Path> javaFiles = javaFileFinder.findJavaFiles(path);
            if (javaFiles.isEmpty()) {
                System.out.println("No Java files found under: " + path);
                return;
            }
            defaultPathHolder.setDefaultPath(path);
            System.out.println("Found " + javaFiles.size() + " Java file(s). Running " + metric.id() + "...");
            List<Report.FileResult> fileResults = new ArrayList<>();
            for (Path file : javaFiles) {
                try {
                    var cu = javaSourceReader.read(file);
                    if (isOnlyInterfaces(cu)) {
                        System.out.println("  " + file.getFileName() + " | skip: interface (not analysed)");
                        continue;
                    }
                    Result result = metric.run(cu);
                    fileResults.add(new Report.FileResult(file.getFileName().toString(), result.value()));
                    System.out.println("  " + file.getFileName() + " | " + result.metricId() + " = " + result.value());
                } catch (Exception e) {
                    System.out.println("  " + file.getFileName() + " | skip: " + e.getMessage());
                }
            }
            if (!fileResults.isEmpty()) {
                var aggregation = metric.projectAggregation();
                List<Double> perFileValues = fileResults.stream().map(Report.FileResult::value).toList();
                double projectValue = aggregation.apply(perFileValues);
                System.out.println("---");
                System.out.println("Project: " + fileResults.size() + " file(s), " + aggregation.label() + " " + metric.id() + " = " + projectValue);

                Report report = new Report(metric.id(), metric.description(), fileResults, projectValue, aggregation.label());
                if (reportProperties.isEnabled()) {
                    try {
                        Path savedPath = reportGenerator.write(report);
                        System.out.println("Report generated and stored in " + savedPath);
                    } catch (Exception e) {
                        System.out.println("Failed to save report: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static boolean isOnlyInterfaces(CompilationUnit cu) {
        var types = cu.findAll(ClassOrInterfaceDeclaration.class);
        if (types.isEmpty()) return true;
        return types.stream().allMatch(ClassOrInterfaceDeclaration::isInterface);
    }

    private Path resolvePath(Scanner sc) {
        String prompt = defaultPathHolder.getDefaultPath()
                .map(p -> "Enter path to Java file or project [default: " + p + "] (press Enter to use default): ")
                .orElse("Enter path to Java file or project (e.g. /path/to/spring-boot-project): ");
        System.out.print(prompt);
        String pathInput = sc.nextLine().trim();
        if (pathInput.isEmpty()) {
            return defaultPathHolder.getDefaultPath().orElse(null);
        }
        return Path.of(pathInput);
    }

    private void changeDefaultPath() {
        Scanner sc = new Scanner(System.in);
        defaultPathHolder.getDefaultPath()
                .ifPresentOrElse(
                        p -> System.out.println("Current default path: " + p),
                        () -> System.out.println("No default path set yet.")
                );
        System.out.print("Enter new default path (or press Enter to keep current): ");
        String pathInput = sc.nextLine().trim();
        if (pathInput.isEmpty()) {
            System.out.println("Default path unchanged.");
            return;
        }
        Path path = Path.of(pathInput);
        try {
            javaFileFinder.findJavaFiles(path);
            defaultPathHolder.setDefaultPath(path);
            System.out.println("Default path set to: " + path);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + " — path not set.");
        }
    }
}
