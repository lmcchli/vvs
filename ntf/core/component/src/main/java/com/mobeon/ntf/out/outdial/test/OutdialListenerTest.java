/*
 * OutdialWorkerTest.java
 * JUnit based test
 *
 * Created on den 27 september 2004, 10:42
 */

package com.mobeon.ntf.out.outdial.test;

import java.util.*;

import com.mobeon.common.storedelay.DelayException;
import com.mobeon.common.storedelay.DelayHandler;
import com.mobeon.common.storedelay.DBDelayHandler;
import com.mobeon.common.storedelay.DelayListener;
import com.mobeon.common.storedelay.DelayInfo;
import com.mobeon.common.storedelay.DelayEvent;
import com.mobeon.common.storedelay.SDLogger;
import com.mobeon.common.commands.Command;
import com.mobeon.common.commands.Operation;
import com.mobeon.common.commands.CommandHandler;
import com.mobeon.common.commands.CommandException;

import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.out.outdial.*;
import com.mobeon.ntf.util.DelayLoggerProxy;
import com.mobeon.common.storedelay.SDLogger;

import junit.framework.*;

/**
 * This class tests both the Listener and the Worker classes
 * since they work intimately together
 */
public class OutdialListenerTest extends TestCase
{

    private DBDelayHandler delayer;
    private PhoneOnMap phoneMap;
    private TestPhoneRequester phoneOnRequester;
    private PhoneOnListener phoneOnListener;
    private Map commandHandlers;
    private CommandHandler commandHandler;
    private OdlWorker odlWorker;
    private OdlListener odlListener;
    private TestOdlCaller caller;
    private ManagedArrayBlockingQueue<Object> queue;
    private UserFactory userFactory;

    public OutdialListenerTest(java.lang.String testName)
    {
        super(testName);
        MerAgent mer = MerAgent.get();
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(OutdialListenerTest.class);
        return suite;
    }


    private void makeDelayer()
        throws DelayException
    {
        Properties p = new Properties();
        p.put(DBDelayHandler.KEY_STORAGE_DIR, "/tmp/ntf/outdial");
        p.put(DBDelayHandler.KEY_STORAGE_BASE, "odllistener");
        delayer = new DBDelayHandler(p);
    }

    private void makeCommandHandlers()
        throws Exception
    {
        Properties props = new Properties();
        props.setProperty("maxwaithours", "6");
        props.setProperty("initialstate", "0");
        props.setProperty("numberofstates", "3");
        props.setProperty("default.200", "END/"); // Quit after successful call
        props.setProperty("default.202", "END/");
        props.setProperty("default.405", "END/fallback");
        props.setProperty("default.900", "1/waiton; wait 2; call");
        props.setProperty("default.910", "END/fallback");
        props.setProperty("default.920", "END/fallback");
        props.setProperty("default.930", "END/fallback");
        props.setProperty("default.940", "1/call"); // Phone on

        props.setProperty("default.402", "1/wait 7; call"); // Busy

        props.setProperty("state.1.401", "2/wait 10; call"); // Not reachable
        props.setProperty("state.1.404", "2/wait 15; call"); // No answer

        props.setProperty("state.2.401", "END/fallback");
        props.setProperty("state.2.404", "END/fallback");


        commandHandler = new CommandHandler(props);
        commandHandlers = new HashMap();
        commandHandlers.put("default", commandHandler);


        Properties props2 = new Properties();
        props2.setProperty("maxwaithours", "6");
        props2.setProperty("initialstate", "0");
        props2.setProperty("numberofstates", "2");
        props2.setProperty("state.0.900","1/waiton; wait 1; call");
        props2.setProperty("state.1.200","END/");
        props2.setProperty("state.1.401", "END/fallback");
        props2.setProperty("state.1.402", "END/fallback");
        props2.setProperty("state.1.404", "END/fallback");
        props2.setProperty("state.1.910", "END/fallback");
        props2.setProperty("state.1.920", "END/fallback");
        props2.setProperty("state.1.930", "END/fallback");
        props2.setProperty("state.1.940", "END/fallback");

        CommandHandler ch2 = new CommandHandler(props2);
        commandHandlers.put("other", ch2);
        
    }

    /**
     * Make the basic object structure for testing.
     */
    public void setUp()
        throws Exception
    {
        DelayLoggerProxy sdLogProxy = new DelayLoggerProxy();
        SDLogger.setLogger(sdLogProxy);
        SDLogger.setLevel(SDLogger.TRACE);
        com.mobeon.ntf.management.ManagementInfo.get().setExit(false);
        makeDelayer();
        phoneMap = new PhoneOnMap();
        phoneOnRequester = new TestPhoneRequester(phoneMap);
        phoneOnListener = new PhoneOnListener(delayer, phoneMap);
        phoneOnRequester.setListener(phoneOnListener);
        phoneOnListener.setDaemon(true);
        phoneOnListener.start();
        makeCommandHandlers();
        queue = new ManagedArrayBlockingQueue(300);
        caller = new TestOdlCaller();
        userFactory = new UserFactory();

        for (int i= 0; i < 10; i++) {
            odlWorker = new TestOdlWorker(delayer, commandHandlers, queue, caller,
                                      phoneOnRequester, userFactory,
                                      "OdlWorker-" + i);
            odlWorker.setDaemon(true);
            odlWorker.start();
        }


        odlListener = new OdlListener("testlistener", queue);
        delayer.registerInterest(OdlInfo.DELAY_TYPE_OUTDIAL, odlListener);
        delayer.start();
        Thread.yield();
    }


    public void tearDown()
    {
        SDLogger.log(SDLogger.DEBUG,"teardown");
        doSleep(10000);
        if (delayer != null) {
            try {Thread.sleep(1500);}catch (Exception ignored) {}
            com.mobeon.ntf.management.ManagementInfo.get().setExit(true);
            try {Thread.sleep(5000);}catch (Exception ignored) {}
            delayer = null;
        }
        doSleep(2000);
    }


    private static final String PHONE_NR_1 = "4658651691";
    private static final String USER_MAIL_1 = "junit30@lab.mobeon.com";
    private static final String PHONE_AND_USER1 = PHONE_NR_1 + "," + USER_MAIL_1;
    private static final String USERDN_1 =
        "uniqueidentifier=um15,ou=C34,o=abcxyz.se";

    private static final String PHONE_NR_2 = "4658651692";
    private static final String PHONE_NR_2_FIXED = "41234567";
    private static final String USER_MAIL_2 = "junit31@lab.mobeon.com";
    private static final String PHONE_AND_USER_2 = PHONE_NR_2 + "," + USER_MAIL_2;
    private static final String USERDN_2 =
        "uniqueidentifier=um16,ou=C34,o=abcxyz.se";

    // Phone number to user "other" command handler
    private static final String PHONE_NR_OTHER = "14658652692";
    private static final String PHONE_AND_USER_OTHER  = PHONE_NR_OTHER + "," + USER_MAIL_1;

    private static final String PHONE_NR_YET_ANOTHER = "24658652692";
    private static final String PHONE_AND_USER_YET_ANOTHER  = 
        PHONE_NR_YET_ANOTHER + "," + USER_MAIL_1;

    /**
     * Test a call that goes ok
     */
    public void testGoodCall()
        throws Exception
    {
        SDLogger.log(SDLogger.DEBUG,"**** testGoodCall ****");

        caller.setResponseCode(PHONE_NR_1, 200); // Call should succeed
        Calendar now = Calendar.getInstance();
        long nowMS = now.getTimeInMillis();
        startOutdial(nowMS, PHONE_NR_1, USER_MAIL_1, USERDN_1);

        // Now the outdial should be attempted, with immediate success
        // Wait a while so it will be run through
        doSleep(10000);

        List phoneOns = phoneOnRequester.getRequestList();
        SDLogger.log(SDLogger.DEBUG, "PhoneOns : " + phoneOns);
        assertEquals("One phone on request", 1, phoneOns.size());
        assertEquals("Phoneon req to correct phone",
                     PHONE_AND_USER1, phoneOns.get(0));

        List calls = caller.getCalls();
        SDLogger.log(SDLogger.DEBUG, "Calls :" + calls);
        assertEquals("One call", 1, calls.size());
        assertEquals("Call to right number", PHONE_NR_1, calls.get(0));

    }


    /**
     * Test when we do not get an answer
     */
    public void testNoAnswer()
        throws Exception
    {
        SDLogger.log(SDLogger.DEBUG, "**** testNoAnswer ****");
        caller.setResponseCode(PHONE_NR_1, 404); // No answer
        caller.setResponseCode(PHONE_NR_1, 404); // No answer again
        Calendar now = Calendar.getInstance();
        long nowMS = now.getTimeInMillis();
        startOutdial(nowMS, PHONE_NR_1, USER_MAIL_1, USERDN_1);

        doSleep(50000);
        List phoneOns = phoneOnRequester.getRequestList();
        SDLogger.log(SDLogger.DEBUG, "PhoneOns : " + phoneOns);
        assertEquals("One phone on request", 1, phoneOns.size());
        assertEquals("Phoneon req to correct phone",
                     PHONE_AND_USER1, phoneOns.get(0));

        List calls = caller.getCalls();
        SDLogger.log(SDLogger.DEBUG, "Calls :" + calls);
        assertEquals("Two call attempts", 2, calls.size());
        assertEquals("First Call to right number", PHONE_NR_1, calls.get(0));
        assertEquals("Second Call to right number", PHONE_NR_1, calls.get(1));

    }


    /**
     * Test when we do not get an answer
     */
    public void testOtherCommandNotReachable()
        throws Exception
    {
        SDLogger.log(SDLogger.DEBUG,"**** testOtherNotReachable ****");
        caller.setResponseCode(PHONE_NR_OTHER, 405); // Not reachable
        Calendar now = Calendar.getInstance();
        long nowMS = now.getTimeInMillis();
        startOutdial(nowMS, PHONE_NR_OTHER, USER_MAIL_1, USERDN_1);

        doSleep(20000);
        List phoneOns = phoneOnRequester.getRequestList();
        SDLogger.log(SDLogger.DEBUG, "PhoneOns : " + phoneOns);
        assertEquals("One phone on request", 1, phoneOns.size());
        assertEquals("Phoneon req to correct phone",
                     PHONE_AND_USER_OTHER, phoneOns.get(0));

        List calls = caller.getCalls();
        SDLogger.log(SDLogger.DEBUG, "Calls :" + calls);
        assertEquals("One call attempt", 1, calls.size());
        assertEquals("Call to right number", PHONE_NR_OTHER, calls.get(0));

    }


    /**
     * Test when we do not get an answer for number with 'yetanother'
     * outdial schema.
     * Since the schema is not defined we should back to 'default' and
     * get exactly the same handling as that.
     */
    public void testYetAnotherNoAnswer()
        throws Exception
    {
        SDLogger.log(SDLogger.DEBUG,"**** testYetAnotherNoAnswer ****");
        caller.setResponseCode(PHONE_NR_YET_ANOTHER, 404); // No answer
        caller.setResponseCode(PHONE_NR_YET_ANOTHER, 404); // No answer again
        Calendar now = Calendar.getInstance();
        long nowMS = now.getTimeInMillis();
        startOutdial(nowMS, PHONE_NR_YET_ANOTHER, USER_MAIL_1, USERDN_1);

        doSleep(50000);
        List phoneOns = phoneOnRequester.getRequestList();
        SDLogger.log(SDLogger.DEBUG, "PhoneOns : " + phoneOns);
        assertEquals("One phone on request", 1, phoneOns.size());
        assertEquals("Phoneon req to correct phone",
                     PHONE_AND_USER_YET_ANOTHER, phoneOns.get(0));

        List calls = caller.getCalls();
        SDLogger.log(SDLogger.DEBUG, "Calls :" + calls);
        assertEquals("Two call attempts", 2, calls.size());
        assertEquals("First Call to right number", PHONE_NR_YET_ANOTHER, calls.get(0));
        assertEquals("Second Call to right number", PHONE_NR_YET_ANOTHER, calls.get(1));

    }


    /**
     * Test when we do not get an answer
     */
    public void testTooOld()
        throws Exception
    {
        SDLogger.log(SDLogger.DEBUG,"**** testTooOld ****");
        for (int i = 0; i < 10; i++) {
            caller.setResponseCode(PHONE_NR_1, 404); // No answer
        }
        Calendar now = Calendar.getInstance();
        // Make sure the info will soon be too old
        now.add(Calendar.HOUR, -5);
        now.add(Calendar.MINUTE, -59);
        now.add(Calendar.SECOND, -50);

        startOutdial(now.getTimeInMillis(), PHONE_NR_1, USER_MAIL_1, USERDN_1);
        // Wait until it should have become too old
        doSleep(50000);

        List calls = caller.getCalls();
        SDLogger.log(SDLogger.DEBUG, "Calls :" + calls);
        // Any number of calls is acceptible...

    }

    public void testTwoDials()
    {
        SDLogger.log(SDLogger.DEBUG,"**** testTwoDials ****");
        caller.setResponseCode(PHONE_NR_1, 402); // Busy
        caller.setResponseCode(PHONE_NR_1, 402); // Busy
        caller.setResponseCode(PHONE_NR_1, 200); // Ok

        caller.setResponseCode(PHONE_NR_2, 404);
        caller.setResponseCode(PHONE_NR_2, 402);
        caller.setResponseCode(PHONE_NR_2, 404);
        caller.setResponseCode(PHONE_NR_2, 402);
        caller.setResponseCode(PHONE_NR_2, 404);
        caller.setResponseCode(PHONE_NR_2, 402);
        caller.setResponseCode(PHONE_NR_2, 200);

        Calendar now = Calendar.getInstance();
        long nowMS = now.getTimeInMillis();
        startOutdial(nowMS, PHONE_NR_1, USER_MAIL_1, USERDN_1);
        doSleep(50);
        startOutdial(nowMS, PHONE_NR_2, USER_MAIL_2, USERDN_2);

        doSleep(90000);
         List phoneOns = phoneOnRequester.getRequestList();
        SDLogger.log(SDLogger.DEBUG, "PhoneOns : " + phoneOns);
        assertEquals("Two phone on request", 2, phoneOns.size());

        List calls = caller.getCalls();
        SDLogger.log(SDLogger.DEBUG, "Calls :" + calls);
        assertEquals("Ten calls", 10, calls.size());


    }

    public void testUnknownResponse()
    {
        SDLogger.log(SDLogger.DEBUG,"**** testUnknownResponse ****");
        caller.setResponseCode(PHONE_NR_1,  1111);
        Calendar now = Calendar.getInstance();
        long nowMS = now.getTimeInMillis();
        startOutdial(nowMS, PHONE_NR_1, USER_MAIL_1,USERDN_1);
        doSleep(10000);
        List calls = caller.getCalls();
        SDLogger.log(SDLogger.DEBUG, "Calls : " + calls);
        assertEquals("One call", 1, calls.size());
    }


    /**
     * Do a load test, this test is normally disabled
     */
    public void XXXtestHighLoad()
    {
        SDLogger.log(SDLogger.DEBUG,"**** testHighLoad ****");

        caller.setCountOnly(true);
        caller.setCallTime(20); // 20 MS wait for a call
        phoneOnRequester.setCountOnly(true);

        final int NR_OK = 500;
        final int NR_BUSY_OK = 500;
        final int NR_NOR_NOR = 1000;

        // REMEMBER - Number arrays should become same size
        String user1Numbers[] = makeNumbers(8100000, NR_OK, NR_BUSY_OK, NR_NOR_NOR);
        String user2Numbers[] = makeNumbers(8200000, NR_OK, NR_BUSY_OK, NR_NOR_NOR);
        Calendar now = Calendar.getInstance();
        long nowMS = now.getTimeInMillis();
        long startMS = nowMS;
        for (int i = 0; i < user1Numbers.length ; i ++) {
            startOutdial(nowMS, user1Numbers[i], USER_MAIL_1, USERDN_1);
            startOutdial(nowMS, user2Numbers[i], USER_MAIL_2, USERDN_2);
            if (( i % 20) == 0) {
                doSleep(100); // Don't pump too hard
                if ( (i % 100) == 0) doSleep(2000);
                now = Calendar.getInstance();
                nowMS = now.getTimeInMillis();
            }
        }
        Calendar end = Calendar.getInstance();
        long endMS = end.getTimeInMillis();
        // Total outcalls
        int totalStartedCalls = 2 * (NR_OK + NR_BUSY_OK + NR_NOR_NOR);
        int expectedPhoneOn = totalStartedCalls;
        int expectedCalls = NR_OK*2 + NR_BUSY_OK * 2 *2 + NR_NOR_NOR * 2 * 2;
        int loops = 0;
        int noCalls;
        int noPhoneOns;


        // Wait until all should be done
        doSleep(50000);
        do {
            loops++;
            noCalls    = caller.getCount();
            noPhoneOns = phoneOnRequester.getCount();

            System.out.println("==================================");
            System.out.println("PhoneOns: " + noPhoneOns + " of " + expectedPhoneOn);
            System.out.println("Calls   : " + noCalls + " of " + expectedCalls);
            System.out.println("==================================");

        } while (loops < 100);

        System.out.println("Total sendtime (in millis) " + (endMS - startMS) );
//      System.out.println("==================================");
//      System.out.println("PhoneOns : " + phoneOnRequester.getRequestList() );
//      System.out.println("Calls: " + caller.getCalls() );
//      System.out.println("==================================");


        noCalls    = caller.getCount();
        noPhoneOns = phoneOnRequester.getCount();

        assertEquals("One phone on for each call", totalStartedCalls, noPhoneOns);
        assertEquals("Calls", expectedCalls, noCalls);

    }

    private String[] makeNumbers(int startNumber, int nrOk, int nrBusyOk,
                                 int nrNoReplyNoReply)
    {
        int sum = nrOk + nrBusyOk + nrNoReplyNoReply;
        String[] numbers = new String[sum];
        int base = 0;
        for (int i = 0; i< nrOk; i++) {
            String number = "" + (startNumber + i + base);
            numbers[i+base] = number;
            caller.setResponseCode(number, 200);
        }
        base += nrOk;
        for (int i = 0; i<nrBusyOk; i++) {
            String number = "" + (startNumber + i + base);
            numbers[i+base] = number;
            caller.setResponseCode(number, 402);
            caller.setResponseCode(number, 200);
        }
        base += nrBusyOk;
        for (int i = 0; i<nrNoReplyNoReply; i++) {
            String number = "" + (startNumber + i + base);
            numbers[i+base] = number;
            caller.setResponseCode(number, 404);
            caller.setResponseCode(number, 404);
        }
        return numbers;
    }


    /**
     * Create info needed to start an outdial and start it.
     */
    private void startOutdial(long startMS, String phoneNr, String userMail, String userdn)
    {
        OdlInfo info = new OdlInfo(phoneNr, userMail,
                                   startMS, userdn);
        DelayInfo dInfo = info.getPersistentRepresentation();

        delayer.schedule(null, dInfo);
        DelayEvent startEvent =
            new DelayEvent(OdlInfo.EVENT_OUTDIAL_START,
                           OdlInfo.EVENT_CODE_DEFAULT);
        delayer.notifyEvent(dInfo.getKey(), dInfo.getType(), startEvent);
    }

    private static void doSleep(int ms)
    {
        SDLogger.log(SDLogger.DEBUG,"-- TestCase: Sleeping for " + ms + " milliseconds --");
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {
        }
        SDLogger.log(SDLogger.DEBUG,"-- TestCase: Done sleeping --");
    }


}
