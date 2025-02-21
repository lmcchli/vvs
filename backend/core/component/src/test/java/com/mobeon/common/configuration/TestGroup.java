/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.configuration;

import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * User: eperber
 * Date: 2005-sep-20
 * Time: 12:18:34.
 */
public final class TestGroup extends TestCase {
    private final Collection<String> configFileNames = new LinkedList<String>();
    private final String configFileName = "test/com/mobeon/common/configuration/cfg/mas.cfg";
    private final String configFileName2 = "test/com/mobeon/common/configuration/cfg/mas2.cfg";
    private IConfiguration config;

    static {
        ILoggerFactory.configureAndWatch("log4j2conf.xml");
    }

    /**
     * .
     *
     * @param lhs .
     * @param rhs .
     */
    private static <T> void assertEqualsArray(T[] lhs, T[] rhs) {
        assertEquals("Array length: ", lhs.length, rhs.length);
        for (int i = 0; i < lhs.length; ++i) {
            assertEquals(lhs[i], rhs[i]);
        }
    }

    /**
     * .
     *
     * @param lhs .
     * @param rhs .
     */
    private static void assertEqualsIntArray(int[] lhs, int[] rhs) {
        assertEquals("Array length: ", lhs.length, rhs.length);
        for (int i = 0; i < lhs.length; ++i) {
            assertEquals(lhs[i], rhs[i]);
        }
    }

    /**
     * .
     *
     * @param lhs .
     * @param rhs .
     */
    private static void assertEqualsDoubleArray(double[] lhs, double[] rhs) {
        assertEquals("Array length: ", lhs.length, rhs.length);
        for (int i = 0; i < lhs.length; ++i) {
            assertEquals(lhs[i], rhs[i]);
        }
    }

    /**
     * .
     */
    public TestGroup() {
        super();
        configFileNames.add(configFileName);
        configFileNames.add(configFileName2);
    }

    /**
     * .
     *
     * @throws MissingConfigurationFileException
     *
     */
    protected void setUp() throws Exception {
        super.setUp();
        config = new ConfigurationImpl(null, configFileNames, false);
    }

    /**
     * .
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        config = null;
    }

    /**
     * .
     *
     * @throws UnknownGroupException
     */
    public void testGetGroup() throws GroupCardinalityException, UnknownGroupException {
        IGroup g = config.getGroup("KnownGroup");
        assertNotNull(g);
        assertEquals("KnownGroup", g.getName());
        IGroup g2 = g.getGroup("");
        assertSame(g, g2);
    }

    /**
     * .
     *
     * @throws UnknownGroupException
     */
    public void testGetGroup2() throws UnknownGroupException {
        try {
            config.getGroup("KnownGroup.MultiGroup");
            fail("Should have thrown GroupCardinalityException.");
        } catch (UnknownGroupException e) {
            throw e;
        } catch (GroupCardinalityException e) {
        }
    }

    /**
     * .
     *
     * @throws UnknownGroupException
     * @throws UnknownParameterException
     */
    public void testGetGroups() throws UnknownGroupException, UnknownParameterException {
        List<IGroup> groups = config.getGroups("KnownGroup.MultiGroup");
        assertNotNull(groups);
        assertEquals(groups.size(), 2);
        assertEquals(groups.get(0).getString("Group"), "M1");
        assertEquals(groups.get(1).getString("Group"), "M2");
        for (IGroup group : groups) {
            assertEquals("MultiGroup", group.getName());
        }
    }

    /**
     * .
     *
     * @throws UnknownGroupException
     * @throws UnknownParameterException
     */
    public void testGetGroups2() throws UnknownGroupException, UnknownParameterException {
        List<IGroup> groups = config.getGroups("KnownGroup.MultiGroup.MultiSubGroup");
        assertNotNull(groups);
        assertEquals(groups.size(), 4);
        assertEquals(groups.get(0).getString("Group"), "M1S1");
        assertEquals(groups.get(1).getString("Group"), "M1S2");
        assertEquals(groups.get(2).getString("Group"), "M2S1");
        assertEquals(groups.get(3).getString("Group"), "M2S2");
        for (IGroup group : groups) {
            assertEquals("MultiSubGroup", group.getName());
        }
    }

    /**
     * .
     *
     * @throws GroupCardinalityException
     * @throws UnknownGroupException
     * @throws ParameterTypeException
     */
    public void testGetUnkwnownInt() throws GroupCardinalityException, UnknownGroupException, ParameterTypeException {
        IGroup g = config.getGroup("KnownGroup");

        int value;
        int defaultValue = 1;
        try {
            g.getInteger("UnknownParameter");
            fail("Should have thrown UnknownParameterException.");
        } catch (UnknownParameterException e) {
            assertEquals("KnownGroup", e.getGroupName());
            assertEquals("UnknownParameter", e.getParameterName());
        } catch (ParameterTypeException e) {
            e.printStackTrace();
        }
        value = g.getInteger("UnknownParameter", defaultValue);
        assertEquals(defaultValue, value);
    }

    /**
     * .
     *
     * @throws GroupCardinalityException
     * @throws UnknownGroupException
     * @throws UnknownParameterException
     * @throws ParameterTypeException
     */
    public void testGetInt() throws GroupCardinalityException, UnknownGroupException, ParameterTypeException, UnknownParameterException {
        IGroup g = config.getGroup("KnownGroup");

        int defaultValue = 1;
        int knownValue = 2;
        int value = g.getInteger("KnownInteger");
        assertEquals(knownValue, value);
        try {
            value = g.getInteger("KnownInteger", defaultValue);
        } catch (ParameterTypeException e) {
            e.printStackTrace();
        }
        assertEquals(knownValue, value);
    }

    /**
     * .
     *
     * @throws GroupCardinalityException
     * @throws UnknownGroupException
     */
    public void testGetUnkwnownString() throws GroupCardinalityException, UnknownGroupException {
        IGroup g = config.getGroup("KnownGroup");

        String value;
        String defaultValue = "DefaultString";
        try {
            g.getString("UnknownParameter");
            fail("Should have thrown UnknownParameterException.");
        } catch (UnknownParameterException e) {
            assertEquals("KnownGroup", e.getGroupName());
            assertEquals("UnknownParameter", e.getParameterName());
        }
        value = g.getString("UnknownParameter", defaultValue);
        assertEquals(defaultValue, value);
    }

    /**
     * .
     *
     * @throws GroupCardinalityException
     * @throws UnknownGroupException
     * @throws UnknownParameterException
     */
    public void testGetString() throws GroupCardinalityException, UnknownGroupException, UnknownParameterException {
        IGroup g = config.getGroup("KnownGroup");

        String defaultValue = "default";
        String knownValue = "known";
        String value = g.getString("KnownString");
        assertEquals(knownValue, value);
        value = g.getString("KnownString", defaultValue);
        assertEquals(knownValue, value);
    }

    /**
     * .
     *
     * @throws GroupCardinalityException
     * @throws UnknownGroupException
     * @throws ParameterTypeException
     */
    public void testGetUnkwnownFloat() throws GroupCardinalityException, UnknownGroupException, ParameterTypeException {
        IGroup g = config.getGroup("KnownGroup");

        double value;
        double defaultValue = 1.2;
        try {
            g.getFloat("UnknownParameter");
            fail("Should have thrown UnknownParameterException.");
        } catch (UnknownParameterException e) {
            assertEquals("KnownGroup", e.getGroupName());
            assertEquals("UnknownParameter", e.getParameterName());
        }
        value = g.getFloat("UnknownParameter", defaultValue);
        assertEquals(defaultValue, value);
    }

    /**
     * .
     *
     * @throws GroupCardinalityException
     * @throws UnknownGroupException
     * @throws UnknownParameterException
     * @throws ParameterTypeException
     */
    public void testGetFloat() throws GroupCardinalityException, UnknownGroupException, ParameterTypeException, UnknownParameterException {
        IGroup g = config.getGroup("KnownGroup");

        double defaultValue = 1.2;
        double knownValue = 2.3;
        double value = g.getFloat("KnownFloat");
        assertEquals(knownValue, value);
        value = g.getFloat("KnownFloat", defaultValue);
        assertEquals(knownValue, value);
    }

    public void testGetUnknownBoolean() throws UnknownGroupException, GroupCardinalityException, ParameterTypeException {
        IGroup g = config.getGroup("KnownGroup");

        boolean value;
        try {
            g.getBoolean("UnknownParameter");
            fail("Should have thrown UnknownParameterException.");
        } catch (UnknownParameterException e) {
            assertEquals("KnownGroup", e.getGroupName());
            assertEquals("UnknownParameter", e.getParameterName());
        }
        value = g.getBoolean("UnknownParameter", true);
        assertTrue(value);
    }

    public void testGetBoolean() throws UnknownGroupException, GroupCardinalityException, ParameterTypeException, UnknownParameterException {
        IGroup g = config.getGroup("KnownGroup2");

        boolean value = g.getBoolean("KnownBooleanTrue");
        assertTrue(value);
        value = g.getBoolean("KnownBooleanFalse");
        assertFalse(value);
        value = g.getBoolean("KnownBoolean1");
        assertTrue(value);
        value = g.getBoolean("KnownBoolean0");
        assertFalse(value);

        value = g.getBoolean("KnownBooleanTrue", false);
        assertTrue(value);
        value = g.getBoolean("KnownBooleanFalse", true);
        assertFalse(value);
        value = g.getBoolean("KnownBoolean1", false);
        assertTrue(value);
        value = g.getBoolean("KnownBoolean0", true);
        assertFalse(value);
    }

    /**
     * .
     *
     * @throws GroupCardinalityException
     * @throws UnknownGroupException
     * @throws ParameterTypeException
     */
    public void testSubGroup() throws GroupCardinalityException, UnknownGroupException, ParameterTypeException {
        IGroup g = config.getGroup("KnownGroup.SubGroup");
        int defaultValue = 3;
        int knownValue = 1;
        int value = g.getInteger("SubGroupValue", defaultValue);
        assertEquals(knownValue, value);
    }

    /**
     * .
     *
     * @throws GroupCardinalityException
     * @throws UnknownGroupException
     * @throws ParameterTypeException
     */
    public void testSubGroup2() throws GroupCardinalityException, UnknownGroupException, ParameterTypeException {
        IGroup g = config.getGroup("KnownGroup.SubGroup.SubSubGroup");
        int defaultValue = 3;
        int knownValue = 2;
        int value = g.getInteger("SubSubGroupValue", defaultValue);
        assertEquals(knownValue, value);
    }

    /**
     * .
     *
     * @throws GroupCardinalityException
     * @throws UnknownGroupException
     * @throws ParameterTypeException
     */
    public void testSubGroup3() throws GroupCardinalityException, UnknownGroupException, ParameterTypeException {
        IGroup g1 = config.getGroup("KnownGroup");
        IGroup g2 = g1.getGroup("SubGroup");
        IGroup g3 = g2.getGroup("SubSubGroup");
        int defaultValue = 3;
        int knownValue = 2;
        int value = g3.getInteger("SubSubGroupValue", defaultValue);
        assertEquals(knownValue, value);
    }

    /**
     * .
     *
     * @throws GroupCardinalityException
     * @throws UnknownGroupException
     */
    public void testGetName() throws GroupCardinalityException, UnknownGroupException {
        IGroup g = config.getGroup("KnownGroup.SubGroup.SubSubGroup");
        assertEquals("SubSubGroup", g.getName());
        assertEquals("KnownGroup.SubGroup.SubSubGroup", g.getFullName());
    }

    /**
     * .
     *
     * @throws GroupCardinalityException
     * @throws UnknownGroupException
     * @throws UnknownParameterException
     */
    public void testGetIncorrectType() throws GroupCardinalityException, UnknownGroupException, UnknownParameterException {
        IGroup g = config.getGroup("KnownGroup");
        try {
            g.getInteger("KnownFloat");
        } catch (ParameterTypeException e) {
            assertEquals("Expected", "int", e.getExpected());
        }
        try {
            g.getFloat("KnownString");
        } catch (ParameterTypeException e) {
            assertEquals("Expected", "float", e.getExpected());
        }
    }

    /**
     * .
     *
     * @throws UnknownGroupException
     * @throws GroupCardinalityException
     */
    public void testGetText() throws UnknownGroupException, GroupCardinalityException {
        IGroup g = config.getGroup("KnownGroup");
        assertEquals("KnownGroupTextValue", g.getText());
    }
}
