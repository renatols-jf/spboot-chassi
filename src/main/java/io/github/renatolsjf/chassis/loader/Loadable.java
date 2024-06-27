package io.github.renatolsjf.chassis.loader;

import io.github.renatolsjf.chassis.util.conversion.ConversionFactory;
import io.github.renatolsjf.utils.string.casestring.CaseString;

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

    default T initializeAndGet(Map<String, Object> m, Class<? extends T> expectedClass) {
        T v = this.getValueOrDefault(m, expectedClass);
        this.setValue(v);
        return v;
    }

    default T initializeIfNeededAndGet(Map<String, Object> m, Class<? extends T> expectedClass) {

        T v = value();
        if (v != null) {
            return v;
        }

        return this.initializeAndGet(m, expectedClass);

    }

    default T resolvedValue() {
        T v = this.value();
        return v != null
                ? v
                : this.defaultValue();
    }

    default T getValueOrDefault(Map<String, Object> m, Class<? extends T> expectedClass) {

        T dValue = this.defaultValue();

        if (m == null || m.isEmpty()) {
            return dValue;
        }

        Object toReturn = this.defaultValue();
        for (String p: this.path()) {
            toReturn = CaseString.parse(p).createMapExtractor().extractValue(m, false);
            if (toReturn == null) {
                break;
            } else if ((toReturn instanceof Map)) {
                m = (Map) toReturn;
            } else {
                break;
            }
        }

        if (toReturn != null) {

            if (dValue != null) {
                T convertedValue = (T) ConversionFactory.converter(toReturn.getClass(), expectedClass).convert(toReturn);
                return convertedValue != null
                        ? convertedValue
                        : dValue;
            }

            return (T) toReturn;
        } else {
            return dValue;
        }
    }

}
