package io.github.renatolsjf.chassis;

import io.github.renatolsjf.chassis.loader.Loader;

public class Chassis {

    private static Chassis instance = new Chassis();

    private Configuration config = new Configuration();
    private Labels labelsInstance;
    private MetricRegistry metricRegistry = new MetricRegistry();
    private ApplicationHealthEngine applicationHealthEngine = new ApplicationHealthEngine();

    private Chassis() {
        this.labelsInstance = new Labels(Loader.defaultLoader().getLabelsData());
    }

    public Configuration getConfig() {
        return this.config;
    }

    public MetricRegistry getMetricRegistry() {
        return this.metricRegistry;
    }

    public ApplicationHealthEngine getApplicationHealthEngine() {
        return this.applicationHealthEngine;
    }

    public Labels labels() {
        return this.labelsInstance;
    }

    public void setConfig(Configuration config) {
        if (config != null) {
            this.config = config;
        }
    }

    public static Chassis getInstance() {
        return instance;
    }

}
