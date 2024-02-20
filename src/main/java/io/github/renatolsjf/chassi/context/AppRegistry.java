package io.github.renatolsjf.chassi.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class AppRegistry {

    private static ApplicationContext context;

    @Autowired
    AppRegistry(ApplicationContext ac) { context = ac; }

    public static ApplicationContext getContext() { return context; }

    public static <T> T getResource(Class<T> clazz) { return context.getBean(clazz); }

}
