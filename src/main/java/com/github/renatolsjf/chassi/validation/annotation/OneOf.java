package com.github.renatolsjf.chassi.validation.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OneOf {
    String[] value() default {};
    String message() default "";
}
