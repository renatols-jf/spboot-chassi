package io.github.renatolsjf.chassi.context;

public class InvalidContextStateException extends RuntimeException {

    protected InvalidContextStateException() { super(); }
    protected InvalidContextStateException(String message) { super(message); }
    protected InvalidContextStateException(Throwable cause) { super(cause); }
    protected InvalidContextStateException(String message, Throwable cause) { super(message, cause); }

}
