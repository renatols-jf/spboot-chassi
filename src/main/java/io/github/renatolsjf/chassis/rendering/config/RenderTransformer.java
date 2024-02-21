package io.github.renatolsjf.chassis.rendering.config;

public interface RenderTransformer<T, U> {
    T transform(U value);
}
