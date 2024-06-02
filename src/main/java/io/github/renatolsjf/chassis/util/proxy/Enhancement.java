package io.github.renatolsjf.chassis.util.proxy;

import java.lang.reflect.Method;

public interface Enhancement {

    void preInvocation(Class<?> requestedType, Object object, Object delegate, Method method, Object[] args);
    void postInvocation(Class<?> requestedType, Object object, Object delegate, Method method, Object[] args);

}
