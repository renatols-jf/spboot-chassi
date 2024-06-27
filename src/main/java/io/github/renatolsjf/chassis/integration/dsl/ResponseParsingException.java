package io.github.renatolsjf.chassis.integration.dsl;

public class ResponseParsingException extends ApiException {

    public ResponseParsingException() { super(); }
    public ResponseParsingException(String message) { super(message); }
    public ResponseParsingException(Throwable cause) { super(cause); }
    public ResponseParsingException(String message, Throwable cause) { super(message, cause); }

}
