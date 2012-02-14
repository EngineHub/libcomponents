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

import com.zachsthings.libcomponents.AbstractComponent;
import org.spout.api.command.*;
import org.spout.api.command.annotated.*;
import org.spout.api.event.Listener;
import org.spout.api.util.Named;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A component written for a Bukkit server
 */
public abstract class SpoutComponent extends AbstractComponent implements Named {

    /**
     * The BasePlugin is a base for all plugins that use this component system.
     */
    private BasePlugin plugin;
    
    private CommandRegistrationsFactory<Class<?>> commandRegistration;

    protected void setUp(BasePlugin plugin) {
        this.plugin = plugin;
        commandRegistration = new AnnotatedCommandRegistrationFactory(new SimpleInjector(this), new SimpleAnnotatedCommandExecutorFactory());
    }

    public void disable() {
        unregisterCommands();
    }

    // -- Command registration
    public void registerCommands(final Class<?> clazz)  {
        if (plugin.lowPriorityCommandRegistration) {
            BasePlugin.game().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    BasePlugin.game().getRootCommand().addSubCommands(SpoutComponent.this, clazz, commandRegistration);
                }
            }, 0L);
        } else {
            BasePlugin.game().getRootCommand().addSubCommands(this, clazz, commandRegistration);
        }
    }

    public void unregisterCommands() {
        BasePlugin.game().getRootCommand().removeChildren(this);
    }
    
    protected void registerEvents(Listener listener) {
        
    }

    @Override
    public Map<String, String> getCommands() {
        Collection<org.spout.api.command.Command> cmds = BasePlugin.game().getRootCommand().getChildCommands();
        Map<String, String> ret = new HashMap<String, String>();
        for (org.spout.api.command.Command cmd : cmds) {
            if (cmd.unlock(this)) {
                cmd.lock(this);
                ret.put(cmd.getPreferredName(), cmd.getUsage(new String[1], 0));
            }
        }
        return ret;
    }
    
    public String getName() {
        return getInformation().friendlyName();
    }
}
