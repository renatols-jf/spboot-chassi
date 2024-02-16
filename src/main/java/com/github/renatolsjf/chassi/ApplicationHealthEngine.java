package com.github.renatolsjf.chassi;


import com.github.renatolsjf.chassi.context.Context;
import com.github.renatolsjf.chassi.monitoring.ApplicationHealth;
import com.github.renatolsjf.chassi.request.RequestOutcome;

public class ApplicationHealthEngine {

    private String ACTIVE_OPERATIONS_METRIC_NAME = "operation_active_requests";
    private String OPERATION_TIME_MILLIS_METRIC_NAME = "operation_request_millis";

    private ApplicationHealth applicationHealth;


    ApplicationHealthEngine() {}

    public void operationStarted() {
        Chassi.getInstance().getMetricRegistry().createBuilder(ACTIVE_OPERATIONS_METRIC_NAME)
                .withTag("action", Context.forRequest().getAction())
                .buildGauge()
                .inc();
    }

    public void operationEnded(RequestOutcome outcome) {

        Context context = Context.forRequest();
        if (Chassi.getInstance().getConfig().exportRequestDurationMetricByType()) {

            context.getOperationTimeByType().entrySet().stream().forEach(entry ->
                    Chassi.getInstance().getMetricRegistry().createBuilder(OPERATION_TIME_MILLIS_METRIC_NAME)
                            .withTag("action", context.getAction())
                            .withTag("outcome", outcome.toString().toLowerCase())
                            .withTag("timer_type", entry.getKey())
                            .buildHistogram(MetricRegistry.HistogramRanges.REQUEST_DURATION)
                            .observe(entry.getValue())

            );

        } else {

            Chassi.getInstance().getMetricRegistry().createBuilder(OPERATION_TIME_MILLIS_METRIC_NAME)
                    .withTag("action", context.getAction())
                    .withTag("outcome", outcome.toString().toLowerCase())
                    .buildHistogram(MetricRegistry.HistogramRanges.REQUEST_DURATION)
                    .observe(context.getElapsedMillis());

        }

        Chassi.getInstance().getMetricRegistry().createBuilder(ACTIVE_OPERATIONS_METRIC_NAME)
                .withTag("action", context.getAction())
                .buildGauge()
                .dec();

        if (applicationHealth == null) {
            this.applicationHealth = new ApplicationHealth(
                    Chassi.getInstance().getConfig().healthTimeWindowDuration(), Chassi.getInstance().getMetricRegistry());
        }

        this.applicationHealth.create(context.getAction(), outcome.isSuccessful(),
                outcome.isClientFault(), outcome.isServerFault(), context.getElapsedMillis(), context.getOperationTimeByType());

    }

    public ApplicationHealth getCurrentApplicationHealth() {
        if (applicationHealth == null) {
            this.applicationHealth = new ApplicationHealth(
                    Chassi.getInstance().getConfig().healthTimeWindowDuration(), Chassi.getInstance().getMetricRegistry());
        }
        return this.applicationHealth;
    }

}
