package io.github.renatolsjf.chassis.monitoring.timing;

import io.github.renatolsjf.chassis.context.Context;

import java.time.Duration;
import java.util.concurrent.Callable;

public class TimedOperation<T> {

    public static final String HTTP_OPERATION = "http";
    public static final String DATABASE_OPERATION = "db";

    private final String tag;
    private long executionTime;

    public static <Q> TimedOperation<Q> http() {
        return new TimedOperation<>(HTTP_OPERATION);
    }

    public static <Q> TimedOperation<Q> db() {
        return new TimedOperation<>(DATABASE_OPERATION);
    }

    public TimedOperation(String tag) {
        this.tag = tag;
    }

    public void run(Runnable r) {
        long l = System.currentTimeMillis();
        try {
            r.run();
        } finally {
            this.executionTime = System.currentTimeMillis() - l;
            Context.forRequest().recordOperationTime(this.tag, this.executionTime);
        }
    }

    public T execute(Callable<T> e) {
        long l = System.currentTimeMillis();
        T t = null;
        try {
            t = e.call();
            return t;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new TimedOperationException(ex);
        } finally {
            this.executionTime = System.currentTimeMillis() - l;
            Context.forRequest().recordOperationTime(this.tag, this.executionTime);
            if (t instanceof TimeSensitive ts) {
                ts.setDurationInMilliseconds(this.executionTime);
            }
        }
    }

    public long getExecutionTimeInMillis() {
        return this.executionTime;
    }
    public double getExecutionTimeInSeconds() {
        return this.getExecutionTimeInMillis() / 1000d;
    }

    public Duration getDuration() {
        return Duration.ofMillis(this.getExecutionTimeInMillis());
    }

}
