package io.github.renatolsjf.chassis;

import io.github.renatolsjf.chassis.monitoring.timing.TimedOperation;
import io.github.renatolsjf.chassis.util.CaseString;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Labels {

    public enum FieldType {
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

    public enum Field {

        LOGGING_TRANSACTION_ID(FieldType.LOGGING + ".transaction-id:transactionId"),
        LOGGING_CORRELATION_ID(FieldType.LOGGING + ".correlation-id:correlationId"),
        LOGGING_OPERATION(FieldType.LOGGING + ".operation:operation"),
        LOGGING_ELAPSED_TIME(FieldType.LOGGING + ".elapsed-time:elapsedTime"),
        LOGGING_OPERATION_TIME(FieldType.LOGGING + ".operation-times:operationTimes"),
        LOGGING_CONTEXT(FieldType.LOGGING + ".context:context"),

        METRICS_NAME_OPERATION_HEALTH(FieldType.METRICS_NAME + ".operation-health:operation_health"),
        METRICS_NAME_INTEGRATION_HEALTH(FieldType.METRICS_NAME + ".integration-health:integration_health"),
        METRICS_NAME_ACTIVE_OPERATIONS(FieldType.METRICS_NAME + ".active-operations:operation_active_requests"),
        METRICS_NAME_OPERATION_TIME(FieldType.METRICS_NAME + ".operation-time:operation_request_time"),
        METRICS_NAME_INTEGRATION_TIME(FieldType.METRICS_NAME + ".integration-time:integration_request_time"),

        METRICS_TAG_OPERATION(FieldType.METRICS_TAG + ".operation:operation"),
        METRICS_TAG_OUTCOME(FieldType.METRICS_TAG + ".outcome:outcome"),
        METRICS_TAG_TIMER_TYPE(FieldType.METRICS_TAG + ".timer-type:timer_type"),
        METRICS_TAG_SERVICE(FieldType.METRICS_TAG + ".service:service"),
        METRICS_TAG_GROUP(FieldType.METRICS_TAG + ".group:group"),
        METRICS_TAG_TYPE(FieldType.METRICS_TAG + ".type:type"),
        METRICS_TAG_VALUE_HTTP_TYPE(FieldType.METRICS_TAG_VALUE + ".http-type:" + TimedOperation.HTTP_OPERATION);

        private String key;
        private String label;

        Field(String key) {
            this.key = key;
        }

        String getKey() {
            return this.key;
        }

        void setLabel(String label) {
            this.label = label;
        }

        String getLabel() {
            return this.label;
        }

        public static Field fromFieldType(FieldType fieldType, String suffix) {
            return Arrays.stream(Field.values())
                    .filter(f -> f.getKey().substring(0, f.getKey().indexOf(":")).equalsIgnoreCase(fieldType + "." + suffix))
                    .findFirst()
                    .orElse(null);
        }

    }

    private Map<String, Object> labelData;

    Labels(Map<String, Object> labelData) {
        this.labelData = labelData;
    }

    public String getLabel(Field field) {
        if (field.getLabel() == null) {
            field.setLabel(this.getLabelOrDefault(field.getKey()));
        }
        return field.getLabel();
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

    private String getLabelOrDefault(String key) {
        DecodedKey dk = this.decode(key);
        return this.getLabelOrDefault(dk.path, dk.defaultLabel);
    }

    private String getLabelOrDefault(List<String> path, String defaultLabel) {

        if (this.labelData == null) {
            return defaultLabel;
        }

        Map<String, Object> m = this.labelData;
        Object toReturn = defaultLabel;
        for (String p: path) {
            toReturn = CaseString.parse(p).getMapValue(m);
            if (toReturn == null) {
                break;
            } else if ((toReturn instanceof Map)) {
                m = (Map) toReturn;
            } else {
                break;
            }
        }

        if (toReturn != null) {
            return toReturn.toString();
        } else {
            return defaultLabel;
        }

    }

    private DecodedKey decode(String toDecode) {
        String[] parts = toDecode.split(":");
        DecodedKey dk = new DecodedKey();
        dk.path = this.resolvePath(parts);
        dk.defaultLabel = this.resolveDefaultLabel(parts);
        return dk;
    }

    private List<String> resolvePath(String[] parts) {
        if (parts == null) {
            return null;
        }
        return Arrays.asList(parts[0].split("\\."));
    }

    private String resolveDefaultLabel(String[] parts) {
        if (parts == null || parts.length < 2) {
            return null;
        }
        return String.join(":", Arrays.asList(parts).subList(1, parts.length));
    }




}

class DecodedKey {
    List<String> path;
    String defaultLabel;
}
