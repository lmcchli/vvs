/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.logging;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.Filter;

/**
 * ConsecutiveLoggingFilter Tester.
 *
 * @author qhast
 */
public class RepetitiveLoggingFilterTest extends TestCase
{
    LoggingEvent eventA0;
    LoggingEvent eventA1;
    LoggingEvent eventA2;
    LoggingEvent eventA3;
    LoggingEvent resetEventA;

    LoggingEvent eventB0;
    LoggingEvent eventB1;
    LoggingEvent eventB2;
    LoggingEvent eventB3;
    LoggingEvent resetEventB;

    LoggingEvent eventC0;
    LoggingEvent eventC1;
    LoggingEvent resetEventC;

    LoggingEvent alwaysNeutralEvent;

    Filter filter;

    BasicLogContext logContextA;
    BasicLogContext logContextB;
    BasicLogContext logContextCimpliedByB;


    public RepetitiveLoggingFilterTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        logContextA = new BasicLogContext("A");
        logContextB = new BasicLogContext("B.*");
        logContextCimpliedByB = new BasicLogContext("B.Q");


        //Event logContext A
        eventA0 = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.ERROR,
                new LogJustOnceMessage(logContextA,"error",false),
                null
        );

        eventA1 = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.ERROR,
                new LogJustOnceMessage(logContextA,"error",false),
                null
        );

        eventA2 = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.ERROR,
                new LogJustOnceMessage(logContextA,"error again",false),
                null
        );

        eventA3 = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.ERROR,
                new LogJustOnceMessage(logContextA,"error",false),
                null
        );

        resetEventA = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.INFO,
                new LogJustOnceMessage(logContextA,"normal",true),
                null
        );


        //Event logContext B
        eventB0 = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.ERROR,
                new LogJustOnceMessage(logContextB,"error",false),
                null
        );

        eventB1 = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.ERROR,
                new LogJustOnceMessage(logContextB,"error",false),
                null
        );

        eventB2 = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.ERROR,
                new LogJustOnceMessage(logContextB,"error again",false),
                null
        );

        eventB3 = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.ERROR,
                new LogJustOnceMessage(logContextB,"error",false),
                null
        );

        resetEventB = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.INFO,
                new LogJustOnceMessage(logContextB,"normal",true),
                null
        );



        //Event logContext C
        eventC0 = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.ERROR,
                new LogJustOnceMessage(logContextCimpliedByB,"error",false),
                null
        );

        eventC1 = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.ERROR,
                new LogJustOnceMessage(logContextCimpliedByB,"error",false),
                null
        );

        resetEventC = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.INFO,
                new LogJustOnceMessage(logContextCimpliedByB,"normal",true),
                null
        );


        //Event always logged
        alwaysNeutralEvent = new LoggingEvent(
                getClass().getName(),
                Logger.getRootLogger(),
                Level.ERROR,
                "error",
                null
        );

        filter = new RepetitiveLoggingFilter();
    }

    /**
     * Test that filter
     * @throws Exception
     */
    public void testDecide() throws Exception
    {
        assertEquals("Decision should be NEUTRAL",Filter.NEUTRAL, filter.decide(alwaysNeutralEvent));
        assertEquals("Decision should be DENY",   Filter.DENY,    filter.decide(resetEventA));
        assertEquals("Decision should be ACCEPT", Filter.ACCEPT,  filter.decide(eventA0));
        assertEquals("Decision should be DENY",   Filter.DENY,    filter.decide(eventA1));
        assertEquals("Decision should be DENY",   Filter.DENY,    filter.decide(eventA2));
        assertEquals("Decision should be NEUTRAL",Filter.NEUTRAL, filter.decide(alwaysNeutralEvent));
        assertEquals("Decision should be DENY",   Filter.DENY,    filter.decide(eventA0));
        assertEquals("Decision should be ACCEPT", Filter.ACCEPT,  filter.decide(resetEventA));
        assertEquals("Decision should be ACCEPT", Filter.ACCEPT,  filter.decide(eventA3));
        assertEquals("Decision should be NEUTRAL",Filter.NEUTRAL, filter.decide(alwaysNeutralEvent));
        assertEquals("Decision should be ACCEPT", Filter.ACCEPT,  filter.decide(resetEventA));
        assertEquals("Decision should be ACCEPT", Filter.ACCEPT,  filter.decide(eventB0));
        assertEquals("Decision should be NEUTRAL",Filter.NEUTRAL, filter.decide(alwaysNeutralEvent));
        assertEquals("Decision should be NEUTRAL",Filter.NEUTRAL, filter.decide(alwaysNeutralEvent));
        assertEquals("Decision should be NEUTRAL",Filter.NEUTRAL, filter.decide(alwaysNeutralEvent));
        assertEquals("Decision should be ACCEPT", Filter.ACCEPT,  filter.decide(eventA3));
        assertEquals("Decision should be DENY",   Filter.DENY,    filter.decide(eventA2));
        assertEquals("Decision should be DENY",   Filter.DENY,    filter.decide(eventB2));
        assertEquals("Decision should be ACCEPT", Filter.ACCEPT,  filter.decide(resetEventB));
        assertEquals("Decision should be NEUTRAL",Filter.NEUTRAL, filter.decide(alwaysNeutralEvent));
        assertEquals("Decision should be ACCEPT", Filter.ACCEPT,  filter.decide(eventB1));
        assertEquals("Decision should be ACCEPT", Filter.DENY,    filter.decide(eventC0));
        assertEquals("Decision should be ACCEPT", Filter.ACCEPT,  filter.decide(resetEventB));
        assertEquals("Decision should be ACCEPT", Filter.ACCEPT,    filter.decide(eventC0));
        assertEquals("Decision should be ACCEPT", Filter.DENY,    filter.decide(eventC1));
        assertEquals("Decision should be ACCEPT", Filter.ACCEPT,  filter.decide(eventB2));
    }

    public static Test suite()
    {
        return new TestSuite(RepetitiveLoggingFilterTest.class);
    }
}
