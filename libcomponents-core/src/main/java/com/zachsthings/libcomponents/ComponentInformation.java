package com.zachsthings.libcomponents;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides useful information about an {@link AbstractComponent}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentInformation {
    /**
     * A name for this component that users see.
     * @return This component's friendly name.
     */
    public String friendlyName();

    /**
     * An array of authors involved in the creation of this component.
     * @return The authors.
     */
    public String[] authors() default "";

    /**
     * A short description of this component to be used, for example, on a component help page.
     * @return The description.
     */
    public String desc();
}
