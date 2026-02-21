package com.team3.report;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for the report generator. Bound to report.* in application.yaml.
 * Only whether to generate reports is configurable; path is fixed (static-metric-report/).
 */
@Component
@ConfigurationProperties(prefix = "report")
public class ReportProperties {

    /** Whether to write JSON reports after running metrics. */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
