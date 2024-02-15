package com.github.renatolsjf.chassi.monitoring;

import java.lang.ref.WeakReference;
import java.util.Map;

public class TrackingGauge extends Metric {

    private WeakReference<ObservableTask> taskReference;
    private ObservableTask task;

    public TrackingGauge(String name, Map<String, String> tags) {
        super(name, tags);
    }

    @Override
    protected void doObserve(double v) {
        throw new InvalidMetricException("A tracking gauge can not have its value set manually");
    }

    public double getValue() {

        ObservableTask t = null;
        if (task != null) {
            t = task;
        } else if (taskReference != null) {
            t = taskReference.get();
        }

        if (t == null) {
            t = () -> 0;
        }

        return t.getCurrentValue();

    }

    public TrackingGauge track(ObservableTask task) {

        if (this.taskReference != null) {
            this.taskReference.clear();
            this.taskReference = null;
        }

        this.task = task;

        return this;

    }

    public TrackingGauge weakTrack(ObservableTask task) {

        this.task = null;
        if (this.taskReference != null) {
            this.taskReference.clear();
            this.taskReference = null;
        }

        if (task != null) {
            this.taskReference = new WeakReference<>(task);
        }

        return this;

    }



    public static TrackingGauge strong(String name, Map<String, String> tags, ObservableTask task) {
        TrackingGauge t = new TrackingGauge(name, tags);
        t.task = task;
        return t;
    }

    public static TrackingGauge weak(String name, Map<String, String> tags, ObservableTask task) {
        TrackingGauge t = new TrackingGauge(name, tags);
        t.taskReference = new WeakReference<>(task);
        return t;
    }

}
