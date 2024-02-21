package io.github.renatolsjf.chassis.rendering.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RenderAlias {
    String value() default "";
}
