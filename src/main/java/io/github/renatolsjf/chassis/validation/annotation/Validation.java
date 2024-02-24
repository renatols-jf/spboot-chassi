package io.github.renatolsjf.chassis.validation.annotation;

import io.github.renatolsjf.chassis.validation.validators.Validator;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Validations.class)
public @interface Validation {
    String[] operation() default {};
    Nullable nullable() default @Nullable;
    Min min() default @Min;
    Max max() default @Max;
    OneOf oneOf() default @OneOf;
    Pattern pattern() default @Pattern;
    Class<? extends Validator>[] custom() default{};
}
