package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.execution_engine.ApplicationExecutionImpl;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.events.ApplicationEnded;
import com.mobeon.masp.execution_engine.runapp.ApplicationBasicTestCase;
import com.mobeon.masp.execution_engine.runapp.TestAppender;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.rule.CCXMLToParentRule;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRules;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.servicerequestmanager.ServiceRequest;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 10, 2006
 * Time: 6:09:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceEnablerMock extends BaseMock implements IEventReceiver {
    /**
     * The event disptacher for this call.
     */
    protected volatile IEventDispatcher eventDispatcher;
    protected CallMediaTypes callMediaTypes;

    protected volatile boolean exited = false;
    protected Semaphore semaphore = new Semaphore(0);

    protected final IApplicationManagment applicationManagement;
    /* Our execution */
    protected ApplicationExecutionImpl api = null;
    public static String sessionInitiator = "sip";
    protected ExecutorService service;


    protected ServiceEnablerMock(ExecutorService service, IApplicationManagment applicationManagement){
        this.service = service;
        this.applicationManagement = applicationManagement;
    }

    public boolean waitForExecutionToFinish(long totalTimeToWait) {
        totalTimeToWait = ApplicationBasicTestCase.configureTimeout(totalTimeToWait);
        /* For now we just satisfies us with a dialog exit
     as a finish */
        long timeSpent = 0;
        long timeAtTestStart = System.currentTimeMillis();

        log.info("MOCK: Waiting for call to finish");

        // Wait for it to finish, ugly loop, replace with semaphore instead
        long wait = totalTimeToWait;
        while (!exited && (totalTimeToWait > timeSpent || totalTimeToWait <= 0)) {
            try {
                semaphore.tryAcquire(wait, TimeUnit.MILLISECONDS);
                timeSpent = System.currentTimeMillis() - timeAtTestStart;
                wait = Math.max(500, totalTimeToWait - timeSpent);
            } catch (InterruptedException e) {
                log.info("MOCK: We got interrupted, probably because the application ended");
                timeSpent = System.currentTimeMillis() - timeAtTestStart;
                wait = Math.max(0, totalTimeToWait - timeSpent);
            }
        }

        // Check the timeout
        boolean finishedInTime = totalTimeToWait > timeSpent || totalTimeToWait == 0;

        if (finishedInTime) {
            log.info("MOCK: Call finish has been detected in " + timeSpent + " ms, we were allowed " + totalTimeToWait + " !");
        } else {
            try {
                TestAppender.stopSave(log);
                Thread.currentThread().getThreadGroup().interrupt();
                Thread.interrupted(); //Clear interrupted flag
                synchronized (this) {
                    this.wait(200);
                }
            } catch (InterruptedException ie) {
                Thread.interrupted(); //Clear interrupted flag
            }
            log.info("MOCK: Timeout for execution ! We spent " + timeSpent + " ms running the test and , we were allowed " + totalTimeToWait);
        }

        // True if this is a clean exit !
        return finishedInTime;
    }


    /**
     * Load the service, but do not start it !
     *
     * @param service
     */
    public void loadService(String service) {

        // Create the application instance
        api = (ApplicationExecutionImpl) applicationManagement.load(service);

        if (api == null) {
            log.error("Failed to load service!");
            return;
        }

        api.setSession(getSessionFactory().create());
        // Set the event dispatche used by this instance
        setEventDispatcher(api.getEventDispatcher());
        eventDispatcher.addEventReceiver(this);

        // Create a sessions
        ISession session = api.getSession();
        session.setData("InboundCall", this);
        session.setData(ISession.SESSION_INITIATOR, sessionInitiator);
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setParameter("kalle", "kalle_value");
        session.setData(ISession.SERVICE_REQUEST, serviceRequest);

        if (callMediaTypes != null) {
            session.setData("selectedcallmediatypes", callMediaTypes);
        }
        exited = false;
    }

    /**
     * Sets the event dispatcher for this call.
     *
     * @param eventDispatcher
     */
    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    /**
     * This is the implementation that loads a service and starts the
     * execution of an application.
     *
     * @deprecated Use loadService() and startService() instead.
     */
    public void loadAndExecuteService(String service) {

        // Create the application instance
        api = (ApplicationExecutionImpl) applicationManagement.load(service);
        api.setSession(getSessionFactory().create());

        // Set the event dispatche used by this instance
        setEventDispatcher(api.getEventDispatcher());

        // Create a sessions
        ISession session = api.getSession();
        if (callMediaTypes != null) {
            session.setData("selectedcallmediatypes", callMediaTypes);
        }
        exited = false;

        // Start the application
        api.createRuntimes();
        if (api.getMaster() != null) {
            EventStream esp = api.getParentStream();
            EventStream es = api.getMaster().
                    getExecutionContext().
                    getEventStream();

            EventStream.Extractor extractor = es.new Extractor(EventRules.TRUE_RULE, EventRules.TRUE_RULE, this);
            EventStream.Extractor extractor2 = esp.new Extractor(new CCXMLToParentRule(), EventRules.FALSE_RULE, this);

            es.add(extractor);
            esp.add(extractor2);
        }

    }

    /**
     * Handle all local events for the execution environment,
     * we do this to listen for when the dialog has finished.
     *
     * @param event
     */
    public void doEvent(Event event) {

        // We used to wait for ccxml.exit before we considetred the call to be finished but nowadays
        // we also wait for ApplicationEnded, and exit as soon as any arrives.

        if(event instanceof ApplicationEnded){
            log.info("MOCK: Call finished! (ApplicationEnded)");
            exited = true;
            semaphore.release();
        } else {
            log.info("MOCK: Event arrived = " + event.toString());
        }
    }

    /**
     * * Handle all global events for the execution evironment
     *
     * @param event
     */
    public void doGlobalEvent(Event event) {
        log.info("MOCK: InboundCallMock.doGlobalEvent is unimplemented!");
    }

    /**
     * Fires an event to the event dispatcher.
     * @param event
     */
    public void fireEvent(Event event) {
        IEventDispatcher eventDispatcher = getEventDispatcher();
        if (eventDispatcher != null) {
            eventDispatcher.fireEvent(event);
            log.info("MOCK: InboundCallMock.fireEvent " + event);
        } else {
            log.error("MOCK: InboundCallMock.fireEvent " + event + " Could not be fired since Event Dispatcher is null");
        }
    }

    /**
     * Returns with the event disptacher for this call.
     *
     * @return the event dispatcher associated with this call.
     */
    public IEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }
}
