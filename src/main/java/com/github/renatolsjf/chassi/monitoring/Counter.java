package com.github.renatolsjf.chassi.monitoring;

import java.util.Map;

public class Counter extends Metric {

    protected double value = 0;

    protected Counter(String name, Map<String, String> labels) {
        super(name, labels);
    }

    public void inc() {
        this.value++;
    }

    public void inc (double v) {
        if (v <= 0) {
            throw new InvalidMetricException("Can not increment a metric with a value lower than 0", v);
        }
        this.value += v;
    }

    public double getValue() {
        return this.value;
    }

}
