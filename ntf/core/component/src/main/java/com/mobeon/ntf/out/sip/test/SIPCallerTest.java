package com.mobeon.ntf.out.sip.test;

import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.test.TestXMPHandler;
import com.mobeon.ntf.out.sip.SIPCaller;
import com.mobeon.ntf.out.sip.SIPInfo;
import com.mobeon.ntf.out.sip.SIPCallListener;
import com.mobeon.ntf.util.ILoggerProxy;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.externalcomponentregister.ServiceInstanceImpl;
import com.mobeon.common.xmp.client.XmpClient;
import com.mobeon.common.xmp.server.HttpServer;
import com.mobeon.common.xmp.server.XmpHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mnify
 * Date: 2007-mar-01
 * Time: 10:19:59
 * To change this template use File | Settings | File Templates.
 */
public class SIPCallerTest extends NtfTestCase implements SIPCallListener {

    private SIPInfo returnInfo = null;
    private int returnCode = -1;
    private int returnTime = -1;

    public SIPCallerTest(String name) {
        super(name);
    }

    public void testRequest() throws Exception {
        l("testrequest");
        SIPCaller caller = new SIPCaller();
        SIPInfo info = new SIPInfo("222222", "kalle@mobeon.com", "333333", new Date().getTime());
        int transId = 7;
        String xmpRequest = caller.getXMPRequest(info, transId);
        assertFalse(xmpRequest.contains("voice"));
        assertFalse(xmpRequest.contains("video"));
        assertFalse(xmpRequest.contains("fax"));
        assertFalse(xmpRequest.contains("email"));
        assertTrue(xmpRequest.contains("<parameter name=\"number\">222222</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"mailbox-id\">333333</parameter>"));
        assertTrue(xmpRequest.contains("service-id=\"MWINotification\""));
        assertTrue(xmpRequest.contains("transaction-id=\"7\""));


        info.setNewVoiceCount(2);
        info.setOldVoiceCount(3);
        xmpRequest = caller.getXMPRequest(info, transId);

        assertTrue(xmpRequest.contains("<parameter name=\"old-voice\">3</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"new-voice\">2</parameter>"));
        assertFalse(xmpRequest.contains("video"));
        assertFalse(xmpRequest.contains("fax"));
        assertFalse(xmpRequest.contains("email"));

        info.setNewVoiceCount(0);
        info.setOldVoiceCount(0);
        xmpRequest = caller.getXMPRequest(info, transId);

        assertTrue(xmpRequest.contains("<parameter name=\"old-voice\">0</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"new-voice\">0</parameter>"));
        assertFalse(xmpRequest.contains("video"));
        assertFalse(xmpRequest.contains("fax"));
        assertFalse(xmpRequest.contains("email"));

        info.setNewVideoCount(2);
        info.setOldVideoCount(0);
        xmpRequest = caller.getXMPRequest(info, transId);

        assertTrue(xmpRequest.contains("<parameter name=\"old-voice\">0</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"new-voice\">0</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"old-video\">0</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"new-video\">2</parameter>"));
        assertFalse(xmpRequest.contains("fax"));
        assertFalse(xmpRequest.contains("email"));

        info.setNewFaxCount(0);
        info.setOldFaxCount(1);
        xmpRequest = caller.getXMPRequest(info, transId);

        assertTrue(xmpRequest.contains("<parameter name=\"old-voice\">0</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"new-voice\">0</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"old-video\">0</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"new-video\">2</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"old-fax\">1</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"new-fax\">0</parameter>"));
        assertFalse(xmpRequest.contains("email"));

        info.setNewEmailCount(1);
        info.setOldEmailCount(1);
        xmpRequest = caller.getXMPRequest(info, transId);

        assertTrue(xmpRequest.contains("<parameter name=\"old-voice\">0</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"new-voice\">0</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"old-video\">0</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"new-video\">2</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"old-fax\">1</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"new-fax\">0</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"old-email\">1</parameter>"));
        assertTrue(xmpRequest.contains("<parameter name=\"new-email\">1</parameter>"));

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

        SIPCaller caller = new SIPCaller();
        SIPInfo info = new SIPInfo("222222", "kalle@mobeon.com", "333333", new Date().getTime());
        info.setNewVoiceCount(5);
        info.setOldVoiceCount(753);
        caller.sendCall(info, this);

        Thread.sleep(5000);
        assertEquals(200, returnCode);
        assertEquals(0, returnTime);
        assertEquals(info, returnInfo);

        handler.setStatusCode(450);
        caller.sendCall(info, this);

        Thread.sleep(5000);
        assertEquals(450, returnCode);
        assertEquals(0, returnTime);
        assertEquals(info, returnInfo);

        handler.addParameter("retry-time", "400");
        handler.setStatusCode(450);
        caller.sendCall(info, this);

        Thread.sleep(5000);
        assertEquals(450, returnCode);
        assertEquals(400, returnTime);
        assertEquals(info, returnInfo);

        client.stop();
        server.stopServer();
    }


    public void handleResult(SIPInfo info, int code, int retryTime) {
        this.returnInfo = info;
        this.returnCode = code;
        this.returnTime = retryTime;

        //To change body of implemented methods use File | Settings | File Templates.
    }
}
