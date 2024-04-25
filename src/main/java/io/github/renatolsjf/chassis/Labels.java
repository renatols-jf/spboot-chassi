package io.github.renatolsjf.chassis;

import io.github.renatolsjf.chassis.loader.Loadable;
import io.github.renatolsjf.chassis.monitoring.timing.TimedOperation;
import io.github.renatolsjf.chassis.util.CaseString;

import java.util.*;

public class Labels {

    private static final String UNKNOWN_APP_NAME = "unknown_app" ;

    public enum FieldType {
        APPLICATION("application"),
        LOGGING("logging"),
        METRICS_NAME("metrics.name"),
        METRICS_TAG("metrics.tag"),
        METRICS_TAG_VALUE("metrics.tag.value");

        private String description;

        FieldType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }

        @Override
        public String toString() {
            return this.getDescription();
        }


    }

    public enum Field implements Loadable<String> {

        APPLICATION_NAME(FieldType.APPLICATION + ".name", UNKNOWN_APP_NAME),
        APPLICATION_INSTANCE_ID(FieldType.APPLICATION + ".instance-id", UUID.randomUUID().toString()),

        LOGGING_APPLICATION_NAME(FieldType.LOGGING + ".application-name", "application"),
        LOGGING_TRANSACTION_ID(FieldType.LOGGING + ".transaction-id", "transactionId"),
        LOGGING_CORRELATION_ID(FieldType.LOGGING + ".correlation-id", "correlationId"),
        LOGGING_OPERATION(FieldType.LOGGING + ".operation", "operation"),
        LOGGING_ELAPSED_TIME(FieldType.LOGGING + ".elapsed-time", "elapsedTime"),
        LOGGING_OPERATION_TIME(FieldType.LOGGING + ".operation-times", "operationTimes"),
        LOGGING_CONTEXT(FieldType.LOGGING + ".context", "context"),

        METRICS_NAME_OPERATION_HEALTH(FieldType.METRICS_NAME + ".operation-health", "operation_health"),
        METRICS_NAME_INTEGRATION_HEALTH(FieldType.METRICS_NAME + ".integration-health", "integration_health"),
        METRICS_NAME_ACTIVE_OPERATIONS(FieldType.METRICS_NAME + ".active-operations", "operation_active_requests"),
        METRICS_NAME_OPERATION_TIME(FieldType.METRICS_NAME + ".operation-time", "operation_request_time"),
        METRICS_NAME_INTEGRATION_TIME(FieldType.METRICS_NAME + ".integration-time", "integration_request_time"),

        METRICS_TAG_APPLICATION_NAME(FieldType.METRICS_TAG + ".application-name", "application"),
        METRICS_TAG_INSTANCE_ID(FieldType.METRICS_TAG + ".instance-id", "instance_id"),
        METRICS_TAG_OPERATION(FieldType.METRICS_TAG + ".operation", "operation"),
        METRICS_TAG_OUTCOME(FieldType.METRICS_TAG + ".outcome", "outcome"),
        METRICS_TAG_TIMER_TYPE(FieldType.METRICS_TAG + ".timer-type", "timer_type"),
        METRICS_TAG_SERVICE(FieldType.METRICS_TAG + ".service", "service"),
        METRICS_TAG_GROUP(FieldType.METRICS_TAG + ".group", "group"),
        METRICS_TAG_TYPE(FieldType.METRICS_TAG + ".type", "type"),
        METRICS_TAG_VALUE_HTTP_TYPE(FieldType.METRICS_TAG_VALUE + ".http-type", TimedOperation.HTTP_OPERATION);

        private String keyValue;
        private String defaultLabel;
        private String label;

        Field(String keyValue, String defaultLabel) {
            this.keyValue = keyValue;
            this.defaultLabel = defaultLabel;
        }

        @Override
        public String key() {
            return this.keyValue;
        }

        @Override
        public String defaultValue() {
            return this.defaultLabel;
        }

        @Override
        public String value() {
            return this.label;
        }

        @Override
        public void setValue(String value) {
            this.label = value;
        }

        public static Field fromFieldType(FieldType fieldType, String suffix) {
            return Arrays.stream(Field.values())
                    .filter(f -> f.key().equalsIgnoreCase(fieldType + "." + suffix))
                    .findFirst()
                    .orElse(null);
        }

    }

    private Map<String, Object> labelData;

    Labels(Map<String, Object> labelData) {
        this.labelData = labelData;
    }

    public boolean isAppNameAvailable() {
        return !(UNKNOWN_APP_NAME.equalsIgnoreCase(this.getLabel(Field.APPLICATION_NAME)));
    }

    public String getLabel(Field field) {
        return field.initializeIfNeededAndGet(this.labelData);
    }

    public String getLabel(FieldType fieldType, String suffix) {
        return this.getLabel(fieldType, suffix, suffix);
    }

    public String getLabel(FieldType fieldType, String suffix, String defaultValue) {
        if (fieldType == null || suffix == null || suffix.isBlank()) {
            return defaultValue;
        }
        Field f = Field.fromFieldType(fieldType, suffix);
        return f != null
                ? this.getLabel(f)
                : defaultValue;
    }

}

