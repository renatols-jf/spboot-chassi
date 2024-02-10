package com.github.renatolsjf.chassi.rendering;

import com.github.renatolsjf.chassi.context.Context;
import com.github.renatolsjf.chassi.rendering.config.RenderConfig;
import com.github.renatolsjf.chassi.rendering.config.RenderPolicy;
import com.github.renatolsjf.chassi.rendering.config.RenderTransformer;

import java.lang.reflect.Modifier;
import java.util.Arrays;

public interface FieldRenderable extends Renderable {

    @Override
    default Media render(Media media) {

        Class clazz = this.getClass();
        while (clazz != null && clazz != Object.class) {
            Arrays.stream(clazz.getDeclaredFields())
                    .forEach( f -> {

                        if (Modifier.isStatic(f.getModifiers())) {
                            return;
                        }

                        String name = f.getName();
                        Object value;
                        f.trySetAccessible();
                        if (f.canAccess(this)) {
                            try {
                                value = f.get(this);
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
                                String[] actions = config.action();
                                if (mostSuitableConfig == null && actions.length == 0) {
                                    mostSuitableConfig = config;
                                } else if (Arrays.asList(actions).contains(Context.forRequest().getAction())) {
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

}
