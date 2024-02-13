package com.github.renatolsjf.chassi.monitoring;

import java.util.Map;

public class MetricId {

    private final Class<? extends Metric> clazz;
    private final String name;
    private final Map<String, String> labels;

    public MetricId (Class<? extends Metric> clazz, String name, Map<String, String> labels) {
        this.clazz = clazz;
        this.name = name;
        this.labels = labels;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(this.clazz.getSimpleName()).append(this.name);
        this.labels.entrySet().forEach(l -> sb.append(l.getKey()).append(l.getValue()));
        return sb.toString();
    }

}
