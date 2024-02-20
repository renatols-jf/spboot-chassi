package io.github.renatolsjf.chassi.validation.validators;

import io.github.renatolsjf.chassi.validation.ValidationException;
import io.github.renatolsjf.chassi.validation.annotation.Validation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OneOfValidator extends DefaultValidator {

    protected OneOfValidator(Object validatable) {
        super(validatable);
    }

    @Override
    public void defaultValidation(String name, Object value, Validation validation) {

        List<String> acceptedValues = Arrays.asList(validation.oneOf().value()).stream()
                .map(s -> s.split(","))
                .flatMap(a -> Arrays.stream(a))
                .map(s -> s.trim())
                .collect(Collectors.toList());

        if (acceptedValues.isEmpty()) {
            return;
        }

        throwErrorIfNotContained(name, acceptedValues, value, validation.oneOf().message());

    }

    private void throwErrorIfNotContained(String name, List<String> acceptedValues,
                                          Object value, String message) {
        if (!(acceptedValues.contains(value))) {
            if (message == null || message.isBlank()) {
                throw new ValidationException(name + " has an invalid value: " + value);
            } else {
                throw new ValidationException(message);
            }
        }
    }
}
