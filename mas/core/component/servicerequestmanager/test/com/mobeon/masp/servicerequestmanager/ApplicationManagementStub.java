/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager;

import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.IApplicationExecution;

import java.net.URI;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author mmawi
 */
public class ApplicationManagementStub implements IApplicationManagment {

    public ApplicationManagementStub() {
    }

    public IApplicationExecution getAppFromURI(URI uri) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IApplicationExecution load(String serviceId) {
        return ApplicationExecutionFactoryStub.create();
    }
}
