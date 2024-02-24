package io.github.renatolsjf.chassis.validation.annotation;

import io.github.renatolsjf.chassis.validation.validators.MinMaxValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Max {
    long value() default MinMaxValidator.MAX_THRESHOLD;
    String message() default "";
}
