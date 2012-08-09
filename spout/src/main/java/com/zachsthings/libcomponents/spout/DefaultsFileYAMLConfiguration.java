package com.zachsthings.libcomponents.spout;

import org.spout.api.util.config.yaml.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * A simple YAMLConfiguration subclass that loads YAML files from the jar's defaults/ folder
 */
public class DefaultsFileYamlConfiguration extends YamlConfiguration {
    private final String file;

    public DefaultsFileYamlConfiguration(String file, boolean writeDefaults) {
        super((File) null);
        setWritesDefaults(writeDefaults);
        this.file = file;
    }

    @Override
    public Reader getReader() {
        return new InputStreamReader(getClass().getResourceAsStream("/defaults/" + file));
    }
}
