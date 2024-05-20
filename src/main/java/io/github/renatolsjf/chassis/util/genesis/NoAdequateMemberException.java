package io.github.renatolsjf.chassis.util.genesis;

public class NoAdequateMemberException extends Exception {

    public NoAdequateMemberException() {}

    public NoAdequateMemberException(String message) {
        super(message);
    }

    public NoAdequateMemberException(Throwable cause) {
        super(cause);
    }

    public NoAdequateMemberException(String message, Throwable cause) {
        super(message, cause);
    }
}
