/*
 * CommandBook
 * Copyright (C) 2011 sk89q <http://www.sk89q.com>
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

package com.zachsthings.libcomponents.loader;

import com.zachsthings.libcomponents.AbstractComponent;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Loads components from a list specified in the constructor. This is a bit of a workaround until dependencies can be implemented..
 */
public class StaticComponentLoader extends AbstractComponentLoader {
    private final List<AbstractComponent> components;

    public StaticComponentLoader(Logger logger, File configDir, AbstractComponent... components) {
        super(logger, configDir);
        this.components = Arrays.asList(components);
    }
    public Collection<AbstractComponent> loadComponents() {
        return components;
    }
}
