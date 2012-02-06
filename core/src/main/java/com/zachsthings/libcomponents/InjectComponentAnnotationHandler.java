package com.zachsthings.libcomponents;

import java.lang.reflect.Field;

/**
 * @author zml2008
 */
public class InjectComponentAnnotationHandler implements AnnotationHandler<InjectComponent> {
    private final ComponentManager<?> componentManager;
    
    public InjectComponentAnnotationHandler(ComponentManager<?> componentManager) {
        this.componentManager = componentManager;
    }

    public boolean handle(AbstractComponent component, Field field, InjectComponent annotation) {
        try {
            Object target = componentManager.getComponent(field.getType());
            if (target != null) {
                field.set(component, target);
                return true;
            }
        } catch (IllegalAccessException ignore) {}
        return false;
    }
}
