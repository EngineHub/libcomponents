package com.zachsthings.libcomponents;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author zml2008
 */
public interface AnnotationHandler<T extends Annotation> {
    public boolean handle(AbstractComponent component, Field field, T annotation);
}
