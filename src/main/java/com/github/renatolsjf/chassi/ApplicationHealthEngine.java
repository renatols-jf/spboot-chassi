package com.github.renatolsjf.chassi;


import com.github.renatolsjf.chassi.context.Context;

public class ApplicationHealthEngine {

    private String ACTIVE_OPERATIONS_METRIC_NAME = "operation_active_requests";
    private String OPERATION_TIME_MILLIS_METRIC_NAME = "operation_request_millis";

    ApplicationHealthEngine() {}

    public void operationStarted() {
        Chassi.getInstance().getMetricRegistry().createBuilder(ACTIVE_OPERATIONS_METRIC_NAME)
                .withTag("action", Context.forRequest().getAction())
                .buildGauge()
                .inc();
    }

    public void operationEnded(String outcome) {

        Context context = Context.forRequest();
        if (Chassi.getInstance().getConfig().exportRequestDurationMetricByType()) {

            context.getOperationTimeByType().entrySet().stream().forEach(entry ->
                    Chassi.getInstance().getMetricRegistry().createBuilder(OPERATION_TIME_MILLIS_METRIC_NAME)
                            .withTag("action", context.getAction())
                            .withTag("outcome", outcome.toLowerCase())
                            .withTag("timer_type", entry.getKey())
                            .buildHistogram(MetricRegistry.HistogramRanges.REQUEST_DURATION)
                            .observe(entry.getValue())

            );

        } else {

            Chassi.getInstance().getMetricRegistry().createBuilder(OPERATION_TIME_MILLIS_METRIC_NAME)
                    .withTag("action", context.getAction())
                    .withTag("outcome", outcome.toLowerCase())
                    .buildHistogram(MetricRegistry.HistogramRanges.REQUEST_DURATION)
                    .observe(context.getElapsedMillis());

        }

        Chassi.getInstance().getMetricRegistry().createBuilder(ACTIVE_OPERATIONS_METRIC_NAME)
                .withTag("action", context.getAction())
                .buildGauge()
                .dec();

    }

}
