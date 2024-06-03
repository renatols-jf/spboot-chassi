package io.github.renatolsjf.chassis.util.proxy;

import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.monitoring.timing.AsTimedOperation;
import io.github.renatolsjf.chassis.monitoring.timing.TimedOperation;
import io.github.renatolsjf.chassis.monitoring.tracing.Span;
import io.github.renatolsjf.chassis.monitoring.tracing.SpanAttribute;
import io.github.renatolsjf.chassis.monitoring.tracing.SpanAttributeParameter;
import io.github.renatolsjf.chassis.util.StringConcatenator;

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

        boolean shouldEnhance = type.isAnnotationPresent(AsTimedOperation.class);

        Class<?> clazz = type;
        List<Method> methods = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            methods.addAll(Arrays.stream(type.getDeclaredMethods())
                    .filter(m -> !Modifier.isStatic(m.getModifiers()))
                    .collect(Collectors.toList()));
            clazz = clazz.getSuperclass();
        }

        for (Method method : methods) {
            boolean timed = method.isAnnotationPresent(AsTimedOperation.class);
            boolean traced = method.isAnnotationPresent(Span.class);
            if (traced && timed) {
                Context.logger().warn("Method " + method.getName() + " for class "
                        + method.getDeclaringClass().getName() + " has both @AsTimedOperation and @Span annotations. @AsTimedOperation is ignored. " +
                        "To trace a @AsTimedOperation method, set @AsTimedOperation::traced to true").log();
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
    public void preInvocation(Class<?> requestedType, Object object, Object delegate, Method method, Object[] args) {

        if (method.isAnnotationPresent(Span.class)) {
            return;
        }

        AsTimedOperation timed = method.getAnnotation(AsTimedOperation.class);
        boolean isMethodAnnotation = timed != null;
        if (timed == null) {
            timed = object.getClass().getSuperclass().getAnnotation(AsTimedOperation.class);
        }
        if (timed == null && delegate != null) {
            timed = delegate.getClass().getAnnotation(AsTimedOperation.class);
        }
        if (timed == null) {
            timed = requestedType.getAnnotation(AsTimedOperation.class);
        }

        if (timed != null) {

            if (method.isAnnotationPresent(Span.class)) {
                Context.logger().warn("Method " + method.getName() + " for class "
                        + method.getDeclaringClass().getName() + " has both @AsTimedOperation - either the method itself or the class - " +
                        "and @Span annotations. @AsTimedOperation is ignored. " +
                        "To trace a @AsTimedOperation method, set @AsTimedOperation::traced to true").log();
                return;
            }

            timedOperation = new TimedOperation(timed.tag());
            if (timed.traced()) {
                String spanName = timed.spanName();
                if (isMethodAnnotation) {
                    if (spanName.isBlank()) {
                        spanName = StringConcatenator.of(object.getClass().getSuperclass().getSimpleName(), method.getName()).twoColons();
                    }
                } else {
                    if (spanName.isBlank()) {
                        spanName = StringConcatenator.of(object.getClass().getSuperclass().getSimpleName(), method.getName()).twoColons();
                    } else {
                        spanName = StringConcatenator.of(spanName, method.getName()).twoColons();
                    }
                }

                timedOperation.traced(spanName);

                Map<String, String> attributes = new HashMap<>();
                Arrays.stream(object.getClass().getSuperclass().getAnnotationsByType(SpanAttribute.class))
                        .forEach(sa -> attributes.put(sa.key(), sa.value()));

                Arrays.stream(requestedType.getAnnotationsByType(SpanAttribute.class))
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

            }

            timedOperation.start();

        }

    }

    @Override
    public void postInvocation(Class<?> requestedType, Object object, Object delegate, Method method, Object[] args) {
        if (timedOperation != null) {
            timedOperation.end();
        }
    }
}
