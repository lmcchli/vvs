package com.mobeon.masp.servicerequestmanager.diagnoseservice;

import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.masp.operateandmaintainmanager.ServiceInstance;
import com.mobeon.masp.operateandmaintainmanager.Status;
import com.mobeon.common.logging.ILoggerFactory;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.util.MissingResourceException;

/**
 * @author mmawi
 */
public class DiagnoseServiceTest extends MockObjectTestCase {

    private static final String LOG4J_CONFIGURATION = "mobeon_log.xml";

    private static final String HOST = "localhost";
    private static final String CLIENT_ID = "diagnoseservicetest@localhost";
    private static final int PORT = 8080;
    private static final int REQUEST_TIMEOUT = 30000;
    private DiagnoseServiceImpl diagnoseService;
    private Mock mockConfiguration;
    private ServiceInstance serviceInstace;
    private static XMPServerStub simulatedSRM;

    static {
        // Initialize console logging
        // Sets the configuration file for the logging
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);

        simulatedSRM = new XMPServerStub(PORT);
        simulatedSRM.start();
    }

    protected void setUp() throws Exception {
        super.setUp();
        setupMockedConfiguration();
        setupServiceInstance(HOST, PORT);

        diagnoseService = new DiagnoseServiceImpl();
        try {
            diagnoseService.init();
            fail("MissingResourceException expected.");
        } catch (MissingResourceException e) {
            //OK
        }

        diagnoseService.setConfiguration((IConfiguration)mockConfiguration.proxy());
        diagnoseService.init();

        // Set up the stubbed server OK.
        simulatedSRM.setResponseCode(200);
        simulatedSRM.setInvalidResponse(false);
        simulatedSRM.setInvalidXml(false);
        simulatedSRM.setTimeout(false);
    }

    //===== Test cases =====

    public void testServiceRequestWithInvalidServiceInstance() throws Exception {
        try {
            diagnoseService.serviceRequest(null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            //OK
        } catch (Exception e) {
            fail("IllegalArgumentException expected, caught " + e);
        }

        setupServiceInstance(null, 0);
        try {
            diagnoseService.serviceRequest(serviceInstace);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            //OK
        } catch (Exception e) {
            fail("IllegalArgumentException expected, caught " + e);
        }

        setupServiceInstance("localhost", -1);
        try {
            diagnoseService.serviceRequest(serviceInstace);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            //OK
        } catch (Exception e) {
            fail("IllegalArgumentException expected, caught " + e);
        }
    }

    public void testServiceRequestWhenCommunicationsError() throws Exception {
        setupServiceInstance(HOST, 8081);
        assertEquals(Status.DOWN, diagnoseService.serviceRequest(serviceInstace));
    }

    public void testServiceRequestWhenErrorCode() throws Exception {
        setupServiceInstance(HOST, PORT);
        simulatedSRM.setResponseCode(421);
        assertEquals(Status.DOWN, diagnoseService.serviceRequest(serviceInstace));
    }

    public void testServiceRequestWhenTimeout() throws Exception {
        setupServiceInstance(HOST, PORT);
        simulatedSRM.setTimeout(true);
        assertEquals(Status.DOWN, diagnoseService.serviceRequest(serviceInstace));
    }

    public void testServiceRequestWhenNoServiceResponse() throws Exception {
        setupServiceInstance(HOST, PORT);
        simulatedSRM.setInvalidResponse(true);
        assertEquals(Status.DOWN, diagnoseService.serviceRequest(serviceInstace));
    }

    public void testServiceRequestWhenParsingFails() throws Exception {
        setupServiceInstance(HOST, PORT);
        simulatedSRM.setInvalidXml(true);
        assertEquals(Status.DOWN, diagnoseService.serviceRequest(serviceInstace));
    }

    public void testServiceRequestSuccessful() throws Exception {
        setupServiceInstance(HOST, PORT);
        assertEquals(Status.UP, diagnoseService.serviceRequest(serviceInstace));
    }

    /**
     * Verify that the configuration can be updated. this is actually just to
     * increase coverage...
     * @throws Exception if test case fails.
     */
    public void testConfiguration() throws Exception {
        assertEquals(CLIENT_ID, DiagnoseServiceConfiguration.getInstance().getClientId());
        DiagnoseServiceConfiguration.getInstance().setClientId("newClientId");
        assertEquals("newClientId", DiagnoseServiceConfiguration.getInstance().getClientId());

        assertEquals(REQUEST_TIMEOUT, DiagnoseServiceConfiguration.getInstance().getRequestTimeout());
        DiagnoseServiceConfiguration.getInstance().setRequestTimeout(45000);
        assertEquals(45000, DiagnoseServiceConfiguration.getInstance().getRequestTimeout());

        try {
            DiagnoseServiceConfiguration.getInstance().setInitialConfiguration(null);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            //OK
        }


        mockConfiguration.stubs()
                .method("getConfiguration")
                .will(returnValue(null));
        DiagnoseServiceConfiguration.getInstance()
                .setInitialConfiguration((IConfiguration)mockConfiguration.proxy());
        DiagnoseServiceConfiguration.getInstance().updateConfiguration();
        assertEquals("newClientId", DiagnoseServiceConfiguration.getInstance().getClientId());
        assertEquals(45000, DiagnoseServiceConfiguration.getInstance().getRequestTimeout());

    }

    //===== End of test cases =====

    private void setupMockedConfiguration() {
        mockConfiguration = mock(IConfiguration.class);

        Mock dsGroup = mock(IGroup.class);
        dsGroup.stubs()
                .method("getString")
                .with(eq("clientid"), ANYTHING)
                .will(returnValue(CLIENT_ID));

        Mock srmGroup = mock(IGroup.class);
        srmGroup.stubs()
                .method("getInteger")
                .with(eq("requesttimeout"), ANYTHING)
                .will(returnValue(REQUEST_TIMEOUT));
        srmGroup.stubs()
                .method("getGroup")
                .with(eq("diagnoseservice"))
                .will(returnValue(dsGroup.proxy()));

        mockConfiguration.stubs()
                .method("getGroup")
                .with(eq("servicerequestmanager"))
                .will(returnValue(srmGroup.proxy()));
        mockConfiguration.stubs()
                .method("getConfiguration")
                .will(returnValue(mockConfiguration.proxy()));
    }

    private void setupServiceInstance(String host, int port) {
        serviceInstace = new ServiceInstance();
        serviceInstace.setHostName(host);
        serviceInstace.setPort(port);
    }
}
