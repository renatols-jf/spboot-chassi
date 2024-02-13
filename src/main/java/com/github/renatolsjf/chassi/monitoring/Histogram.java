package com.github.renatolsjf.chassi.monitoring;


import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

    //TODO This won't update interfaced metrics buckets. The listener has to be aware of this to.
    public Histogram addBucket(double le) {
        if (!(this.buckets.stream().anyMatch(b -> b.isSameRange(le)))) {
            this.buckets.add(new HistogramBucket(le));
        }
        return this;
    }

    public double[] getRanges() {
        return this.buckets.stream().filter(b -> Double.compare(Double.MAX_VALUE, b.getLe()) != 0).mapToDouble(b -> b.getLe()).toArray();
    }

    @Override
    public void doObserve(double value) {
        ++count;
        sum += value;
        this.buckets.forEach(hb -> hb.increaseCountIfInRange(value));
    }

}
