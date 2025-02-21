/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager.diagnoseservice;

import junit.framework.TestCase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.operateandmaintainmanager.DiagnoseService;
import com.mobeon.masp.operateandmaintainmanager.ServiceInstance;
import com.mobeon.masp.operateandmaintainmanager.Status;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author mmawi
 */
public class DiagnoseServiceImplMTest extends TestCase {
    /**
     * The {@link com.mobeon.common.logging.ILogger} logger used for logging purposes.
     */
    protected final ILogger LOGGER = ILoggerFactory.getILogger(getClass());

    private final String HOST = "buell.mvas.lab.mobeon.com";
    private final int PORT = 9090;

    private DiagnoseService diagnoseService;

    protected void setUp() throws Exception {
        super.setUp();

        XmlBeanFactory bf = new XmlBeanFactory(new ClassPathResource("test_beans.xml"));

        diagnoseService = (DiagnoseServiceImpl) bf.getBean("DiagnoseServiceXMP");
    }

    public void testSendRequest() throws Exception {
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setHostName(HOST);
        serviceInstance.setPort(PORT);

        Status status = diagnoseService.serviceRequest(serviceInstance);
        assertEquals("Status is not UP", Status.UP, status);
    }
}
