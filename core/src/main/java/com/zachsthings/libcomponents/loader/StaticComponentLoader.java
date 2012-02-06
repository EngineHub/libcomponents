package com.zachsthings.libcomponents.loader;

import com.zachsthings.libcomponents.AbstractComponent;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Loads components from a list specified in the constructor. This is a bit of a workaround until dependencies can be implemented..
 */
public abstract class StaticComponentLoader extends AbstractComponentLoader {
    private final List<AbstractComponent> components;

    public StaticComponentLoader(Logger logger, File configDir, AbstractComponent... components) {
        super(logger, configDir);
        this.components = Arrays.asList(components);
    }
    public Collection<AbstractComponent> loadComponents() {
        return components;
    }
}
