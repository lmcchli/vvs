package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;
import com.mobeon.masp.servicerequestmanager.ServiceResponse;
import com.mobeon.masp.servicerequestmanager.ServiceRequest;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerOperate;
import com.mobeon.masp.execution_engine.ServiceEnablerException;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.ApplicationManagmentImpl;
import com.mobeon.masp.execution_engine.ApplicationExecutionImpl;
import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.masp.execution_engine.ServiceEnabler;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRules;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGenerator;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.rule.CCXMLToParentRule;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.ISessionFactory;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.xmp.server.XmpResponseQueue;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 10, 2006
 * Time: 4:12:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceRequestManagerMock extends BaseMock implements IServiceRequestManager, ServiceEnabler {

    public String serviceName;
    /**
     * Holds the application manager for this mock object.
     */
    private  IApplicationManagment applicationManagement;
    IEventDispatcher eventDispatcher;
    private ExecutorService executorService;
    private static ServiceResponse theServiceResponse;

    public void handleRequest(XmpResponseQueue xmpResponseQueue, String string, String string1, Integer integer, int i, Document document, ArrayList arrayList) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void cancelRequest(String string, Integer integer) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServiceRequestManagerMock(){
        log.info("MOCK: ServiceRequestManagerMock.ServiceRequestManagerMock");
    }

    /*
    * Neede for autowiring
    */
    public void init() throws ServiceEnablerException {
        if (applicationManagement == null){
            throw new IllegalStateException("Init was called prior to " +
                    "setting necessary fields. ApplicationManagement: " +
                    applicationManagement);
        }
    }


    public void setApplicationManagment(
            IApplicationManagment applicationManagement) {
        this.applicationManagement = (ApplicationManagmentImpl) applicationManagement;;
    }

    public ServiceRequestManagerMock(String serviceName){
        this.serviceName = serviceName;
    }

    public void start(){
        loadService();
    }

    public ServiceRequestRunner createRunner(String service){
        ServiceRequestRunner r = new ServiceRequestRunner(executorService, this.applicationManagement);
        r.setSessionFactory(getSessionFactory());
        r.loadService(service);
        return r;
    }

    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }
    private void loadService(){
        // Create the application instance
        IApplicationExecution iap = applicationManagement.load(serviceName);

        // Set the event dispatche used by this instance
        setEventDispatcher(iap.getEventDispatcher());

        // Create a sessions
        ISession session = iap.getSession();
        iap.start();
    }

    public ServiceResponse sendRequest(ServiceRequest request) {
        log.info("MOCK: ServiceRequestManagerMock.sendRequest ServiceRequest. serviceID:"+request.getServiceId());
        log.info("MOCK: ServiceRequestManagerMock.sendRequest ServiceRequest. responseRequired:"+request.getResponseRequired());
        log.info("MOCK: ServiceRequestManagerMock.sendRequest ServiceRequest. validityTime:"+request.getValidityTime());
        String[] parameterNames = request.getParameterNames();
        if(parameterNames.length == 0){
            log.info("MOCK: ServiceRequestManagerMock.sendRequest ServiceRequest. no parameters.");
        } else {
            for (String parameterName : parameterNames) {
                log.info("MOCK: ServiceRequestManagerMock.sendRequest ServiceRequest. parameter:"
                        + parameterName + "," + request.getParameter(parameterName));
            }
        }
        if(request.getResponseRequired()){
            return theServiceResponse;
        } else {
            return null;
        }
    }

    public ServiceResponse sendRequest(ServiceRequest request, String hostName) {
        log.info("MOCK: ServiceRequestManagerMock.sendRequest. hostName:"+hostName);
        log.info("MOCK: ServiceRequestManagerMock.sendRequest. serviceID:"+request.getServiceId());
        log.info("MOCK: ServiceRequestManagerMock.sendRequest. responseRequired:"+request.getResponseRequired());
        log.info("MOCK: ServiceRequestManagerMock.sendRequest. validityTime:"+request.getValidityTime());
        String[] parameterNames = request.getParameterNames();
        if(parameterNames.length == 0){
            log.info("MOCK: ServiceRequestManagerMock.sendRequest. no parameters.");
        } else {
            for (String parameterName : parameterNames) {
                log.info("MOCK: ServiceRequestManagerMock.sendRequest. parameter:"
                        + parameterName + "," + request.getParameter(parameterName));
            }
        }
        if(request.getResponseRequired()){
            return theServiceResponse;
        } else {
            return null;
        }
    }

    public ServiceResponse sendRequest(ServiceRequest request, String hostName, int portNumber) {
        return theServiceResponse;
    }

    public int sendRequestAsync(ServiceRequest request) {
        return 0;
    }

    public int sendRequestAsync(ServiceRequest request, String hostName) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int sendRequestAsync(ServiceRequest request, String hostName, int portNumber) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isTransactionCompleted(int transactionId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServiceResponse receiveResponse(int transactionId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void sendResponse(String sessionId, ServiceResponse response) {
        log.info("MOCK: ServiceRequestManagerMock.sendResponse response "+response);
    }

    public ServiceEnablerOperate getController(String serviceId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServiceEnablerOperate getController() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServiceEnablerOperate initService(String service, String host, int port) throws ServiceEnablerException {
        log.info("MOCK: ServiceRequestManagerMock.initService service "+service);
        log.info("MOCK: ServiceRequestManagerMock.initService host "+host);
        log.info("MOCK: ServiceRequestManagerMock.initService port "+port);
        return new ServiceEnablerOperateMock(service, host, port);
    }

    public void setSessionIdGenerator(IdGenerator<ISession> idGen) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void doEvent(Event event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void doGlobalEvent(Event event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public static void setResponse(ServiceResponse serviceResponse) {
        theServiceResponse = serviceResponse;
    }
}
