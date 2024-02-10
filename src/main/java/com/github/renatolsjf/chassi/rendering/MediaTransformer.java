package com.github.renatolsjf.chassi.rendering;

public abstract class MediaTransformer {

    private MediaTransformer parentTransformer;

    public abstract Media doTransform(Media media);

    public MediaTransformer next(MediaTransformer mediaTransformer) {
        mediaTransformer.parentTransformer = this;
        return mediaTransformer;
    }

    public final Media transform(Media media) {

        if (parentTransformer != null) {
            media = parentTransformer.transform(media);
        }
        return doTransform(media);

    }

}
