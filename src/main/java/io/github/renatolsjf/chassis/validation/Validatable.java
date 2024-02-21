package io.github.renatolsjf.chassis.validation;

import io.github.renatolsjf.chassis.validation.validators.ValidatorFactory;

import java.util.Arrays;

public interface Validatable {
    default void validate() throws ValidationException {
        ValidatorFactory.createValidator(this).validate();
        Class<?> clazz = this.getClass();
        while (clazz != null && clazz != Object.class) {
            Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> Validatable.class.isAssignableFrom(f.getType()))
                    .map(f -> {
                        try {
                            f.trySetAccessible();
                            return (Validatable) f.get(this);
                        } catch (IllegalAccessException e) {
                            return null;
                        }
                    })
                    .filter(v -> v != null)
                    .forEach(v -> v.validate());
            clazz = clazz.getSuperclass();
        }

    }
}
