package io.github.renatolsjf.chassis.util.genesis;


import io.github.renatolsjf.chassis.util.expression.ExpressionParser;

import java.lang.reflect.*;
import java.util.Map;

public class ObjectBuilder {

    public enum InitializationType {
        METHOD_ONLY,
        FIELD_ONLY,
        METHOD_FIRST,
        FIELD_FIRST
    }

    private InitializationType initializationType = InitializationType.METHOD_FIRST;

    public ObjectBuilder withInitializationType(InitializationType initializationType) {
        this.initializationType = initializationType;
        return this;
    }

    public <T> T build(Class<T> type, Map<String, Object> map) throws UnableToBuildObjectException {

        if (!type.isAnnotationPresent(Buildable.class)) {
            throw new UnableToBuildObjectException("Type " + type + " is not annotated with @Buildable");
        }

        T t;
        try {
            Constructor<T> c = type.getConstructor();
            t = c.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new UnableToBuildObjectException(e);
        }

        ObjectExtractor objectExtractor = new ObjectExtractor(t);
        MethodExtractor methodExtractor = objectExtractor.methodExtractor().setter();
        FieldExtractor fieldExtractor = objectExtractor.fieldExtractor();


        map.forEach((k, v) -> {

            Object parsedValue = ExpressionParser.parse(v);
            if (this.initializationType == InitializationType.METHOD_FIRST || this.initializationType == InitializationType.METHOD_ONLY) {
                ExtractedMember<Method> ex = methodExtractor.withName(k).mostAdequateOrNull(parsedValue);
                if (ex != null) {
                    try {
                        ex.callOrSet();
                        return;
                    } catch (UnableToSetMemberException e) {}
                }
            }

            if (this.initializationType != InitializationType.METHOD_ONLY) {
                ExtractedMember<Field> ex = fieldExtractor.withName(k).mostAdequateOrNull(parsedValue);
                if (ex != null) {
                    try {
                        ex.callOrSet();
                        return;
                    } catch (UnableToSetMemberException e) {}
                }
            }

            if (this.initializationType == InitializationType.FIELD_FIRST) {
                ExtractedMember<Method> ex = methodExtractor.withName(k).mostAdequateOrNull(parsedValue);
                try {
                    ex.callOrSet();
                } catch (UnableToSetMemberException e) {}
            }

        });

        return t;

    }

}
