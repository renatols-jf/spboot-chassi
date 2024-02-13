package com.github.renatolsjf.chassi;

import com.github.renatolsjf.chassi.context.AppRegistry;
import com.github.renatolsjf.chassi.monitoring.*;
import io.micrometer.core.instrument.MeterRegistry;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MetricRegistry implements MetricListener {

    public enum HistogramRanges {
        REQUEST_DURATION;
    }

    public class MetricBuilder {

        private final String name;
        private final Map<String, String> tags = new HashMap<>();

        MetricBuilder(String name) {
            this.name = name;
        }

        public MetricBuilder withTag(String tagName, String tagValue) {
            this.tags.put(tagName, tagValue);
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
            switch (range) {
                case REQUEST_DURATION:
                    return this.buildHistogram(Chassi.getInstance().getConfig().monitoringRequestDurationRanges());
                default:
                    return this.registerMetric(Histogram.class);
            }
        }

        public Histogram buildHistogram(double... ranges) {
            Histogram h = this.registerMetric(Histogram.class);
            Arrays.stream(ranges).forEach(r -> h.addBucket(r));
            return h;
        }

        private <T extends Metric> T registerMetric(Class<T> metricType) {
            T metric = (T) MetricRegistry.this.registeredMetrics.get(new MetricId(metricType, this.name, this.tags).toString());
            if (metric == null) {
                try {
                    metric = metricType.getConstructor(String.class, Map.class).newInstance(name, tags);
                    metric.setMetricListener(MetricRegistry.this);
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

    @Override
    public void metricObserved(Metric metric, double value) {
        if (metric instanceof Counter) {
            io.micrometer.core.instrument.Counter.Builder b = io.micrometer.core.instrument.Counter.builder(metric.getName());
            metric.getTags().entrySet().forEach(e -> b.tag(e.getKey(), e.getValue()));
            b.register(AppRegistry.getResource(MeterRegistry.class)).increment(value);
        } else if (metric instanceof Gauge) {
            io.micrometer.core.instrument.Gauge.Builder b = io.micrometer.core.instrument.Gauge.builder(metric.getName(), ()-> ((Gauge) metric).getValue());
            metric.getTags().entrySet().forEach(e -> b.tag(e.getKey(), e.getValue()));
            b.register(AppRegistry.getResource(MeterRegistry.class));
        } else if (metric instanceof Histogram) {
            io.micrometer.core.instrument.DistributionSummary.Builder b = io.micrometer.core.instrument.DistributionSummary.builder(metric.getName());
            metric.getTags().entrySet().forEach(e -> b.tag(e.getKey(), e.getValue()));
            b.serviceLevelObjectives(((Histogram) metric).getRanges())
                    .register(AppRegistry.getResource(MeterRegistry.class)).record(value);
        }


    }


}


