package com.github.renatolsjf.chassi.monitoring.timing;

import com.github.renatolsjf.chassi.context.Context;

import java.time.Duration;

public class TimedOperation<T> {

    public static final String HTTP_OPERATION = "Http";
    public static final String DATABASE_OPERATION = "Db";

    public interface Executable {
         Object execute();
    }

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

    public T execute(Executable e) {
        long l = System.currentTimeMillis();
        try {
            return (T) e.execute();
        } finally {
            this.executionTime = System.currentTimeMillis() - l;
            Context.forRequest().recordOperationTime(this.tag, this.executionTime);
        }
    }

    public long getExecutionTime() {
        return this.executionTime;
    }

    public Duration getDuration() {
        return Duration.ofMillis(this.executionTime);
    }

}
