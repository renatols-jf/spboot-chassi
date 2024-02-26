package io.github.renatolsjf.chassis.context;

import io.github.renatolsjf.chassis.Chassis;
import io.github.renatolsjf.chassis.Labels;
import io.github.renatolsjf.chassis.context.data.LoggingAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;


public class ApplicationLogger {

    private static ObjectWriter objectWriter = new ObjectMapper().writer();

    private final Logger logger;
    private Map<String, LoggingAttribute> fixedAttributes;

    public ApplicationLogger(Map<String, LoggingAttribute> fixedAttributes) {
        this(ApplicationLogger.class, fixedAttributes);
    }

    public ApplicationLogger(Class<?> clazz, Map<String, LoggingAttribute> fixedAttributes) {
        this.logger = LoggerFactory.getLogger(clazz.getCanonicalName());
        this.fixedAttributes = fixedAttributes;
    }

    public LogRecord info(String message, Object... args) {
        return new LogRecord(message, LogRecord.Level.INFO, this, args);
    }

    void info(String message, Map<String, Object> data, Object... args) {
        this.prepareMDC(data);
        this.logger.info(message, args);
        this.clearMDC();
    }

    public LogRecord warn(String message, Object... args) {
        return new LogRecord(message, LogRecord.Level.WARN, this, args);
    }

    void warn(String message, Map<String, Object> data, Object... args) {
        this.prepareMDC(data);
        this.logger.warn(message, args);
        this.clearMDC();
    }

    public LogRecord error(String message, Object... args) {
        return new LogRecord(message, LogRecord.Level.ERROR, this, args);
    }

    void error(String message, Map<String, Object> data, Object... args) {
        this.prepareMDC(data);
        this.logger.error(message, args);
        this.clearMDC();
    }

    public LogRecord debug(String message, Object... args) {
        return new LogRecord(message, LogRecord.Level.DEBUG, this, args);
    }

    void debug(String message, Map<String, Object> data, Object... args) {
        this.prepareMDC(data);
        this.logger.debug(message, args);
        this.clearMDC();
    }

    public LogRecord trace(String message, Object... args) {
        return new LogRecord(message, LogRecord.Level.TRACE, this, args);
    }


    public void trace(String message, Map<String, Object> data, Object... args){
        this.prepareMDC(data);
        this.logger.trace(message, args);
        this.clearMDC();
    }

    private void prepareMDC(Map<String, Object> data) {

        if (this.fixedAttributes != null) {
            for (Map.Entry<String, LoggingAttribute> e: this.fixedAttributes.entrySet()) {
                String v = e.getValue().value();
                if (v != null) {
                    MDC.put(e.getKey(), v);
                }
            }
        }

        if (data == null) {
            return;
        }

        Map<String, Object> extraFields = new HashMap<>();

        data.forEach((k,v) -> {

            Object value = v != null
                    ? v
                    : "null / empty";

            if (k != null && !(k.isBlank())) {
                extraFields.put(k, value);
            }

        });

        String contextString;
        if (Chassis.getInstance().getConfig().printLoggingContextAsJson()) {
            try {
                contextString = objectWriter.writeValueAsString(extraFields);
            } catch (JsonProcessingException e) {
                contextString = extraFields.toString();
            }
        } else {
            contextString = extraFields.toString();
        }

        MDC.put(Chassis.getInstance().labels().getLabel(Labels.Field.LOGGING_CONTEXT), contextString);

    }

    private void clearMDC() {
        MDC.clear();
    }

}
