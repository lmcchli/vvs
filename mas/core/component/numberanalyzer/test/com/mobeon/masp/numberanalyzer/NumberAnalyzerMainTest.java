/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

import com.mobeon.common.configuration.*;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.MockObjectTestCase;
import org.jmock.Mock;

import java.util.ArrayList;

/**
 * Test the NumberAnalyzer class. The the rules are configured in the numberanalyzer.xml file. The IConfiguration is
 * injected into the NumberAnalyzer via the setConfiguration method.
 *
 * @author ermmaha
 */
public class NumberAnalyzerMainTest extends MockObjectTestCase {
    private static final String cfgFile = "../numberanalyzer/test/com/mobeon/masp/numberanalyzer/numberanalyzer.xml";
    private static final String cfgFileChanged = "../numberanalyzer/test/com/mobeon/masp/numberanalyzer/numberanalyzer_changed.xml";

    protected IConfiguration configuration;
    protected EventDispatcherStub eventDispatcherStub;

    static {
        // Initialize console logging
        // Sets the configuration file for the logging
        ILoggerFactory.configureAndWatch("../numberanalyzer/log4jconf.xml");
    }

    public NumberAnalyzerMainTest(String string) throws Exception {
        super(string);
        configuration = getConfiguration(cfgFile);
        eventDispatcherStub = new EventDispatcherStub();
    }

    /**
     * @throws Exception
     */
    public void test1() throws Exception {
        NumberAnalyzer numberAnalyzer = new NumberAnalyzer();
        numberAnalyzer.setConfiguration(configuration);

        IAnalysisInput analysisInput = numberAnalyzer.getAnalysisInput();
        analysisInput.setRule("INBOUNDCALL");
        analysisInput.setNumber("1000");
        String result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("4660101000", result);

        try {
            analysisInput.setNumber("999");
            numberAnalyzer.analyzeNumber(analysisInput);
            fail("Expected NumberAnalyzerException");
        } catch (NumberAnalyzerException nax) {
            assertTrue(nax.toString().indexOf("Wrong min length") > -1);
            assertEquals("MIN=4", nax.getReason());
        }

        try {
            analysisInput.setNumber("1234567890123");
            numberAnalyzer.analyzeNumber(analysisInput);
            fail("Expected NumberAnalyzerException");
        } catch (NumberAnalyzerException nax) {
            assertTrue(nax.toString().indexOf("Wrong max length") > -1);
            assertEquals("MAX=12", nax.getReason());
        }
    }

    /**
     * Do some tests with the test2 rule that has regioncodes configured
     *
     * @throws Exception
     */
    public void test2() throws Exception {
        NumberAnalyzer numberAnalyzer = new NumberAnalyzer();
        numberAnalyzer.setConfiguration(configuration);

        IAnalysisInput analysisInput = numberAnalyzer.getAnalysisInput();
        analysisInput.setRule("test2");
        analysisInput.setNumber("161074");
        analysisInput.setInformationContainingRegionCode("060161074");

        // test regioncode
        String result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("060161074", result);

        // test regioncode that does not match
        analysisInput.setInformationContainingRegionCode("065161074");
        result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("161074", result);

        // test length
        try {
            analysisInput.setNumber("12345678");
            numberAnalyzer.analyzeNumber(analysisInput);
            fail("Expected NumberAnalyzerException");
        } catch (NumberAnalyzerException nax) {
            assertTrue(nax.toString().indexOf("Wrong length") > -1);
            assertEquals("EXACTLY=6", nax.getReason());
        }

        // test length
        try {
            analysisInput.setNumber("12345");
            numberAnalyzer.analyzeNumber(analysisInput);
            fail("Expected NumberAnalyzerException");
        } catch (NumberAnalyzerException nax) {
            assertTrue(nax.toString().indexOf("Wrong length") > -1);
            assertEquals("EXACTLY=6", nax.getReason());
        }
    }

    /**
     * Test the rule named BLOCK
     *
     * @throws Exception
     */
    public void testBlockedRule() throws Exception {
        NumberAnalyzer numberAnalyzer = new NumberAnalyzer();
        numberAnalyzer.setConfiguration(configuration);

        IAnalysisInput analysisInput = numberAnalyzer.getAnalysisInput();
        analysisInput.setRule("BLOCK");
        analysisInput.setNumber("555123");
        try {
            //verify that the number is blocked
            numberAnalyzer.analyzeNumber(analysisInput);
        } catch (NumberAnalyzerException nax) {
            assertTrue(nax.toString().indexOf("blocked") > -1);
        }

        analysisInput.setNumber("1555123");
        try {
            //verify that the number is not blocked (but no rule will match)
            numberAnalyzer.analyzeNumber(analysisInput);
        } catch (NumberAnalyzerException nax) {
            assertTrue(nax.toString().indexOf("No rule matched") > -1);
        }
    }

    /**
     * Test the Configuration Changed Event
     *
     * @throws Exception
     */
    public void testConfigurationChangedEvent() throws Exception {
        NumberAnalyzer numberAnalyzer = new NumberAnalyzer();
        numberAnalyzer.setEventDispatcher(eventDispatcherStub);
        numberAnalyzer.setConfiguration(configuration);

        // First test the INBOUNDCALL rule from the numberanalyzer.xml file
        IAnalysisInput analysisInput = numberAnalyzer.getAnalysisInput();
        analysisInput.setRule("INBOUNDCALL");
        analysisInput.setNumber("1000");
        String result = numberAnalyzer.analyzeNumber(analysisInput);
        assertEquals("4660101000", result);

        // then change and reload the configuration
        IConfiguration changedConfiguration = getConfiguration(cfgFileChanged);
        eventDispatcherStub.fireGlobalEvent(new ConfigurationChanged(changedConfiguration));

        // Test the INBOUNDCALL rule again (the rules have been changed)
        // min length is 6 now
        try {
            numberAnalyzer.analyzeNumber(analysisInput);
        } catch (NumberAnalyzerException nax) {
            assertTrue(nax.toString().indexOf("Wrong min length") > -1);
        }

        // CALLEROUTDIAL does not exist now
        analysisInput = numberAnalyzer.getAnalysisInput();
        analysisInput.setRule("CALLEROUTDIAL");
        analysisInput.setNumber("10000");

        try {
            numberAnalyzer.analyzeNumber(analysisInput);
        } catch (NumberAnalyzerException nax) {
        }
    }

    /**
     * Do a multithreaded test
     *
     * @throws Exception
     */
    public void testMT() throws Exception {
        final NumberAnalyzer numberAnalyzer = new NumberAnalyzer();
        numberAnalyzer.setConfiguration(configuration);

        final IAnalysisInput analysisInput = numberAnalyzer.getAnalysisInput();
        analysisInput.setRule("INBOUNDCALL");
        analysisInput.setNumber("1000");

        for (int i = 0; i < 5; i++) {
            new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < 10; i++) {
                        try {
                            String result = numberAnalyzer.analyzeNumber(analysisInput);
                            assertEquals("4660101000", result);
                        } catch (NumberAnalyzerException e) {
                            fail("Exception in " + e);
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * @throws Exception if test case fails.
     */
    public void testConfigurationFailure() throws Exception {
        Mock jmockConfiguration = mock(IConfiguration.class);
        // test configuration read failure
        jmockConfiguration.expects(once()).method("getGroups").will(
                throwException(new UnknownGroupException("Error", null)));

        NumberAnalyzer numberAnalyzer = new NumberAnalyzer();
        numberAnalyzer.setEventDispatcher(eventDispatcherStub);

        try {
            numberAnalyzer.setConfiguration((IConfiguration) jmockConfiguration.proxy());
            fail("Expected NumberAnalyzerException");
        } catch (NumberAnalyzerException nax) {
        }

        // make a changed event with configuration error
        jmockConfiguration.expects(once()).method("getGroups").will(
                throwException(new UnknownGroupException("Error", null)));
        eventDispatcherStub.fireGlobalEvent(new ConfigurationChanged((IConfiguration) jmockConfiguration.proxy()));
    }

    private IConfiguration getConfiguration(String configFile) throws Exception {
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(configFile);
        return configurationManager.getConfiguration();
    }

    public static Test suite() {
        return new TestSuite(NumberAnalyzerMainTest.class);
    }
}

class EventDispatcherStub implements IEventDispatcher {
    private ArrayList<IEventReceiver> eventReceivers = new ArrayList<IEventReceiver>();

    public void addEventReceiver(IEventReceiver rec) {
        eventReceivers.add(rec);
    }

    public void removeEventReceiver(IEventReceiver rec) {
        eventReceivers.remove(rec);
    }

    public void removeAllEventReceivers() {
    }

    public ArrayList<IEventReceiver> getEventReceivers() {
        return eventReceivers;
    }

    public int getNumReceivers() {
        return eventReceivers.size();
    }

    public void fireEvent(Event e) {
        for (IEventReceiver eventReceiver : eventReceivers) {
            eventReceiver.doEvent(e);
        }
    }

    public void fireGlobalEvent(Event e) {
        for (IEventReceiver eventReceiver : eventReceivers) {
            eventReceiver.doGlobalEvent(e);
        }
    }
}
