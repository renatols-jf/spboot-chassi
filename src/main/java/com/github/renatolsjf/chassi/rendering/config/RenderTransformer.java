package com.github.renatolsjf.chassi.rendering.config;

public interface RenderTransformer<T, U> {
    T transform(U value);
}
