package com.github.renatolsjf.chassi.validation.validators;

public class ValidatorFactory {

    private ValidatorFactory() {}

    public static Validator createValidator(Object validatable) {
        return new NullableValidator(validatable)
                .next(new MinimumValidator(validatable))
                .next(new OneOfValidator(validatable))
                .next(new PatternValidator(validatable));
    }

}
