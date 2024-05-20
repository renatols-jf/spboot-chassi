package io.github.renatolsjf.chassis.integration;

public class IOErrorOperationException extends OperationException {

    public IOErrorOperationException() { super(); }
    public IOErrorOperationException(String message) { super(message); }
    public IOErrorOperationException(Throwable cause) { super(cause); }
    public IOErrorOperationException(String message, Throwable cause) { super(message, cause); }

}
