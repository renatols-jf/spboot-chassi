package com.github.renatolsjf.chassi;

import java.util.EnumMap;
import java.util.Map;

public class Configuration {

    public enum Properties {
        LOGGER_USE_CALLING_CLASS,
        LOGGER_PRINT_CONTEXT_AS_JSON,
        LOGGER_EXPLODE_ATTACHED_OBJECTS,
        LOGGER_ENABLE_DEFAULT_ATTRIBUTES_OVERWRITE,
        VALIDATOR_FAIL_ON_EXECUTION_ERROR,
        CONTEXT_FORBID_UNAUTHORIZED_CREATION,
        CONTEXT_ALLOW_CORRELATION_ID_UPDATE,
        METRIC_REQUEST_DURATION_HISTOGRAM_RANGES
    }

    Map<Properties, Object> configData = new EnumMap<>(Properties.class);

    public Boolean useCallingClassNameForLogging() {
        return (Boolean) this.configData
                .getOrDefault(Properties.LOGGER_USE_CALLING_CLASS, Boolean.TRUE);
    }

    public Boolean printLoggingContextAsJson() {
        return (Boolean) this.configData
                .getOrDefault(Properties.LOGGER_PRINT_CONTEXT_AS_JSON, Boolean.TRUE);
    }

    //Não habilitar, ainda não trata coleções
    public Boolean explodeLoggingAttachedObjects() {
        return (Boolean) this.configData
                .getOrDefault(Properties.LOGGER_EXPLODE_ATTACHED_OBJECTS, Boolean.FALSE);
    }

    public Boolean allowDefaultLoggingAttributesOverride() {
        return (Boolean) this.configData
                .getOrDefault(Properties.LOGGER_ENABLE_DEFAULT_ATTRIBUTES_OVERWRITE, Boolean.TRUE);
    }

    public Boolean validatorFailOnExecutionError() {
        return (Boolean) this.configData
                .getOrDefault(Properties.VALIDATOR_FAIL_ON_EXECUTION_ERROR, Boolean.TRUE);
    }

    public Boolean forbidUnauthorizedContextCreation() {
        return (Boolean) this.configData
                .getOrDefault(Properties.CONTEXT_FORBID_UNAUTHORIZED_CREATION, Boolean.TRUE);
    }

    public Boolean allowContextCorrelationIdUpdate() {
        return (Boolean) this.configData
                .getOrDefault(Properties.CONTEXT_ALLOW_CORRELATION_ID_UPDATE, Boolean.TRUE);
    }

    public double[] monitoringRequestDurationRanges() {
        return (double[]) this.configData
                .getOrDefault(Properties.METRIC_REQUEST_DURATION_HISTOGRAM_RANGES, new double[]{.2d, .5d, 1d, 2d, 5d, 10d});
    }

}
