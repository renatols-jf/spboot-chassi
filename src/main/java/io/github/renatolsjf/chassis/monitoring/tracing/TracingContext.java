package io.github.renatolsjf.chassis.monitoring.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;

import java.util.Collections;

public class TracingContext {

    private static final String W3C_HEADER = "traceparent";

    private static final int W3C_HEADER_VALUE_LENGTH = 55;
    private static final int TRACE_ID_OFFSET = 3;
    private static final int SPAN_ID_OFFSET = 36;
    private static final int FLAGS_OFFSET = 53;

    private static final int TRACE_ID_LENGTH = 32;
    private static final int SPAN_ID_LENGTH = 16;

    private String w3cHeaderValue;

    private TracingContext() {

    }

    static TracingContext fromContext() {

        TracingContext tracingContext = new TracingContext();

        SpanContext spanContext = Span.current().getSpanContext();
        char[] header = new char[W3C_HEADER_VALUE_LENGTH];
        header[0] = '0';
        header[1] = '0';

        header[2] = '-';
        String traceId = spanContext.getTraceId();
        traceId.getChars(0, traceId.length(), header, TRACE_ID_OFFSET);

        header[SPAN_ID_OFFSET - 1] = '-';
        String spanId = spanContext.getSpanId();
        spanId.getChars(0, spanId.length(), header, SPAN_ID_OFFSET);

        header[FLAGS_OFFSET - 1] = '-';
        header[FLAGS_OFFSET] = '0';
        header[FLAGS_OFFSET + 1] = '1';

        tracingContext.w3cHeaderValue = new String(header);

        return tracingContext;

    }

    public static TracingContext fromHeader(String traceparent) {
        TracingContext tracingContext = new TracingContext();
        tracingContext.w3cHeaderValue = traceparent;
        return tracingContext;
    }

    public String getW3cHeaderName() {
        return W3C_HEADER;
    }

    public String getW3cHeaderValue() {
        if (!this.isTracingContextAvailable()) {
            throw new IllegalStateException("No trace context available");
        }
        return this.w3cHeaderValue;
    }

    public String getTraceId() {
        if (!this.isTracingContextAvailable()) {
            throw new IllegalStateException("No trace context available");
        }
        return this.w3cHeaderValue.substring(TRACE_ID_OFFSET, TRACE_ID_OFFSET + TRACE_ID_LENGTH);
    }

    public String getSpanId() {
        if (!this.isTracingContextAvailable()) {
            throw new IllegalStateException("No trace context available");
        }
        return this.w3cHeaderValue.substring(SPAN_ID_OFFSET, SPAN_ID_OFFSET + SPAN_ID_LENGTH);
    }

    public Context getSpanContext() {

        if (!this.isTracingContextAvailable()) {
            throw new IllegalStateException("No trace context available");
        }

        TextMapGetter<TracingContext> getter =
                new TextMapGetter<>() {
                    @Override
                    public String get(TracingContext carrier, String key) {
                        if ("traceparent".equalsIgnoreCase(key)) {
                            return carrier.getW3cHeaderValue();
                        }
                        return null;
                    }

                    @Override
                    public Iterable<String> keys(TracingContext carrier) {
                        return Collections.singletonList("traceparent");
                    }
                };

        return W3CTraceContextPropagator.getInstance()
                .extract(Context.current(), this, getter);

    }

    public boolean isTracingContextAvailable() {
        return w3cHeaderValue != null;
    }

    public boolean isBeginTraced() {
        return this.w3cHeaderValue.charAt(FLAGS_OFFSET + 1) == '1';
    }

    public TracingContext notSampled() {
        if (!this.isTracingContextAvailable()) {
            throw new IllegalStateException("No trace context available");
        }
        this.w3cHeaderValue = this.w3cHeaderValue.substring(0, this.w3cHeaderValue.length() - 1) + "0";
        return this;
    }

    public TracingContext sampled() {
        if (!this.isTracingContextAvailable()) {
            throw new IllegalStateException("No trace context available");
        }
        this.w3cHeaderValue = this.w3cHeaderValue.substring(0, this.w3cHeaderValue.length() - 1) + "1";
        return this;
    }

}
