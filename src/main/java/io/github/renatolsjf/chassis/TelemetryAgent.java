package io.github.renatolsjf.chassis;

import io.github.renatolsjf.chassis.monitoring.tracing.NotTraceable;
import io.github.renatolsjf.chassis.monitoring.tracing.TelemetryContext;
import io.github.renatolsjf.chassis.monitoring.tracing.TracingContext;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import io.opentelemetry.semconv.ResourceAttributes;

import java.util.List;
import java.util.Map;

public class TelemetryAgent {

    private SdkTracerProvider sdkTracerProvider;

    TelemetryAgent(String appName, Configuration configuration) {

        Resource resource = Resource.getDefault().toBuilder()
                .put(ResourceAttributes.SERVICE_NAME, appName).build();

        this.sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(ZipkinSpanExporter.builder().setEndpoint("http://localhost:9411/api/v2/spans").build()).build())
                .setResource(resource)
                .setSampler(createSampler(configuration))
                .build();

    }

    public TelemetryContext start(String scopeOwner, String traceName, String traceHeader, Map<String, String> attributes) {

        if (!Chassis.getInstance().getConfig().isTracingEnabled()) {
            throw new IllegalStateException("Tracing is not enabled");
        }

        TracingContext tracingContext = null;
        if (traceHeader != null) {
            tracingContext = TracingContext.fromHeader(traceHeader);
        }

        Tracer tracer = sdkTracerProvider.get(traceName);

        return TelemetryContext.start(tracer, tracingContext, traceName, scopeOwner, attributes);

    }

    public TelemetryContext empty() {
        return TelemetryContext.empty();
    }

    private static Sampler createSampler(Configuration configuration) {

        Sampler rootSampler = switch (configuration.tracingStrategy()) {
            case ALWAYS_SAMPLE -> Sampler.alwaysOn();
            case NEVER_SAMPLE -> Sampler.alwaysOff();
            case RATIO_BASED_SAMPLE -> Sampler.traceIdRatioBased(configuration.tracingRatio());
            case PARENT_BASED_OR_ALWAYS_SAMPLE -> Sampler.parentBased(Sampler.alwaysOn());
            case PARENT_BASED_OR_NEVER_SAMPLE -> Sampler.parentBased(Sampler.alwaysOff());
            case PARENT_BASED_OR_RATIO_BASED_SAMPLE -> Sampler.parentBased(Sampler.traceIdRatioBased(configuration.tracingRatio()));
        };

        return new Sampler() {

            private SamplingResult result;

            @Override
            public SamplingResult shouldSample(Context context, String s, String s1, SpanKind spanKind, Attributes attributes, List<LinkData> list) {

                if (result != null) {
                    return result;
                } else {
                    try {

                        String scopeOwner = attributes.get(AttributeKey.stringKey(TelemetryContext.SCOPE_OWNER_ATTRIBUTE));
                        if (scopeOwner != null) {
                            Class<?> type = Class.forName(scopeOwner);
                            if (type.isAnnotationPresent(NotTraceable.class)) {
                                result =  SamplingResult.drop();
                                return result;
                            }
                        }
                        return rootSampler.shouldSample(context, s, s1, spanKind, attributes, list);

                    } catch (Throwable t) {
                        return rootSampler.shouldSample(context, s, s1, spanKind, attributes, list);
                    }
                }

            }

            @Override
            public String getDescription() {
                return "";
            }

        };

    }

}
