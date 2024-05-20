package io.github.renatolsjf.chassis.util.proxy;

import io.github.renatolsjf.chassis.monitoring.tracing.NotTraceable;
import io.github.renatolsjf.chassis.monitoring.tracing.Telemetry;
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
            System.out.println("ENHANCED: " + o.getClass().getSimpleName() + " - " + method.getName());
            Tracer tracer = new Telemetry().openTelemetry().getTracer(o.getClass().getSimpleName()); // TODO this needs to be in the context so the tracer is already initialized
            Span span = tracer.spanBuilder(method.getName()).startSpan(); //TODO span annotation to configure
            try (Scope scope = span.makeCurrent()) {
                return method.invoke(delegate != null ? delegate : o, args);
            } finally {
                span.end();
            }
        };
    }

    @Override
    public boolean isEnhanceable(Class<?> type) {
        return type.isAnnotationPresent(Traceable.class) && !type.isAnnotationPresent(NotTraceable.class);
    }


}

