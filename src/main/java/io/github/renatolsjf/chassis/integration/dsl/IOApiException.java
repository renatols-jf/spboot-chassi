package io.github.renatolsjf.chassis.integration.dsl;

public class IOApiException extends ApiException {

    public IOApiException() { super(); }
    public IOApiException(String message) { super(message); }
    public IOApiException(Throwable cause) { super(cause); }
    public IOApiException(String message, Throwable cause) { super(message, cause); }

}
