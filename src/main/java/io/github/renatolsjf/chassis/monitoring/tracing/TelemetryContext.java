package io.github.renatolsjf.chassis.monitoring.tracing;

import io.opentelemetry.api.trace.Tracer;

public class TelemetryContext {

    private Tracer tracer;

    public TelemetryContext(Tracer tracer) {
        this.tracer = tracer;
    }

    public Tracer getTracer() {
        return this.tracer;
    }

}
