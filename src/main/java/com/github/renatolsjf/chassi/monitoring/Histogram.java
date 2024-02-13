package com.github.renatolsjf.chassi.monitoring;


import java.util.Map;
import java.util.TreeSet;

public class Histogram extends Metric {

    private TreeSet<HistogramBucket> buckets = new TreeSet<>();
    private double sum;
    private int count;

    public Histogram(String name, Map<String, String> labels) {
        super(name, labels);
        if (labels.containsKey("le")) {
            throw new InvalidMetricException("Histograms can not have le as labels");
        }
        this.buckets.add(new HistogramBucket(Double.MAX_VALUE));
    }

    public Histogram addBucket(double le) {
        if (!(this.buckets.stream().anyMatch(b -> b.isSameRange(le)))) {
            this.buckets.add(new HistogramBucket(le));
        }
        return this;
    }

    public void observe(double value) {
        ++count;
        sum += value;
        this.buckets.forEach(hb -> hb.increaseCountIfInRange(value));
    }

}
