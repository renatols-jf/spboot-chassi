package io.github.renatolsjf.chassis.validation.validators;

import io.github.renatolsjf.chassis.validation.Validatable;

public class ValidatorFactory {

    private ValidatorFactory() {}

    public static Validator createValidator(Validatable validatable) {
        return new NullableValidator(validatable)
                .next(new MinMaxValidator(validatable))
                .next(new OneOfValidator(validatable))
                .next(new PatternValidator(validatable))
                .next(new CustomValidator(validatable));
    }

}
