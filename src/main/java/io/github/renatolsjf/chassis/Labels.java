package io.github.renatolsjf.chassis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Labels {

    public enum Field {

        LOGGING_TRANSACTION_ID("logging.transactionId:transactionId"),
        LOGGING_CORRELATION_ID("logging.correlationId:correlationId"),
        LOGGING_OPERATION("logging.operation:operation"),
        LOGGING_ELAPSED_TIME("logging.elapsedTime:elapsedTime"),
        LOGGING_OPERATION_TIME("logging.operationTimes:operationTimes"),
        LOGGING_CONTEXT("logging.context:context"),

        METRICS_NAME_OPERATION_HEALTH("metrics.name.operationHealth:operation_health"),
        METRICS_NAME_ACTIVE_OPERATIONS("metrics.name.activeOperations:operation_active_requests"),
        METRICS_NAME_OPERATION_TIME("metrics.name.operationTime:operation_request_time"),

        METRICS_TAG_OPERATION("metrics.tag.operation:operation");

        private String key;

        Field(String key) {
            this.key = key;
        }

        String getKey() {
            return this.key;
        }

    }

    private Map<String, Object> labelData = new HashMap<>();

    Labels(Map<String, Object> labelData) {
        this.labelData = labelData;
    }

    public String getLabel(Field field) {
        return this.getLabelOrDefault(field.getKey());
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
            toReturn = m.get(p);
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
