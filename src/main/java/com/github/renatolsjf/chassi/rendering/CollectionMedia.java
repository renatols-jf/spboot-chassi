package com.github.renatolsjf.chassi.rendering;

import com.github.renatolsjf.chassi.rendering.transforming.TransformingPath;

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
