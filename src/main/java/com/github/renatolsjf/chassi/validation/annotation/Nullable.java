package com.github.renatolsjf.chassi.validation.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface Nullable {

    enum NullableType {
        CAN_BE_NULL,
        CANT_BE_NULL,
        MUST_BE_NULL,
    }

    NullableType value() default NullableType.CAN_BE_NULL;
    String message() default "";

}
