package io.github.renatolsjf.chassis.context;

public interface ExecutionContext {
    String getTraceId();
    String getSpanId();
    default boolean isExecutionContextAvailable() {
        String s = this.getSpanId();
        return s != null && !s.isBlank();
    }

    static ExecutionContext unavailable() {
        return new ExecutionContext() {
            @Override
            public String getTraceId() {
                return null;
            }

            @Override
            public String getSpanId() {
                return null;
            }
        };
    }

}
