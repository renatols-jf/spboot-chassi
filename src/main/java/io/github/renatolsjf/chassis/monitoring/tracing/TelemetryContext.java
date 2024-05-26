package io.github.renatolsjf.chassis.monitoring.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;


public class TelemetryContext {

    private Tracer tracer;
    private TracingContext originatingTracingContext;
    private Span rootSpan;
    private Scope parentScope;
    private Scope rootScope;

    private TelemetryContext(Tracer tracer, TracingContext originatingTracingContext) {
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

    public void clear(boolean success) {
        if (this.isBeingTraced()) {
            this.rootSpan.setStatus(success ? StatusCode.OK : StatusCode.ERROR);
            this.rootScope.close();
            this.rootSpan.end();
            if (this.parentScope != null) {
                this.parentScope.close();
            }
        }
    }

    public static TelemetryContext start(Tracer tracer, TracingContext originatingTracingContext, String rootSpanName) {

        TelemetryContext telemetryContext = new TelemetryContext(tracer, originatingTracingContext);

        if (telemetryContext.isBeingTraced()) {
            io.opentelemetry.context.Context originatingContext = telemetryContext.getParentContext();
            if (originatingContext != null) {
                telemetryContext.parentScope = originatingContext.makeCurrent();
            }
            telemetryContext.rootSpan = telemetryContext.tracer.spanBuilder(rootSpanName).startSpan();
            telemetryContext.rootScope = telemetryContext.rootSpan.makeCurrent();
        }

        return telemetryContext;

    }


}
