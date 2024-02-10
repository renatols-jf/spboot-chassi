package com.github.renatolsjf.chassi.validation.validators;

import com.github.renatolsjf.chassi.validation.ValidationException;
import com.github.renatolsjf.chassi.validation.annotation.Validation;

import java.util.regex.Pattern;

public class PatternValidator extends DefaultValidator {

    protected PatternValidator(Object validatable) {
        super(validatable);
    }

    @Override
    public void defaultValidation(String name, Object value, Validation validation) {

        String s = validation.pattern().value();
        if (s == null || s.isBlank() || value == null) {
            return;
        }

        Pattern p = Pattern.compile(s);
        if (!(p.matcher(value.toString()).matches())) {
            String message = validation.pattern().message();
            if (message == null || message.isBlank()) {
                throw new ValidationException(name + " does not conform to pattern");
            } else {
                throw new ValidationException(message);
            }
        }

    }
}
