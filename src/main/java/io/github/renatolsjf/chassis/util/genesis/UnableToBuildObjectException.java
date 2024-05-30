package io.github.renatolsjf.chassis.util.genesis;

public class UnableToBuildObjectException extends Exception {

    public UnableToBuildObjectException() {}

    public UnableToBuildObjectException(String message) {
        super(message);
    }

    public UnableToBuildObjectException(Throwable cause) {
        super(cause);
    }

    public UnableToBuildObjectException(String message, Throwable cause) {
        super(message, cause);
    }

}
