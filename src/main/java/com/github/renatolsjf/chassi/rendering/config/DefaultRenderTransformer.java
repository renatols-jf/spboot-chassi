package com.github.renatolsjf.chassi.rendering.config;

public class DefaultRenderTransformer implements RenderTransformer<Object, Object> {
    @Override
    public Object transform(Object value) {
        return value;
    }
}
