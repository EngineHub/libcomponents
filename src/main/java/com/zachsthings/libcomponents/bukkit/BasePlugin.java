package com.zachsthings.libcomponents.bukkit;

import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.PermissionsResolverManager;
import com.zachsthings.libcomponents.ComponentManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @author zml2008
 */
public abstract class BasePlugin extends JavaPlugin {

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
    protected ComponentManager<CommandSender> componentManager;

    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        componentManager.unloadComponents();
    }

    public void onEnable() {
        // Make the data folder for the plugin where configuration files
        // and other data files will be stored
        getDataFolder().mkdirs();

        loadConfiguration();

        // Prepare permissions
        PermissionsResolverManager.initialize(this);

        componentManager = new ComponentManager<CommandSender>(getLogger()) {
            @Override
            protected CommandsManager<CommandSender> createComponentManager() {
                return new CommandsManager<CommandSender>() {
                    @Override
                    public boolean hasPermission(CommandSender sender, String permission) {
                        return BasePlugin.this.hasPermission(sender, permission);
                    }
                };
            }
        };

        registerComponentLoaders();

        componentManager.loadComponents();
        
        config.save();
    }

    public abstract void registerComponentLoaders();

    public void loadConfiguration() {
        config = populateConfiguration();

        lowPriorityCommandRegistration = config.getBoolean("low-priority-command-registration", false);
        opPermissions = config.getBoolean("op-permissions", true);
    }

    public abstract YAMLProcessor populateConfiguration();

    /**
     * Get the permissions resolver.
     *
     * @return
     */
    public PermissionsResolverManager getPermissionsResolver() {
        return PermissionsResolverManager.getInstance();
    }

    public YAMLProcessor getGlobalConfiguration() {
        return config;
    }

    public ComponentManager<?> getComponentManager() {
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
     * @param sender
     * @param perm
     * @return
     */
    public boolean hasPermission(CommandSender sender, String perm) {
        if (!(sender instanceof Player)) {
            return ((sender.isOp() && (opPermissions || sender instanceof ConsoleCommandSender))
                    || getPermissionsResolver().hasPermission(sender.getName(), perm));
        }
        return hasPermission(sender, ((Player) sender).getWorld(), perm);
    }

    public boolean hasPermission(CommandSender sender, World world, String perm) {
        if ((sender.isOp() && opPermissions) || sender instanceof ConsoleCommandSender) {
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
     * @param sender
     * @param perm
     * @throws com.sk89q.minecraft.util.commands.CommandPermissionsException
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
