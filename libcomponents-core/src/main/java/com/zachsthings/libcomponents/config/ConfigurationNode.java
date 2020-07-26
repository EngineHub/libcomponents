package com.zachsthings.libcomponents.config;

import java.util.List;

/**
 *
 * @author zml2008
 */
public interface ConfigurationNode {
    public ConfigurationNode getNode(String node);

    public Object getProperty(String node);

    public void setProperty(String node, Object value);

    public void removeProperty(String node);

    public List<String> getKeys(String node);

    public boolean getBoolean(String node);

    public String getString(String node, String def);

    public List<String> getStringList(String node, List<String> def);
}
