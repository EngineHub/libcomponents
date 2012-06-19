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

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import com.zachsthings.libcomponents.AbstractComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Map;

/**
 * A component written for a Bukkit server
 */
public abstract class BukkitComponent extends AbstractComponent implements CommandExecutor {

    /**
     * The {@link com.sk89q.bukkit.util.CommandsManagerRegistration} used to handle dynamic
     * registration of commands contained within this component
     */
    private CommandsManagerRegistration commandRegistration;

    /**
     * The {@link CommandsManager} where all commands are registered for this component.
     */
    protected CommandsManager<CommandSender> commands;

    /**
     * The BasePlugin is a base for all plugins that use this component system.
     */
    private BasePlugin plugin;

    public void setUp(BasePlugin plugin, CommandsManager<CommandSender> commands) {
        this.commands = commands;
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
            }, 1L);
        } else {
            commandRegistration.register(clazz);
        }
    }

    public void unregisterCommands() {
        commandRegistration.unregisterCommands();
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        try {
            commands.execute(command.getName(), args, sender, sender);
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

    @Override
    public Map<String, String> getCommands() {
        if (commands == null) {
            return Collections.emptyMap();
        } else {
            return commands.getCommands();
        }
    }
}
