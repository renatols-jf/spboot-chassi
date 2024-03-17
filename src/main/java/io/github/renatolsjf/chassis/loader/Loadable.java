package io.github.renatolsjf.chassis.loader;

public interface Loadable<T> {

    String key();
    T defaultValue();

}
