package io.github.renatolsjf.chassis.util.conversion;

public interface Converter<T1, T2> {
    T2 convert(T1 value);
}
