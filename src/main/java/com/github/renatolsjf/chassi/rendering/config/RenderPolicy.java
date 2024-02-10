package com.github.renatolsjf.chassi.rendering.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RenderPolicy {

    enum Policy {
        IGNORE,
        RENDER
    }

    Policy value() default Policy.RENDER;

}
