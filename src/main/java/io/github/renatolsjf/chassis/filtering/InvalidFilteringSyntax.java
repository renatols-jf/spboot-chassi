package io.github.renatolsjf.chassis.filtering;

public class InvalidFilteringSyntax extends Exception {

    public InvalidFilteringSyntax() {
        super();
    }

    public InvalidFilteringSyntax(String message) {
        super(message);
    }

    public InvalidFilteringSyntax(Throwable cause) {
        super(cause);
    }

    public  InvalidFilteringSyntax(String message, Throwable cause) {
        super(message, cause);
    }

}
