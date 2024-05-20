package io.github.renatolsjf.chassis.util.proxy;


import org.springframework.cglib.proxy.Enhancer;

public class ChassisEnhancer {

    public <T> T enhance(Class<T> type) {
        return this.enhance(type, null);
    }

    public <T> T enhance(Class<T> type, T delegate) {

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(type);
        enhancer.setCallback(new TracingEnhancer().createInterceptor(delegate));
        return (T) enhancer.create();

    }

    public boolean isEnhanceable(Class<?> type) {
        return new TracingEnhancer().isEnhanceable(type);
    }

}
