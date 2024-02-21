package io.github.renatolsjf.chassis.monitoring;

import java.util.Map;

public abstract class Metric {

    private String name;
    private Map<String, String> tags;

    private MetricListener metricListener;

    protected Metric(String name, Map<String, String> tags) {
        this.name = name;
        this.tags = tags;
    }

    public MetricId toMetricId() {
        return new MetricId(this.getClass(), this.name, this.tags);
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

    public Map<String, String> getTags() {
        return this.tags;
    }

}
