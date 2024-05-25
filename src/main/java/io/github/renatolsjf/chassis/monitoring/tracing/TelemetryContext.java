package io.github.renatolsjf.chassis.monitoring.tracing;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;


public class TelemetryContext {

    private Tracer tracer;
    private TracingContext originatingTracingContext;

    public TelemetryContext(Tracer tracer, TracingContext originatingTracingContext) {
        this.tracer = tracer;
        this.originatingTracingContext = originatingTracingContext;
    }

    public Tracer getTracer() {
        return this.tracer;
    }

    public TracingContext getTracingContext() {
        if (this.isBeingTraced()) {
            return TracingContext.fromContext();
        } else {
            return this.originatingTracingContext;
        }
    }

    public Context getParentContext() {
        if (this.isBeingTraced() && this.isOriginatingTracingContextAvailable()) {
            return this.originatingTracingContext.getSpanContext();
        }
        return null;
    }

    public boolean isBeingTraced(){
        return this.tracer != null;
    }

    public boolean isTracingContextAvailable() {
        return this.isBeingTraced() || this.originatingTracingContext != null;
    }

    public boolean isOriginatingTracingContextAvailable() {
        return this.originatingTracingContext != null;
    }


}
