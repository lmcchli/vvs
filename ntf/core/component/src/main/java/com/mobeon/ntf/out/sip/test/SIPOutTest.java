/*
 * SIPOutTest.java
 *
 * Created on den 22 februari 2007, 13:10
 */

package com.mobeon.ntf.out.sip.test;


import java.util.*;

import com.mobeon.ntf.out.sip.SIPOut;
import com.mobeon.ntf.out.sip.SIPCaller;
import com.mobeon.ntf.out.sip.SIPInfo;
import com.mobeon.ntf.test.*;
import com.mobeon.ntf.userinfo.mur.*;
import com.mobeon.ntf.mail.*;
import com.mobeon.ntf.userinfo.SIPFilterInfo;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.util.DelayLoggerProxy;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.ILoggerProxy;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.storedelay.SDLogger;
import com.mobeon.common.storedelay.DBDelayHandler;
import com.mobeon.common.storedelay.DelayException;
import com.mobeon.common.storedelay.DelayHandler;
import com.mobeon.common.xmp.client.XmpClient;
import com.mobeon.common.xmp.server.HttpServer;
import com.mobeon.common.xmp.server.XmpHandler;

/**
 *
 * @author  mnify
 */
public class SIPOutTest extends NtfTestCase {
    private DelayHandler delayHandler;


    public SIPOutTest(String name) {
        super(name);

    }

    int okCount = 0;
    int retryCount = 0;
    int expireCount = 0;
    int failCount = 0;

    private void resetCount() {
        okCount = 0;
        retryCount = 0;
        expireCount = 0;
        failCount = 0;
    }



    public void testSending() throws Exception {
        l("testsending");
        XmpClient client = XmpClient.get();
        client.setLogger(new ILoggerProxy());
        client.setValidity(80);
        HttpServer server = new HttpServer();
        TestXMPHandler handler = new TestXMPHandler();
        handler.setStatusCode(200);

        server.setPort(9988);
        server.setLogger(new ILoggerProxy());
        XmpHandler.setServiceHandler(IServiceName.MWI_NOTIFICATION, handler);
        List<IServiceInstance> instList = new ArrayList<IServiceInstance>();
        IServiceInstance inst = handler.getServiceInstance(IServiceName.MWI_NOTIFICATION, "9988");
        instList.add(inst);
        client.setComponents(IServiceName.MWI_NOTIFICATION, instList);
        client.refreshStatus();
        server.start();

        initDelayHandler();
        SIPOut sipOut = new SIPOut(delayHandler);
        delayHandler.registeringDone();
        UserMailbox inbox = new UserMailbox(2, 3, 0, 0, 0, 0, 0, 0, false);
        TestUser user = new TestUser();
        SIPFilterInfo filter = new SIPFilterInfo(new String[] { "123123" });
        FeedbackHandler ng = new MyFeedbackHandler();

        int returnCount = sipOut.handleMWI(user, filter, inbox);
        assertEquals(1, returnCount);

        Thread.sleep(5000);
        assertEquals(1, handler.getHandledRequests());

        handler.setStatusCode(450);
        returnCount = sipOut.handleMWI(user, filter, inbox);
        assertEquals(1, returnCount);

        // retryes should come every 10 seconds in SIP.cfg
        Thread.sleep(5000);
        assertEquals(2, handler.getHandledRequests());

        // new try should happen here
        Thread.sleep(10000);
        assertEquals(3, handler.getHandledRequests());

        handler.setStatusCode(200);
        Thread.sleep(10000);
        assertEquals(4, handler.getHandledRequests());

        // new new attempts should be done since the last result was 200
        Thread.sleep(10000);
        assertEquals(4, handler.getHandledRequests());

        handler.setStatusCode(450);
        handler.addParameter("retry-time", "5");

        returnCount = sipOut.handleMWI(user, filter, inbox);
        assertEquals(1, returnCount);

        Thread.sleep(3000);
        assertEquals(5, handler.getHandledRequests());

        // sleep between 5 and 10 seconds
        Thread.sleep(5000);
        assertEquals(6, handler.getHandledRequests());

        handler.setStatusCode(200);
        Thread.sleep(7000);
        assertEquals(7, handler.getHandledRequests());

        handler.clearParameters();
        handler.addParameter("retry-time", "100");
        handler.setStatusCode(450);


        // test cancel
        returnCount = sipOut.handleMWI(user, filter, inbox);
        Thread.sleep(5000);
        assertEquals(8, handler.getHandledRequests());
        sipOut.cancel("123123", user.getMail());

        Thread.sleep(10000);
        assertEquals(8, handler.getHandledRequests());


        // test reschedule
        returnCount = sipOut.handleMWI(user, filter, inbox);
        Thread.sleep(5000);
        assertEquals(9, handler.getHandledRequests());

        inbox = new UserMailbox(2, 3, 1, 0, 0, 0, 0, 0, false);
        returnCount = sipOut.handleMWI(user, filter, inbox);
        assertEquals(1, returnCount);

        Thread.sleep(5000);
        assertEquals(10, handler.getHandledRequests());
        sipOut.cancel("123123", user.getMail());

        /** test reschedule with error
         *  TR 31182
         */
        returnCount = sipOut.handleMWI(user, filter, inbox);
        delayHandler.cleanInfo("123123,"+user.getMail(), SIPInfo.DELAY_TYPE_SIP);
        Thread.sleep(10000);
        assertEquals(1, returnCount);

        client.stop();
        server.stopServer();
    }

  public void testNullFilter() throws Exception {
    l("testnullfilter");
    XmpClient client = XmpClient.get();
    client.setLogger(new ILoggerProxy());
    client.setValidity(80);
    HttpServer server = new HttpServer();
    TestXMPHandler handler = new TestXMPHandler();
    handler.setStatusCode(200);

    server.setPort(9988);
    server.setLogger(new ILoggerProxy());
    XmpHandler.setServiceHandler(IServiceName.MWI_NOTIFICATION, handler);
    List<IServiceInstance> instList = new ArrayList<IServiceInstance>();
    IServiceInstance inst = handler.getServiceInstance(IServiceName.MWI_NOTIFICATION, "9988");
    instList.add(inst);
    client.setComponents(IServiceName.MWI_NOTIFICATION, instList);
    client.refreshStatus();
    server.start();

    initDelayHandler();
    SIPOut sipOut = new SIPOut(delayHandler);
    delayHandler.registeringDone();
    UserMailbox inbox = new UserMailbox(2, 3, 0, 0, 0, 0, 0, 0, false);
    TestUser user = new TestUser();
    SIPFilterInfo filter = new SIPFilterInfo(null); // Oh, no good, but we don't want a nullp.
    FeedbackHandler ng = new MyFeedbackHandler();

    int returnCount = -1;
    try {
    returnCount = sipOut.handleMWI(user, filter, inbox);
    } catch (Exception np) {
      ;
    }
    assertEquals(0, returnCount);
 }



    private class MyFeedbackHandler implements FeedbackHandler {

        public void expired(com.mobeon.ntf.userinfo.UserInfo user, int notifType) {
            expireCount++;
        }

        public void failed(com.mobeon.ntf.userinfo.UserInfo user, int notifType, String msg) {
            failCount++;
        }

        public void ok(com.mobeon.ntf.userinfo.UserInfo user, int notifType) {
            okCount++;
        }

        public void retry(com.mobeon.ntf.userinfo.UserInfo user, int notifType, String msg) {
            retryCount++;
        }

    }

    private void initDelayHandler()
    {
        // Log coupling
        DelayLoggerProxy sdLogProxy = new DelayLoggerProxy();
        SDLogger.setLogger(sdLogProxy);

        SDLogger.setLevel(SDLogger.DEBUG);

        // Make the delayer
        String directory  = Config.getDataDirectory();
            directory += "/delaydb";
        String dbBaseName = "delaydb";
        Properties props  = new Properties();
        props.setProperty(DBDelayHandler.KEY_STORAGE_DIR, directory);
        props.setProperty(DBDelayHandler.KEY_STORAGE_BASE, dbBaseName);
        try {
            delayHandler = new DBDelayHandler(props);
        } catch (DelayException de) {
            log.logMessage("Failed to create DelayHandler, No system Reminders will be handled\n" + de.toString(),
                           log.L_ERROR);
        }

    }
}
