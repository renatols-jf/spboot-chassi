package io.github.renatolsjf.chassis.monitoring;


//TODO count is not thread safe, but we are not exporting this information yet. The data displayed is controlled by micrometer MeterRegistry
public class HistogramBucket implements Comparable<HistogramBucket> {

    private double le;
    private int count = 0;

    public HistogramBucket (double le) {
        this.le = le;
    }

    public void inc() {
        ++count;
    }

    public void increaseCountIfInRange(double value) {
        if (Double.compare(le, value) > 0) {
            this.inc();
        }
    }

    public boolean isSameRange(double v) {
        return Double.compare(le, v) == 0;
    }

    public double getLe() {
        return this.le;
    }

    @Override
    public int compareTo(HistogramBucket o) {
        return Double.compare(this.le, o.le);
    }
}
