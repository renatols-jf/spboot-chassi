package com.github.renatolsjf.chassi.rendering.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RenderAlias {
    String value() default "";
}
