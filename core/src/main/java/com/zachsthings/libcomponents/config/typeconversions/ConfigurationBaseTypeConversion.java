package com.zachsthings.libcomponents.config.typeconversions;

import com.zachsthings.libcomponents.config.ConfigurationBase;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zml2008
 */
public class ConfigurationBaseTypeConversion extends TypeConversion {
    private static final Map<Class<? extends ConfigurationBase>,
            Constructor<? extends ConfigurationBase>> CACHED_CONSTRUCTORS =
            new HashMap<Class<? extends ConfigurationBase>, Constructor<? extends ConfigurationBase>>();
    @Override
    protected Object cast(Class<?> target, Type[] neededGenerics, Object value) {
        Class<? extends ConfigurationBase> configClass = target.asSubclass(ConfigurationBase.class);
        Constructor<? extends ConfigurationBase> constructor = CACHED_CONSTRUCTORS.get(configClass);
        if (constructor == null) {
            try {
                constructor = configClass.getDeclaredConstructor();
                constructor.setAccessible(true);
            } catch (NoSuchMethodException e) {
                return null;
            }
            CACHED_CONSTRUCTORS.put(configClass, constructor);
        }
        ConfigurationBase config = null;

        try {
            config = constructor.newInstance();
        } catch (InstantiationException ignore) {
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace();
        }

        /*if (config != null) {
            config.load();
        }*/

        return config;
    }

    @Override
    public boolean isApplicable(Class<?> target, Object value) {
        return ConfigurationBase.class.isAssignableFrom(target) && value instanceof Map;
    }

    @Override
    protected int getParametersRequired() {
        return -1;
    }
}
