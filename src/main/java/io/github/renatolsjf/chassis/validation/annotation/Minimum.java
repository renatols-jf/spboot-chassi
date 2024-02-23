package io.github.renatolsjf.chassis.validation.annotation;

import io.github.renatolsjf.chassis.validation.validators.MinimumValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Minimum {
    int value() default MinimumValidator.IGNORE_THRESHOLD;
    String message() default "";
}
