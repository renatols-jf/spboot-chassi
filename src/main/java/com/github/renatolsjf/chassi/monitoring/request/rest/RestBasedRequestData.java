package com.github.renatolsjf.chassi.monitoring.request.rest;

import com.github.renatolsjf.chassi.monitoring.request.RequestData;
import com.github.renatolsjf.chassi.rendering.Media;

import java.util.List;
import java.util.Map;

public class RestBasedRequestData extends RequestData {

    private String group;
    private String service;
    private String operation;
    private long duration;
    private int status;
    private boolean connectionError;

    public RestBasedRequestData(String group, String service, String operation, long duration) {
        this.group = group;
        this.service = service;
        this.operation = operation;
        this.duration = duration;
        this.connectionError = true;
    }

    public RestBasedRequestData(String group, String service, String operation, long duration, int status) {
        this.group = group;
        this.service = service;
        this.operation = operation;
        this.duration = duration;
        this.status = status;
        this.connectionError = false;
    }

    @Override
    public String getName() {
        return operation;
    }

    @Override
    public int getRpm() {
        return 1;
    }

    @Override
    public List<Long> getRequestDuration() {
        return List.of(this.duration);
    }

    @Override
    public long averageRequestDuration() {
        return this.duration;
    }

    @Override
    public long max10RequestDuration() {
        return this.duration;
    }

    @Override
    public long max1RequestDuration() {
        return this.duration;
    }

    @Override
    public Map<String, Integer> getResult() {
        String s = connectionError
                ? "CONNECTION_ERROR"
                : String.valueOf(status);
        return Map.of(s, 1);
    }

    @Override
    public int healthPercentage() {
        return connectionError || status >= 400 ? 0 : 100;
    }

    @Override
    public boolean belongsTo(String... ids) {
        return ids.length == 3 && ids[0].equalsIgnoreCase(this.group) &&
                ids[1].equalsIgnoreCase(this.service) &&
                ids[2].equalsIgnoreCase(this.operation);
    }

    @Override
    public Media render(Media media) {
        return media;
    }
}
