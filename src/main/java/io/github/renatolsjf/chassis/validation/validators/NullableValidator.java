package io.github.renatolsjf.chassis.validation.validators;

import io.github.renatolsjf.chassis.validation.Validatable;
import io.github.renatolsjf.chassis.validation.ValidationException;
import io.github.renatolsjf.chassis.validation.annotation.Nullable;
import io.github.renatolsjf.chassis.validation.annotation.Validation;

public class NullableValidator extends Validator<Object> {

    protected NullableValidator(Validatable validatable) {
        super(validatable);
    }

    @Override

    public void doValidate(String name, Object value, Validation validation) throws ValidationException {
        Nullable nullable = validation.nullable();
        switch (nullable.value()) {
            case CANT_BE_NULL:
                throwErrorIfNull(name, value, nullable.message());
                break;

            case MUST_BE_NULL:
                throwErrorIfNotNull(name, value, nullable.message());
                break;

            default:
        }
    }


    private void throwErrorIfNull(String name, Object value, String message) {
        if (value == null
                || (value instanceof String && ((String) value).isBlank())) {
            if (message == null || message.isBlank()) {
                throw new ValidationException(name + " for " + this.validatable.getClass().getSimpleName()
                        + " can not be null");
            } else {
                throw new ValidationException(message);
            }
        }
    }

    private void throwErrorIfNotNull(String name, Object value, String message) {
        if (value != null) {
            if (message == null || message.isBlank()) {
                throw new ValidationException(name + " for " + this.validatable.getClass().getSimpleName()
                        + " must be null");
            } else {
                throw new ValidationException(message);
            }
        }
    }

}
