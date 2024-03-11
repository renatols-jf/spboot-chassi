package io.github.renatolsjf.chassis.filtering;

public class InvalidSyntaxException extends Exception {

    public InvalidSyntaxException() {
        super();
    }

    public InvalidSyntaxException(String message) {
        super(message);
    }

    public InvalidSyntaxException(Throwable cause) {
        super(cause);
    }

    public InvalidSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

}
