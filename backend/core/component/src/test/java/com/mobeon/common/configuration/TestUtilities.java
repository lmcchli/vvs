/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.configuration;

import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.TestCase;

import java.util.List;

/**
 * User: eperber
 * Date: 2005-sep-20
 * Time: 16:12:12.
 */
public final class TestUtilities extends TestCase {

    static {
        ILoggerFactory.configureAndWatch("log4j2conf.xml");
    }

    public TestUtilities() {
    }

    private static <T> void assertEqualsArray(T[] lhs, T[] rhs) {
        assertEquals("Array length: ", lhs.length, rhs.length);
        for (int i = 0; i < lhs.length; ++i) {
            assertEquals(lhs[i], rhs[i]);
        }
    }

    private static <T> void assertEqualsArrayList(T[] lhs, List<T> rhs) {
        assertEquals("Array length: ", lhs.length, rhs.size());
        for (int i = 0; i < lhs.length; ++i) {
            assertEquals(lhs[i], rhs.get(i));
        }
    }

    /**
     * .
     */
    public void testDotSplit() {
        assertEqualsArrayList(new String[]{"1", "2", "3"}, Utilities.dotSplit("1.2.3"));
        assertEqualsArrayList(new String[]{"1", "2"}, Utilities.dotSplit("1.2."));
        assertEqualsArrayList(new String[]{"2", "3"}, Utilities.dotSplit(".2.3"));
        assertEqualsArrayList(new String[]{"2"}, Utilities.dotSplit(".2."));
        assertEqualsArrayList(new String[]{}, Utilities.dotSplit("."));
        assertEqualsArrayList(new String[]{"1"}, Utilities.dotSplit("1"));
        assertEqualsArrayList(new String[]{}, Utilities.dotSplit(""));
        assertEqualsArrayList(new String[]{}, Utilities.dotSplit(null));
    }

    /**
     * .
     */
    public void testTail() {
        String[] list = new String[]{"1", "2", "3"};
        list = Utilities.tail(list);
        assertEqualsArray(new String[]{"2", "3"}, list);
        list = Utilities.tail(list);
        assertEqualsArray(new String[]{"3"}, list);
        list = Utilities.tail(list);
        assertEqualsArray(new String[]{}, list);
        list = Utilities.tail(list);
        assertEqualsArray(new String[]{}, list);
    }
}
