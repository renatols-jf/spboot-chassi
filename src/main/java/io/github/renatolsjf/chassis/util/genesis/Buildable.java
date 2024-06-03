package io.github.renatolsjf.chassis.util.genesis;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Buildable {
    String[] ignoreContaining() default {};
}
