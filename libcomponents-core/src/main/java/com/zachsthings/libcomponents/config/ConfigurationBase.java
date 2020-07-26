package com.zachsthings.libcomponents.config;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import static com.zachsthings.libcomponents.config.ConfigUtil.prepareSerialization;
import static com.zachsthings.libcomponents.config.ConfigUtil.smartCast;

/**
 * The base class for configuration of {@link com.zachsthings.libcomponents.AbstractComponent}s
 */
public abstract class ConfigurationBase {
    private static final Logger logger = Logger.getLogger(ConfigurationBase.class.getCanonicalName());

    private boolean isConfigured;

    public boolean isConfigured() {
        return isConfigured;
    }
    
    public void load(ConfigurationNode node) {
        if (getClass().isAnnotationPresent(SettingBase.class)) {
            node = node.getNode(getClass().getAnnotation(SettingBase.class).value());
        }
        
        for (Field field : getFieldsRecur(getClass())) {
            if (!field.isAnnotationPresent(Setting.class)) continue;
            String key = field.getAnnotation(Setting.class).value();
            final Object value = smartCast(field.getGenericType(), node.getProperty(key));
            try {
                field.setAccessible(true);
                if (value != null) {
                    field.set(this, value);
                } else {
                    node.setProperty(key, prepareSerialization(field.get(this)));
                }
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "Error setting configuration value of field: ", e);
                e.printStackTrace();
            }
        }
        isConfigured = true;
    }
    
    public void save(ConfigurationNode node) {
        if (getClass().isAnnotationPresent(SettingBase.class)) {
            node = node.getNode(getClass().getAnnotation(SettingBase.class).value());
        }
        for (Field field : getFieldsRecur(getClass())) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(Setting.class)) continue;
            String key = field.getAnnotation(Setting.class).value();
            try {
                node.setProperty(key, prepareSerialization(field.get(this)));
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "Error getting configuration value of field: ", e);
                e.printStackTrace();
            }
        }
    }
    private static List<Field> getFieldsRecur(Class<?> clazz) {
        return getFieldsRecur(clazz, false);
    }
    
    private static List<Field> getFieldsRecur(Class<?> clazz, boolean includeObject) {
        List<Field> fields = new ArrayList<Field>();
        while(clazz != null && (includeObject || !Object.class.equals(clazz))) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
