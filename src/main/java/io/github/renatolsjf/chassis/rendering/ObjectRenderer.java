package io.github.renatolsjf.chassis.rendering;

import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.rendering.config.RenderConfig;
import io.github.renatolsjf.chassis.rendering.config.RenderPolicy;
import io.github.renatolsjf.chassis.rendering.config.RenderTransformer;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

public class ObjectRenderer {

    public static Renderable asRenderable(Object obj) {
        return (media) -> renderProperties(media, obj);
    }

    public static Media renderProperties(Media media, Object obj) {
        Class clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            Arrays.stream(clazz.getDeclaredFields())
                    .forEach( f -> {

                        if (Modifier.isStatic(f.getModifiers())) {
                            return;
                        }

                        String name = f.getName();
                        Object value;
                        f.trySetAccessible();
                        if (f.canAccess(obj)) {
                            try {
                                value = f.get(obj);
                            } catch (IllegalAccessException e) {
                                return;
                            }
                        } else {
                            return;
                        }

                        RenderConfig[] configs = f.getAnnotationsByType(RenderConfig.class);
                        if (configs.length > 0) {

                            RenderConfig mostSuitableConfig = null;
                            for (RenderConfig config: configs) {
                                String[] operations = config.operation();
                                if (mostSuitableConfig == null && operations.length == 0) {
                                    mostSuitableConfig = config;
                                } else if (Arrays.asList(operations).contains(Context.forRequest().getOperation())) {
                                    mostSuitableConfig = config;
                                    break;
                                }
                            }

                            if (mostSuitableConfig != null) {

                                if (mostSuitableConfig.policy().value() == RenderPolicy.Policy.IGNORE) {
                                    return;
                                }

                                String alias = mostSuitableConfig.alias().value();
                                if (alias != null && !(alias.isBlank())) {
                                    name = alias;
                                }

                                Class<? extends RenderTransformer> transfomerClass = mostSuitableConfig.transformer().value();
                                try {
                                    RenderTransformer transformer = transfomerClass.getConstructor().newInstance();
                                    value = transformer.transform(value);
                                } catch (Exception e) {
                                    return;
                                }

                            }

                        }

                        media.print(name, value);

                    });
            clazz = clazz.getSuperclass();
        }
        return media;
    }

    public static Renderable asRenderable(Map<String, Object> map) {
        return (media) -> renderMap(media, map);
    }

    public static Media renderMap(Media media, Map<String, Object> map) {
        map.forEach((k, v) -> media.print(k, v));
        return media;
    }

}
