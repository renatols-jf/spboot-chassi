package io.github.renatolsjf.chassis;

import io.github.renatolsjf.chassis.monitoring.tracing.TelemetryContext;
import io.github.renatolsjf.chassis.monitoring.tracing.TracingContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;

public class TelemetryAgent {

    private SdkTracerProvider sdkTracerProvider;

    TelemetryAgent(String appName) {

        Resource resource = Resource.getDefault().toBuilder()
                .put(ResourceAttributes.SERVICE_NAME, appName).build();

        this.sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(ZipkinSpanExporter.builder().setEndpoint("http://localhost:9411/api/v2/spans").build()).build())
                .setResource(resource)
                .build();

    }

    public TelemetryContext start(String traceName, String traceHeader) {

        TracingContext tracingContext = null;
        if (traceHeader != null) {
            tracingContext = TracingContext.fromHeader(traceHeader);
        }

        Tracer tracer = null;
        if (Chassis.getInstance().getConfig().distributedTracingEnabled()
                && (tracingContext == null || tracingContext.isBeginTraced())) {
            tracer = sdkTracerProvider.get(traceName);
        }

        /*if (tracingContext != null && tracer == null) { //Should mark as not sample for downstream? Probably a config.
            tracingContext = tracingContext.notSampled();
        }*/

        return new TelemetryContext(tracer, tracingContext);

    }

}
