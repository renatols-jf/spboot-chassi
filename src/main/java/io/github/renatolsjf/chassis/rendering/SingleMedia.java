package io.github.renatolsjf.chassis.rendering;

import io.github.renatolsjf.chassis.rendering.transforming.TransformingPath;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class SingleMedia extends Media {

    protected Map<String, Object> renderable = new LinkedHashMap<>();

    public SingleMedia(boolean printNull) {
        super(printNull);
    }

    @Override
    protected void doPrint(String name, Object content) {
        renderable.put(name, content);
    }

    @Override
    protected MediaContent getContent(TransformingPath transformingPath) {
        return MediaContent.createContent(Arrays.asList(this.renderable), transformingPath);
    }

    @Override
    public Object render() {
        return renderable;
    }
}
