package com.github.renatolsjf.chassi.monitoring;


import com.github.renatolsjf.chassi.MetricRegistry;
import com.github.renatolsjf.chassi.rendering.Media;
import com.github.renatolsjf.chassi.rendering.Renderable;
import com.github.renatolsjf.chassi.util.RollingTimedWindowList;

import java.time.Duration;
import java.util.*;

public class ApplicationHealth implements Renderable {

    private static final String OPERATION_HEALTH_METRIC_NAME = "operation_health";

    private Map<String, List<OperationData>> operationDataMap = new HashMap();
    private Set<String> operationSet = new TreeSet<>();
    private Duration dataDuration;
    private MetricRegistry metricRegistry;

    public ApplicationHealth(Duration dataDuration, MetricRegistry metricRegistry) {
        this.dataDuration = dataDuration;
        this.metricRegistry = metricRegistry;
    }

    public void create(String operation, boolean success, boolean clientFault, boolean serverFault,
                       long durationInMillis, Map<String, Long> operationTimes) {
        List<OperationData> operationDataList = this.ensureOperationDataListForOperation(operation);
        operationDataList.add(new OperationData(operation, success, clientFault, serverFault,
                durationInMillis, operationTimes));
    }

    public void success(String operation, long durationInMillis, Map<String, Long> operationTimes) {
        List<OperationData> operationDataList = this.ensureOperationDataListForOperation(operation);
        operationDataList.add(new OperationData(operation, true, false, false,
                durationInMillis, operationTimes));
    }

    public void clientFault(String operation, long durationInMillis, Map<String, Long> operationTimes) {
        List<OperationData> operationDataList = this.ensureOperationDataListForOperation(operation);
        operationDataList.add(new OperationData(operation, false, true, false,
                durationInMillis, operationTimes));
    }

    public void serverFault(String operation, long durationInMillis, Map<String, Long> operationTimes) {
        List<OperationData> operationDataList = this.ensureOperationDataListForOperation(operation);
        operationDataList.add(new OperationData(operation, false, false, true,
                durationInMillis, operationTimes));
    }

    private List<OperationData> ensureOperationDataListForOperation(String operation) {

        if (operation == null) {
            throw new NullPointerException("Operation is null");
        }

        List<OperationData> operationDataList = this.operationDataMap.get(operation);
        if (operationDataList == null) {
            synchronized(this) {
                operationDataList = this.operationDataMap.get(operation);
                if (operationDataList == null) {

                    this.operationSet.add(operation);
                    operationDataList = new RollingTimedWindowList<>(dataDuration);
                    this.operationDataMap.put(operation, operationDataList);

                    this.metricRegistry.createBuilder(OPERATION_HEALTH_METRIC_NAME)
                            .withTag("action", operation)
                            .buildTrackingGauge()
                            .track(new OperationHealhObservableTask(operationDataList));

                }
            }
        }

        return operationDataList;

    }

    @Override
    public Media render(Media media) {

        Set<Map.Entry<String, List<OperationData>>> set = this.operationDataMap.entrySet();
        List<OperationSummary> summaryList = new ArrayList<>();

        Map<String, List<Long>> applicationOperationTimes = new HashMap<>();
        int applicationRequestCount = 0;
        int applicationSuccessCount = 0;
        int applicationClientErrorCount = 0;
        int applicationServerErrorCount = 0;

        for (String operation: operationSet) {

            List<OperationData> operationDataList = operationDataMap.get(operation);
            if (operationDataList == null || operationDataList.isEmpty()) {
                OperationSummary os = new OperationSummary();
                summaryList.add(os);
                os.name = operation;
                os.health = 100;
                os.requestCount = 0;
                os.successCount = 0;
                os.clientErrorCount = 0;
                os.serverErrorCount = 0;
                os.operationTimes = new HashMap<>();
                continue;
            }

            int requestCount = 0;
            int successCount = 0;
            int clientErrorCount = 0;
            int serverErrorCount = 0;
            double operationHealth = new OperationHealhObservableTask(operationDataList).getCurrentValue();

            Map<String, List<Long>> operationTimes = new HashMap<>();
            for (OperationData operationData: operationDataList) {
                ++requestCount;
                successCount += operationData.success ? 1 : 0;
                clientErrorCount += operationData.clientFault ? 1 : 0;
                serverErrorCount += operationData.serverFault ? 1 : 0;
                operationData.operationTimes.entrySet().forEach(e -> {

                    applicationOperationTimes.putIfAbsent(e.getKey(), new ArrayList<>());
                    operationTimes.putIfAbsent(e.getKey(), new ArrayList<>());

                    applicationOperationTimes.get(e.getKey()).add(e.getValue());
                    operationTimes.get(e.getKey()).add(e.getValue());

                });

            }

            applicationRequestCount += requestCount;
            applicationSuccessCount += successCount;
            applicationClientErrorCount += clientErrorCount;
            applicationServerErrorCount += serverErrorCount;


            OperationSummary os = new OperationSummary();
            summaryList.add(os);
            os.name = operation;
            os.health = operationHealth;
            os.requestCount = requestCount;
            os.successCount = successCount;
            os.clientErrorCount = clientErrorCount;
            os.serverErrorCount = serverErrorCount;
            os.operationTimes = operationTimes;

        }

        OperationSummary aos = new OperationSummary();
        aos.health = summaryList.stream().mapToDouble(os -> os.health).sum() / summaryList.size();
        aos.requestCount = applicationRequestCount;
        aos.successCount = applicationSuccessCount;
        aos.clientErrorCount = applicationClientErrorCount;
        aos.serverErrorCount = applicationServerErrorCount;
        aos.operationTimes = applicationOperationTimes;

        return media.forkRenderable("application", aos)
                .forkCollection("operations", summaryList);

    }
}

class OperationData {

    String operationName;
    boolean success;
    boolean clientFault;
    boolean serverFault;
    long durationInMillis;
    Map<String, Long> operationTimes;

    OperationData(String operationName, boolean success, boolean clientFault, boolean serverFault,
                  long durationInMillis, Map<String, Long> operationTimes) {
        this.operationName = operationName;
        this.success = success;
        this.clientFault = clientFault;
        this.serverFault = serverFault;
        this.durationInMillis = durationInMillis;
        this.operationTimes = operationTimes;
    }

}

class OperationSummary implements Renderable {

    String name;
    double health;
    int requestCount = 0;
    int successCount = 0;
    int clientErrorCount = 0;
    int serverErrorCount = 0;
    Map<String, List<Long>> operationTimes = new HashMap<>();

    private Long getQuantile(double qtl, List<Long> values) {

        if (Double.compare(qtl, 0) <= 0 || Double.compare(qtl, 1) > 1) {
            throw new IllegalArgumentException("Quantile must be between 0 and 1");
        }

        if (values == null || values.isEmpty()) {
            return 0l;
        }

        return values.get(Math.toIntExact(Math.round(values.size() * qtl)) - 1);

    }

    @Override
    public Media render(Media media) {

        return media.print("name", name)
                .print("health", health)
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
                        }))
                .forkRenderable("result", m-> m.print("success", successCount)
                        .print("clientError", clientErrorCount)
                        .print("serverError", serverErrorCount));

    }

}

class OperationHealhObservableTask implements ObservableTask {

    private final List<OperationData> operationDataList;

    OperationHealhObservableTask(List<OperationData> operationDataList) {
        this.operationDataList = operationDataList;
    }

    @Override
    public double getCurrentValue() {
        if (this.operationDataList.isEmpty()) {
            return 100;
        } else {
            long c = this.operationDataList.stream().filter(od -> od.success).count();
            double s = this.operationDataList.size();
            double v = (c / s) * 100d;
            return Math.round(v);
        }
    }
}

