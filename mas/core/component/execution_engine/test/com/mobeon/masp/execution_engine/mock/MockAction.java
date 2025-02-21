package com.mobeon.masp.execution_engine.mock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Mikael Andersson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MockAction {
    Action value() default Action.DELEGATE;

    public static enum Action {
        DELEGATE,
        MOCK
    }
}
