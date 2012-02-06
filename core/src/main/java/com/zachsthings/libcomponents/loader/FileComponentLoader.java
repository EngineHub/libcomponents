package com.zachsthings.libcomponents.loader;

import java.io.File;
import java.util.logging.Logger;

/**
 * A parent class for component loaders that load components from the raw filesystem.
 */
public abstract class FileComponentLoader extends AbstractComponentLoader {
    protected FileComponentLoader(Logger logger, File configDir) {
        super(logger, configDir);
    }
    
    public String formatPath(String path) {
        if (path.length() < 6) return path;
        return path.substring(0, path.length() - 6).replaceAll("/", ".");
    }
}
