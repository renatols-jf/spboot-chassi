package io.github.renatolsjf.chassis.monitoring.timing;

import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.context.ExecutionContext;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Scope;

import java.time.Duration;
import java.util.concurrent.Callable;

public class TimedOperation<T> implements ExecutionContext {

    public static final String HTTP_OPERATION = "http";
    public static final String DATABASE_OPERATION = "db";

    private final String tag;
    private long executionTime;
    private String traceName;
    private String traceId;
    private String spanId;

    public static <Q> TimedOperation<Q> http() {
        return new TimedOperation<>(HTTP_OPERATION);
    }

    public static <Q> TimedOperation<Q> db() {
        return new TimedOperation<>(DATABASE_OPERATION);
    }

    public TimedOperation(String tag) {
        this.tag = tag;
    }

    public void run(Runnable r) {
        long l = System.currentTimeMillis();
        try {

            if (Context.isAvailable()
                    && Context.forRequest().isBeingTraced()
                    && this.traceName != null
                    && !this.traceName.isBlank()) {

                Span span = Context.forRequest().getTelemetryContext().getTracer()
                        .spanBuilder(this.tag + "::" + this.traceName).startSpan();
                try (Scope scope = span.makeCurrent()) {
                    SpanContext spc = span.getSpanContext();
                    this.traceId = spc.getTraceId();
                    this.spanId = spc.getSpanId();
                    r.run();
                } finally {
                    span.end();
                }

            } else {
                r.run();
            }

        } finally {
            this.executionTime = System.currentTimeMillis() - l;
            Context.forRequest().recordOperationTime(this.tag, this.executionTime);
        }
    }

    public T execute(Callable<T> e) {
        long l = System.currentTimeMillis();
        T t = null;
        try {

            if (Context.isAvailable()
                    && Context.forRequest().isBeingTraced()
                    && this.traceName != null
                    && !this.traceName.isBlank()) {

                Span span = Context.forRequest().getTelemetryContext().getTracer()
                        .spanBuilder(this.tag + "::" + this.traceName).startSpan();
                try (Scope scope = span.makeCurrent()) {
                    SpanContext spc = span.getSpanContext();
                    this.traceId = spc.getTraceId();
                    this.spanId = spc.getSpanId();
                    t = e.call();
                } finally {
                    span.end();
                }

            } else {
                t = e.call();
            }

            return t;

        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new TimedOperationException(ex);
        } finally {
            this.executionTime = System.currentTimeMillis() - l;
            Context.forRequest().recordOperationTime(this.tag, this.executionTime);
            if (t instanceof TimeSensitive ts) {
                ts.setDurationInMilliseconds(this.executionTime);
            }
        }
    }

    public TimedOperation traced(String traceName) {
        this.traceName = traceName;
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
