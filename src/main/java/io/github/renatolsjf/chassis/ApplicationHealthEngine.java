package io.github.renatolsjf.chassis;


import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.monitoring.ApplicationHealth;
import io.github.renatolsjf.chassis.request.RequestOutcome;

public class ApplicationHealthEngine {

    private ApplicationHealth applicationHealth;


    ApplicationHealthEngine() {}

    public void operationStarted() {
        Chassis.getInstance().getMetricRegistry().createBuilder(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_NAME_ACTIVE_OPERATIONS))
                .withTag(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_TAG_OPERATION), Context.forRequest().getOperation())
                .buildGauge()
                .inc();
    }

    public void operationEnded(RequestOutcome outcome) {

        Context context = Context.forRequest();
        if (Chassis.getInstance().getConfig().exportRequestDurationMetricByType()) {

            context.getOperationTimeByType().entrySet().stream().forEach(entry ->
                    Chassis.getInstance().getMetricRegistry().createBuilder(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_NAME_OPERATION_TIME))
                            .withTag(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_TAG_OPERATION), context.getOperation())
                            .withTag(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_TAG_OUTCOME), outcome.toString().toLowerCase())
                            .withTag(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_TAG_TIMER_TYPE), entry.getKey())
                            .buildHistogram(MetricRegistry.HistogramRanges.REQUEST_DURATION)
                            .observe(entry.getValue())

            );

        } else {

            Chassis.getInstance().getMetricRegistry().createBuilder(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_NAME_OPERATION_TIME))
                    .withTag(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_TAG_OPERATION), context.getOperation())
                    .withTag(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_TAG_OUTCOME), outcome.toString().toLowerCase())
                    .buildHistogram(MetricRegistry.HistogramRanges.REQUEST_DURATION)
                    .observe(context.getElapsedMillis());

        }

        Chassis.getInstance().getMetricRegistry().createBuilder(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_NAME_ACTIVE_OPERATIONS))
                .withTag(Chassis.getInstance().labels().getLabel(Labels.Field.METRICS_TAG_OPERATION), context.getOperation())
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
