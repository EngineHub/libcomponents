package com.zachsthings.libcomponents.spout;

import com.zachsthings.libcomponents.config.ConfigurationFile;
import org.spout.api.exception.ConfigurationException;
import org.spout.api.util.config.Configuration;

import java.io.IOException;

/**
 *
 * @author zml2008
 */
public class SpoutConfigurationFile extends SpoutConfigurationNode implements ConfigurationFile {
    private final Configuration wrapped;

    public SpoutConfigurationFile(Configuration wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }

    public void load() throws IOException {
        try {
            wrapped.load();
        } catch (ConfigurationException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException(e);
            }
        }
    }

    public boolean save() {
        try {
            wrapped.save();
            return true;
        } catch (ConfigurationException e) {
            return false;
        }
    }
}
