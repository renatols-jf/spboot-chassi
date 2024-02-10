package com.github.renatolsjf.chassi.validation;

public class ValidationException extends RuntimeException {

    public ValidationException() { super(); }
    public ValidationException(String message) { super(message); }
    public ValidationException(Throwable cause) { super(cause); }
    public ValidationException(String message, Throwable cause) { super(message, cause); }

}
