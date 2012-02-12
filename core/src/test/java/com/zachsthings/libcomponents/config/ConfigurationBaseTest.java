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
    private static final String MAP_STRING_STRING_KEY = "map.string-string";
    private static final Map<String, String> MAP_STRING_STRING_VALUE = createMapStringString();
    private static final String SET_INTEGER_KEY = "int-set";
    private static final Set<Integer> SET_INTEGER_VALUE = new HashSet<Integer>(Arrays.asList(1, 2, 3, 4 , 5));
    private static final String NESTED_MAP_KEY = "map.nested";
    private static final Map<String, Map<String, Object>> NESTED_MAP_VALUE = createNestedMap();
    private static final String DEFAULT_KEY = "dead";
    private static final String DEFAULT_VALUE = "parrot";

    private static class LocalConfiguration extends ConfigurationBase {
        @Setting(BOOLEAN_KEY) public boolean booleanSetting;
        @Setting(INT_KEY) public int intSetting;
        @Setting(NESTED_STRING_KEY) public String nestedStringSetting;
        @Setting(MAP_STRING_STRING_KEY) public Map<String, String> mapStringStringSetting;
        @Setting(SET_INTEGER_KEY) public Set<Integer> setIntegerSetting;
        @Setting(NESTED_MAP_KEY) public Map<String, Map<String, Object>> nestedMapSetting;
        @Setting(DEFAULT_KEY) public String defaultSetting = DEFAULT_VALUE;
    }

    private static Map<String, String> createMapStringString() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("hello", "world");
        result.put("command", "book");
        return result;
    }
    
    private static Map<String, Map<String, Object>> createNestedMap() {
        Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
        result.put("one", (Map)createMapStringString());
        Map<String, Object> two = new HashMap<String, Object>();
        two.put("something", "else");
        two.put("andnowforsomething", "completelydifferent");
        result.put("two", two);
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
        node.setProperty(NESTED_MAP_KEY, NESTED_MAP_VALUE);
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
        assertEquals(SET_INTEGER_VALUE, config.setIntegerSetting);
    }
    
    @Test
    public void testNestedMapValue() {
        assertEquals(NESTED_MAP_VALUE, config.nestedMapSetting);
    }
    
    @Test
    public void testDefaultValue() {
        assertEquals(DEFAULT_VALUE, config.defaultSetting);
    }

}
