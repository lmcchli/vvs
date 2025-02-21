/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.greetings;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * GreetingNotFoundException Tester.
 *
 * @author mande
 * @since <pre>01/24/2006</pre>
 * @version 1.0
 */
public class GreetingNotFoundExceptionTest extends TestCase
{
    public GreetingNotFoundExceptionTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
    }

    public void tearDown() throws Exception {
    }

    public void testMessageVoice() throws Exception {
        GreetingSpecification specification = new GreetingSpecification("allcalls", GreetingFormat.VOICE);
        GreetingNotFoundException greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("ALL_CALLS(VOICE)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("noanswer", GreetingFormat.VOICE);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("NO_ANSWER(VOICE)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("busy", GreetingFormat.VOICE);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("BUSY(VOICE)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("outofhours", GreetingFormat.VOICE);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("OUT_OF_HOURS(VOICE)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("extended_absence", GreetingFormat.VOICE);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("EXTENDED_ABSENCE(VOICE)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("cdg", GreetingFormat.VOICE, "12345");
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("CDG(VOICE)[12345]", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("temporary", GreetingFormat.VOICE);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("TEMPORARY(VOICE)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("ownrecorded", GreetingFormat.VOICE);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("OWN_RECORDED(VOICE)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification(GreetingType.SPOKEN_NAME, GreetingFormat.VOICE);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("SPOKEN_NAME(VOICE)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification(GreetingType.DIST_LIST_SPOKEN_NAME, GreetingFormat.VOICE);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("DIST_LIST_SPOKEN_NAME(VOICE)", greetingNotFoundException.getMessage());
    }

    public void testMessageVideo() throws Exception {
        GreetingSpecification specification = new GreetingSpecification("allcalls", GreetingFormat.VIDEO);
        GreetingNotFoundException greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("ALL_CALLS(VIDEO)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("noanswer", GreetingFormat.VIDEO);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("NO_ANSWER(VIDEO)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("busy", GreetingFormat.VIDEO);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("BUSY(VIDEO)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("outofhours", GreetingFormat.VIDEO);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("OUT_OF_HOURS(VIDEO)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("extended_absence", GreetingFormat.VIDEO);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("EXTENDED_ABSENCE(VIDEO)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("cdg", GreetingFormat.VIDEO, "12345");
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("CDG(VIDEO)[12345]", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("temporary", GreetingFormat.VIDEO);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("TEMPORARY(VIDEO)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification("ownrecorded", GreetingFormat.VIDEO);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("OWN_RECORDED(VIDEO)", greetingNotFoundException.getMessage());

        specification = new GreetingSpecification(GreetingType.SPOKEN_NAME, GreetingFormat.VIDEO);
        greetingNotFoundException = new GreetingNotFoundException(specification);
        assertEquals("SPOKEN_NAME(VIDEO)", greetingNotFoundException.getMessage());
    }

    public static Test suite() {
        return new TestSuite(GreetingNotFoundExceptionTest.class);
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
}
