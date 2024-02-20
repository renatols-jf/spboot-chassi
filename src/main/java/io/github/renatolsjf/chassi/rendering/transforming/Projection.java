package io.github.renatolsjf.chassi.rendering.transforming;

import io.github.renatolsjf.chassi.rendering.Media;
import io.github.renatolsjf.chassi.rendering.MediaTransformer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Projection {

    private List<String> fields;

    public Projection(List<String> fields) {
        this.fields = fields;
    }

    MediaTransformer createTransformer(MediaTransformer transformer) {

        if (fields == null || fields.isEmpty()) {
            return transformer;
        }

        Map<List<String>, String[]> projectionMap = this.createProjectionMap();
        for(Map.Entry<List<String>, String[]> entry: projectionMap.entrySet()) {
            TransformingPath tp = TransformingPath.fromList(entry.getKey());
            transformer = transformer.next(new MediaTransformer() {
                @Override
                public Media doTransform(Media media) {
                    return media.shrinkTo(tp, entry.getValue());
                }
            });
        }

        return transformer;

    }

    private Map<List<String>, String[]> createProjectionMap() {

        Map<String, List<String>> m = new LinkedHashMap<>();
        for (String s: this.fields) {

            if (s == null || s.isBlank()) {
                continue;
            }

            boolean shouldRun = true;
            String toCheck = s;
            while (shouldRun) {

                String key;
                String value;

                int idx = toCheck.lastIndexOf(".");
                if (idx == -1) {
                    key = "";
                    value = toCheck;
                    shouldRun = false;
                } else {
                    key = toCheck.substring(0, idx);
                    value = toCheck.substring(idx + 1);
                }

                List<String> l = m.getOrDefault(key, new ArrayList<>());
                if (!(l.contains(value))) {
                    l.add(value);
                }
                m.put(key, l);
                toCheck = key;
            }

        }

        return m.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e ->  {
                            String[] paths = (e.getKey().split("\\."));
                            List<String> l = new ArrayList<>();
                            for (String path: paths) {
                                if (!("".equals(path))) {
                                    l.add(path);
                                }
                            }
                            return l;
                        },
                        e -> e.getValue().toArray(new String[e.getValue().size()])));

    }

}
