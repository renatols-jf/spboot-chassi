package io.github.renatolsjf.chassis.context;

import io.github.renatolsjf.chassis.util.proxy.ChassisEnhancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Service
public class AppRegistry {

    private static Map<Class<?>, Object> instanceMap = new HashMap<>();

    private static ApplicationContext context;

    @Autowired
    AppRegistry(ApplicationContext ac) { context = ac; }

    public static ApplicationContext getContext() { return context; }

    public static <T> T getResource(Class<T> type) {

            Object instance = instanceMap.get(type);
            if (instance != null) {
                return (T) instance;
            }

            try {
                instance = context.getBean(type);
            } catch (Exception e) {
                //DO NOTHING
            }

            if (new ChassisEnhancer().isEnhanceable(type)) {
                Object proxy = new ChassisEnhancer().enhance(type);
                if (instance != null) {
                    instanceMap.put(type, proxy);
                }
                return (T) proxy;
            } else {
                if (instance != null) {
                    return (T) instance;
                } else {
                    try {
                        Constructor<T> c = type.getConstructor();
                        return c.newInstance();
                    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                             InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

    }

}
