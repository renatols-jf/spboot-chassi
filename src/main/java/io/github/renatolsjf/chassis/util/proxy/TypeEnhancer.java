package io.github.renatolsjf.chassis.util.proxy;


import org.springframework.cglib.proxy.MethodInterceptor;

public interface TypeEnhancer {

    default MethodInterceptor createInterceptor() {
        return this.createInterceptor(null);
    }

    MethodInterceptor createInterceptor(Object delegate);
    boolean isEnhanceable(Class<?> type);

}
