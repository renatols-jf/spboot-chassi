package com.github.renatolsjf.chassi.monitoring;

import java.util.Map;

public abstract class Metric {

    private String name;
    private Map<String, String> labels;

    private MetricListener metricListener;

    protected Metric(String name, Map<String, String> labels) {
        this.name = name;
        this.labels = labels;
    }

    public MetricId toMetricId() {
        return new MetricId(this.getClass(), this.name, this.labels);
    }

    protected abstract void doObserve(double v);

    public final void observe(double v) {
        this.doObserve(v);
        if (metricListener != null) {
            metricListener.metricObserved(this, v);
        }
    }

    public void setMetricListener(MetricListener metricListener) {
        this.metricListener = metricListener;
    }

    public void removeMetricListener() {
        this.metricListener = null;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String> getLabels() {
        return this.labels;
    }

}
