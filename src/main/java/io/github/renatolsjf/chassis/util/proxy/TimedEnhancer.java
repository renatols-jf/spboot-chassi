package io.github.renatolsjf.chassis.util.proxy;

import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.monitoring.timing.Timed;
import io.github.renatolsjf.chassis.monitoring.timing.TimedOperation;
import io.github.renatolsjf.chassis.monitoring.tracing.Span;
import io.github.renatolsjf.chassis.monitoring.tracing.SpanAttribute;
import io.github.renatolsjf.chassis.monitoring.tracing.SpanAttributeParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class TimedEnhancer implements TypeEnhancer {

    @Override
    public Enhancement createEnhancement() {
        return new TimingEnhancement();
    }

    @Override
    public boolean isEnhanceable(Class<?> type) {

        Class<?> clazz = type;
        List<Method> methods = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            methods.addAll(Arrays.stream(type.getDeclaredMethods())
                    .filter(m -> !Modifier.isStatic(m.getModifiers()))
                    .collect(Collectors.toList()));
            clazz = clazz.getSuperclass();
        }

        boolean shouldEnhance = false;
        for (Method method : methods) {
            boolean timed = method.isAnnotationPresent(Timed.class);
            boolean traced = method.isAnnotationPresent(Span.class);
            if (traced && timed) {
                Context.logger().warn("Method " + method.getName() + " for class "
                        + method.getDeclaringClass().getName() + " has both @Timed and @Span annotations. @Timed is ignored. " +
                        "To trace a @Timed method, set @Timed::traced to true").log();
            } else if (timed) {
                shouldEnhance = true;
            }
        }

        return shouldEnhance;

    }
}

class TimingEnhancement implements Enhancement {

    TimedOperation timedOperation;

    @Override
    public void preInvocation(Object object, Object delegate, Method method, Object[] args) {

        if (method.isAnnotationPresent(Span.class)) {
            return;
        }

        if (method.isAnnotationPresent(Timed.class)) {

            Timed timed = method.getAnnotation(Timed.class);
            timedOperation = new TimedOperation(timed.tag());
            if (timed.traced()) {
                timedOperation.traced(timed.spanName().isBlank() ? method.getName() : timed.spanName());
            }

            Map<String, String> attributes = new HashMap<>();
            Arrays.stream(object.getClass().getSuperclass().getAnnotationsByType(SpanAttribute.class))
                    .forEach(sa -> attributes.put(sa.key(), sa.value()));

            if (delegate != null) {
                Arrays.stream(delegate.getClass().getAnnotationsByType(SpanAttribute.class))
                        .forEach(sa -> attributes.put(sa.key(), sa.value()));
            }

            Arrays.stream(method.getAnnotationsByType(SpanAttribute.class))
                    .forEach(sa -> attributes.put(sa.key(), sa.value()));

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
                        attributes.put(name, s);
                    }
                }
            }
            attributes.forEach((k, v) -> timedOperation.withTraceAttribute(k, v));

            timedOperation.start();

        }

    }

    @Override
    public void postInvocation(Object object, Object delegate, Method method, Object[] args) {
        if (timedOperation != null) {
            timedOperation.end();
        }
    }
}
