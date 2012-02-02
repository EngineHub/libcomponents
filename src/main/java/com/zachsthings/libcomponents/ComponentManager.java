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
public abstract class ComponentManager<Player> {
    protected final Logger logger;
    protected final List<ComponentLoader<Player>> loaders = new ArrayList<ComponentLoader<Player>>();
    protected final Map<String, AbstractComponent<Player>> registeredComponents = new LinkedHashMap<String, AbstractComponent<Player>>();
    protected final Map<Class<? extends Annotation>, AnnotationHandler<?>> annotationHandlers = new LinkedHashMap<Class<? extends Annotation>, AnnotationHandler<?>>();
    
    public ComponentManager(Logger logger) {
        this.logger = logger;
    }

    public synchronized boolean addComponentLoader(ComponentLoader<Player> loader) {
        return loaders.add(loader);
    }

    public synchronized boolean loadComponents() {
        for (ComponentLoader<Player> loader : loaders) {
            for (AbstractComponent<Player> component : loader.loadComponents()) {
                // Create a CommandsManager instance
                CommandsManager<Player> commands = createComponentManager();
                commands.setInjector(new SimpleInjector(component));
                
                ComponentInformation info = component.getClass().getAnnotation(ComponentInformation.class);

                component.setUp(commands, loader, info);
                
                registeredComponents.put(info.friendlyName().replaceAll(" ", "-").toLowerCase(), component);
            }
        }
        return true;
    }

    protected abstract CommandsManager<Player> createComponentManager();

    public synchronized void enableComponents() {
        for (AbstractComponent<Player> component : registeredComponents.values()) {
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
        for (AbstractComponent<Player> component : registeredComponents.values()) {
            component.disable();
        }
        registeredComponents.clear();
    }
    
    public synchronized void reloadComponents() {
        for (AbstractComponent<Player> component : registeredComponents.values()) {
            component.reload();
        }
    }
    
    public synchronized <T> T getComponent(Class<T> type) {
        for (AbstractComponent<Player> component : registeredComponents.values()) {
            if (component.getClass().equals(type)) {
                return type.cast(component);
            }
        }
        return null;
    }
    
    public Collection<AbstractComponent<Player>> getComponents() {
        return Collections.unmodifiableCollection(registeredComponents.values());
    }
    
    public AbstractComponent<Player> getComponent(String friendlyName) {
        return registeredComponents.get(friendlyName);
    }
    
    public synchronized <T extends Annotation> void registerAnnotationHandler(Class<T> annotation, AnnotationHandler<T> handler) {
        annotationHandlers.put(annotation, handler);
    }
    
    public synchronized <T extends Annotation> AnnotationHandler<T> getAnnotationHandler(Class<T> annotation) {
        return (AnnotationHandler<T>)annotationHandlers.get(annotation);
    }
}
