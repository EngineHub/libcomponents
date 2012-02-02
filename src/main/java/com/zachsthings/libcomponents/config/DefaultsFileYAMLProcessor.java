package com.zachsthings.libcomponents.config;

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
