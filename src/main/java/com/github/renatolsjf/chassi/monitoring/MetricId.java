package com.github.renatolsjf.chassi.monitoring;

import java.util.Map;

public class MetricId {

    private final Class<? extends Metric> clazz;
    private final String name;
    private final Map<String, String> tags;

    public MetricId (Class<? extends Metric> clazz, String name, Map<String, String> tags) {
        this.clazz = clazz;
        this.name = name;
        this.tags = tags;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(this.clazz.getSimpleName()).append(this.name);
        this.tags.entrySet().forEach(l -> sb.append(l.getKey()).append(l.getValue()));
        return sb.toString();
    }

}
