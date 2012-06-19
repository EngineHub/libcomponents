package com.zachsthings.libcomponents.spout;

import com.zachsthings.libcomponents.ComponentManager;
import com.zachsthings.libcomponents.InvalidComponentException;
import com.zachsthings.libcomponents.config.ConfigurationFile;
import org.spout.api.Engine;
import org.spout.api.Spout;
import org.spout.api.command.CommandSource;
import org.spout.api.event.Event;
import org.spout.api.exception.CommandException;
import org.spout.api.exception.ConfigurationException;
import org.spout.api.geo.World;
import org.spout.api.plugin.CommonPlugin;
import org.spout.api.util.config.Configuration;

import java.io.*;
import java.util.jar.JarFile;
import java.util.logging.Level;
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

    protected Configuration config;
    private SpoutConfigurationFile abstractConfig;
    protected ComponentManager<SpoutComponent> componentManager;

    public void onDisable() {
        this.getEngine().getScheduler().cancelTasks(this);
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

        try {
            config.save();
        } catch (ConfigurationException e) {
            getLogger().log(Level.WARNING, "Error saving configuration for " + getName(), e);
        }
    }

    public abstract void registerComponentLoaders();

    public void loadConfiguration() {
        config = populateConfiguration();
        abstractConfig = new SpoutConfigurationFile(config);

        lowPriorityCommandRegistration = config.getNode("low-priority-command-registration").getBoolean(false);
    }

    public abstract Configuration populateConfiguration();

    public Configuration getGlobalConfiguration() {
        return config;
    }

    public ConfigurationFile getAbstractedConfiguration() {
        return abstractConfig;
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
