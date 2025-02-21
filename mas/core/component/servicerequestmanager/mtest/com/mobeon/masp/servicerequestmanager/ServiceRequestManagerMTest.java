/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.servicerequestmanager;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import jakarta.activation.DataSource;
import java.util.List;

/**
 * Manual test the ServiceRequestManager. Uses the real version of the XMP interface. (The XMPClient)
 * An external test XMP server must be started for these tests to work.
 *
 * @author ermmaha
 */
public class ServiceRequestManagerMTest extends MockObjectTestCase {
    private static final String cfgFile = "../servicerequestmanager/servicerequestmanager.xml";
    private static final String LOG4J_CONFIGURATION = "../trafficeventsender/log4jconf.xml";

    protected Mock serviceLocator;
    protected IConfiguration iConfiguration;

    static {
        // Initialize console logging
        // Sets the configuration file for the logging
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    public ServiceRequestManagerMTest(String name) throws Exception {
        super(name);

        iConfiguration = getConfiguration();
        setupMockedServiceLocator();
    }

    /**
     * Test the CallMWINotification service request.
     *
     * @throws Exception
     */
    public void testCallMWINotification() throws Exception {
        ServiceRequestManager srm = new ServiceRequestManager((ILocateService) serviceLocator.proxy(), iConfiguration);

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setServiceId(IServiceName.CALL_MWI_NOTIFICATION);
        serviceRequest.setParameter("mailbox-id", "ermmaha@mobeon.com");
        serviceRequest.setParameter("called-number", "161074");
        serviceRequest.setParameter("called-type-of-number", 1);
        serviceRequest.setParameter("called-numbering-plan-id", 2);
        serviceRequest.setParameter("calling-number", "0702660291");
        serviceRequest.setParameter("calling-type-of-number", 3);
        serviceRequest.setParameter("calling-numbering-plan-id", 4);

        ServiceResponse response = srm.sendRequest(serviceRequest);
        assertEquals(200, response.getStatusCode());
        assertEquals("Success", response.getStatusText());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Test some errors
     *
     * @throws Exception
     */
    public void testErrorHandling() throws Exception {
        ServiceRequestManager srm = new ServiceRequestManager((ILocateService) serviceLocator.proxy(), iConfiguration);

        //Test a bad service id, the response should be null
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setServiceId("NotAService");
        ServiceResponse response = srm.sendRequest(serviceRequest);
        assertNull(response);

        //Test bad hostname for the TextToSpeech service
        Mock serviceInstance = mock(IServiceInstance.class);
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue("xbrage.mobeon.com"));
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue("8899"));
        serviceLocator.stubs().method("locateService").with(eq(IServiceName.TEXT_TO_SPEECH)).will(returnValue(serviceInstance.proxy()));

        serviceRequest.setServiceId(IServiceName.TEXT_TO_SPEECH);
        response = srm.sendRequest(serviceRequest);
        assertEquals(421, response.getStatusCode());
        assertEquals("Service TextToSpeech not available.", response.getStatusText());
    }

    /**
     * Test the MediaConversion service request.
     *
     * @throws Exception
     */
    public void testMediaConversion() throws Exception {
        ServiceRequestManager srm = new ServiceRequestManager((ILocateService) serviceLocator.proxy(), iConfiguration);
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setServiceId(IServiceName.MEDIA_CONVERSION);
        serviceRequest.setParameter("toFormat", "amr");

        ServiceResponse response = srm.sendRequest(serviceRequest);
        assertEquals(200, response.getStatusCode());
        assertEquals("Success", response.getStatusText());
        String[] paramNames = response.getParameterNames();
        assertEquals("length", paramNames[0]);
        assertEquals("3" , (String) response.getParameter("length"));

        List<DataSource> attachments = response.getAttachments();
        assertEquals(1, attachments.size());
        DataSource ds = attachments.get(0);
        assertTrue(ds.getContentType().indexOf("audio/amr") > -1);
    }

    private void setupMockedServiceLocator() {
        Mock serviceInstance = mock(IServiceInstance.class);
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue("brage.mobeon.com"));
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue("8899"));
        serviceInstance.stubs().method("getProperty").with(eq(IServiceInstance.LOGICALZONE)).will(returnValue("UNDEFINED"));

        serviceLocator = mock(ILocateService.class);

        String[] serviceNames = new String[] {IServiceName.TEXT_TO_SPEECH, IServiceName.MEDIA_CONVERSION, IServiceName.CALL_MWI_NOTIFICATION};
        for (int i = 0; i < serviceNames.length; i++) {
            serviceLocator.stubs().method("locateService").with(eq(serviceNames[i])).will(returnValue(serviceInstance.proxy()));
            serviceLocator.stubs().method("locateService").with(isA(String.class), eq(serviceNames[i])).will(returnValue(serviceInstance.proxy()));
        }
    }

    private IConfiguration getConfiguration() throws Exception {
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(cfgFile);
        return configurationManager.getConfiguration();
    }

    public static Test suite() {
        return new TestSuite(ServiceRequestManagerMTest.class);
    }
}
//System.out.println(response.getStatusCode() + " " + response.getStatusText());