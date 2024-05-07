package io.github.renatolsjf.chassis.rendering;

import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.rendering.config.RenderConfig;
import io.github.renatolsjf.chassis.rendering.config.RenderPolicy;
import io.github.renatolsjf.chassis.rendering.config.RenderTransformer;

import java.lang.reflect.Modifier;
import java.util.Arrays;

public interface FieldRenderable extends Renderable {

    @Override
    default Media render(Media media) {
        return ObjectRenderer.properties(media, this);
    }

}
