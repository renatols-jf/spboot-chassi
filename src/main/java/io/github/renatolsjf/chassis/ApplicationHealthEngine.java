package io.github.renatolsjf.chassis;


import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.monitoring.ApplicationHealth;
import io.github.renatolsjf.chassis.request.RequestOutcome;

public class ApplicationHealthEngine {

    private static final String ACTIVE_OPERATIONS_METRIC_NAME = "operation_active_requests";
    private static final String OPERATION_TIME_MILLIS_METRIC_NAME = "operation_request_millis";

    private static final String OPERATION_TAG_NAME = "operation";

    private ApplicationHealth applicationHealth;


    ApplicationHealthEngine() {}

    public void operationStarted() {
        Chassis.getInstance().getMetricRegistry().createBuilder(ACTIVE_OPERATIONS_METRIC_NAME)
                .withTag(OPERATION_TAG_NAME, Context.forRequest().getOperation())
                .buildGauge()
                .inc();
    }

    public void operationEnded(RequestOutcome outcome) {

        Context context = Context.forRequest();
        if (Chassis.getInstance().getConfig().exportRequestDurationMetricByType()) {

            context.getOperationTimeByType().entrySet().stream().forEach(entry ->
                    Chassis.getInstance().getMetricRegistry().createBuilder(OPERATION_TIME_MILLIS_METRIC_NAME)
                            .withTag(OPERATION_TAG_NAME, context.getOperation())
                            .withTag("outcome", outcome.toString().toLowerCase())
                            .withTag("timer_type", entry.getKey())
                            .buildHistogram(MetricRegistry.HistogramRanges.REQUEST_DURATION)
                            .observe(entry.getValue())

            );

        } else {

            Chassis.getInstance().getMetricRegistry().createBuilder(OPERATION_TIME_MILLIS_METRIC_NAME)
                    .withTag(OPERATION_TAG_NAME, context.getOperation())
                    .withTag("outcome", outcome.toString().toLowerCase())
                    .buildHistogram(MetricRegistry.HistogramRanges.REQUEST_DURATION)
                    .observe(context.getElapsedMillis());

        }

        Chassis.getInstance().getMetricRegistry().createBuilder(ACTIVE_OPERATIONS_METRIC_NAME)
                .withTag(OPERATION_TAG_NAME, context.getOperation())
                .buildGauge()
                .dec();

        if (applicationHealth == null) {
            this.applicationHealth = new ApplicationHealth(
                    Chassis.getInstance().getConfig().healthTimeWindowDuration(), Chassis.getInstance().getMetricRegistry());
        }

        this.applicationHealth.create(context.getOperation(), outcome.isSuccessful(),
                outcome.isClientFault(), outcome.isServerFault(), context.getElapsedMillis(), context.getOperationTimeByType());

    }

    public ApplicationHealth getCurrentApplicationHealth() {
        if (applicationHealth == null) {
            this.applicationHealth = new ApplicationHealth(
                    Chassis.getInstance().getConfig().healthTimeWindowDuration(), Chassis.getInstance().getMetricRegistry());
        }
        return this.applicationHealth;
    }

}
