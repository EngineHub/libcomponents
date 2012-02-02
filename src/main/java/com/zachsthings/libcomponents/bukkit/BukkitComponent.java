package com.zachsthings.libcomponents.bukkit;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import com.zachsthings.libcomponents.AbstractComponent;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.loader.ComponentLoader;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * A component written for a Bukkit server
 */
public abstract class BukkitComponent extends AbstractComponent<CommandSender> implements CommandExecutor {

    /**
     * The {@link com.sk89q.bukkit.util.CommandsManagerRegistration} used to handle dynamic
     * registration of commands contained within this component
     */
    private CommandsManagerRegistration commandRegistration;

    /**
     * The BasePlugin is a base for all plugins that use this component system.
     */
    private BasePlugin plugin;

    public void setUp(BasePlugin plugin, CommandsManager<CommandSender> commands, ComponentLoader loader, ComponentInformation info) {
        super.setUp(commands, loader, info);
        this.plugin = plugin;
        commandRegistration = new CommandsManagerRegistration(plugin, this, commands);
    }

    public void disable() {
        unregisterCommands();
    }

    // -- Command registration
    public void registerCommands(final Class<?> clazz)  {
        if (plugin.lowPriorityCommandRegistration) {
            BasePlugin.server().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    commandRegistration.register(clazz);
                }
            }, 0L);
        } else {
            commandRegistration.register(clazz);
        }
    }

    public void unregisterCommands() {
        commandRegistration.unregisterCommands();
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        try {
            commands.execute(alias, args, sender, sender);
            return true;
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (UnhandledCommandException e) {
            sender.sendMessage("Unknown command: " + command.getName() + "! This should never be happening!");
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
        return false;
    }
}
