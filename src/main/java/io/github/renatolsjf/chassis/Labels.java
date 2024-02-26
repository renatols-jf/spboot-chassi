package io.github.renatolsjf.chassis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Labels {

    public enum Field {

        LOGGING_TRANSACTION_ID("logging.transaction-id:transactionId"),
        LOGGING_CORRELATION_ID("logging.correlation-id:correlationId"),
        LOGGING_OPERATION("logging.operation:operation"),
        LOGGING_ELAPSED_TIME("logging.elapsed-time:elapsedTime"),
        LOGGING_OPERATION_TIME("logging.operation-times:operationTimes"),
        LOGGING_CONTEXT("logging.context:context"),

        METRICS_NAME_OPERATION_HEALTH("metrics.name.operation-health:operation_health"),
        METRICS_NAME_ACTIVE_OPERATIONS("metrics.name.active-operations:operation_active_requests"),
        METRICS_NAME_OPERATION_TIME("metrics.name.operation-time:operation_request_time"),

        METRICS_TAG_OPERATION("metrics.tag.operation:operation"),
        METRICS_TAG_OUTCOME("metrics.tag.outcome:outcome"),
        METRICS_TAG_TIMER_TYPE("metrics.tag.timer-type:timer_type");

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
            toReturn = this.doGet(m, p);
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

    //TODO this will need to be implemented somewhere else as other yaml stuff with have
    //the same issue.
    private Object doGet(Map<String, Object> m, String s) {
        Object o = m.get(s);
        if (o == null) {
            o = m.get(s.replaceAll("-", "_"));
        }

        if (o == null) {
            int idx;
            String camelCase = s;
            while ((idx = camelCase.indexOf("-")) != -1) {
                if (idx == 0) {
                    camelCase = camelCase.substring(1);
                } else if (idx == camelCase.length()) {
                    camelCase = camelCase.substring(0, camelCase.length() - 1);
                } else {
                    camelCase = camelCase.substring(0, idx) + camelCase.substring(idx + 1, idx + 2).toUpperCase() + camelCase.substring(idx + 2);
                }
            }
            o = m.get(camelCase);
        }

        return o;
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
