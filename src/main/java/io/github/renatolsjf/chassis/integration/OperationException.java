package io.github.renatolsjf.chassis.integration;

public class OperationException extends RuntimeException {

    protected OperationException() { super(); }
    protected OperationException(String message) { super(message); }
    protected OperationException(Throwable cause) { super(cause); }
    protected OperationException(String message, Throwable cause) { super(message, cause); }

}
