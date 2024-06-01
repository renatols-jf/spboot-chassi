package io.github.renatolsjf.chassis.monitoring.timing;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeRecording {

    public static final String HTTP = "http";
    public static final String DB = "db";

    String tag();
    boolean traced() default false;
    String spanName() default "";
}
