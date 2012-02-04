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

import com.zachsthings.libcomponents.loader.ComponentLoader;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.SimpleInjector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple manager that keeps track of components and what they should do.
 * @author zml2008
 */
public abstract class ComponentManager<T extends AbstractComponent> {
    protected final Logger logger;
	protected final Class<T> componentClass;
    protected final List<ComponentLoader> loaders = new ArrayList<ComponentLoader>();
    protected final Map<String, T> registeredComponents = new LinkedHashMap<String, T>();
    protected final Map<Class<? extends Annotation>, AnnotationHandler<?>> annotationHandlers = new LinkedHashMap<Class<? extends Annotation>, AnnotationHandler<?>>();
    
    public ComponentManager(Logger logger, Class<T> componentCass) {
        this.logger = logger;
		this.componentClass = componentCass;
    }

    public synchronized boolean addComponentLoader(ComponentLoader loader) {
        return loaders.add(loader);
    }

	
    public synchronized boolean loadComponents() throws InvalidComponentException {
        for (ComponentLoader loader : loaders) {
            for (AbstractComponent baseComponent : loader.loadComponents()) {
				if (!componentClass.isAssignableFrom(baseComponent.getClass())) {
					throw new InvalidComponentException(baseComponent.getClass(), "Component is not an instance of " + componentClass.getCanonicalName());
				}
				T component = componentClass.cast(baseComponent);
				ComponentInformation info = component.getClass().getAnnotation(ComponentInformation.class);
                component.setUp(loader, info);
				setUpComponent(component);
                registeredComponents.put(info.friendlyName().replaceAll(" ", "-").toLowerCase(), component);
            }
        }
        return true;
    }

    protected abstract void setUpComponent(T component);

    public synchronized void enableComponents() {
        for (T component : registeredComponents.values()) {
            for (Field field : component.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                for (Annotation annotation : field.getAnnotations()) {
                    AnnotationHandler<Annotation> handler =
                            (AnnotationHandler<Annotation>)annotationHandlers.get(annotation.annotationType());
                    if (handler != null) {
                        if (!handler.handle(component, field, annotation)) {
                            logger.log(Level.WARNING, "Component "
                                    + component.getClass().getSimpleName() +
                                    " could not be enabled! Error in annotation handler for field " + field);
                        }
                    }
                }
            }
            component.enable();
            component.setEnabled(true);
            logger.log(Level.FINEST, "Component " +
                    component.getClass().getSimpleName() + " successfully enabled!");
        }

    }
    
    public synchronized void unloadComponents() {
        for (T component : registeredComponents.values()) {
            component.disable();
        }
        registeredComponents.clear();
    }
    
    public synchronized void reloadComponents() {
        for (T component : registeredComponents.values()) {
            component.reload();
        }
    }
    
    public synchronized <C> C getComponent(Class<C> type) {
        for (T component : registeredComponents.values()) {
            if (component.getClass().equals(type)) {
                return type.cast(component);
            }
        }
        return null;
    }
    
    public Collection<T> getComponents() {
        return Collections.unmodifiableCollection(registeredComponents.values());
    }
    
    public AbstractComponent getComponent(String friendlyName) {
        return registeredComponents.get(friendlyName);
    }
    
    public synchronized <T extends Annotation> void registerAnnotationHandler(Class<T> annotation, AnnotationHandler<T> handler) {
        annotationHandlers.put(annotation, handler);
    }
    
    public synchronized <T extends Annotation> AnnotationHandler<T> getAnnotationHandler(Class<T> annotation) {
        return (AnnotationHandler<T>)annotationHandlers.get(annotation);
    }
}
