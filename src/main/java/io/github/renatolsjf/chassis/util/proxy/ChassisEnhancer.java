package io.github.renatolsjf.chassis.util.proxy;


import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChassisEnhancer {

    private TypeEnhancer[] enhancers = new TypeEnhancer[] {new TracingEnhancer()};
    //private TracingEnhancer tracingEnhancer = new TracingEnhancer();

    public <T> T enhance(Class<T> type) {
        return this.enhance(type, null);
    }

    public <T> T enhance(Class<T> type, T delegate) {

        List<Enhancement> enhancements = Arrays.stream(enhancers)
                .filter(e -> e.isEnhanceable(type))
                .map(e -> e.createEnhancement())
                .collect(Collectors.toList());

        if (enhancements.isEmpty()) {
            throw new TypeNotEnhanceableException();
        }

        MethodInterceptor interceptor = (Object o, Method method, Object[] args, MethodProxy methodProxy) -> {

            try {
                enhancements.forEach(e -> e.preInvocation(o, delegate, method, args));
                if (delegate != null) {
                    return methodProxy.invoke(delegate, args);
                } else {
                    return methodProxy.invokeSuper(o, args);
                }
            } finally {
                enhancements.forEach(e -> e.postInvocation(o, delegate, method, args));
            }

        };

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(type);
        enhancer.setCallback(interceptor);
        return (T) enhancer.create();

    }

    public boolean isEnhanceable(Class<?> type) {
        return Arrays.stream(this.enhancers).anyMatch(enhancer -> enhancer.isEnhanceable(type));
        //return tracingEnhancer.isEnhanceable(type);
    }

}

