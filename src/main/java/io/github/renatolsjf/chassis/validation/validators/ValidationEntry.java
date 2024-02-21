package io.github.renatolsjf.chassis.validation.validators;

import io.github.renatolsjf.chassis.validation.annotation.Validation;

import java.util.List;

public class ValidationEntry {

    private final String name;
    private final Object value;
    private final List<Validation> validations;

    public ValidationEntry(String name, Object value, List<Validation> validations) {
        this.name = name;
        this.value = value;
        this.validations = validations;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return this.value;
    }

    public List<Validation> getValidations() {
        return this.validations;
    }

}
