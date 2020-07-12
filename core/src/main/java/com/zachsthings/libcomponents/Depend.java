package com.zachsthings.libcomponents;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A way for components to register deps on other things
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Depend {
    public Class<?>[] components() default {};
    public String[] plugins() default {};
}
