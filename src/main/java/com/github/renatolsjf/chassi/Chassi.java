package com.github.renatolsjf.chassi;

public class Chassi {

    private static Chassi instance = new Chassi();

    private Configuration config = new Configuration();
    private MetricRegistry metricRegistry = new MetricRegistry();

    private Chassi() {}

    public Configuration getConfig() {
        return this.config;
    }

    public MetricRegistry getMetricRegistry() {
        return this.metricRegistry;
    }

    public void setConfig(Configuration config) {
        if (config != null) {
            this.config = config;
        }
    }

    public static Chassi getInstance() {
        return instance;
    }

}
