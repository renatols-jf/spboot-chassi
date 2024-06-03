package io.github.renatolsjf.chassis.monitoring.tracing;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpanAttributes {
    SpanAttribute[] value();
}
