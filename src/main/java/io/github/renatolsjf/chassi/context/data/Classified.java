package io.github.renatolsjf.chassi.context.data;

import io.github.renatolsjf.chassi.context.data.cypher.ClassifiedCypher;
import io.github.renatolsjf.chassi.context.data.cypher.HiddenClassifiedCypher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Classified {
    Class<? extends ClassifiedCypher> value() default HiddenClassifiedCypher.class;
}
