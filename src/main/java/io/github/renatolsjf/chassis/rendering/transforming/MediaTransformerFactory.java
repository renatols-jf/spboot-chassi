package io.github.renatolsjf.chassis.rendering.transforming;

import io.github.renatolsjf.chassis.rendering.Media;
import io.github.renatolsjf.chassis.rendering.MediaTransformer;
import io.github.renatolsjf.chassis.context.Context;

public class MediaTransformerFactory {

    private MediaTransformerFactory() {}

    public static MediaTransformer createTransformerFromContext(Context context) {

        MediaTransformer trasnformer = new MediaTransformer() {
            @Override
            public Media doTransform(Media media) {
                return media;
            }
        };

        trasnformer = context.getProjection().createTransformer(trasnformer);

        return trasnformer;

    }

}
