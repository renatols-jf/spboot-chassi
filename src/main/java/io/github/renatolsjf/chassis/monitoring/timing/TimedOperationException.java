package io.github.renatolsjf.chassis.monitoring.timing;

public class TimedOperationException extends RuntimeException {

    public TimedOperationException(Throwable cause) {
        super(cause);
    }

    public TimedOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}
