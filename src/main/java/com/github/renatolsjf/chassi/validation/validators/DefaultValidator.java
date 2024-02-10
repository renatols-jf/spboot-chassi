package com.github.renatolsjf.chassi.validation.validators;

import com.github.renatolsjf.chassi.validation.ValidationException;
import com.github.renatolsjf.chassi.validation.annotation.Validation;

import java.util.List;

abstract class DefaultValidator extends Validator<Object> {

    protected DefaultValidator(Object validatable) {
        super(validatable);
    }

    @Override
    public void doValidate(List<ValidationEntry> entries) throws ValidationException {

        if (entries == null || entries.isEmpty()) {
            return;
        }

        entries.forEach(e -> {

            String name = e.getName();
            Object value = e.getValue();

            for (Validation validation : e.getValidations()) {
                this.defaultValidation(name, value, validation);
            }
        });

    }

    public abstract void defaultValidation(String name, Object value, Validation validation);

}
