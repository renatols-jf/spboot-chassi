package io.github.renatolsjf.chassis.util.proxy;

import io.github.renatolsjf.chassis.Chassis;
import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.monitoring.tracing.NotTraceable;
import io.github.renatolsjf.chassis.monitoring.tracing.TelemetryAgent;
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
                    return method.invoke(delegate, args);
                } else {
                    return methodProxy.invokeSuper(o, args);
                }
            } else {
                spanAnnotation = method.getAnnotation(io.github.renatolsjf.chassis.monitoring.tracing.Span.class);
            }

            System.out.println("ENHANCED: " + o.getClass().getSimpleName() + " - " + method.getName());
            Tracer tracer = Context.forRequest().getTelemetryAgent().getTracer(); // TODO this needs to be in the context so the tracer is already initialized
            Span span = tracer.spanBuilder(spanAnnotation.value().isBlank()
                    ? method.getName() : spanAnnotation.value()).startSpan(); //TODO span annotation to configure
            try (Scope scope = span.makeCurrent()) {
                if (delegate != null) {
                    return method.invoke(delegate, args);
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

