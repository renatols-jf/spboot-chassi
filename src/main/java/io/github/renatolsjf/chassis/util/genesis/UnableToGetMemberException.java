package io.github.renatolsjf.chassis.util.genesis;

public class UnableToGetMemberException extends Exception {

    public UnableToGetMemberException() {}

    public UnableToGetMemberException(String message) {
        super(message);
    }

    public UnableToGetMemberException(Throwable cause) {
        super(cause);
    }

    public UnableToGetMemberException(String message, Throwable cause) {
        super(message, cause);
    }
}

