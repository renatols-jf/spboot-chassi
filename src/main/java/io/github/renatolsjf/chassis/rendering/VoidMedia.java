package io.github.renatolsjf.chassis.rendering;

import io.github.renatolsjf.chassis.rendering.transforming.TransformingPath;

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
