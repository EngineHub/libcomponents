/*
 * libcomponents
 * Copyright (C) 2012 zml2008
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
package com.zachsthings.libcomponents.spout;

import com.sk89q.util.yaml.YAMLProcessor;
import com.zachsthings.libcomponents.ComponentManager;
import com.zachsthings.libcomponents.InvalidComponentException;
import org.spout.api.Engine;
import org.spout.api.Spout;
import org.spout.api.command.CommandSource;
import org.spout.api.event.Event;
import org.spout.api.exception.CommandException;
import org.spout.api.geo.World;
import org.spout.api.plugin.CommonPlugin;

import java.io.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @author zml2008
 */
public abstract class BasePlugin extends CommonPlugin {

    public static Engine engine() {
        return Spout.getEngine();
    }

    public static <T extends Event> T callEvent(T event) {
        return engine().getEventManager().callEvent(event);
    }

    public boolean lowPriorityCommandRegistration;

    protected YAMLProcessor config;
    protected ComponentManager<SpoutComponent> componentManager;

    public void onDisable() {
        this.getGame().getScheduler().cancelTasks(this);
        componentManager.unloadComponents();
    }

    public void onEnable() {
        // Make the data folder for the plugin where configuration files
        // and other data files will be stored
        getDataFolder().mkdirs();

        loadConfiguration();


        componentManager = new ComponentManager<SpoutComponent>(getLogger(), SpoutComponent.class) {
            @Override
            protected void setUpComponent(SpoutComponent component) {
                component.setUp(BasePlugin.this);
            }
        };

        registerComponentLoaders();

        try {
            componentManager.loadComponents();
        } catch (InvalidComponentException e) {
            getLogger().severe(e.getMessage());
        }

        componentManager.enableComponents();

        config.save();
    }

    public abstract void registerComponentLoaders();

    public void loadConfiguration() {
        config = populateConfiguration();

        lowPriorityCommandRegistration = config.getBoolean("low-priority-command-registration", false);
    }

    public abstract YAMLProcessor populateConfiguration();

    public YAMLProcessor getGlobalConfiguration() {
        return config;
    }

    public ComponentManager<SpoutComponent> getComponentManager() {
        return componentManager;
    }


    /**
     * Create a default configuration file from the .jar.
     *
     * @param name
     */
    public void createDefaultConfiguration(String name) {
        File actual = new File(getDataFolder(), name);
        if (!actual.exists()) {

            InputStream input = null;
            try {
                JarFile file = new JarFile(getFile());
                ZipEntry copy = file.getEntry("defaults/" + name);
                if (copy == null) throw new FileNotFoundException();
                input = file.getInputStream(copy);
            } catch (IOException e) {
                getLogger().severe("Unable to read default configuration: " + name);
            }
            if (input != null) {
                FileOutputStream output = null;

                try {
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length;
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }

                    getLogger().info("Default configuration file written: " + name);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        input.close();
                    } catch (IOException ignore) {}

                    try {
                        if (output != null)
                            output.close();
                    } catch (IOException ignore) {}
                }
            }
        }
    }

    /**
     * Checks permissions and throws an exception if permission is not met.
     *
     * @param sender
     * @param perm
     * @throws org.spout.api.exception.CommandException
     */
    public void checkPermission(CommandSource sender, String perm)
            throws CommandException {
        if (!sender.hasPermission(perm)) {
            throw new CommandException("You do not have permission to use this command!");
        }
    }

    public void checkPermission(CommandSource sender, World world, String perm)
            throws CommandException {
        if (!sender.hasPermission(world, perm)) {
            throw new CommandException("You do not have permission to use this command!");
        }
    }
}
