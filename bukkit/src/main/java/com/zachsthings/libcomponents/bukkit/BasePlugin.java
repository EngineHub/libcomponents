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
package com.zachsthings.libcomponents.bukkit;

import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.SimpleInjector;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.PermissionsResolverManager;
import com.zachsthings.libcomponents.ComponentManager;
import com.zachsthings.libcomponents.InvalidComponentException;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Base plugin for Bukkit libcomponents users
 */
public abstract class BasePlugin extends JavaPlugin implements Listener {

    public static Server server() {
        return Bukkit.getServer();
    }

    public static <T extends Event> T callEvent(T event) {
        server().getPluginManager().callEvent(event);
        return event;
    }

    public boolean lowPriorityCommandRegistration;
    private boolean opPermissions;

    protected YAMLProcessor config;
    protected ComponentManager<BukkitComponent> componentManager;

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        componentManager.unloadComponents();
    }

    @Override
    public void onEnable() {
        // Make the data folder for the plugin where configuration files
        // and other data files will be stored
        getDataFolder().mkdirs();

        server().getPluginManager().registerEvents(this, this);

        loadConfiguration();

        // Prepare permissions
        PermissionsResolverManager.initialize(this);

        componentManager = new ComponentManager<BukkitComponent>(getLogger(), BukkitComponent.class) {
            @Override
            protected void setUpComponent(BukkitComponent component) {
                // Create a CommandsManager instance
                CommandsManager<CommandSender> commands = new CommandsManager<CommandSender>() {
                    @Override
                    public boolean hasPermission(CommandSender sender, String permission) {
                        return BasePlugin.this.hasPermission(sender, permission);
                    }
                };
                commands.setInjector(new SimpleInjector(component));
                component.setUp(BasePlugin.this, commands);
            }

            private Plugin getPlugin(String pluginName) {
                return Bukkit.getPluginManager().getPlugin(pluginName);
            }

            @Override
            protected boolean isPluginRegistered(String pluginName) {
                return getPlugin(pluginName) != null;
            }

            @Override
            protected boolean isPluginEnabled(String pluginName) {
                return getPlugin(pluginName).isEnabled();
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

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        componentManager.handlePluginEnable(event.getPlugin().getName());
    }

    public abstract void registerComponentLoaders();

    /**
     * Create a new configuration. This method is only called once on server start
     * and should be used to setup base configuration information and migrate legacy configurations
     *
     * @return The initialized configuration
     */
    public YAMLProcessor createConfiguration() {
        return populateConfiguration();
    }

    public void loadConfiguration() {
        if  (config == null) {
            config = createConfiguration();
        }
        populateConfiguration(config);

        lowPriorityCommandRegistration = config.getBoolean("low-priority-command-registration", false);
        opPermissions = config.getBoolean("op-permissions", true);
    }

    /**
     * No longer used
     *
     * @return The new configuration instance
     * @deprecated see {@link #populateConfiguration(YAMLProcessor)} and {@link #createConfiguration()}
     */
    @Deprecated
    public YAMLProcessor populateConfiguration() {return config;}

    public void populateConfiguration(YAMLProcessor processor) {
    }

    /**
     * Get the permissions resolver.
     *
     * @return The permissions resolver
     */
    public PermissionsResolverManager getPermissionsResolver() {
        return PermissionsResolverManager.getInstance();
    }

    public YAMLProcessor getGlobalConfiguration() {
        return config;
    }

    public ComponentManager<BukkitComponent> getComponentManager() {
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
     * Checks permissions.
     *
     * @param sender The sender to check
     * @param perm The permission to check
     * @return Whether the sender has the permission
     */
    public boolean hasPermission(CommandSender sender, String perm) {
        if (!(sender instanceof Player)) {
            return ((sender.isOp() && opPermissions) || sender instanceof ConsoleCommandSender
                    || sender instanceof BlockCommandSender
                    || getPermissionsResolver().hasPermission(sender.getName(), perm));
        }
        return hasPermission(sender, ((Player) sender).getWorld(), perm);
    }

    public boolean hasPermission(CommandSender sender, World world, String perm) {
        if ((sender.isOp() && opPermissions) || sender instanceof ConsoleCommandSender
                || sender instanceof BlockCommandSender) {
            return true;
        }

        // Invoke the permissions resolver
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return getPermissionsResolver().hasPermission(world.getName(), player.getName(), perm);
        }

        return false;
    }

    /**
     * Checks permissions and throws an exception if permission is not met.
     *
     * @param sender The sender to check
     * @param perm the permission to check
     * @throws com.sk89q.minecraft.util.commands.CommandPermissionsException if the sender
     * doesn't have the required permission
     */
    public void checkPermission(CommandSender sender, String perm)
            throws CommandPermissionsException {
        if (!hasPermission(sender, perm)) {
            throw new CommandPermissionsException();
        }
    }

    public void checkPermission(CommandSender sender, World world, String perm)
            throws CommandPermissionsException {
        if (!hasPermission(sender, world, perm)) {
            throw new CommandPermissionsException();
        }
    }
}
