package com.github.renatolsjf.chassi.monitoring.request;

import java.util.*;
import java.util.stream.Collectors;

public abstract class MultiRequestData extends RequestData {

    protected List<RequestData> requests;
    protected String name;

    protected MultiRequestData(String name) {
        this.name = name;
        this.requests = new ArrayList<>();
    }

    protected MultiRequestData(List<RequestData> requests, String name) {
        this.name = name;
        this.requests = new ArrayList<>(requests);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getRpm() {
        return this.requests.stream().mapToInt(RequestData::getRpm).sum();
    }

    @Override
    public List<Long> getRequestDuration() {
        return this.requests.stream()
                .map(RequestData::getRequestDuration)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public long averageRequestDuration() {

        if (this.getRpm() == 0) {
            return 0;
        }

        return this.getRequestDuration().stream().mapToLong(Long::longValue).sum() / this.getRpm();
    }

    @Override
    public long max10RequestDuration() {

        int size = this.getRpm();
        if (size == 0) {
            return 0;
        }

        int s = size / 10;
        if (size % 10 > 0) {
            s++;
        }

        return this.getRequestDuration().stream()
                .sorted(Comparator.reverseOrder())
                .limit(s)
                .mapToLong(Long::longValue)
                .sum() / s;

    }

    @Override
    public long max1RequestDuration() {

        int size = this.getRpm();
        if (size == 0) {
            return 0;
        }

        int s = size / 100;
        if (size % 100 > 0) {
            s++;
        }

        return this.getRequestDuration().stream()
                .sorted(Comparator.reverseOrder())
                .limit(s)
                .mapToLong(Long::longValue)
                .sum() / s;


    }

    @Override
    public Map<String, Integer> getResult() {
        Map<String, Integer> m = new HashMap<>();
        this.requests.forEach(r ->
                r.getResult().keySet().forEach(
                        k -> m.put(k, m.getOrDefault(k, 0) + r.getResult().get(k))));
        return m;
    }

    @Override
    public int healthPercentage() {
        if (this.requests.isEmpty()) {
            return 100;
        }

        if (this.requests.get(0) instanceof MultiRequestData) {
            return this.requests.stream().mapToInt(RequestData::healthPercentage).min().orElse(0);
        } else {
            int rpm = this.getRpm();
            if (rpm == 0) rpm = 1;
            return this.requests.stream().mapToInt(RequestData::healthPercentage).sum() / rpm;
        }


    }

    @Override
    public boolean belongsTo(String... ids) {
        return ids.length == 1 && ids[0].equalsIgnoreCase(this.getName());
    }

    public void add(RequestData rd) {
        this.requests.add(rd);
    }

}
