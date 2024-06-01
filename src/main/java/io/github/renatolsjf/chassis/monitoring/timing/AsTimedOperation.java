package io.github.renatolsjf.chassis.monitoring.timing;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AsTimedOperation {

    public static final String HTTP = "http";
    public static final String DB = "db";

    String tag();
    boolean traced() default false;
    String spanName() default "";
}
