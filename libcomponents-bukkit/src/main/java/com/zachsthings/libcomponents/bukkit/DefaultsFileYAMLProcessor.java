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

import java.io.InputStream;

/**
 * A simple YAMLProcessor that loads YAML files from the jar's defaults/ folder
 */
public class DefaultsFileYAMLProcessor extends YAMLProcessor {
    private final String file;
    
    public DefaultsFileYAMLProcessor(String file, boolean writeDefaults) {
        super(null, writeDefaults);
        this.file = file;
    }

    @Override
    public InputStream getInputStream() {
        return getClass().getResourceAsStream("/defaults/" + file);
    }
}
