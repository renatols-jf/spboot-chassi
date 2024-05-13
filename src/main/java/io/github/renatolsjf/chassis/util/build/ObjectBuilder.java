package io.github.renatolsjf.chassis.util.build;


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

        ObjectExtractor<T> objectExtractor = new ObjectExtractor<>(t);
        MethodExtractor methodExtractor = objectExtractor.methodExtractor()
                .withPrefix("set")
                .withPrefix("with");
        FieldExtractor fieldExtractor = objectExtractor.fieldExtractor();


        map.forEach((k, v) -> {

            if (this.initializationType == InitializationType.METHOD_FIRST || this.initializationType == InitializationType.METHOD_ONLY) {
                ExtractedMember<Method> ex = methodExtractor.withName(k).mostAdequateOrNull(v);
                if (ex != null) {
                    if (ex.setAndIgnore()) {
                        return;
                    }
                }
            }

            if (this.initializationType != InitializationType.METHOD_ONLY) {
                ExtractedMember<Field> ex = fieldExtractor.withName(k).mostAdequateOrNull(v);
                if (ex != null) {
                    if (ex.setAndIgnore()) {
                        return;
                    }
                }
            }

            if (this.initializationType == InitializationType.FIELD_FIRST) {
                ExtractedMember<Method> ex = methodExtractor.withName(k).mostAdequateOrNull(v);
                if (ex != null) {
                    ex.setAndIgnore();
                }
            }

        });

        return t;

    }

}
