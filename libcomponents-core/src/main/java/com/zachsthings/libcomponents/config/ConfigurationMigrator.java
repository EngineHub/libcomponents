package com.zachsthings.libcomponents.config;

import java.io.File;
import java.util.Map;

/**
 * A simple migrator for configurations that moves values from one key to another.
 * Values do not have their types converted.
 */
public abstract class ConfigurationMigrator {
    protected final ConfigurationFile config;
    protected final File oldFile;
    
    protected ConfigurationMigrator(File configFile, ConfigurationFile processor) {
        this.oldFile = configFile;
        this.config = processor;
    }
    
    protected abstract Map<String, String> getMigrationKeys();

    protected abstract boolean shouldMigrate();
    
    public String migrate() {
        if (!shouldMigrate()) {
            return null;
        }

        if (!oldFile.renameTo(new File(oldFile.getAbsolutePath() + ".old"))) {
            return "Unable to rename backup old configuration file!";
        }
        for (Map.Entry<String, String> entry : getMigrationKeys().entrySet()) {
            Object existing = config.getProperty(entry.getKey());
            config.removeProperty(entry.getKey());
            if (existing == null || entry.getValue() == null) {
                continue;
            }
            config.setProperty(entry.getValue().replaceAll("%", entry.getKey()), existing);
        }
        if (!config.save()) {
            return "Failed to save migrated configuration!";
        }
        return null;
    }

}
