package io.github.renatolsjf.chassis.validation.validators;

public class ValidatorFactory {

    private ValidatorFactory() {}

    public static Validator createValidator(Object validatable) {
        return new NullableValidator(validatable)
                .next(new MinMaxValidator(validatable))
                .next(new OneOfValidator(validatable))
                .next(new PatternValidator(validatable));
    }

}
