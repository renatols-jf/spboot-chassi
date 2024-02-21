package io.github.renatolsjf.chassis.rendering;

import io.github.renatolsjf.chassis.rendering.transforming.TransformingPath;

import java.util.*;

public class CollectionMedia extends SingleMedia {

    private List<Map<String, Object>> renderables = new ArrayList<>();

    public CollectionMedia(boolean printNull) {
        super(printNull);
    }

    @Override
    protected MediaContent getContent(TransformingPath transformingPath) {
        return MediaContent.createContent(this.renderables, transformingPath);
    }

    @Override
    public Object render() {
        return renderables;
    }

    void next() {
        if (!(renderable.isEmpty())) {
            renderables.add(renderable);
        }
        renderable = new LinkedHashMap<>();
    }
}
