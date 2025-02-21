package com.mobeon.masp.servicerequestmanager;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.masp.servicerequestmanager.diagnoseservice.XMPServerStub;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: 2007-mar-29
 * Time: 17:40:56
 * To change this template use File | Settings | File Templates.
 */
public class TestVersusRealServer extends MockObjectTestCase implements Thread.UncaughtExceptionHandler  {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private static final String CLIENT_ID = "mas@test.com";
    private static final String host = "localhost";
    private static final String port = "8899";
    private static final String zone =  "UNDEFINED";

    protected Mock jmockConfiguration;
    protected Mock jmockServiceLocator;


    public void setUp() throws Exception{
        super.setUp();
        jmockConfiguration = mock(IConfiguration.class);
        jmockServiceLocator = mock(ILocateService.class);

        Mock jmockConfigSrm = mock(IGroup.class);
        jmockConfigSrm.expects(once()).method("getInteger")
                .with(eq("requesttimeout"))
                .will(returnValue(10000)); // currently foundation XMP sleeps for 5 secs if fails to connect. lower value than this is not recommended
        jmockConfigSrm.expects(once()).method("getInteger")
                .with(eq("requestretries"))
                .will(returnValue(3));
            jmockConfigSrm.expects(once()).method("getString")
                    .with(eq("clientid"))
                    .will(returnValue(CLIENT_ID));

        jmockConfiguration.expects(once()).method("getGroup")
                .with(eq("servicerequestmanager")).will(returnValue(jmockConfigSrm.proxy()));

        Mock serviceInstance = mock(IServiceInstance.class);

        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue(host));
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue(port));
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.LOGICALZONE)).will(returnValue(zone));

        jmockServiceLocator = mock(ILocateService.class);
        jmockServiceLocator.stubs()
                .method("locateService")
                .withAnyArguments()
                .will(returnValue(serviceInstance.proxy()));

        jmockServiceLocator.stubs()
                .method("reportServiceError")
                .withAnyArguments()
                .isVoid();
    }


    /**
     * Test that if the XMP server becomes up after have been down, it is again
     * possible to send requests to it (TR 29975)
     * @throws Exception
     */
    public void testSend1() throws Exception {

        XMPServerStub xmpServerStub1 = null;
        XMPServerStub xmpServerStub2 = null;
        try {

            IServiceRequestManager manager = new ServiceRequestManager((ILocateService) jmockServiceLocator.proxy(),
                    (IConfiguration)jmockConfiguration.proxy());
            ServiceRequest request = makeRequest();

            XMPServerStub xmpServerStub = createStartedServer(Integer.parseInt(port));

            ServiceResponse xmpResponse = manager.sendRequest(request);
            log.debug("Sent first request");
            assertTrue(xmpResponse.getStatusCode() == 200);

            stopServer(xmpServerStub);

            // now we expect a "service unavailable"
            xmpResponse = manager.sendRequest(request);
            log.debug("Sent second request");
            assertTrue(xmpResponse.getStatusCode() == 421);

            // Start a server again

            xmpServerStub2 = createStartedServer(Integer.parseInt(port));

            xmpResponse = manager.sendRequest(request);
            log.debug("Sent third request");

            assertTrue(xmpResponse.getStatusCode() == 200);
        } finally {
            stopServer(xmpServerStub1);
            stopServer(xmpServerStub2);
        }
    }

    /**
     * Verify that it is possible to send a request to 2 different servers, and stopping ione does not interfer
     * communication with the other.
     * @throws Exception
     */
    public void testSend2() throws Exception {

        XMPServerStub xmpServerStub1 = null;
        XMPServerStub xmpServerStub2 = null;

        try {
            IServiceRequestManager manager = new ServiceRequestManager((ILocateService) jmockServiceLocator.proxy(),
                    (IConfiguration)jmockConfiguration.proxy());
            ServiceRequest request = makeRequest();

            int port1 = 9100;
            int port2 = 9101;

            xmpServerStub1 = createStartedServer(port1);
            xmpServerStub2 = createStartedServer(port2);

            ServiceResponse xmpResponse = manager.sendRequest(request, host, port1);
            assertTrue(xmpResponse.getStatusCode() == 200);

            xmpResponse = manager.sendRequest(request, host, port2);
            assertTrue(xmpResponse.getStatusCode() == 200);

            // Stop first server.
            // now we expect a "service unavailable"
            stopServer(xmpServerStub1);
            xmpResponse = manager.sendRequest(request, host, port1);
            assertTrue(xmpResponse.getStatusCode() == 421);

            // Send to second server, should work.

            xmpResponse = manager.sendRequest(request, host, port2);
            assertTrue(xmpResponse.getStatusCode() == 200);
        } finally {
            stopServer(xmpServerStub1);
            stopServer(xmpServerStub2);
        }
    }

    private XMPServerStub createStartedServer(int port) throws Exception {
        XMPServerStub xmpServerStub = new XMPServerStub(port);
        xmpServerStub.start();
        waitUntilServerStarted(xmpServerStub);
        return xmpServerStub;
    }

    private void stopServer(XMPServerStub xmpServerStub) throws Exception {
        if(xmpServerStub != null){
            if(xmpServerStub.isReady()){
                xmpServerStub.stopMe();
                while( xmpServerStub.isReady()){
                    Thread.sleep(100);
                }
            }
        }
    }


    private ServiceRequest makeRequest() {
        ServiceRequest request = new ServiceRequest();
        request.setValidityTime(10000);
        request.setServiceId("jaja");
        request.setParameter("param1", "value1");
        return request;
    }

    private void waitUntilServerStarted(XMPServerStub xmpServerStub) throws InterruptedException {
        while(! xmpServerStub.isReady()){
            Thread.sleep(100);
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        fail("Uncaught exception: "+e);
    }
}
