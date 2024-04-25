package io.github.renatolsjf.chassis.loader;

import io.github.renatolsjf.chassis.util.CaseString;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface Loadable<T> {


    String key();
    T defaultValue();
    T value();
    void setValue(T value);

    private List<String> path() {
        return Arrays.asList(this.key().split("\\."));
    }

    default T initializeAndGet(Map<String, Object> m) {
        T v = this.getValueOrDefault(m);
        this.setValue(v);
        return v;
    }

    default T initializeIfNeededAndGet(Map<String, Object> m) {

        T v = value();
        if (v != null) {
            return v;
        }

        return this.initializeAndGet(m);

    }

    default T resolvedValue() {
        T v = this.value();
        return v != null
                ? v
                : this.defaultValue();
    }

    default T getValueOrDefault(Map<String, Object> m) {

        if (m == null || m.isEmpty()) {
            return this.defaultValue();
        }

        Object toReturn = this.defaultValue();
        for (String p: this.path()) {
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
            return (T) toReturn;
        } else {
            return this.defaultValue();
        }
    }

}
