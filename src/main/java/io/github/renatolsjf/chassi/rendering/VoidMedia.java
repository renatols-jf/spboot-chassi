package io.github.renatolsjf.chassi.rendering;

import io.github.renatolsjf.chassi.rendering.transforming.TransformingPath;

public class VoidMedia extends Media {

    public VoidMedia(boolean printNull) {
        super(printNull);
    }

    @Override
    protected void doPrint(String name, Object content) {
        //Void
    }

    @Override
    protected MediaContent getContent(TransformingPath transformingPath) { return null; }

    @Override
    public Object render() { return null; }
}
