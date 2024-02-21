package io.github.renatolsjf.chassis.rendering.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RenderConfigs.class)
public @interface RenderConfig {
    String[] operation() default {};
    RenderPolicy policy() default @RenderPolicy;
    RenderAlias alias() default @RenderAlias;
    RenderTransform transformer() default @RenderTransform;
}
