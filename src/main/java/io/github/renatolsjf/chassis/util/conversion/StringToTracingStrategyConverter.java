package io.github.renatolsjf.chassis.util.conversion;

import io.github.renatolsjf.chassis.monitoring.tracing.TracingStrategy;

public class StringToTracingStrategyConverter implements Converter<String, TracingStrategy> {

    @Override
    public TracingStrategy convert(String value) {
        try {
            return TracingStrategy.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

}
