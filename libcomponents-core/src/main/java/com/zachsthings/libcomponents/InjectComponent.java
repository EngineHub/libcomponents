package com.zachsthings.libcomponents;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Directs a {@link ComponentManager} to set the value of a field to the specified component.
 * The field's type is used for lookup of the required component.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectComponent {
}
