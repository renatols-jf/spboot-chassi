package io.github.renatolsjf.chassis.util.proxy;


import org.springframework.cglib.proxy.Enhancer;

import java.util.Arrays;

public class ChassisEnhancer {

    //private TypeEnhancer[] enhancers = new TypeEnhancer[] {new TracingEnhancer()};
    private TracingEnhancer tracingEnhancer = new TracingEnhancer();

    public <T> T enhance(Class<T> type) {
        return this.enhance(type, null);
    }

    public <T> T enhance(Class<T> type, T delegate) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(type);
        enhancer.setCallback(tracingEnhancer.createInterceptor(delegate));
        return (T) enhancer.create();
    }

    public boolean isEnhanceable(Class<?> type) {
        //return Arrays.stream(this.enhancers).anyMatch(enhancer -> enhancer.isEnhanceable(type));
        return tracingEnhancer.isEnhanceable(type);
    }

}
