package com.github.renatolsjf.chassi;

import com.github.renatolsjf.chassi.monitoring.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MetricRegistry {

    public enum HistogramRanges {
        REQUEST_DURATION;
    }

    public class MetricBuilder {

        private final String name;
        private final Map<String, String> labels = new HashMap<>();

        MetricBuilder(String name) {
            this.name = name;
        }

        public MetricBuilder withLabel(String labelName, String labelValue) {
            this.labels.put(labelName, labelValue);
            return this;
        }

        public Counter buildCounter() {
            return this.registerMetric(Counter.class);
        }

        public Gauge buildGauge() {
            return this.registerMetric(Gauge.class);
        }

        public Histogram buildHistogram() {
            return this.registerMetric(Histogram.class);
        }

        public Histogram buildHistogram(HistogramRanges range) {
            return null;
        }

        public Histogram buildHistogram(double... ranges) {
            Histogram h = this.registerMetric(Histogram.class);
            Arrays.stream(ranges).forEach(r -> h.addBucket(r));
            return h;
        }

        private <T extends Metric> T registerMetric(Class<T> metricType) {
            T metric = (T) MetricRegistry.this.registeredMetrics.get(new MetricId(metricType, this.name, this.labels).toString());
            if (metric == null) {
                try {
                    metric = metricType.getConstructor(String.class, Map.class).newInstance(name, labels);
                    MetricRegistry.this.registeredMetrics.put(metric.toMetricId().toString(), metric);
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    throw new InvalidMetricException("Error creating metric", e);
                }
            }

            return metric;
        }

    }

    MetricRegistry() {}

    Map<String, Metric> registeredMetrics = new HashMap<>();

    public MetricBuilder createBuilder(String name) {
        return new MetricBuilder(name);
    }


}


