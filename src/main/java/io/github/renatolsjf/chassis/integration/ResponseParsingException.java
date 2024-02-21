package io.github.renatolsjf.chassis.integration;

public class ResponseParsingException extends OperationException {

    protected ResponseParsingException() { super(); }
    protected ResponseParsingException(String message) { super(message); }
    protected ResponseParsingException(Throwable cause) { super(cause); }
    protected ResponseParsingException(String message, Throwable cause) { super(message, cause); }

}
