package io.github.renatolsjf.chassis.loader;

import io.github.renatolsjf.chassis.util.CaseString;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface Loadable<T> {

    String key();
    T defaultValue();

    private List<String> path() {
        return Arrays.asList(this.key().split("\\."));
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
