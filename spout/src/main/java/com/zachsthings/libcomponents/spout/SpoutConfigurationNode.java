package com.zachsthings.libcomponents.spout;

import com.zachsthings.libcomponents.config.ConfigurationNode;
import org.spout.api.util.config.ConfigurationNodeSource;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zml2008
 */
public class SpoutConfigurationNode implements ConfigurationNode {

    private final ConfigurationNodeSource wrapped;

    public SpoutConfigurationNode(ConfigurationNodeSource wrapped) {
        this.wrapped = wrapped;
    }

    public ConfigurationNode getNode(String node) {
        ConfigurationNodeSource ret = wrapped.getNode(node);
        return new SpoutConfigurationNode(ret);
    }

    public Object getProperty(String node) {
        return wrapped.getNode(node).getValue();
    }

    public void setProperty(String node, Object value) {
        wrapped.getNode(node).setValue(value);
    }

    public void removeProperty(String node) {
        wrapped.getNode(node).setValue(null);
    }

    public List<String> getKeys(String node) {
        return new ArrayList<String>(wrapped.getNode(node).getKeys(false));
    }

    public boolean getBoolean(String node) {
        return wrapped.getNode(node).getBoolean();
    }

    public String getString(String node, String def) {
        return wrapped.getNode(node).getString(def);
    }

    public List<String> getStringList(String node, List<String> def) {
        return wrapped.getNode(node).getStringList(def);
    }
}
