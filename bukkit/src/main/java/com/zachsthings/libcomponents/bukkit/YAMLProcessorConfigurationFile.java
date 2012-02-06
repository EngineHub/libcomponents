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

import com.sk89q.util.yaml.YAMLProcessor;
import com.zachsthings.libcomponents.config.ConfigurationFile;

import java.io.IOException;

/**
 *
 * @author zml2008
 */
public class YAMLProcessorConfigurationFile extends YAMLNodeConfigurationNode implements ConfigurationFile {
    private final YAMLProcessor wrapped;

    public YAMLProcessorConfigurationFile(YAMLProcessor wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }

    public void load() throws IOException {
        wrapped.load();
    }

    public boolean save() {
        return wrapped.save();
    }
}
