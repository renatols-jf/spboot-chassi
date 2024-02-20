package io.github.renatolsjf.chassi.monitoring;

import java.time.Instant;
import java.util.Map;

public class Gauge extends Metric {

    protected volatile double value = 0;

    //private WeakReference<ObservableTask> taskReference;

    public Gauge(String name, Map<String, String> tags) {
        super(name, tags);
    }

    public synchronized void inc() {
        /*if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }*/
        this.inc(1);
    }

    public synchronized void inc (double v) {
        /*if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }*/
        this.observe(this.value + v);
    }

    public synchronized void dec() {
        /*if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }*/
        this.dec(1);
    }

    public synchronized void dec (double v) {
        /*if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }
        if (v <= 0) {
            throw new InvalidMetricException("Can not decrement a metric with a value lower than 0", v);
        }*/
        this.observe(this.value - v);
    }

    @Override
    protected synchronized void doObserve(double v) {
        /*if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }*/
        this.value = v;
    }

    public synchronized void set(double v) {
        this.observe(v);
    }

    public synchronized void setToCurrentTime() {
        /*if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }*/
        this.observe(Instant.now().getEpochSecond());
    }

    public synchronized void reset() {
        /*if (this.isInTrackingMode()) {
            throw new InvalidMetricException("Metric in tracking mode can not have its value changed");
        }*/
        this.observe(0);
    }

    /*public void track(ObservableTask task) {
        if (task == null) {
            throw new NullPointerException("Observable task is null");
        }
        this.value = 0;
        this.taskReference = new WeakReference<>(task);
    }*/

    public double getValue() {
        /*if (this.isInTrackingMode()) {
            return this.getTrackedTaskValue();
        } else {*/
            return this.value;
        //}
    }

    /*private boolean isInTrackingMode() {
        return this.taskReference != null && this.taskReference.get() != null;
    }

    private double getTrackedTaskValue() {
        ObservableTask task = taskReference != null ? taskReference.get() : null;
        if (task != null) {
            return task.getCurrentProgress();
        } else {
            return 0;
        }
    }*/

}
