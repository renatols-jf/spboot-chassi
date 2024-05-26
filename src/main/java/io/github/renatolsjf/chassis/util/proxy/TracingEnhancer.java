package io.github.renatolsjf.chassis.util.proxy;

import io.github.renatolsjf.chassis.Chassis;
import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.monitoring.tracing.NotTraceable;
import io.github.renatolsjf.chassis.monitoring.tracing.Traceable;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class TracingEnhancer implements TypeEnhancer{

    public MethodInterceptor createInterceptor(Object delegate) {
        return (Object o, Method method, Object[] args, MethodProxy methodProxy) -> {

            io.github.renatolsjf.chassis.monitoring.tracing.Span spanAnnotation;
            if (!method.isAnnotationPresent(io.github.renatolsjf.chassis.monitoring.tracing.Span.class)
                    || !Context.isAvailable()
                    || !Context.forRequest().isBeingTraced()) {
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
                    spanName = delegate.getClass().getSimpleName() + "::" + method.getName();
                } else {
                    spanName = o.getClass().getSuperclass().getSimpleName() + "::" + method.getName();
                }
            }
            Span span = tracer.spanBuilder(spanName).startSpan();
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
        return Chassis.getInstance().getConfig().distributedTracingEnabled()
                && Context.isAvailable()
                && type.isAnnotationPresent(Traceable.class)
                && !type.isAnnotationPresent(NotTraceable.class);
    }


}

