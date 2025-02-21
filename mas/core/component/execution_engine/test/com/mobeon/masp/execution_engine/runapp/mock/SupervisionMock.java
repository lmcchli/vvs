package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.operateandmaintainmanager.*;

/**
 * @author David Looberger
 */
public class SupervisionMock implements Supervision {
    private static final ILogger log = ILoggerFactory.getILogger(SupervisionMock.class);

    private SessionInfoFactoryMock sif = new SessionInfoFactoryMock();
    public ProvidedService createProvidedServiceEntry(String ServiceName, String Host, int Port, ServiceEnablerOperate serviceEnablerOperate) {
        log.debug("SupervisionMock: createProvidedServiceEntry not implemented");
        return null;
    }

    public ProvidedService createProvidedServiceEntry(String ServiceName, String Host, Integer Port, ServiceEnablerOperate serviceEnablerOperate) throws IlegalServiceParametersException {
        log.debug("SupervisionMock: createProvidedServiceEntry not implemented");        
        return null;
    }

    public ConsumedService createConsumedServiceEntry(String ServiceName, String Host, int Port) {
        log.debug("SupervisionMock: createConsumedServiceEntry not implemented");
        return null;
    }

    public ServiceEnablerInfo getServiceEnablerStatistics(ServiceEnablerOperate serviceEnablerRef) throws Exception {
        log.debug("SupervisionMock: getServiceEnablerStatistics not implemented");
        return null;
    }

    public SessionInfoFactory getSessionInfoFactory() {

        return sif;
    }
}
