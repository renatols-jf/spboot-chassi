package io.github.renatolsjf.chassis.monitoring.tracing;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;

public class TelemetryAgent {

    private static SdkTracerProvider sdkTracerProvider;

    private Tracer tracer;

    static {

        Resource resource = Resource.getDefault().toBuilder()
                .put(ResourceAttributes.SERVICE_NAME, "PLACEHOLDER").put(ResourceAttributes.SERVICE_VERSION, "0.0.1").build();

        sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
                .setResource(resource)
                .build();

    }

    private TelemetryAgent(String trace) {
        this.tracer = sdkTracerProvider.get("trace");
    }

    public static TelemetryAgent start(String trace) {
        return new TelemetryAgent(trace);
    }

    public Tracer getTracer() {
        return this.tracer;
    }

}
