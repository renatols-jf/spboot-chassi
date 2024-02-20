package io.github.renatolsjf.chassi.monitoring;


import java.util.Map;
import java.util.TreeSet;

//TODO count and sum are not thread safe, but we are not exporting this information yet. The data displayed is controlled by micrometer MeterRegistry
public class Histogram extends Metric {

    private TreeSet<HistogramBucket> buckets = new TreeSet<>();
    private double sum;
    private int count;

    public Histogram(String name, Map<String, String> tags) {
        super(name, tags);
        if (tags.containsKey("le")) {
            throw new InvalidMetricException("Histograms can not have le as tag");
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
    public synchronized void doObserve(double value) {
        ++count;
        sum += value;
        this.buckets.forEach(hb -> hb.increaseCountIfInRange(value));
    }

}
