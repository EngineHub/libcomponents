package com.zachsthings.libcomponents.config;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tests for ConfigurationBase
 */
public class ConfigurationBaseTest {

    // Constants for easier accurate results
    private static final String BOOLEAN_KEY = "boolean-setting";
    private static final boolean BOOLEAN_VALUE = true;
    private static final String INT_KEY = "int-setting";
    private static final int INT_VALUE = 42;
    private static final String NESTED_STRING_KEY = "nested.key";
    private static final String NESTED_STRING_VALUE = "cute asian cadvahns";
    private static final String MAP_STRING_STRING_KEY = "map-string-string";
    private static final Map<String, String> MAP_STRING_STRING_VALUE = createMapStringString();
    private static final String SET_INTEGER_KEY = "int-set";
    private static final Set<Integer> SET_INTEGER_VALUE = new HashSet<Integer>(Arrays.asList(1, 2, 3, 4 , 5));

    private static class LocalConfiguration extends ConfigurationBase {
        @Setting(BOOLEAN_KEY) public boolean booleanSetting;
        @Setting(INT_KEY) public int intSetting;
        @Setting(NESTED_STRING_KEY) public String nestedStringSetting;
        @Setting(MAP_STRING_STRING_KEY) public Map<String, String> mapStringStringSetting;
        @Setting(SET_INTEGER_KEY) public Set<Integer> setInteger;
    }

    private static Map<String, String> createMapStringString() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("hello", "world");
        result.put("command", "book");
        return result;
    }

    // The real tests!

    protected ConfigurationNode node;
    protected LocalConfiguration config;


    @Before
    public void setUp() {
        node = new TerribleConfigurationNode();
        node.setProperty(BOOLEAN_KEY, BOOLEAN_VALUE);
        node.setProperty(INT_KEY, INT_VALUE);
        node.setProperty(NESTED_STRING_KEY, NESTED_STRING_VALUE);
        node.setProperty(MAP_STRING_STRING_KEY, MAP_STRING_STRING_VALUE);
        node.setProperty(SET_INTEGER_KEY, new ArrayList<Integer>(SET_INTEGER_VALUE));
        config = new LocalConfiguration();
        config.load(node);
    }

    @Test
    public void testBooleanValue() {
        assertEquals(BOOLEAN_VALUE, config.booleanSetting);
    }

    @Test
    public void testIntValue() {
        assertEquals(INT_VALUE, config.intSetting);
    }

    @Test
    public void testNestedStringValue() {
        assertEquals(NESTED_STRING_VALUE, config.nestedStringSetting);
    }

    @Test
    public void testMapStringStringValue() {
        assertEquals(MAP_STRING_STRING_VALUE, config.mapStringStringSetting);
    }

    @Test
    public void testSetIntegerValue() {
        assertEquals(SET_INTEGER_VALUE, config.setInteger);
    }

}
