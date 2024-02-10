package com.github.renatolsjf.chassi.monitoring.request.internal;

import com.github.renatolsjf.chassi.monitoring.request.RequestData;
import com.github.renatolsjf.chassi.rendering.Media;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EmptyContextBasedRequestData extends RequestData {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getRpm() {
        return 0;
    }

    @Override
    public List<Long> getRequestDuration() {
        return Collections.emptyList();
    }

    @Override
    public long averageRequestDuration() {
        return 0L;
    }

    @Override
    public long max10RequestDuration() {
        return 0L;
    }

    @Override
    public long max1RequestDuration() {
        return 0L;
    }

    @Override
    public Map<String, Integer> getResult() {
        return Map.of("success", 0,
                "client_error", 0,
                "server_error", 0);
    }

    @Override
    public int healthPercentage() {
        return 100;
    }

    @Override
    public boolean belongsTo(String... ids) {
        return false;
    }

    @Override
    public Media render(Media media) {
        return media;
    }
}
