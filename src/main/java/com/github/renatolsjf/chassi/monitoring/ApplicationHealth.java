package com.github.renatolsjf.chassi.monitoring;


import com.github.renatolsjf.chassi.Chassi;
import com.github.renatolsjf.chassi.MetricRegistry;
import com.github.renatolsjf.chassi.context.Context;
import com.github.renatolsjf.chassi.request.RequestOutcome;
import com.github.renatolsjf.chassi.util.RollingTimedWindowList;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationHealth {

    private static final String OPERATION_HEALTH_METRIC_NAME = "operation_health";

    private Map<String, List<OperationData>> operationDataMap = new HashMap();
    private Duration dataDuration;
    private MetricRegistry metricRegistry;

    public ApplicationHealth(Duration dataDuration, MetricRegistry metricRegistry) {
        this.dataDuration = dataDuration;
        this.metricRegistry = metricRegistry;
    }

    public void create(String operation, boolean success, boolean clientFault, boolean serverFault, long durationInMillis) {
        List<OperationData> operationDataList = this.ensureOperationDataListForOperation(operation);
        operationDataList.add(new OperationData(operation, success, clientFault, serverFault, durationInMillis));
    }

    public void success(String operation, long durationInMillis) {
        List<OperationData> operationDataList = this.ensureOperationDataListForOperation(operation);
        operationDataList.add(new OperationData(operation, true, false, false, durationInMillis));
    }

    public void clientFault(String operation, long durationInMillis) {
        List<OperationData> operationDataList = this.ensureOperationDataListForOperation(operation);
        operationDataList.add(new OperationData(operation, false, true, false, durationInMillis));
    }

    public void serverFault(String operation, long durationInMillis) {
        List<OperationData> operationDataList = this.ensureOperationDataListForOperation(operation);
        operationDataList.add(new OperationData(operation, false, false, true, durationInMillis));
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

}

class OperationData {

    String operationName;
    boolean success;
    boolean clientFault;
    boolean serverFault;
    long durationInMillis;

    OperationData(String operationName, boolean success, boolean clientFault, boolean serverFault, long durationInMillis) {
        this.operationName = operationName;
        this.success = success;
        this.clientFault = clientFault;
        this.serverFault = serverFault;
        this.durationInMillis = durationInMillis;
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

