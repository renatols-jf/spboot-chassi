package io.github.renatolsjf.chassis.monitoring.tracing;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Span {
    String value() default "";
}
