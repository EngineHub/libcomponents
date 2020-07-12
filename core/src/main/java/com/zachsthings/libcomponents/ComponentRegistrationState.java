package com.zachsthings.libcomponents;

class ComponentRegistrationState<T extends AbstractComponent> {
    private final T component;
    private final Depend dependencyInfo;
    private boolean enabled = false;
    private boolean broken = false;

    public ComponentRegistrationState(T component, Depend dependencyInfo) {
        this.component = component;
        this.dependencyInfo = dependencyInfo;
    }

    public T getComponent() {
        return component;
    }

    public Depend getDependencyInfo() {
        return dependencyInfo;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isBroken() {
        return broken;
    }

    public void setBroken(boolean broken) {
        this.broken = broken;
    }
}
