package com.zachsthings.libcomponents;

import com.zachsthings.libcomponents.loader.ComponentLoader;

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
                                    + component.getInformation().friendlyName() +
                                    " could not be enabled! Error in annotation handler for field " + field);
                        }
                    }
                }
            }
            component.enable();
            component.setEnabled(true);
            component.saveConfig();
            logger.log(Level.FINEST, "Component " +
                    component.getInformation().friendlyName() + " successfully enabled!");
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
    
    public synchronized <A extends Annotation> void registerAnnotationHandler(Class<A> annotation, AnnotationHandler<A> handler) {
        annotationHandlers.put(annotation, handler);
    }
    
    public synchronized <A extends Annotation> AnnotationHandler<A> getAnnotationHandler(Class<A> annotation) {
        return (AnnotationHandler<A>) annotationHandlers.get(annotation);
    }
}
