package com.github.renatolsjf.chassi.monitoring;

import java.util.Map;

public class Counter extends Metric {

    protected double value = 0;

    protected Counter(String name, Map<String, String> labels) {
        super(name, labels);
    }

    @Override
    protected void doObserve(double v) {
        if (v <= 0) {
            throw new InvalidMetricException("Can not increment a metric with a value lower than 0", v);
        }
        this.value += v;
    }

    public void inc() {
        this.observe(1);
    }

    public void inc (double v) {
        this.observe(v);
    }

    public double getValue() {
        return this.value;
    }

}
