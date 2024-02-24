package io.github.renatolsjf.chassis.validation.validators;

import io.github.renatolsjf.chassis.validation.Validatable;
import io.github.renatolsjf.chassis.validation.ValidationException;
import io.github.renatolsjf.chassis.validation.annotation.Validation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OneOfValidator extends Validator<Object> {

    protected OneOfValidator(Validatable validatable) {
        super(validatable);
    }

    @Override
    public void doValidate(String name, Object value, Validation validation) {

        if (value == null) {
            return;
        }

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
