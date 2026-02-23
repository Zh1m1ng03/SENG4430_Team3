package com.team3.staticMetric.report;

import com.team3.staticMetric.entity.Report;

import java.nio.file.Path;

/**
 * Writes metric reports as JSON to the fixed metric-report folder (subfolder per metric).
 */
public interface ReportGenerator {

    /**
     * Write the report as JSON to metric-report/&lt;metricId&gt;/ with auto-generated filename.
     * No user input required.
     *
     * @return the path where the report was written
     */
    Path write(Report report);
}
