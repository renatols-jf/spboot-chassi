package io.github.renatolsjf.chassis;

import io.github.renatolsjf.chassis.loader.Loadable;

import java.time.Duration;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

    public enum Properties implements Loadable<Object> {
        LOGGER_USE_CALLING_CLASS("logging.use-calling-class", Boolean.TRUE),
        LOGGER_PRINT_CONTEXT_AS_JSON("logging.print-context-as-json", Boolean.TRUE),
        //LOGGER_EXPLODE_ATTACHED_OBJECTS("logging.explode-attached-objects", Boolean.FALSE),
        LOGGER_ENABLE_DEFAULT_ATTRIBUTES_OVERWRITE("logging.enable-default-attributes-overwrite", Boolean.FALSE),
        VALIDATOR_FAIL_ON_EXECUTION_ERROR("validation.fail-on-execution-error", Boolean.TRUE),
        CONTEXT_FORBID_UNAUTHORIZED_CREATION("context.forbid-unauthorized-creation", Boolean.TRUE),
        CONTEXT_ALLOW_CORRELATION_ID_UPDATE("context.allow-correlation-id-update", Boolean.TRUE),
        METRIC_REQUEST_DURATION_HISTOGRAM_RANGES("metrics.request.duration.histogram-range", new double[]{200, 500, 1000, 2000, 5000, 10000}),
        METRIC_REQUEST_DURATION_EXPORT_BY_TYPE("metrics.request.duration.export-by-type", Boolean.TRUE),
        HEALTH_TIME_WINDOW_DURATION("metrics.health-window-duration-minutes", 5);

        private String keyValue;
        private Object defaultValue;
        private Object value;

        Properties(String keyValue, Object defaultValue) {
            this.keyValue = keyValue;
            this.defaultValue = defaultValue;
        }

        @Override
        public String key() {
            return this.keyValue;
        }

        @Override
        public Object defaultValue() {
            return this.defaultValue;
        }

        @Override
        public Object value() {
            return this.value;
        }

        @Override
        public void setValue(Object value) {
            this.value = value;
        }

    }

    Map<String, Object> configData;

    Configuration(Map<String, Object> configData) {
        this.configData = configData;
    }

    public Boolean useCallingClassNameForLogging() {
        return (Boolean) Properties.LOGGER_USE_CALLING_CLASS.initializeIfNeededAndGet(this.configData, Boolean.class);
    }

    public Boolean printLoggingContextAsJson() {
        return (Boolean) Properties.LOGGER_PRINT_CONTEXT_AS_JSON.initializeIfNeededAndGet(this.configData, Boolean.class);
    }

    public Boolean allowDefaultLoggingAttributesOverride() {
        return (Boolean) Properties.LOGGER_ENABLE_DEFAULT_ATTRIBUTES_OVERWRITE.initializeIfNeededAndGet(this.configData, Boolean.class);
    }

    public Boolean validatorFailOnExecutionError() {
        return (Boolean) Properties.VALIDATOR_FAIL_ON_EXECUTION_ERROR.initializeIfNeededAndGet(this.configData, Boolean.class);
    }

    public Boolean forbidUnauthorizedContextCreation() {
        return (Boolean) Properties.CONTEXT_FORBID_UNAUTHORIZED_CREATION.initializeIfNeededAndGet(this.configData, Boolean.class);
    }

    public Boolean allowContextCorrelationIdUpdate() {
        return (Boolean) Properties.CONTEXT_ALLOW_CORRELATION_ID_UPDATE.initializeIfNeededAndGet(this.configData, Boolean.class);
    }

    public double[] monitoringRequestDurationRanges() {
        return (double[]) Properties.METRIC_REQUEST_DURATION_HISTOGRAM_RANGES.initializeIfNeededAndGet(this.configData, double[].class);
    }

    public Boolean exportRequestDurationMetricByType() {
        return (Boolean) Properties.METRIC_REQUEST_DURATION_EXPORT_BY_TYPE.initializeIfNeededAndGet(this.configData, Boolean.class);
    }

    public Duration healthTimeWindowDuration() {
        return Duration.ofMinutes((Integer) Properties.HEALTH_TIME_WINDOW_DURATION.initializeIfNeededAndGet(this.configData, Integer.class));
    }

}
