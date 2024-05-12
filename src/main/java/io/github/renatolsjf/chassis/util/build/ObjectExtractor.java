package io.github.renatolsjf.chassis.util.build;

import io.github.renatolsjf.chassis.util.CaseString;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectExtractor<T> {

    private T object;
    private List<Method> methods;
    private List<Field> fields;

    public ObjectExtractor(T object) {
        this.object = object;
    }

    public MethodExtractor methodExtractor() {
        this.ensureMethods();
        return new MethodExtractor(this.object, this.methods);
    }

    public FieldExtractor fieldExtractor() {
        this.ensureFields();
        return new FieldExtractor(this.object, this.fields);
    }

    private void ensureMethods() {
        if (this.methods == null) {
            this.methods = new ArrayList<>();
            Class<?> type = object.getClass();
            while (type != null && type != Object.class) {
                this.methods.addAll(Arrays.stream(type.getDeclaredMethods())
                        .filter(m -> !Modifier.isStatic(m.getModifiers()))
                        .collect(Collectors.toList()));
                type = type.getSuperclass();
            }
        }
    }

    private void ensureFields() {
        if (this.fields == null) {
            this.fields = new ArrayList<>();
            Class<?> type = object.getClass();
            while (type != null && type != Object.class) {
                this.fields.addAll(Arrays.stream(type.getDeclaredFields())
                        .filter(f -> !Modifier.isStatic(f.getModifiers()))
                        .collect(Collectors.toList()));
                type = type.getSuperclass();
            }
        }
    }

}
