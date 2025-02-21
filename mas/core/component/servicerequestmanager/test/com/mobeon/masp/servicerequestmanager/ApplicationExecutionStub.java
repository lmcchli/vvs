/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager;

import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Random;

/**
 * @author mmawi
 */
public class ApplicationExecutionStub implements IApplicationExecution {

    private static ILogger log = ILoggerFactory.getILogger(ApplicationExecutionStub.class);

    private ISession session;
    private IServiceRequestManager serviceRequestManager;
    private Application application;


    public ApplicationExecutionStub() {
    }

    public ApplicationExecutionStub(IServiceRequestManager serviceRequestManager) {
        this.serviceRequestManager = serviceRequestManager;
    }

    public void setServiceRequestManager(IServiceRequestManager serviceRequestManager) {
        this.serviceRequestManager = serviceRequestManager;
    }

    public void setSupervision(Supervision supervision) {
    }
    
    public void setSession(ISession session) {
        this.session = session;
    }

    public void start() {
        ServiceRequest request = (ServiceRequest) session.getData(ISession.SERVICE_REQUEST);
        for (String parameterName : request.getParameterNames()) {
            log.debug("Parameter: " + parameterName + ":" + request.getParameter(parameterName));
        }
        
        final ServiceResponse response = new ServiceResponse();
        response.setStatusCode(ServiceResponse.STATUSCODE_SUCCESS_COMPLETE);
        response.setStatusText(ServiceResponse.STATUSTEXT_SUCCESS_COMPLETE);
        response.setParameter("testParameter", "OK");

        application = new Application(response);
        application.start();
    }

    public ISession getSession() {
        return session;
    }

    public IEventDispatcher getEventDispatcher() {
        return null;
    }

    public void terminate() {
        log.debug("Terminating session " + session.getId());
        application.interrupt();
    }

    private class Application extends Thread {
        private Random random = new Random();
        private ServiceResponse response;

        public Application(ServiceResponse response) {
            this.response = response;
        }

        public void run() {
            int time = random.nextInt(5);
            time = (time + 5) * 1000;
            try {
                sleep(time);
                //send response to ServiceRequestManager
                serviceRequestManager.sendResponse(session.getId(), response);
            } catch (InterruptedException e) {
                //do nothing
                log.debug("application interrupted.");
            } catch (ServiceRequestManagerException e) {
                log.warn("Failed to send response.");
            }
        }
    }
}
