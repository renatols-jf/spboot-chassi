package io.github.renatolsjf.chassis.util.proxy;

public interface TypeEnhancer {

    Enhancement createEnhancement();
    boolean isEnhanceable(Class<?> type);

}
