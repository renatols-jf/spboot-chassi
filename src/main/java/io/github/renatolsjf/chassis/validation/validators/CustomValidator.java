package io.github.renatolsjf.chassis.validation.validators;

import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.validation.Validatable;
import io.github.renatolsjf.chassis.validation.ValidationException;
import io.github.renatolsjf.chassis.validation.ValidatorExecutionException;
import io.github.renatolsjf.chassis.validation.annotation.Validation;

import java.lang.reflect.Constructor;

public class CustomValidator extends Validator <Object> {

    public CustomValidator(Validatable validatable) {
        super(validatable);
    }

    @Override
    protected void doValidate(String name, Object value, Validation validation) {

        for (Class<? extends Validator> aClass: validation.custom()) {

            Validator v;
            try {
                Constructor<? extends Validator> c = aClass.getConstructor(Validatable.class);
                v = c.newInstance(this.validatable);
                v.doValidate(name, value, validation);
            } catch (ValidationException e) {
                throw e;
            } catch (Exception e) {
                if (this.failOnError) {
                    throw new ValidatorExecutionException("Error while processing custom validation "
                            + "for " + name + " on class " + this.validatable.getClass().getName(), e);

                } else {
                    Context.forRequest().createLogger().warn("Ignoring failing custom validator for {} on class {}. Cause: {}",
                            name, this.validatable.getClass().getName(), e.getMessage());
                }
            }



        }

    }
}
