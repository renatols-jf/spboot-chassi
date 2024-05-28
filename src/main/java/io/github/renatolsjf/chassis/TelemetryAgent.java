package io.github.renatolsjf.chassis;

import io.github.renatolsjf.chassis.monitoring.tracing.TelemetryContext;
import io.github.renatolsjf.chassis.monitoring.tracing.TracingContext;
import io.github.renatolsjf.chassis.monitoring.tracing.TracingStrategy;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.ResourceAttributes;

public class TelemetryAgent {

    private SdkTracerProvider sdkTracerProvider;

    TelemetryAgent(String appName) {

        Resource resource = Resource.getDefault().toBuilder()
                .put(ResourceAttributes.SERVICE_NAME, appName).build();

        this.sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(ZipkinSpanExporter.builder().setEndpoint("http://localhost:9411/api/v2/spans").build()).build())
                .setResource(resource)
                .setSampler(createSampler())
                .build();

    }

    public TelemetryContext start(String traceName, String traceHeader) {

        if (!Chassis.getInstance().getConfig().tracingEnabled()) {
            throw new IllegalStateException("Tracing is not enabled");
        }

        TracingContext tracingContext = null;
        if (traceHeader != null) {
            tracingContext = TracingContext.fromHeader(traceHeader);
        }

        Tracer tracer = sdkTracerProvider.get(traceName);

        return TelemetryContext.start(tracer, tracingContext, traceName);

    }

    public TelemetryContext empty() {
        return TelemetryContext.empty();
    }

    private static Sampler createSampler() {
        return switch (Chassis.getInstance().getConfig().tracingStrategy()) {
            case ALWAYS_SAMPLE -> Sampler.alwaysOn();
            case NEVER_SAMPLE -> Sampler.alwaysOff();
            case RATIO_BASED_SAMPLE -> Sampler.traceIdRatioBased(Chassis.getInstance().getConfig().tracingRatio());
            case PARENT_BASED_OR_ALWAYS_SAMPLE -> Sampler.parentBased(Sampler.alwaysOn());
            case PARENT_BASED_OR_NEVER_SAMPLE -> Sampler.parentBased(Sampler.alwaysOff());
            case PARENT_BASED_OR_RATIO_BASED_SAMPLE -> Sampler.parentBased(Sampler.traceIdRatioBased(Chassis.getInstance().getConfig().tracingRatio()));
        };
    }

}
