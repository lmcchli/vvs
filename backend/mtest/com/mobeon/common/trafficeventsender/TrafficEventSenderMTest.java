package com.mobeon.common.trafficeventsender;

import com.mobeon.common.MTestBaseTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Manual tests for TrafficEventSender.
 *
 * @author emahagl
 */
public class TrafficEventSenderMTest extends MTestBaseTestCase {

    private TrafficEventSender trafficEventSender;

    public TrafficEventSenderMTest(String string) {
        super(string);
    }

    public void setUp() throws Exception {
        super.setUp();
        trafficEventSender = createTrafficEventSender();
    }

    private TrafficEventSender createTrafficEventSender() throws Exception {
        TrafficEventSender trafficEventSender = new TrafficEventSender();
        trafficEventSender.setConfiguration(configuration);
        //trafficEventSender.setServiceLocator(serviceLocator);
        trafficEventSender.setInternetMailSender(internetMailSender);
        //trafficEventSender.setEmailClientEnabled(false);
        trafficEventSender.init();
        return trafficEventSender;
    }

    public void testRadiusEvent() throws Exception {

        TrafficEvent trEvent = new TrafficEvent();
        trEvent.setName("login");
        trEvent.setProperty("sessionid", "1234567");
        trEvent.setProperty("username", "161074@test.com");
        trEvent.setProperty("callednumber", "98765");
        trEvent.setProperty("callingnumber", "0098765");
        trEvent.setProperty("operation", "7");
        trEvent.setProperty("objecttype", "3");
        trEvent.setProperty("objectid", "Object-Id");

        trafficEventSender.reportTrafficEvent(trEvent);
    }

    public void testEmailEvent() throws Exception {
        TrafficEvent trEvent = new TrafficEvent();
        trEvent.setName("slamdowninformation");
        trEvent.setProperty("callingnumber", "161074");
        trEvent.setProperty("mailhost", "husqvarna.lab.mobeon.com");
        trEvent.setProperty("emailaddress", "161000@lab.mobeon.com");

        trafficEventSender.reportTrafficEvent(trEvent);

        sleep(8);
    }

    public static Test suite() {
        return new TestSuite(TrafficEventSenderMTest.class);
    }
}
