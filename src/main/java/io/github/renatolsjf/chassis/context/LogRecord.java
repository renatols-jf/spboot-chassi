package io.github.renatolsjf.chassis.context;

import io.github.renatolsjf.chassis.Chassis;
import io.github.renatolsjf.chassis.context.data.Classified;
import io.github.renatolsjf.chassis.context.data.cypher.ClassifiedCypher;
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

    private final ApplicationLogger logger;
    private Map<String, Object> recordData = new HashMap<>();

    LogRecord(ApplicationLogger logger) {
        this.logger = logger;
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
                /*Chassis.getInstance().getConfig().explodeLoggingAttachedObjects()*/ false, fieldToPrint);
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

    public void trace(String message, Object... args) {
        this.logger.trace(message, this.recordData, args);
    }

    public void info(String message, Object... args) {
        this.logger.info(message, this.recordData, args);
    }

    public void debug(String message, Object... args) {
        this.logger.debug(message, this.recordData, args);
    }

    public void warn(String message, Object... args) {
        this.logger.warn(message, this.recordData, args);
    }

    public void error(String message, Object... args) {
        this.logger.error(message, this.recordData, args);
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
