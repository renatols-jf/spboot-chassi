package io.github.renatolsjf.chassis.util.conversion;

import java.util.List;
import java.util.Map;

public class ClassName {

    private Class<?> clazz;
    private String parsedName;

    private ClassName(Class<?> clazz, String parsedName) {
        this.clazz = clazz;
        this.parsedName = parsedName;
    }

    public String getParsedName() {
        return this.parsedName;
    }

    @Override
    public String toString() {
        return this.parsedName + " - " + this.getClass().getName();
    }

    static ClassName parse(Class<?> clazz) {

        if (clazz == null) {
            throw new NullPointerException();
        }

        if (List.class.isAssignableFrom(clazz)) {
            return new ClassName(clazz, "List");
        } else if (Map.class.isAssignableFrom(clazz)) {
            return new ClassName(clazz, "Map");
        }

        return new ClassName(clazz, clazz.getSimpleName().replace("[]", "Array"));

    }

}
