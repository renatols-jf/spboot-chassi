package io.github.renatolsjf.chassis.monitoring;

public class InvalidMetricException extends RuntimeException {

    private double value;

    public InvalidMetricException(String message) {
        super(message);
    }

    public InvalidMetricException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidMetricException(Throwable cause) {
        super(cause);
    }

    public InvalidMetricException(String message, double value) {
        this(message);
        this.value = value;
    }

}
