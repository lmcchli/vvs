/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.util;
import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;

import com.mobeon.masp.util.TimeValue;
import com.mobeon.masp.util.TimeValueParser;

public class TimeValueParserTest extends TestCase {
    TimeValueParser timeValueParser;

    public void testGetTime() throws Exception {

        TimeValue tv = TimeValueParser.getTime("0.5s");
        assertTrue(tv != null);
        assertTrue(tv.getTime() == 500 && tv.getUnit() == TimeUnit.MILLISECONDS);
        tv = TimeValueParser.getTime(".5s");
        assertTrue(tv != null);
        assertTrue(tv.getTime() == 500 && tv.getUnit() == TimeUnit.MILLISECONDS);
        tv = TimeValueParser.getTime("5s");
        assertTrue(tv != null);
        assertTrue(tv.getTime() == 5 && tv.getUnit() == TimeUnit.SECONDS);
        tv = TimeValueParser.getTime("300s");
        assertTrue(tv != null);
        assertTrue(tv.getTime() == 300 && tv.getUnit() == TimeUnit.SECONDS);
        tv = TimeValueParser.getTime("0.5ms");
        assertTrue(tv != null);
        assertTrue(tv.getTime() == 500 && tv.getUnit() == TimeUnit.MICROSECONDS);
        tv = TimeValueParser.getTime("90ms");
        assertTrue(tv != null);
        assertTrue(tv.getTime() == 90 && tv.getUnit() == TimeUnit.MILLISECONDS);
        tv = TimeValueParser.getTime("500ms");
        assertTrue(tv != null);
        assertTrue(tv.getTime() == 500 && tv.getUnit() == TimeUnit.MILLISECONDS);

    }
}