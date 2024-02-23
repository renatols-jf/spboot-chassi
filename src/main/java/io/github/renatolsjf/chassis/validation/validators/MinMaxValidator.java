package io.github.renatolsjf.chassis.validation.validators;

import io.github.renatolsjf.chassis.validation.ValidationException;
import io.github.renatolsjf.chassis.validation.annotation.Max;
import io.github.renatolsjf.chassis.validation.annotation.Min;
import io.github.renatolsjf.chassis.validation.annotation.Validation;

import java.math.BigDecimal;

public class MinMaxValidator extends DefaultValidator {

    public static final long MIN_THRESHOLD = Long.MIN_VALUE;
    public static final long MAX_THRESHOLD = Long.MAX_VALUE;

    protected MinMaxValidator(Object validatable) {
        super(validatable);
    }

    @Override
    public void defaultValidation(String name, Object value, Validation validation) {

        if (value == null) {
            return;
        }

        Min min = validation.min();
        Max max = validation.max();

        if (min.value() == MIN_THRESHOLD && max.value() == MAX_THRESHOLD) {
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

        if (min.value() != MIN_THRESHOLD) {
            throwErrorIfLessThan(name, bd, new BigDecimal(min.value()), min.message());
        }

        if (max.value() != MAX_THRESHOLD) {
            throwErrorIfGreaterThan(name, bd, new BigDecimal(max.value()), max.message());
        }


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

    private void throwErrorIfGreaterThan(String name, BigDecimal value, BigDecimal max, String message) {
        if (value == null || value.compareTo(max) > 0) {
            if (message == null || message.isBlank()) {
                throw new ValidationException(name + " can not be greater than " + max.toPlainString());
            } else {
                throw new ValidationException(message);
            }
        }
    }

}
