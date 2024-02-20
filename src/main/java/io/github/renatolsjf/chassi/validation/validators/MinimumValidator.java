package io.github.renatolsjf.chassi.validation.validators;

import io.github.renatolsjf.chassi.validation.ValidationException;
import io.github.renatolsjf.chassi.validation.annotation.Minimum;
import io.github.renatolsjf.chassi.validation.annotation.Validation;

import java.math.BigDecimal;

public class MinimumValidator extends DefaultValidator {

    protected MinimumValidator(Object validatable) {
        super(validatable);
    }

    @Override
    public void defaultValidation(String name, Object value, Validation validation) {

        Minimum minimum = validation.minimum();
        if (minimum.value() == 0) {
            return;
        }

        BigDecimal bd;
        if (value == null) {
            bd = null;
        } else if (value instanceof BigDecimal) {
            bd = (BigDecimal) value;
        } else {
            try {
                bd = new BigDecimal(value.toString());
            } catch (Throwable t) {
                bd = null;
            }
        }

        throwErrorIfLessThan(name, bd, new BigDecimal(minimum.value()), minimum.message());

    }

    private void throwErrorIfLessThan(String name, BigDecimal value, BigDecimal min, String message) {
        if (value == null || value.compareTo(min) < 0) {
            if (message == null || message.isBlank()) {
                throw new ValidationException(name + " can not be less than " + min.toPlainString());
            } else {
                throw new ValidationException(message);
            }
        }
    }

}
