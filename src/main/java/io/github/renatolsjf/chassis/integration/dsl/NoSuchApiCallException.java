package io.github.renatolsjf.chassis.integration.dsl;

public class NoSuchApiCallException extends RuntimeException {

    public NoSuchApiCallException() {
        super();
    }

    public NoSuchApiCallException(final String message) {
        super(message);
    }

    public NoSuchApiCallException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NoSuchApiCallException(final Throwable cause) {
        super(cause);
    }

}
