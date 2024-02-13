package com.github.renatolsjf.chassi.monitoring;

import java.util.Map;

public abstract class Metric {

    private String name;
    private Map<String, String> labels;

    protected Metric(String name, Map<String, String> labels) {
        this.name = name;
        this.labels = labels;
    }

    public MetricId toMetricId() {
        return new MetricId(this.getClass(), this.name, this.labels);
    }

}
