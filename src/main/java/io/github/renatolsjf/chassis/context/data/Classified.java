package io.github.renatolsjf.chassis.context.data;

import io.github.renatolsjf.chassis.context.data.cypher.ClassifiedCypher;
import io.github.renatolsjf.chassis.context.data.cypher.HiddenClassifiedCypher;
import io.github.renatolsjf.chassis.context.data.cypher.IgnoringCypher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Classified {
    Class<? extends ClassifiedCypher> value() default IgnoringCypher.class;
}
