package io.github.renatolsjf.chassis.util.genesis;

import java.lang.annotation.*;

//TODO use thins for object creation and reading
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Introspectable {

    enum InstrospectionType {
        METHOD_ONLY,
        FIELD_ONLY,
        METHOD_FIRST,
        FIELD_FIRST
    }

    InstrospectionType value() default InstrospectionType.METHOD_FIRST;

}
