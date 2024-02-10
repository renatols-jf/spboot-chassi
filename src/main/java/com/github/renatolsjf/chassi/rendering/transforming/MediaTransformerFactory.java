package com.github.renatolsjf.chassi.rendering.transforming;

import com.github.renatolsjf.chassi.rendering.Media;
import com.github.renatolsjf.chassi.rendering.MediaTransformer;
import com.github.renatolsjf.chassi.context.Context;

public class MediaTransformerFactory {

    private MediaTransformerFactory() {}

    public static MediaTransformer createTransformerFromContext(Context context) {

        MediaTransformer trasnformer = new MediaTransformer() {
            @Override
            public Media doTransform(Media media) {
                return media;
            }
        };

        return trasnformer;

    }

}
