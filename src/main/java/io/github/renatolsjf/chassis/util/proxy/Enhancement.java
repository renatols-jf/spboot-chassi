package io.github.renatolsjf.chassis.util.proxy;

import java.lang.reflect.Method;

public interface Enhancement {

    void preInvocation(Object object, Object delegate, Method method, Object[] args);
    void postInvocation(Object object, Object delegate, Method method, Object[] args);

}
