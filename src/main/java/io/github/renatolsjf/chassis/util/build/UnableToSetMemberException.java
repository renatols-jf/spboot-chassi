package io.github.renatolsjf.chassis.util.build;

public class UnableToSetMemberException extends Exception {

    public UnableToSetMemberException() {}

    public UnableToSetMemberException(String message) {
        super(message);
    }

    public UnableToSetMemberException(Throwable cause) {
        super(cause);
    }

    public UnableToSetMemberException(String message, Throwable cause) {
        super(message, cause);
    }
}
