package com.seng4430.qualitytesttool.staticanalysis.metric;

import java.util.List;

public class MetricResult {

    private final String metricName;
    private final String qualityAspect;
    private final double score;
    private final MetricRating rating;
    private final List<String> details;

    public MetricResult(String metricName, String qualityAspect, double score, MetricRating rating, List<String> details) {
        this.metricName = metricName;
        this.qualityAspect = qualityAspect;
        this.score = score;
        this.rating = rating;
        this.details = details;
    }

    public String getMetricName()    { return metricName; }
    public String getQualityAspect() { return qualityAspect; }
    public double getScore()          { return score; }
    public MetricRating getRating()   { return rating; }
    public List<String> getDetails()  { return details; }
}
