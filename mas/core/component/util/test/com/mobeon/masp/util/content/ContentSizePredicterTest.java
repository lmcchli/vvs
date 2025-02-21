/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.content;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * ContentSizePredicter Tester.
 *
 * @author qhast
 */
public class ContentSizePredicterTest extends TestCase
{
    public ContentSizePredicterTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        ILoggerFactory.configureAndWatch("test/log4jconf.xml");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testClassCall() throws Exception {
        new ContentSizePredicter();
    }

    public void testPredict() throws Exception
    {
        assertEquals(0,ContentSizePredicter.predict(0,"7bit","text/plain"));
        assertEquals(-1,ContentSizePredicter.predict(-1,"7bit","text/plain"));
        assertEquals(Math.round(1000/ContentSizePredicter.DEFAULT_ENCODED_SIZE_DIVIDER_SEED),ContentSizePredicter.predict(1000,"7bit","text/plain"));
        assertEquals(Math.round(1000/ContentSizePredicter.BASE64_ENCODED_SIZE_DIVIDER_SEED), ContentSizePredicter.predict(1000,"base64","text/plain"));
    }

    public void testLearn() throws Exception
    {
        ContentSizePredicter.learn(0,0,"7bit","text/plain");
        for(int i=0; i<105; i++) {
            ContentSizePredicter.learn(2,2,"7bit","text/plain");
        }
        assertEquals(2,ContentSizePredicter.predict(2,"7bit","text/plain"));

    }

    public void testPredictWithIllegalArguments() throws Exception
    {
        try {
            ContentSizePredicter.predict(1,null,"text/plain");
            fail("Predicting with parameter \"encoding\" set to null should throw an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            ContentSizePredicter.predict(1,"","text/plain");
            fail("Predicting with parameter \"encoding\" set to empty string should throw an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            ContentSizePredicter.predict(1,"7bit",null);
            fail("Predicting with parameter \"contentType\" set to null should throw an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            ContentSizePredicter.predict(1,"7bit","");
            fail("Predicting with parameter \"contentType\" set to empty string should throw an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }

    }

    public static Test suite()
    {
        return new TestSuite(ContentSizePredicterTest.class);
    }
}
