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

package com.zachsthings.libcomponents.loader;

import com.zachsthings.libcomponents.AbstractComponent;
import com.zachsthings.libcomponents.config.ConfigUtil;
import com.sk89q.util.yaml.YAMLNode;
import com.sk89q.util.yaml.YAMLProcessor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author zml2008
 */
public class ConfigListedComponentLoader extends AbstractComponentLoader {
    private final YAMLProcessor jarComponentAliases, globalConfig;

    public ConfigListedComponentLoader(Logger logger, YAMLProcessor globalConfig, YAMLProcessor aliasList, File configDir) {
        super(logger, configDir);
        this.jarComponentAliases = aliasList;
        this.globalConfig = globalConfig;
        try {
            jarComponentAliases.load();
        } catch (IOException e) {
            getLogger().severe("Error loading component aliases!");
            e.printStackTrace();
        } catch (YAMLException e) {
            getLogger().severe("Error loading component aliases!");
            e.printStackTrace();
        }
    }

    public Collection<AbstractComponent> loadComponents() {
        List<AbstractComponent> components = new ArrayList<AbstractComponent>();
        // The lists of components to load.
        Set<String> disabledComponents =
                new LinkedHashSet<String>(globalConfig.getStringList("components.disabled", null));
        Set<String> stagedEnabled =
                new LinkedHashSet<String>(globalConfig.getStringList("components.enabled", null));
        for (String key : jarComponentAliases.getKeys(null)) { // Load the component aliases
            if (!stagedEnabled.contains(key)
                    && !stagedEnabled.contains(jarComponentAliases.getString(key + ".class", key))) { // Not already in the enabled list.
                if (jarComponentAliases.getBoolean(key + ".default")) { // Enabled by default
                    stagedEnabled.add(key);
                } else {
                    disabledComponents.add(key);
                }
            }
        }
        stagedEnabled.removeAll(disabledComponents);

        // And go through the enabled components from the configuration
        for (Iterator<String> i = stagedEnabled.iterator(); i.hasNext(); ) {
            String nextName = i.next();
            nextName = jarComponentAliases.getString(nextName + ".class", nextName);
            Class<?> clazz = null;
            try {
                clazz = Class.forName(nextName);
            } catch (ClassNotFoundException ignore) {}


            if (!isComponentClass(clazz)) {
                getLogger().warning("Invalid or unknown class found in enabled components: "
                        + nextName + ". Moving to disabled components list.");
                i.remove();
                disabledComponents.add(nextName);
                continue;
            }

            try {
                components.add(instantiateComponent(clazz));
            } catch (Throwable t) {
                getLogger().warning("Error initializing component "
                        + clazz + ": " + t.getMessage());
                t.printStackTrace();
            }
        }

        // And update the configuration now that we're done loading from this loader
        globalConfig.setProperty("components.disabled", new ArrayList<String>(disabledComponents));
        globalConfig.setProperty("components.enabled", new ArrayList<String>(stagedEnabled));
        return components;
    }

    @Override
    public YAMLNode getConfiguration(AbstractComponent component) {
        return ConfigUtil.getNode(globalConfig,
                "component." + toFileName(component));
    }
}
