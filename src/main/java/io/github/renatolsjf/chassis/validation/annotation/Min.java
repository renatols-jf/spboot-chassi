package io.github.renatolsjf.chassis.validation.annotation;

import io.github.renatolsjf.chassis.validation.validators.MinMaxValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Min {
    long value() default MinMaxValidator.MIN_THRESHOLD;
    String message() default "";
}
