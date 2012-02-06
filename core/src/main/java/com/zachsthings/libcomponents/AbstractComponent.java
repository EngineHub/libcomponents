package com.zachsthings.libcomponents;

import com.zachsthings.libcomponents.config.ConfigurationNode;
import com.zachsthings.libcomponents.loader.ComponentLoader;
import com.zachsthings.libcomponents.config.ConfigurationBase;

import java.util.Map;

/**
 * @author zml2008
 */
public abstract class AbstractComponent {

    /**
     * The raw configuration for this component. This is usually accessed through
     * ConfigurationBase subclasses and #configure()
     */
    private ConfigurationNode rawConfiguration;

    private ComponentLoader loader;
    
    private ComponentInformation info;

    private boolean enabled;

    public void setUp(ComponentLoader loader, ComponentInformation info) {
        this.loader = loader;
        this.info = info;
    }

    /**
     * This method is called once all of this Component's fields have been set up
     * and all other Component classes have been discovered
     */
    public abstract void enable();

    public void disable() {}

    public void reload() {
        if (rawConfiguration != null) {
            rawConfiguration = getComponentLoader().getConfiguration(this);
        }
    }

    protected <T extends ConfigurationBase> T configure(T config) {
        config.load(getRawConfiguration());
        return config;
    }
    
    public <T extends ConfigurationBase>  T saveConfig(T config) {
        config.save(getRawConfiguration());
        return config;
    }

    public boolean isEnabled() {
        return enabled;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ComponentLoader getComponentLoader() {
        return loader;
    }
    
    public ComponentInformation getInformation() {
        return info;
    }
    
    public ConfigurationNode getRawConfiguration() {
        if (rawConfiguration != null) {
            return rawConfiguration;
        } else {
            return rawConfiguration = getComponentLoader().getConfiguration(this);
        }
    }
    
    public abstract Map<String, String> getCommands();
}
