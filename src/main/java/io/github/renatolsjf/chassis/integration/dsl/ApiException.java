package io.github.renatolsjf.chassis.integration.dsl;

public class ApiException extends RuntimeException {

    protected ApiException() { super(); }
    protected ApiException(String message) { super(message); }
    protected ApiException(Throwable cause) { super(cause); }
    protected ApiException(String message, Throwable cause) { super(message, cause); }

}
