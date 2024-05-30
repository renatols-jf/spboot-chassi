package io.github.renatolsjf.chassis.monitoring.tracing;

public enum TracingStrategy {
    ALWAYS_SAMPLE,
    NEVER_SAMPLE,
    RATIO_BASED_SAMPLE,
    PARENT_BASED_OR_ALWAYS_SAMPLE,
    PARENT_BASED_OR_NEVER_SAMPLE,
    PARENT_BASED_OR_RATIO_BASED_SAMPLE
}
