package io.github.renatolsjf.chassis.context.data.cypher;

import java.lang.reflect.InvocationTargetException;

public interface ClassifiedCypher {

    static <T extends ClassifiedCypher> ClassifiedCypher createCypher(Class<T> cypher) {
        try {
            return cypher.getConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            return new HiddenClassifiedCypher();
        }
    }

    String encrypt(Object value);

}
