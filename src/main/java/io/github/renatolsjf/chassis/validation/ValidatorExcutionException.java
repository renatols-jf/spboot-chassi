package io.github.renatolsjf.chassis.validation;

public class ValidatorExcutionException extends RuntimeException {

    public ValidatorExcutionException() { super(); }
    public ValidatorExcutionException(String message) { super(message); }
    public ValidatorExcutionException(Throwable cause) { super(cause); }
    public ValidatorExcutionException(String message, Throwable cause) { super(message, cause); }

}
