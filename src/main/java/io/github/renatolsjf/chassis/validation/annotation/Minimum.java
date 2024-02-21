package io.github.renatolsjf.chassis.validation.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Minimum {
    int value() default 0;
    String message() default "";
}
