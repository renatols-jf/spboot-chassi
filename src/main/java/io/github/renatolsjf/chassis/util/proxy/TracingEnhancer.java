package io.github.renatolsjf.chassis.util.proxy;

import io.github.renatolsjf.chassis.Chassis;
import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.monitoring.tracing.*;
import io.github.renatolsjf.chassis.util.StringConcatenator;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TracingEnhancer implements TypeEnhancer {

    public MethodInterceptor createInterceptor(Object delegate) {
        return (Object o, Method method, Object[] args, MethodProxy methodProxy) -> {

            io.github.renatolsjf.chassis.monitoring.tracing.Span spanAnnotation;
            if (!method.isAnnotationPresent(io.github.renatolsjf.chassis.monitoring.tracing.Span.class)
                    || !Context.isTracingEnabled()) {
                if (delegate != null) {
                    return methodProxy.invoke(delegate, args);
                } else {
                    return methodProxy.invokeSuper(o, args);
                }
            } else {
                spanAnnotation = method.getAnnotation(io.github.renatolsjf.chassis.monitoring.tracing.Span.class);
            }

            System.out.println("ENHANCED: " + o.getClass().getSimpleName() + " - " + method.getName());
            Tracer tracer = Context.forRequest().getTelemetryContext().getTracer(); // TODO this needs to be in the context so the tracer is already initialized
            String spanName = spanAnnotation.value();
            if (spanName.isBlank()) {
                if (delegate != null) {
                    spanName = StringConcatenator.of(delegate.getClass().getSimpleName(), method.getName()).twoColons();
                } else {
                    spanName = StringConcatenator.of(o.getClass().getSuperclass().getSimpleName(), method.getName()).twoColons();
                }
            }

            Boolean addPrefix = Chassis.getInstance().getConfig().tracingAddCustomPrefix();
            Map<String, String> attributes = new HashMap<>();
            Arrays.stream(o.getClass().getSuperclass().getAnnotationsByType(SpanAttribute.class))
                    .forEach(sa -> attributes.put(this.resolveAttributeName(addPrefix, sa.key()), sa.value()));

            if (delegate != null) {
                Arrays.stream(delegate.getClass().getAnnotationsByType(SpanAttribute.class))
                        .forEach(sa -> attributes.put(this.resolveAttributeName(addPrefix, sa.key()), sa.value()));
            }

            Arrays.stream(method.getAnnotationsByType(SpanAttribute.class))
                    .forEach(sa -> attributes.put(this.resolveAttributeName(addPrefix, sa.key()), sa.value()));

            int idx = 0;
            for (Parameter parameter: method.getParameters()) {
                if (parameter.isAnnotationPresent(SpanAttributeParameter.class)) {
                    Object arg = args[idx];
                    String s = arg != null ? arg.toString() : null;
                    if (s != null && !s.isBlank()) {
                        String name = parameter.getAnnotation(SpanAttributeParameter.class).value();
                        if (name == null || name.isBlank()) {
                            name = parameter.getName();
                        }
                        attributes.put(this.resolveAttributeName(addPrefix,name), s);
                    }
                }
            }

            Span span = tracer.spanBuilder(spanName).startSpan();
            attributes.forEach(span::setAttribute);
            try (Scope scope = span.makeCurrent()) {
                if (delegate != null) {
                    return methodProxy.invoke(delegate, args);
                } else {
                    return methodProxy.invokeSuper(o, args);
                }
            } finally {
                span.end();
            }

        };
    }

    @Override
    public boolean isEnhanceable(Class<?> type) {
        return Context.isAvailable()
                && type.isAnnotationPresent(Traceable.class)
                && !type.isAnnotationPresent(NotTraceable.class);
    }

    private String resolveAttributeName(boolean addPrefix, String name) {
        return addPrefix
                ? StringConcatenator.of("custom", name).dot()
                : name;
    }


}

