package com.zachsthings.libcomponents.config;

import java.io.IOException;

/**
 *
 * @author zml2008
 */
public interface ConfigurationFile extends ConfigurationNode {
    public void load() throws IOException;

    public boolean save();
}
