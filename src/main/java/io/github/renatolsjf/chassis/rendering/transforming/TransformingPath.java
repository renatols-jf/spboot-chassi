package io.github.renatolsjf.chassis.rendering.transforming;

import java.util.ArrayList;
import java.util.List;

public class TransformingPath {

    private List<String> pathHierarchy = new ArrayList<>();

    public TransformingPath(String startingPath) {
        if (startingPath == null || startingPath.isBlank()) {
            throw new IllegalArgumentException("Caminho de transformação não pdoe ser vazio");
        }
        this.pathHierarchy.add(startingPath);
    }

    private TransformingPath(List<String> paths) {
        this.pathHierarchy = paths;
    }

    public TransformingPath path(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Caminho de transformação não pdoe ser vazio");
        }
        this.pathHierarchy.add(path);
        return this;
    }

    public boolean hasNext() {
        return !(this.pathHierarchy.isEmpty());
    }

    public String next() {
        if (!this.hasNext()) {
            throw new IndexOutOfBoundsException("Path inválido; não há hierarquia a se percorrer");
        }
        return pathHierarchy.remove(0);
    }

    public static TransformingPath fromList(List<String> paths) {
        return new TransformingPath(paths);
    }


}
