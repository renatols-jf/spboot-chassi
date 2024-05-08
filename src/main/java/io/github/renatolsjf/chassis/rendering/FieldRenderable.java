package io.github.renatolsjf.chassis.rendering;

public interface FieldRenderable extends Renderable {

    @Override
    default Media render(Media media) {
        return ObjectRenderer.renderProperties(media, this);
    }

}
