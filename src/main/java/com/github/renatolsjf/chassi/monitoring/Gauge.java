package com.github.renatolsjf.chassi.monitoring;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.Map;

public class Gauge extends Counter {

    private WeakReference<ObservableTask> taskReference;

    protected Gauge(String name, Map<String, String> labels) {
        super(name, labels);
    }

    @Override
    public void inc() {
        if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }
        super.inc();
    }

    @Override
    public void inc (double v) {
        if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }
        super.inc(v);
    }

    public void dec() {
        if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }
        this.value--;
    }

    public void dec (double v) {
        if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }
        if (v <= 0) {
            throw new InvalidMetricException("Can not decrement a metric with a value lower than 0", v);
        }
        this.value -= v;
    }

    public void set(double v) {
        if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }
        this.value = v;
    }

    public void setToCurrentTime() {
        if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }
        this.value = Instant.now().getEpochSecond();
    }

    public void reset() {
        if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }
        this.value = 0;
    }

    public void track(ObservableTask task) {
        if (task == null) {
            throw new NullPointerException("Observable task is null");
        }
        this.value = 0;
        this.taskReference = new WeakReference<>(task);
    }

    public double getValue() {
        if (this.isInTrackingMode()) {
            return this.getTrackedTaskValue();
        } else {
            return this.getValue();
        }
    }

    private boolean isInTrackingMode() {
        return this.taskReference != null && this.taskReference.get() != null;
    }

    private double getTrackedTaskValue() {
        ObservableTask task = taskReference != null ? taskReference.get() : null;
        if (task != null) {
            return task.getCurrentProgress();
        } else {
            return 0;
        }
    }

}
