/*
 * CommandBook
 * Copyright (C) 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
