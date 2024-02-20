package io.github.renatolsjf.chassi.context;

import io.github.renatolsjf.chassi.Chassi;
import io.github.renatolsjf.chassi.context.data.Classified;
import io.github.renatolsjf.chassi.context.data.cypher.ClassifiedCypher;
import io.vavr.control.Try;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class LogRecord {

    enum Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    private static final Class[] CLASSES_NOT_TO_EXPLODE = {String.class, Number.class, Void.class, Enum.class};

    private final String message;
    private final Object[] messageData;
    private final Level level;
    private final ApplicationLogger logger;
    private Map<String, Object> recordData = new HashMap<>();

    LogRecord(String message, Level level, ApplicationLogger logger, Object... messageData) {
        this.message = message;
        this.level = level;
        this.logger = logger;
        this.messageData = messageData;
    }

    public LogRecord attach(String key, Object value) {
        return this.attach(key, value, this.recordData);
    }

    private LogRecord attach(String key, Object value, Map<String, Object> container) {
        container.put(key, value);
        return this;
    }

    public LogRecord attachMap(final Map<String, Object> m, String... fieldsToPrint) {

        if (m == null || m.isEmpty()) {
            return this;
        }

        List<String> toPrint;
        if (fieldsToPrint == null) {
            toPrint = Collections.emptyList();
        } else {
            toPrint = Arrays.asList(fieldsToPrint);
        }

        for (Map.Entry<String, Object> e: m.entrySet()) {
            if (toPrint.isEmpty() || toPrint.contains(e.getKey())) {
                this.attach(e.getKey(), e.getValue());
            }
        }

        return this;

    }

    public LogRecord attachObject(final Object value, String... fieldToPrint) {
        return this.attachObject(value, this.recordData,
                Chassi.getInstance().getConfig().explodeLoggingAttachedObjects(), fieldToPrint);
    }

    private LogRecord attachObject(final Object value, Map<String, Object> container,
                                   boolean explodeInnerObjects, String... fieldToPrint) {

        List<String> toPrint;
        if (fieldToPrint == null) {
            toPrint = Collections.emptyList();
        } else {
            toPrint = Arrays.asList(fieldToPrint);
        }

        Try.run(() -> {
            List<Field> fields = this.getFields(value.getClass());
            for (Field f: fields) {

                if (Modifier.isStatic(f.getModifiers())) {
                    return;
                }

                String k = (f.getName());
                if (!(toPrint.isEmpty()) && !(toPrint.contains(k))) {
                    continue;
                }

                Object v;
                if (f.trySetAccessible()) {

                    v = f.get(value);
                    Classified c = f.getDeclaredAnnotation(Classified.class);
                    if (c != null) {
                        v = ClassifiedCypher.createCypher(c.value()).encrypt(v);
                        if (v == null) {
                            continue;
                        }
                    }

                } else {
                    v = "Value not accessible";
                }

                if (explodeInnerObjects) {

                    boolean shouldExplode = true;
                    for (Class<?> clazz: CLASSES_NOT_TO_EXPLODE) {
                        if (clazz.isAssignableFrom(v.getClass())) {
                            shouldExplode = false;
                            break;
                        }
                    }

                    if (shouldExplode) {
                        Map<String, Object> newContainer = new HashMap<>();
                        this.attachObject(v, newContainer, true);
                        v = newContainer;
                    }

                }

                this.attach(k, v, container);

            }
        });

        return this;
    }

    public void log() {
        switch (this.level) {

            case TRACE:
                this.logger.trace(this.message, this.recordData, this.messageData);
                break;

            case DEBUG:
                this.logger.debug(this.message, this.recordData, this.messageData);
                break;

            case INFO:
                this.logger.info(this.message, this.recordData, this.messageData);
                break;

            case WARN:
                this.logger.warn(this.message, this.recordData, this.messageData);
                break;

            case ERROR:
                this.logger.error(this.message, this.recordData, this.messageData);
                break;

        }
    }

    private List<Field> getFields(Class clazz) {
        List<Field> fields = new ArrayList<>();
        Class c = clazz;
        while (c != null && c != Object.class) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
            c = c.getSuperclass();
        }
        return fields;
    }

}
