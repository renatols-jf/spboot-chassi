package com.github.renatolsjf.chassi.monitoring;

public class HistogramBucket {

    private double le;
    private int count = 0;

    public HistogramBucket (double le) {
        this.le = le;
    }

    public void inc() {
        ++count;
    }

    public void increaseCountIfInRange(double value) {
        if (le >= value) {
            this.inc();
        }
    }

    public boolean isSameRange(double v) {
        return Double.compare(le, v) == 0;
    }

}
