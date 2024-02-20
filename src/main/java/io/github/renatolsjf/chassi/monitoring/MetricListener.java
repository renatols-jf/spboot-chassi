package io.github.renatolsjf.chassi.monitoring;

public interface MetricListener {

    void metricObserved(Metric metric, double value);

}
