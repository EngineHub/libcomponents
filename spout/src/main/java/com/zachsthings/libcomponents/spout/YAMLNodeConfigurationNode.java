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

import com.sk89q.util.yaml.YAMLNode;
import com.zachsthings.libcomponents.config.ConfigurationNode;

import java.util.List;

/**
 *
 * @author zml2008
 */
public class YAMLNodeConfigurationNode implements ConfigurationNode {

    private final YAMLNode wrapped;

    public YAMLNodeConfigurationNode(YAMLNode wrapped) {
        this.wrapped = wrapped;
    }

    public ConfigurationNode getNode(String node) {
        YAMLNode ret = wrapped.getNode(node);
        if (ret == null) {
            ret = wrapped.addNode(node);
        }
        return new YAMLNodeConfigurationNode(ret);
    }

    public Object getProperty(String node) {
        return wrapped.getProperty(node);
    }

    public void setProperty(String node, Object value) {
        wrapped.setProperty(node, value);
    }

    public void removeProperty(String node) {
        wrapped.removeProperty(node);
    }

    public List<String> getKeys(String node) {
        return wrapped.getKeys(node);
    }

    public boolean getBoolean(String node) {
        return wrapped.getBoolean(node);
    }

    public String getString(String node, String def) {
        return wrapped.getString(node, def);
    }

    public List<String> getStringList(String node, List<String> def) {
        return wrapped.getStringList(node, def);
    }
}
