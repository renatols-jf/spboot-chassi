package io.github.renatolsjf.chassis.integration;

public class ResponseParsingException extends OperationException {

    public ResponseParsingException() { super(); }
    public ResponseParsingException(String message) { super(message); }
    public ResponseParsingException(Throwable cause) { super(cause); }
    public ResponseParsingException(String message, Throwable cause) { super(message, cause); }

}
