package com.github.renatolsjf.chassi.context;

import java.lang.annotation.*;

/**
 * Enables the annotated type to initialize a context. This is done to avoid duplicate context creation in a unit of behavior.
 * Generally, only a Request should initialize a context. If a context needs to be initialized somewhere else, that type should
 * be annotated with this.
 *
 * @see com.github.renatolsjf.chassi.request.Request
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContextCreator {
}
