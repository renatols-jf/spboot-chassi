package io.github.renatolsjf.chassis.monitoring.timing;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Timed {
    String tag();
    boolean traced() default false;
    String spanName() default "";
}
