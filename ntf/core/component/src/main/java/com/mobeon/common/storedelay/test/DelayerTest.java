/*
 * DelayerTest.java
 * JUnit based test
 *
 * Created on den 12 augusti 2004, 16:20
 */

package com.mobeon.common.storedelay.test;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.List;
import java.util.LinkedList;
import junit.framework.*;
import com.mobeon.common.storedelay.*;
import com.mobeon.ntf.util.DelayLoggerProxy;

/**
 * Test Delayer (DBDelayer)
 */
public class DelayerTest extends TestCase {

    // Each test use a different type of message.
    // this is so that if one test failes to clean after itself
    // the leftover message shall not interfere with other tests.
    //
    private static final short TEST_TYPE_1 = 10010;
    private static final short TEST_TYPE_2 = 10020;
    private static final short TEST_TYPE_3 = 10030;
    private static final short TEST_TYPE_4 = 10040;
    private static final short TEST_TYPE_5 = 10050;
    private static final short TEST_TYPE_6 = 10060;
    private static final short TEST_TYPE_7A = 10070;
    private static final short TEST_TYPE_7B = 10071;
    private static final short TEST_TYPE_8  = 10080;
    private static final short TEST_TYPE_9  = 10090;



    private DelayHandler delayer;

    public DelayerTest(java.lang.String testName) {
        super(testName);
    }

    public void setUp()
        throws DelayException
    {
        SDLogger.setLogger(new DelayLoggerProxy());
        SDLogger.log(SDLogger.INFO,
                     "DO NOT RELOAD CLASSES IN JUNIT RUNNER, THAT WILL " +
                     "FAIL ON RUNS AFTER THE FIRST");
        Properties p = new Properties();
        p.put(DBDelayHandler.KEY_STORAGE_DIR, "/tmp/ntf/delayertest");
        p.put(DBDelayHandler.KEY_STORAGE_BASE, "delaytest");
        com.mobeon.ntf.management.ManagementInfo.get().setExit(false);
        delayer = new DBDelayHandler(p);
        // We wait with starting so that each test case
        // my register its listener(s) before starting.
    }

    public void tearDown()
    {
        // Let delayer do final work so we see output and
        // get cleanings done
        try {Thread.sleep(1500);}catch (Exception ignored) {}
        com.mobeon.ntf.management.ManagementInfo.get().setExit(true);
        try {Thread.sleep(5000);}catch (Exception ignored) {}
        delayer = null;
        com.mobeon.ntf.management.ManagementInfo.get().setExit(false);

    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DelayerTest.class);
        return suite;
    }


    /**
     * Test of immediate scheduling.
     */
    public void testScheduleImmediate()
      throws DelayException
    {

        SDLogger.log(SDLogger.DEBUG, "-- TestScheduleImmediate --");

        DelayInfo di = new DelayInfo("0001",TEST_TYPE_1, "Test Data 1", null);
        ListenerTest lt =  new ListenerTest("L1",true);
        delayer.registerInterest(TEST_TYPE_1,lt);
        delayer.registeringDone(); // Start running after all listeners are registered
        try {Thread.sleep(500); }catch (InterruptedException ignored) {}
        // Wait until event loop is started for immediate test
        Calendar aBitBefore = Calendar.getInstance();
        aBitBefore.add(Calendar.SECOND, -2);
        delayer.schedule(aBitBefore, di);
        doDelay(1000);
        assertTrue("Event should be handled (almost) immediately",
                    lt.getEventCount() == 1);
    }

    /**
     * Test that listener is called after a while.
     * Note; this assumes that the granularity for scheduling is
     * better than 15  seconds.
     */
    public void testScheduleDelayed()
      throws DelayException
    {
        SDLogger.log(SDLogger.DEBUG, "-- TestScheduleDelayed --");
        ListenerTest lt = new ListenerTest("L1",true);
        delayer.registerInterest(TEST_TYPE_2, lt);
        delayer.registeringDone();

        /* Create an info and schedule it 30 seconds in the future */
        DelayInfo di = new DelayInfo("0001",TEST_TYPE_2, "Test Data 2", null);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, 30);
        delayer.schedule(c, di);

        doDelay(500);
        assertTrue("Event should not be handled this early",
                              lt.getEventCount() == 0);
        c.add(Calendar.SECOND, 15);
        Calendar now = Calendar.getInstance();
        // Wait until we should have been notified
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        while (now.before(c)) {
          try {
                Thread.sleep(5000);
           } catch (InterruptedException ie) { /* ignore */ }
          now = Calendar.getInstance();
          SDLogger.log(SDLogger.DEBUG, "Time now: " + sdf.format(now.getTime()));
        }
        assertTrue("Event should be handled by now", lt.getEventCount()==1);
    }


    /**
     * Wait a number of milliseconds.
     */
    private void doDelay(long milliseconds)
    {
        SDLogger.log(SDLogger.DEBUG, "Going to delay " + milliseconds + " milliseconds");
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {
            SDLogger.log(SDLogger.DEBUG, "Got interrupted");
        }
        SDLogger.log(SDLogger.DEBUG, "Delay done");
    }



    /**
     * Test that we get notifications.
     *
     */
    public void testNotifications()
    {
        SDLogger.log(SDLogger.DEBUG, "-- TestNotifications --");
        DelayInfo di = new DelayInfo("0001",TEST_TYPE_3, "Test Data 3", null);

        ListenerTest lt = new ListenerTest("L1",true);
        ListenerTest zeroType = new ListenerTest("OLD",false);
        delayer.registerInterest(TEST_TYPE_3, lt);
        delayer.registerInterest((short)0, zeroType);
        delayer.registeringDone();
        // Let the delayer run a little so the scheduling will not
        // be taken at once as an "old" event
        SDLogger.log(SDLogger.DEBUG,"Waiting for delayer");
        try {Thread.sleep(500); }catch (InterruptedException ignored) {}
        SDLogger.log(SDLogger.DEBUG, "Going to schedule for notify");
        delayer.schedule(null, di);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, 5);
        Calendar now = Calendar.getInstance();
        /*
         * Wait a while, we still should not have a notificatin
         * since they must be explicitely requested.
         */
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        while (now.before(c)) {
          try {
                Thread.sleep(5000);
           } catch (InterruptedException ie) { /* ignore */ }
          now = Calendar.getInstance();
          SDLogger.log(SDLogger.DEBUG, "Time now: " + sdf.format(now.getTime()));
        }
        assertTrue("No event since no notify!", lt.getEventCount()==0);

        // Notify!
        delayer.notifyEvent("0001", TEST_TYPE_3, new DelayEvent("TestEvent",0));
        // Wait to allow things to happen
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) { /* ignore */ }
        assertEquals("Notify event should be handled", 1, lt.getEventCount());
    }

   /**
     * Test separate notify for a time based notification
     * Schedule notification in a while, then notify separately
     * No time notification should come
     */
    public void testNotifyTimeEvent()
      throws DelayException
    {
        SDLogger.log(SDLogger.DEBUG, "-- testNotifyTimeEvent --");
        ListenerTest lt = new ListenerTest("L1",true);
        delayer.registerInterest(TEST_TYPE_4, lt);
        delayer.registeringDone();

        DelayInfo di = new DelayInfo("0001",TEST_TYPE_4, "Test Data 4", null);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, 30);
        delayer.schedule(c, di);
        doDelay(2000);
        assertTrue("Event should not be handled yet",
                              lt.getEventCount() == 0);
        delayer.notifyEvent("0001", TEST_TYPE_4, new DelayEvent("Interrupt",0));
        doDelay(500);
        assertTrue("Notification should be handled", lt.getEventCount()==1);
        // Wait until time for time event
        c.add(Calendar.SECOND, 15);
        Calendar now = Calendar.getInstance();
        // Wait until we should have been notified
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        while (now.before(c)) {
          try {
                Thread.sleep(5000);
           } catch (InterruptedException ie) { /* ignore */ }
          now = Calendar.getInstance();
          SDLogger.log(SDLogger.DEBUG, "Time now: " + sdf.format(now.getTime()));
        }
        assertTrue("No more events should have come", lt.getEventCount()==1);
    }

    /**
     * Test rescheduling
     */
    public void testReschedule()
      throws DelayException
    {
        SDLogger.log(SDLogger.DEBUG, "---- testReschedule ----");
        ListenerTest lt = new ListenerTest("L1",true);
        delayer.registerInterest(TEST_TYPE_5, lt);
        delayer.registeringDone();

        DelayInfo di = new DelayInfo("0005",TEST_TYPE_5, "Test Data 5", null);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, 30);
        delayer.schedule(c, di);
        doDelay(25000);
        assertTrue("Event should not be handled yet",
                   lt.getEventCount() == 0);
        // Reschedule
        delayer.reschedule(30, di);
        SDLogger.log(SDLogger.DEBUG, "Have called reschedule");
        // Wait until time for first time
        c.add(Calendar.SECOND, 15);
        Calendar now = Calendar.getInstance();
        // Wait until we should have been notified
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        while (now.before(c)) {
          try {
                Thread.sleep(5000);
           } catch (InterruptedException ie) { /* ignore */ }
          now = Calendar.getInstance();
          SDLogger.log(SDLogger.DEBUG, "Time now: " + sdf.format(now.getTime()));
        }
        SDLogger.log(SDLogger.DEBUG,"Events before delay : " + lt.getEventCount());
        assertEquals("No event yet", 0, lt.getEventCount());
        doDelay(40000);
        SDLogger.log(SDLogger.DEBUG, "Events after delay : " + lt.getEventCount());
        assertEquals("Event should be here now", 1, lt.getEventCount());
    }


    /**
     * Test that we get an error notificatin with a bad rescheduling
     */
    public void testRescheduleError()
      throws DelayException
    {
        SDLogger.log(SDLogger.DEBUG, "-- testRescheduleError --");
        ListenerTest lt = new ListenerTest("L1",true);
        delayer.registerInterest(TEST_TYPE_6, lt);
        delayer.registeringDone();
        doDelay(1000);
        DelayInfo di = new DelayInfo("0001",TEST_TYPE_6, "Test Data 6", null);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, 15);
        delayer.reschedule(c, di);
        doDelay(2500);
        assertTrue("Error event should be reported", lt.getErrorCount() == 1);
        doDelay(20000);
        assertTrue("No event reported",
                   lt.getEventCount() == 0);

    }


    /**
     * Test that register/unregister of listeners work.
     * Also checks that unhandled messages are removed.
     * (If they are not removed a second run of this test against
     * the same DB will fail)
     */
    public void testListenerAddRemove()
    {
        SDLogger.log(SDLogger.DEBUG, "-- testListenerAddRemove --");
        delayer.registeringDone();
        ListenerTest ltA = new ListenerTest("L7A", true);
        ListenerTest ltB = new ListenerTest("L7B", true);
        DelayInfo di1A = new DelayInfo("0001", TEST_TYPE_7A,"Test 7A1",null);
        DelayInfo di2A = new DelayInfo("0002", TEST_TYPE_7A,"Test 7A2",null);
        DelayInfo di1B = new DelayInfo("0001", TEST_TYPE_7B,"Test 7B1",null);
        DelayInfo di2B = new DelayInfo("0002", TEST_TYPE_7B,"Test 7B2",null);

        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, -1); // Ensure immediate delivery

        delayer.schedule(now, di1A);
        delayer.schedule(now, di1B);

        doDelay(1500);
        // We should have lost the events now
        delayer.registerInterest(TEST_TYPE_7A, ltA);
        delayer.registerInterest(TEST_TYPE_7B, ltB);
        doDelay(2000);
        SDLogger.log(SDLogger.DEBUG, "Checking received events");
        assertEquals("Nothing for type A", 0, ltA.getTotalCount());
        assertEquals("Nothing for type B", 0, ltB.getTotalCount());
        SDLogger.log(SDLogger.DEBUG, "Scheduling events we want");
        delayer.schedule(now, di2A);
        delayer.schedule(now, di2B);
        doDelay(1500);
        assertEquals("Got one event for A", 1, ltA.getTotalCount());
        assertEquals("Got one event for B", 1, ltB.getTotalCount());

        // Not longer interested for ltB
        delayer.unregisterInterest(TEST_TYPE_7B, ltB);
        doDelay(1500);
        delayer.schedule(now, di1A);
        delayer.schedule(now, di1B);
        doDelay(1500);
        assertEquals("Got two events for A", 2, ltA.getTotalCount());
        assertEquals("Still ot one event for B", 1, ltB.getTotalCount());

    }

    /**
     * Test that several listeners to the same info both gets the info
     */
    public void testTwoListeners()
    {
        SDLogger.log(SDLogger.DEBUG, "-- testTwoListeners --");
        delayer.registeringDone();
        ListenerTest lt1 = new ListenerTest("L8/1", true);
        ListenerTest lt2 = new ListenerTest("L8/2", false);
        doDelay(500); // Allow registering to take hold
        DelayInfo di1 = new DelayInfo("0001", TEST_TYPE_8,"Test 8_1",null);
        DelayInfo di2 = new DelayInfo("0002", TEST_TYPE_8,"Test 8_2",null);
        DelayInfo di3 = new DelayInfo("0003", TEST_TYPE_8,"Test 8_3",null);
        DelayInfo di4 = new DelayInfo("0004", TEST_TYPE_8,"Test 8_4",null);

        delayer.registerInterest(TEST_TYPE_8, lt1);
        delayer.registerInterest(TEST_TYPE_8, lt2);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, -1);

        delayer.schedule(now, di1);
        delayer.schedule(now, di2);

        doDelay(1500);

        assertEquals("Two events in first listener", 2, lt1.getTotalCount());
        assertEquals("Two events in second listener", 2, lt2.getTotalCount());

        delayer.unregisterInterest(TEST_TYPE_8, lt2);
        delayer.schedule(now, di3);
        delayer.schedule(now, di4);
        doDelay(1500);
        SDLogger.log(SDLogger.DEBUG, "Going to check event count");
        assertEquals("Four events in first listener", 4, lt1.getTotalCount());
        assertEquals("Two events in second listener", 2, lt2.getTotalCount());


    }



    /**
     * Test rescheduling with time before now.
     * This should give one notification, not
     * one now and one when the notification has been read
     * from the DB and is treated as new
     */
    public void testOldScheduling()
    {
        SDLogger.log(SDLogger.DEBUG, "-- testOldRescheduling --");
        // SDLogger.setLevel(SDLogger.TRACE);
        final int NO_SCHEDULINGS = 100;
        String keyBase = "oldsched-";
        ListenerTest lt = new ListenerTest("L9", false); // Do not remove
        lt.setSleepTime(1000);
        delayer.registerInterest(TEST_TYPE_9, lt);
        delayer.registeringDone();
        doDelay(1000);


        try {
            for (int i =0; i < NO_SCHEDULINGS; i++) {
                DelayInfo di = new DelayInfo(keyBase + i, TEST_TYPE_9, "-", null);
                delayer.schedule(null, di);
            }
            doDelay(2000);
            Calendar c = Calendar.getInstance();
            // Rescheduling
            for (int i =0; i < NO_SCHEDULINGS; i++) {
                DelayInfo di = new DelayInfo(keyBase + i, TEST_TYPE_9, "-", null);
                c.add(Calendar.MILLISECOND, 500);
                delayer.reschedule(c, di);
                Thread.yield();
            }

            doDelay(120000);
            assertEquals("No extra events", NO_SCHEDULINGS, lt.getEventCount());
        } finally {
            // Ensure object removed
            for (int i =0; i < NO_SCHEDULINGS; i++) {
                DelayInfo di = new DelayInfo(keyBase + i, TEST_TYPE_9, "-", null);
                delayer.cleanInfo(di.getKey(), di.getType());
            }

        }
    }



    class ListenerTest implements DelayListener {

        private String id;
        private boolean clean;
        private List receivedInfo = null;
        private List badInfo = null;
        private int sleepTime = 0;

        public ListenerTest(String id, boolean clean)
        {
            this.id = id;
            this.clean = clean;
            resetSavedInfo();
        }

        public String toString()
        {
            return this.id;
        }

        public void setSleepTime(int sleepTime)
        {
            this.sleepTime = sleepTime;
        }


        public void resetSavedInfo()
        {
            receivedInfo = new LinkedList();
            badInfo = new LinkedList();
        }

        public int getEventCount()
        {
            return receivedInfo.size();
        }

        public int getErrorCount()
        {
            return badInfo.size();
        }

        public int getTotalCount()
        {
            return getEventCount() + getErrorCount();
        }

        public String getListenerId() {
            return id;
        }

        public void handle(DelayHandler delayer, DelayInfo info,
                           int status, DelayEvent event)
        {
            Calendar now = Calendar.getInstance();
            SDLogger.log(SDLogger.DEBUG,
               "DelayerTest.Handling: " + info +
               " Status: " + status + " Event: " + event +
               "\tAt: " + now.getTime().getTime());

            if (status >= 0 ) {
                receivedInfo.add(info);
                if (clean) {
                    delayer.cleanInfo(info.getKey(), info.getType());
                }
            } else {
                badInfo.add(info);
                SDLogger.log(SDLogger.ERROR, "Error code to listener " + status);
            }
            if (sleepTime > 0) {
                try { Thread.sleep(sleepTime); } catch (Exception ignored) {}
            }
        }



    }


}
