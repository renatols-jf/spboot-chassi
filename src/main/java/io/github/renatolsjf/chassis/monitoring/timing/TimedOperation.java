package io.github.renatolsjf.chassis.monitoring.timing;

import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.context.ExecutionContext;
import io.github.renatolsjf.chassis.util.StringConcatenator;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Scope;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class TimedOperation<T> implements ExecutionContext {

    public static final String HTTP_OPERATION = "http";
    public static final String DATABASE_OPERATION = "db";

    private final String tag;
    private long startingTime;
    private long executionTime;
    private String traceName;
    private String traceId;
    private String spanId;
    private Map<String, String> traceAttributes = new HashMap<>();

    private Span span;
    private Scope scope;

    public static <Q> TimedOperation<Q> http() {
        return new TimedOperation<>(HTTP_OPERATION);
    }

    public static <Q> TimedOperation<Q> db() {
        return new TimedOperation<>(DATABASE_OPERATION);
    }

    public TimedOperation(String tag) {
        this.tag = tag;
    }

    public void start() {

        this.startingTime = System.currentTimeMillis();
        if (Context.isTracingEnabled()
                && this.traceName != null
                && !this.traceName.isBlank()) {

            this.span = Context.forRequest().getTelemetryContext().getTracer()
                    .spanBuilder(StringConcatenator.of(this.tag, this.traceName).twoColons())
                    .startSpan();
            this.traceAttributes.forEach(span::setAttribute);
            this.scope = span.makeCurrent();

            SpanContext spc = span.getSpanContext();
            this.traceId = spc.getTraceId();
            this.spanId = spc.getSpanId();

        }

    }

    public void end() {

        if (scope != null) {
            scope.close();
        }
        if (span != null) {
            span.end();
        }

        this.executionTime = System.currentTimeMillis() - this.startingTime;
        Context.forRequest().recordOperationTime(this.tag, this.executionTime);

    }

    public void run(Runnable r) {
        try {
            this.start();
            r.run();
        } finally {
            this.end();
        }
    }

    public T execute(Callable<T> e) {
        T t = null;
        try {
            this.start();
            t = e.call();
            return t;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new TimedOperationException(ex);
        } finally {
            this.end();
            if (t instanceof TimeSensitive ts) {
                ts.setDurationInMilliseconds(this.executionTime);
            }
        }
    }

    public TimedOperation traced(String traceName) {
        this.traceName = traceName;
        return this;
    }

    public TimedOperation withTraceAttribute(String key, String value) {
        this.traceAttributes.put(new StringConcatenator(this.tag, key).dot(), value);
        return this;
    }

    public long getExecutionTimeInMillis() {
        return this.executionTime;
    }

    public double getExecutionTimeInSeconds() {
        return this.getExecutionTimeInMillis() / 1000d;
    }

    public Duration getDuration() {
        return Duration.ofMillis(this.getExecutionTimeInMillis());
    }

    @Override
    public String getTraceId() {
        return this.traceId;
    }

    @Override
    public String getSpanId() {
        return this.spanId;
    }

}
