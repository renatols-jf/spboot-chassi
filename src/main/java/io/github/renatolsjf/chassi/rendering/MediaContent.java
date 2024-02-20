package io.github.renatolsjf.chassi.rendering;

import io.github.renatolsjf.chassi.rendering.transforming.TransformingPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MediaContent {

    private List<Map<String, Object>> content;

    private MediaContent(List<Map<String, Object>> content) {
        this.content = content;
    }

    public static MediaContent createContent(List<Map<String, Object>> originalContent, TransformingPath transformingPath) {

        if (transformingPath.hasNext()) {

            String path = transformingPath.next();
            try {
                List newContent = new ArrayList<>();
                List mappedData = originalContent.stream().map(m -> m.get(path)).collect(Collectors.toList());
                mappedData.forEach(d -> {
                    if (List.class.isInstance(d)) {
                        newContent.addAll((List)d);
                    } else {
                        newContent.add(d);
                    }
                });
                return createContent(newContent, transformingPath);
            } catch (Exception ex) {
                throw new RuntimeException("Houve um problema ao obter o conteúdo da mídia para transformação. " +
                        "Provavelmente o caminho de transformação está errado para o contexto",ex);
            }

        } else {
            return new MediaContent(originalContent);
        }

    }

    public void remove(String... attribute) {
        this.remove(Arrays.asList(attribute));
    }

    public void remove(List<String> attributes) {
        for (String attribute: attributes) {
            for (Map m: this.content) {
                m.remove(attribute);
            }
        }
    }

    public void keepOnly(String... atributes) {
        this.keepOnly(Arrays.asList(atributes));
    }

    public void keepOnly(List<String> attributes) {
        for (String attribute: attributes) {
            for (Map m: this.content) {
                m.entrySet().removeIf(e -> !(attributes.contains(((Map.Entry) e).getKey())));
            }
        }
    }

}
