package com.zachsthings.libcomponents.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author zml2008
 */
public class TerribleConfigurationNode implements ConfigurationNode {
    private final Map<String, Object> properties = new HashMap<String, Object>();
    public ConfigurationNode getNode(String node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getProperty(String node) {
        return properties.get(node);
    }

    public void setProperty(String node, Object value) {
        properties.put(node, value);
    }

    public void removeProperty(String node) {
        properties.remove(node);
    }

    public List<String> getKeys(String node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean getBoolean(String node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getString(String node, String def) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<String> getStringList(String node, List<String> def) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
