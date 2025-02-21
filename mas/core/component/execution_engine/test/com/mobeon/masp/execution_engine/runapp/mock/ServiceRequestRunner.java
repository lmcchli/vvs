package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.execution_engine.ApplicationExecutionImpl;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.rule.CCXMLToParentRule;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRules;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.stream.IStreamFactory;
import com.mobeon.masp.callmanager.events.AlertingEvent;
import com.mobeon.masp.callmanager.Call;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 10, 2006
 * Time: 5:52:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceRequestRunner extends ServiceEnablerMock {

    public ServiceRequestRunner(ExecutorService executorService, IApplicationManagment applicationManagement){
        super(executorService, applicationManagement);
        sessionInitiator = "ntf1@host.com";
        log.info("MOCK: ServiceRequestRunner.ServiceRequestRunner");

    }

    public void startCall() {
        api.start();
    }


    public void terminate() {
        api.terminate();
    }
}
