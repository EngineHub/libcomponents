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

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.zachsthings.libcomponents.loader.ComponentLoader;
import com.zachsthings.libcomponents.config.ConfigurationBase;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.util.yaml.YAMLNode;

import java.util.Collections;
import java.util.Map;

/**
 * @author zml2008
 */
public abstract class AbstractComponent<Player> {

    /**
     * The {@link CommandsManager} where all commands are registered for this component.
     */
    protected CommandsManager<Player> commands;

    /**
     * The raw configuration for this component. This is usually accessed through
     * ConfigurationBase subclasses and #configure()
     */
    private YAMLNode rawConfiguration;

    private ComponentLoader loader;
    
    private ComponentInformation info;

    private boolean enabled;

    public void setUp(CommandsManager<Player> commands, ComponentLoader loader, ComponentInformation info) {
        this.commands = commands;
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
    
    public YAMLNode getRawConfiguration() {
        if (rawConfiguration != null) {
            return rawConfiguration;
        } else {
            return rawConfiguration = getComponentLoader().getConfiguration(this);
        }
    }
    
    public Map<String, String> getCommands() {
        if (commands == null) {
            return Collections.emptyMap();
        } else {
            return commands.getCommands();
        }
    }
}
