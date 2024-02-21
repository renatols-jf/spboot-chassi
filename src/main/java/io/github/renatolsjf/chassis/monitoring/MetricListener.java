package io.github.renatolsjf.chassis.monitoring;

public interface MetricListener {

    void metricObserved(Metric metric, double value);

}
