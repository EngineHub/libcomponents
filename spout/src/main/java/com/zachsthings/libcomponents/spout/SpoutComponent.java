package com.zachsthings.libcomponents.spout;

import com.zachsthings.libcomponents.AbstractComponent;
import org.spout.api.Spout;
import org.spout.api.command.*;
import org.spout.api.command.annotated.*;
import org.spout.api.event.HandlerList;
import org.spout.api.event.Listener;
import org.spout.api.scheduler.TaskPriority;
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
        HandlerList.unregisterAll(this);
    }

    // -- Command registration
    protected void registerCommands(final Class<?> clazz)  {
        if (plugin.lowPriorityCommandRegistration) {
            BasePlugin.engine().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    BasePlugin.engine().getRootCommand().addSubCommands(SpoutComponent.this, clazz, commandRegistration);
                }
            }, 0L, TaskPriority.LOWEST);
        } else {
            BasePlugin.engine().getRootCommand().addSubCommands(this, clazz, commandRegistration);
        }
    }

    public void unregisterCommands() {
        BasePlugin.engine().getRootCommand().removeChildren(this);
    }

    protected void registerEvents(Listener listener) {
        Spout.getEngine().getEventManager().registerEvents(listener, this);
    }

    @Override
    public Map<String, String> getCommands() {
        Collection<org.spout.api.command.Command> cmds = BasePlugin.engine().getRootCommand().getChildCommands();
        Map<String, String> ret = new HashMap<String, String>();
        for (org.spout.api.command.Command cmd : cmds) {
            if (cmd.isOwnedBy(this)) {
                ret.put(cmd.getPreferredName(), cmd.getUsage());
            }
        }
        return ret;
    }

    public String getName() {
        return getInformation().friendlyName();
    }
}
