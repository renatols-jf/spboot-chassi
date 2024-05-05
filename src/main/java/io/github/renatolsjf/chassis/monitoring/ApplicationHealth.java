package io.github.renatolsjf.chassis.monitoring;


import io.github.renatolsjf.chassis.Chassis;
import io.github.renatolsjf.chassis.Configuration;
import io.github.renatolsjf.chassis.Labels;
import io.github.renatolsjf.chassis.MetricRegistry;
import io.github.renatolsjf.chassis.monitoring.timing.TimedOperation;
import io.github.renatolsjf.chassis.rendering.Media;
import io.github.renatolsjf.chassis.rendering.Renderable;
import io.github.renatolsjf.chassis.util.RollingTimedWindowList;

import java.time.Duration;
import java.util.*;
import java.util.stream.DoubleStream;

public class ApplicationHealth implements Renderable {

    List<OperationSummary> summaryList = new ArrayList<>();
    private RollingTimedWindowList<OperationData> operationDataList;
    private MetricRegistry metricRegistry;
    private volatile boolean updateNeeded = false;
    private volatile int lastUpdateRecordsCount = 0;

    public ApplicationHealth(Duration dataDuration, MetricRegistry metricRegistry) {
        operationDataList = new RollingTimedWindowList<>(dataDuration);
        this.metricRegistry = metricRegistry;
    }

    public void operation(String operation, boolean success, boolean clientFault, boolean serverFault,
                          Map<String, Long> operationTimes) {
        this.updateNeeded = true;

        OperationData od = new OperationData(new DataKey().withEmptyKey("application").withKey("operations", operation),  success, clientFault,
                serverFault, null, operationTimes);
        operationDataList.add(od);

        this.metricRegistry.createBuilder(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_NAME_OPERATION_HEALTH))
                .withTag(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_TAG_OPERATION), operation)
                .buildTrackingGauge()
                .track(() -> {
                    ApplicationHealth.this.update();
                    return od.dataKey.ensureSummary(summaryList).getHealth();
                });
    }

    public void http(String group, String service, String operation, boolean success, boolean clientFault, boolean serverFault,
                     String result, long duration) {
        this.updateNeeded = true;
        OperationData od = new OperationData(new DataKey().withEmptyKey("integration").withKey("groups", group)
                .withKey("services", service).withKey("operations", operation),  success, clientFault,
                serverFault, result, Map.of(TimedOperation.HTTP_OPERATION, duration));
        operationDataList.add(od);
        this.metricRegistry.createBuilder(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_NAME_INTEGRATION_HEALTH))
                .withTag(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_TAG_GROUP), group)
                .withTag(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_TAG_SERVICE), service)
                .withTag(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_TAG_OPERATION), operation)
                .withTag(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_TAG_TYPE),
                                Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_TAG_VALUE_HTTP_TYPE))
                .buildTrackingGauge()
                .track(() ->  {
                    ApplicationHealth.this.update();
                    return od.dataKey.ensureSummary(summaryList).getHealth();
                });
    }

    // TODO There is a small chance of a rolling happening within metrics update,
    // e.g. update health metric 1, no rolling happened, update metric 2 rolling happened.
    // We should take action to avoid this. But since the window for that to happen
    // is so small, it's neglected for the moment.
    private void update() {
        if (this.updateNeeded || lastUpdateRecordsCount != this.operationDataList.size()) {
            this.summaryList.forEach(os -> os.clear());
            for (OperationData operationData: operationDataList) {
                operationData.dataKey.ensureSummary(this.summaryList).add(operationData);
            }
            this.summaryList.sort(Comparator.comparing(os -> os.group));
            this.updateNeeded = false;
        }
        lastUpdateRecordsCount = this.operationDataList.size();
    }

    @Override
    public Media render(Media media) {

        this.update();

        if (summaryList.isEmpty()) {
            OperationSummary os = new OperationSummary();
            media.forkRenderable("application", os);
        } else {
            for (OperationSummary os: summaryList) {
                media = media.forkRenderable(os.group, os);
            }
        }

        return media;

    }
}

class OperationData {

    DataKey dataKey;
    boolean success;
    boolean clientFault;
    boolean serverFault;
    String result;
    Map<String, Long> operationTimes;

    OperationData(DataKey dataKey, boolean success, boolean clientFault, boolean serverFault,
                  String result, Map<String, Long> operationTimes) {
        this.dataKey = dataKey;
        this.success = success;
        this.clientFault = clientFault;
        this.serverFault = serverFault;
        this.result = result;
        this.operationTimes = operationTimes;
    }

}

class OperationSummary implements Renderable {

    OperationSummary parent;
    String group;
    String name;
    int requestCount = 0;
    int successCount = 0;
    int clientErrorCount = 0;
    int serverErrorCount = 0;
    Map<String, Integer> resultCount = new HashMap<>();
    Map<String, List<Long>> operationTimes = new HashMap<>();
    Map<String, List<OperationSummary>> childSummaries = new HashMap<>();

    private Long getQuantile(double qtl, List<Long> values) {

        if (Double.compare(qtl, 0) <= 0 || Double.compare(qtl, 1) > 1) {
            throw new IllegalArgumentException("Quantile must be between 0 and 1");
        }

        if (values == null || values.isEmpty()) {
            return 0l;
        }

        return values.get(Math.toIntExact(Math.round(values.size() * qtl)) - 1);

    }

    public double getHealth() {

        DoubleStream ds = this.childSummaries.values().stream()
                .flatMap(List::stream)
                .mapToDouble(cs -> cs.getHealth());

        OptionalDouble opt;
        if (Chassis.getInstance().getConfig().healthValueType() == Configuration.HealthValueType.LOWEST) {
            opt = ds.min();
        } else {
            opt = ds.average();
        }

        return opt.orElse(requestCount == 0 ? 100 : (((double) (successCount + clientErrorCount)) / requestCount) * 100);

    }

    public void add(OperationData operationData) {
        this.requestCount++;
        this.successCount += operationData.success ? 1 : 0;
        this.clientErrorCount += operationData.clientFault ? 1 : 0;
        this.serverErrorCount += operationData.serverFault ? 1 : 0;
        if (operationData.result != null) {
            this.resultCount.put(operationData.result, this.resultCount.getOrDefault(operationData.result, 0) + 1);
        }
        operationData.operationTimes.forEach((k,v) ->  {
            this.operationTimes.putIfAbsent(k, new ArrayList<>());
            this.operationTimes.get(k).add(v);
        });
        if (this.parent != null) {
            this.parent.add(operationData);
        }
    }

    public void clear() {
        this.requestCount = 0;
        this.successCount = 0;
        this.clientErrorCount = 0;
        this.serverErrorCount = 0;
        this.resultCount.clear();
        this.operationTimes.clear();
        this.childSummaries.entrySet().stream()
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .forEach(os -> os.clear());
    }

    @Override
    public Media render(Media media) {

        media = media.print("name", name)
                .print("health", this.getHealth())
                .forkRenderable("load", m -> m.print("requestCount", requestCount)
                        .forkRenderable("requestTime", m2 -> {
                            this.operationTimes.entrySet().forEach(e -> {
                                Collections.sort(e.getValue());
                                m2.forkRenderable(e.getKey(), m3 ->
                                        m3.forkRenderable("quantiles", m4 ->
                                                m4.print("0.5", this.getQuantile(0.5, e.getValue()))
                                                        .print("0.95", this.getQuantile(0.95, e.getValue()))
                                                        .print("0.99", this.getQuantile(0.99, e.getValue()))));
                            });
                            return m2;
                        }));
        if (!(resultCount.isEmpty())) {
            media.forkRenderable("result", m -> {
                this.resultCount.entrySet().forEach(e -> m.print(e.getKey(), e.getValue()));
                return m;
            });
        } else {
            media = media.forkRenderable("result", m -> m.print("success", successCount)
                    .print("clientError", clientErrorCount)
                    .print("serverError", serverErrorCount));
        }

        for (Map.Entry<String, List<OperationSummary>> entry: this.childSummaries.entrySet()) {
            media = media.forkCollection(entry.getKey(), entry.getValue());
        }

        return media;

    }

}

class DataKey {

    private Map<String, String> keys = new LinkedHashMap<>();

    public DataKey withEmptyKey(String key) {
        return this.withKey(key, null);
    }

    public DataKey withKey(String key, String keyValue) {
        this.keys.put(key, keyValue);
        return this;
    }

    public OperationSummary ensureSummary(List<OperationSummary> summaryList) {
        OperationSummary summary = null;
        for(Map.Entry<String, String> entry: this.keys.entrySet()) {
            if (summary == null) {
                summary = summaryList.stream()
                        .filter(s -> s.group.equals(entry.getKey()) && (s.name != null ? s.name.equals(entry.getValue()) : entry.getValue() == null))
                        .findFirst()
                        .orElseGet(() -> {
                            OperationSummary os = new OperationSummary();
                            os.group = entry.getKey();
                            os.name = entry.getValue();
                            summaryList.add(os);
                            return os;
                        });
            } else {
                OperationSummary parentSummary = summary;
                summary = summary.childSummaries.entrySet().stream()
                        .filter(e -> e.getKey().equals(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .flatMap(List::stream)
                        .filter(s -> s.name.equals(entry.getValue()))
                        .findFirst()
                        .orElseGet(() -> {

                            OperationSummary os = new OperationSummary();
                            os.group = entry.getKey();
                            os.name = entry.getValue();
                            os.parent = parentSummary;

                            parentSummary.childSummaries.putIfAbsent(entry.getKey(), new ArrayList<>());
                            parentSummary.childSummaries.get(entry.getKey()).add(os);

                            return os;

                        });
            }
        }
        return summary;
    }


}

