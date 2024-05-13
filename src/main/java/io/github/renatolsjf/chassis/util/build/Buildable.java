package io.github.renatolsjf.chassis.util.build;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Buildable {
}
