package com.team3.report.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.team3.entity.Report;
import com.team3.report.ReportGenerator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes metric reports as JSON to static-metric-report/&lt;metricId&gt;/. Filename: &lt;metricId&gt;_yy-MM-dd_HHmmss.json
 */
@Component
public class ReportGeneratorImpl implements ReportGenerator {

    /** Fixed directory for all reports; not configurable. */
    public static final String REPORT_DIR = "static-metric-report";

    private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yy-MM-dd_HHmmss");

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public Path write(Report report) {
        Path metricDir = Path.of(REPORT_DIR).resolve(report.metricId());
        String fileName = report.metricId() + "_" + LocalDateTime.now().format(NAME_FORMAT) + ".json";
        Path path = metricDir.resolve(fileName);
        try {
            Files.createDirectories(metricDir);
            Files.writeString(path, gson.toJson(report), StandardCharsets.UTF_8);
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report: " + e.getMessage(), e);
        }
    }
}
