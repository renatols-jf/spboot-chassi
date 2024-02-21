package io.github.renatolsjf.chassis.validation.validators;

import io.github.renatolsjf.chassis.Chassi;
import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.validation.ValidationException;
import io.github.renatolsjf.chassis.validation.ValidatorExcutionException;
import io.github.renatolsjf.chassis.validation.annotation.Validation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Validator<T> {

    protected T validatable;
    protected Validator previous;
    private boolean failOnError;

    protected Validator(T validatable) {
        this(validatable, Chassi.getInstance().getConfig().validatorFailOnExecutionError());
    }

    protected Validator(T validatable, boolean failOnError) {
        this.validatable = validatable;
        this.failOnError = failOnError;
    }

    public Validator next(Validator otherValidator) {
        otherValidator.previous = this;
        return otherValidator;
    }

    public final void validate() throws ValidationException {

        List<AccessibleObject> members = this.getValidationMembers(this.validatable.getClass());
        List<ValidationEntry> entries = new ArrayList<>(members.size());

        members.stream()
                .forEach(m -> {

                    ValidationEntry entry;
                    if (m instanceof Method) {
                        entry = processMethod((Method) m);
                    } else if (m instanceof Field) {
                        entry = processField((Field) m);
                    } else {
                        entry = null;
                    }

                    if (entry != null) {
                        entries.add(entry);
                    }

                });
        this.validate(entries);

    }

    public final void validate(List<ValidationEntry> entries) {
        if (this.previous != null) {
            this.previous.validate(entries);
        }
        this.doValidate(entries);
    }

    public abstract void doValidate(List<ValidationEntry> entries) throws ValidationException;

    private List<AccessibleObject> getValidationMembers(Class clazz) {
        List<AccessibleObject> members = new ArrayList<>();
        this.addValidationMembers(clazz, members);
        return members;
    }

    private void addValidationMembers(Class clazz, List<AccessibleObject> members) {
        if (clazz == null || clazz == Object.class) {
            return;
        } else {
            addFields(clazz, members);
            addMethods(clazz, members);
            addValidationMembers(clazz.getSuperclass(), members);
        }
    }

    private void addMethods(Class clazz, List<AccessibleObject> members) {
        members.addAll(Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getAnnotationsByType(Validation.class).length > 0)
                .collect(Collectors.toList()));
    }


    private void addFields(Class clazz, List<AccessibleObject> members) {
        members.addAll(Arrays.stream(clazz.getDeclaredFields())
                .filter(m -> m.getAnnotationsByType(Validation.class).length > 0)
                .collect(Collectors.toList()));

    }

    private ValidationEntry processMethod(Method method) {

        List<Validation> validations = Arrays.stream(method.getAnnotationsByType(Validation.class))
                .filter(v -> {
                    String[] operations = v.operation();
                    return operations == null || operations.length == 0
                            || Arrays.asList(operations).contains(Context.forRequest().getOperation());
                })
                .collect(Collectors.toList());

        if (validations.isEmpty()) {
            return null;
        }

        String name = method.getName().substring(3);
        name = name.substring(0, 1).toLowerCase() + name.substring(1);

        Object value;
        try {
            if (!(method.canAccess(this.validatable))) {
                method.setAccessible(true);
            }
            value = method.invoke(this.validatable);
        } catch (InvocationTargetException e) {
            if (this.failOnError) {
                throw new ValidatorExcutionException("Error while processing validation "
                        + "for method " + method.getName() + " on class " + this.validatable.getClass().getName()
                        + ": " + e.getCause().getMessage(), e.getCause());
            } else {
                return null;
            }
        } catch (Exception e) {
            if (this.failOnError) {
                throw new ValidatorExcutionException("Error while processing validation "
                        + "for method " + method.getName() + " on class " + this.validatable.getClass().getName()
                        + ": " + e.getMessage(), e);
            } else {
                return null;
            }
        }

        return new ValidationEntry(name, value, validations);
    }

    private ValidationEntry processField(Field field) {

        List<Validation> validations = Arrays.stream(field.getAnnotationsByType(Validation.class))
                .filter(v -> {
                    String[] operations = v.operation();
                    return operations == null || operations.length == 0
                            || Arrays.asList(operations).contains(Context.forRequest().getOperation());
                })
                .collect(Collectors.toList());

        if (validations.isEmpty()) {
            return null;
        }

        String name = field.getName();
        Object value;
        try {
            if (!(field.canAccess(this.validatable))) {
                field.setAccessible(true);
            }
            value = field.get(this.validatable);
        } catch (Exception e) {
            if (this.failOnError) {
                throw new ValidatorExcutionException("Error while processing validation "
                        + "for field " + field.getName() + " on class " + this.validatable.getClass().getName()
                        + ": " + e.getMessage(), e);
            } else {
                return null;
            }
        }

        return new ValidationEntry(name, value, validations);

    }

}


