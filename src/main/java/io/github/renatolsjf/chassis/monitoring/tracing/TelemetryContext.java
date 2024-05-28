package io.github.renatolsjf.chassis.monitoring.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;


public class TelemetryContext {

    public static final String SCOPE_OWNER_ATTRIBUTE = "custom.owner";

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
        if (!this.isTracingEnabled()) {
            throw new IllegalStateException("Tracing is not enabled");
        }
        return TracingContext.fromContext();
    }

    public Context getParentContext() {
        if (this.isTracingEnabled() && this.isOriginatingTracingContextAvailable()) {
            return this.originatingTracingContext.getSpanContext();
        }
        return null;
    }

    public boolean isTraceRecording(){
        if (!this.isTracingEnabled()) {
            throw new IllegalStateException("Tracing is not enabled");
        }
        return this.getTracingContext().isRecording();
    }

    public boolean isTracingEnabled() {
        return this.tracer != null;
    }

    public boolean isOriginatingTracingContextAvailable() {
        return this.originatingTracingContext != null;
    }

    public void clear(boolean success) {
        if (this.isTracingEnabled()) {
            this.rootSpan.setStatus(success ? StatusCode.OK : StatusCode.ERROR);
            this.rootScope.close();
            this.rootSpan.end();
            if (this.parentScope != null) {
                this.parentScope.close();
            }
        }
    }

    public static TelemetryContext start(Tracer tracer, TracingContext originatingTracingContext, String rootSpanName, String scopeOwner) {

        TelemetryContext telemetryContext = new TelemetryContext(tracer, originatingTracingContext);

        io.opentelemetry.context.Context originatingContext = telemetryContext.getParentContext();
        if (originatingContext != null) {
            telemetryContext.parentScope = originatingContext.makeCurrent();
        }

        telemetryContext.rootSpan = telemetryContext.tracer.spanBuilder(rootSpanName)
                .setAttribute(SCOPE_OWNER_ATTRIBUTE, scopeOwner)
                .startSpan();
        telemetryContext.rootScope = telemetryContext.rootSpan.makeCurrent();

        return telemetryContext;

    }

    public static TelemetryContext empty() {
        return new TelemetryContext(null, null);
    }


}
