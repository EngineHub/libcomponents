package com.zachsthings.libcomponents;

import com.zachsthings.libcomponents.loader.ComponentLoader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A simple manager that keeps track of components and what they should do.
 * @author zml2008
 */
public abstract class ComponentManager<T extends AbstractComponent> {
    protected final Logger logger;
    protected final Class<T> componentClass;
    protected final List<ComponentLoader> loaders = new ArrayList<>();
    protected final Map<Class<? extends Annotation>, AnnotationHandler<Annotation>> annotationHandlers = new HashMap<>();

    protected final Map<String, ComponentRegistrationState<T>> registeredComponentsByName = new HashMap<>();
    protected final Map<Class<?>, ComponentRegistrationState<T>> registeredComponentsByClass = new HashMap<>();
    protected final List<ComponentRegistrationState<T>> registeredComponents = new ArrayList<>();

    protected final Set<String> requiredPlugins = new HashSet<>();

    public ComponentManager(Logger logger, Class<T> componentCass) {
        this.logger = logger;
        this.componentClass = componentCass;
    }

    public synchronized boolean addComponentLoader(ComponentLoader loader) {
        return loaders.add(loader);
    }

    private String standardizeName(String friendlyName) {
        return friendlyName.replaceAll(" ", "-").toLowerCase();
    }

    public synchronized boolean loadComponents() throws InvalidComponentException {
        for (ComponentLoader loader : loaders) {
            for (AbstractComponent baseComponent : loader.loadComponents()) {
                if (!componentClass.isAssignableFrom(baseComponent.getClass())) {
                    throw new InvalidComponentException(baseComponent.getClass(), "Component is not an instance of " + componentClass.getCanonicalName());
                }
                T component = componentClass.cast(baseComponent);
                Class<?> componentClass = component.getClass();
                ComponentInformation info = componentClass.getAnnotation(ComponentInformation.class);
                component.setUp(loader, info);
                setUpComponent(component);

                // Create and add the component registration
                Depend dependencyInfo = componentClass.getAnnotation(Depend.class);
                ComponentRegistrationState<T> registrationState = new ComponentRegistrationState<>(
                        component,
                        dependencyInfo
                );

                registeredComponents.add(registrationState);
                registeredComponentsByName.put(standardizeName(info.friendlyName()), registrationState);
                registeredComponentsByClass.put(componentClass, registrationState);
            }
        }
        return true;
    }

    protected abstract void setUpComponent(T component);

    protected abstract boolean isPluginRegistered(String pluginName);
    protected abstract boolean isPluginEnabled(String pluginName);

    public void handlePluginEnable(String pluginName) {
        if (requiredPlugins.contains(pluginName)) {
            enableComponents();
        }
    }

    private synchronized boolean hasDependenciesMet(ComponentRegistrationState<T> registrationState) {
        Depend dependencyInfo = registrationState.getDependencyInfo();
        if (dependencyInfo == null) {
            return true;
        }

        T component = registrationState.getComponent();

        // Walk components looking for any dependency that isn't already enabled.
        for (Class<?> dependentComponent : dependencyInfo.components()) {
            ComponentRegistrationState<T> dependencyState = registeredComponentsByClass.get(dependentComponent);
            if (dependencyState == null) {
                registrationState.setBroken(true);
                logger.log(Level.WARNING, "Component "
                        + component.getInformation().friendlyName() +
                        " could not be enabled! Dependent component was not present " + dependentComponent.getCanonicalName());
                continue;
            }

            if (!dependencyState.isEnabled()) {
                return false;
            }
        }

        // Walk plugins looking for any dependency that isn't already enabled.
        for (String dependentPlugin : dependencyInfo.plugins()) {
            if (!isPluginRegistered(dependentPlugin)) {
                registrationState.setBroken(true);
                logger.log(Level.WARNING, "Component "
                        + component.getInformation().friendlyName() +
                        " could not be enabled! Dependent plugin was not present " + dependentPlugin);
                continue;
            }

            if (!isPluginEnabled(dependentPlugin)) {
                requiredPlugins.add(dependentPlugin);
                return false;
            }
        }

        return true;
    }

    private synchronized boolean enableComponent(ComponentRegistrationState<T> registrationState) {
        T component = registrationState.getComponent();

        try {
            for (Field field : component.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                for (Annotation annotation : field.getAnnotations()) {
                    AnnotationHandler<Annotation> handler = annotationHandlers.get(annotation.annotationType());
                    if (handler != null) {
                        if (!handler.handle(component, field, annotation)) {
                            registrationState.setBroken(true);
                            logger.log(Level.WARNING, "Component "
                                    + component.getInformation().friendlyName() +
                                    " could not be enabled! Error in annotation handler for field " + field);
                        }
                    }
                }
            }

            if (registrationState.isBroken()) {
                return false;
            }

            component.enable();
            component.setEnabled(true);
            component.saveConfig();
            logger.log(Level.FINEST, "Component " +
                    component.getInformation().friendlyName() + " successfully enabled!");

            registrationState.setEnabled(true);
            return true;
        } catch (Throwable t) {
            registrationState.setBroken(true);
            logger.log(Level.SEVERE, "Component " +
                    component.getInformation().friendlyName() + " failed to load!");
            t.printStackTrace();

            return false;
        }
    }

    public synchronized void enableComponents() {
        boolean changed;

        do {
            changed = false;

            for (ComponentRegistrationState<T> registrationState : registeredComponents) {
                // If this component is already enabled, or unloadable skip it
                if (registrationState.isEnabled() || registrationState.isBroken()) {
                    continue;
                }

                if (hasDependenciesMet(registrationState) && enableComponent(registrationState)) {
                    changed = true;
                }
            }
        } while (changed);
    }
    
    public synchronized void unloadComponents() {
        for (ComponentRegistrationState<T> registrationState : registeredComponents) {
            if (registrationState.isEnabled()) {
                registrationState.getComponent().disable();
            }
        }
        registeredComponents.clear();
    }
    
    public synchronized void reloadComponents() {
        for (ComponentRegistrationState<T> registrationState : registeredComponents) {
            if (registrationState.isEnabled()) {
                registrationState.getComponent().reload();
            }
        }
    }
    
    public synchronized <C> C getComponent(Class<C> type) {
        ComponentRegistrationState<T> result = registeredComponentsByClass.get(type);
        if (result == null) {
            return null;
        }

        return type.cast(result.getComponent());
    }
    
    public Collection<T> getComponents() {
        return registeredComponents.stream().map(ComponentRegistrationState::getComponent).collect(Collectors.toList());
    }
    
    public T getComponent(String friendlyName) {
        ComponentRegistrationState<T> result = registeredComponentsByName.get(standardizeName(friendlyName));
        if (result == null) {
            return null;
        }

        return result.getComponent();
    }
    
    public synchronized <A extends Annotation> void registerAnnotationHandler(Class<A> annotation, AnnotationHandler<A> handler) {
        annotationHandlers.put(annotation, (AnnotationHandler<Annotation>) handler);
    }
    
    public synchronized <A extends Annotation> AnnotationHandler<A> getAnnotationHandler(Class<A> annotation) {
        return (AnnotationHandler<A>) annotationHandlers.get(annotation);
    }
}
