package com.github.renatolsjf.chassi.monitoring.request.internal;

import com.github.renatolsjf.chassi.context.Context;
import com.github.renatolsjf.chassi.monitoring.request.RequestData;
import com.github.renatolsjf.chassi.rendering.Media;

import java.util.List;
import java.util.Map;

public class ContextBasedRequestData extends RequestData {

    private final String action;
    private final long requestDuration;
    private final boolean clientError;
    private final boolean serverError;

    public ContextBasedRequestData(Context c) {
        this.action = c.getAction();
        this.requestDuration = c.getElapsedMillis();
        this.clientError = false;
        this.serverError = false;
    }

    public ContextBasedRequestData(Context c, boolean clientError, boolean serverError) {
        this.action = c.getAction();
        this.requestDuration = c.getElapsedMillis();
        this.clientError = clientError;
        this.serverError = serverError;
    }

    @Override
    public String getName() {
        return this.action != null ? this.action.toLowerCase() : "";
    }

    @Override
    public int getRpm() {
        return 1;
    }

    @Override
    public List<Long> getRequestDuration() {
        return List.of(this.requestDuration);
    }

    @Override
    public long averageRequestDuration() {
        return this.requestDuration;
    }

    @Override
    public long max10RequestDuration() {
        return this.requestDuration;
    }

    @Override
    public long max1RequestDuration() {
        return this.requestDuration;
    }

    @Override
    public Map<String, Integer> getResult() {
        return Map.of("success", !(this.clientError) && !(this.serverError) ? 1 : 0,
                "client_error", this.clientError ? 1 : 0,
                "server_error", this.serverError ? 1 : 0);
    }

    @Override
    public int healthPercentage() {
        return this.serverError ? 0 : 100;
    }

    @Override
    public boolean belongsTo(String... ids) {
        return ids.length == 1 && this.getName().equalsIgnoreCase(ids[0]);
    }

    @Override
    public Media render(Media media) {
        return media;
    }
}
