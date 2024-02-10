package com.github.renatolsjf.chassi.rendering.transforming;

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


}
