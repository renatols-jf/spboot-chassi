package com.github.renatolsjf.chassi.monitoring.request;

import com.github.renatolsjf.chassi.rendering.Renderable;

import java.util.List;
import java.util.Map;

public abstract class RequestData implements Renderable {

    public abstract String getName();
    public abstract int getRpm();
    public abstract List<Long> getRequestDuration();
    public abstract long averageRequestDuration();
    public abstract long max10RequestDuration();
    public abstract long max1RequestDuration();
    public abstract Map<String, Integer> getResult();
    public abstract int healthPercentage();
    public abstract boolean belongsTo(String... ids);

    public String getHealthDescription() {
        int percentage = this.healthPercentage();
        if (percentage <= 25) {
            return "DOWN";
        } else if (percentage < 50) {
            return "MAJOR_DEGRADATION";
        } else if (percentage < 75) {
            return "DEGRADED";
        } else if (percentage < 90) {
            return "MINOR_DEGRADATION";
        } else {
            return "UP";
        }
    }

}
