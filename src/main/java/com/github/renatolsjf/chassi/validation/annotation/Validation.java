package com.github.renatolsjf.chassi.validation.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Validations.class)
public @interface Validation {
    String[] action() default {};
    Nullable nullable() default @Nullable;
    Minimum minimum() default @Minimum;
    OneOf oneOf() default @OneOf;
    Pattern pattern() default @Pattern;
}
