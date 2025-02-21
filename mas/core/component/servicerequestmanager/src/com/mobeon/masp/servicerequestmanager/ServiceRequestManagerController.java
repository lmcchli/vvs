/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerInfo;
import com.mobeon.masp.operateandmaintainmanager.CallType;
import com.mobeon.masp.operateandmaintainmanager.CallDirection;
import com.mobeon.masp.operateandmaintainmanager.CallResult;
import com.mobeon.masp.servicerequestmanager.events.ServiceClosed;
import com.mobeon.masp.servicerequestmanager.states.*;
import com.mobeon.common.eventnotifier.IEventDispatcher;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of the <code>IServiceRequestManagerController</code> interface.
 * <p/>
 * The controller is responsible for counting the number of concurrent XMP
 * requests handled by the <code>ServiceRequestManager</code>.
 *
 * @author mmawi
 */
public class ServiceRequestManagerController implements IServiceRequestManagerController {

    private final ILogger LOGGER = ILoggerFactory.getILogger(getClass());
    private static IServiceRequestManagerController instance;

    private final OpenedState openedState = new OpenedState(this);
    private final ClosingUnforcedState closingUnforcedState = new ClosingUnforcedState(this);
    private final ClosingForcedState closingForcedState = new ClosingForcedState(this);
    private final ClosedState closedState = new ClosedState(this);

    private AtomicReference<AdministrativeState> currentState =
            new AtomicReference<AdministrativeState>(closedState);

    private AtomicInteger threshold = new AtomicInteger(0);
    // counts the total number of XMP sessions
    private AtomicInteger currentSessions = new AtomicInteger(0);

    private ServiceEnablerInfo serviceEnablerInfo = null;
    private IEventDispatcher eventDispatcher = null;
    private String host;
    private int port;
    private static final String PROTOCOL = "xmp";

    public static IServiceRequestManagerController getInstance() {
        if (instance == null) {
            instance = new ServiceRequestManagerController();
        }
        return instance;
    }

    // ===== Initialization methods =====

    private ServiceRequestManagerController() {
    }

    public synchronized void setServiceEnablerInfo(ServiceEnablerInfo serviceEnablerInfo) {
        serviceEnablerInfo.setProtocol(PROTOCOL);
        serviceEnablerInfo.setMaxConnections(threshold.get());
        this.serviceEnablerInfo = serviceEnablerInfo;
    }

    public synchronized void setEventDispatcher(IEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public synchronized void setHost(String host) {
        this.host = host;
    }

    public synchronized void setPort(int port) {
        this.port = port;
    }

    // ===== End of initialization methods =====

    // ===== ServiceEnablerOperate methods =====

    public synchronized void open() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.clearSessionInfo();
            LOGGER.debug("Open received.");
        }
        getCurrentState().open();
    }

    public synchronized void close(boolean forced) {
        if (forced) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.clearSessionInfo();
                LOGGER.debug("Close (forced) received. Current sessions: " + currentSessions.get());
            }
            getCurrentState().closeForced();

        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.clearSessionInfo();
                LOGGER.debug("Close (unforced) received. Current sessions: " + currentSessions.get());
            }
            getCurrentState().closeUnforced();
        }
    }

    public synchronized void updateThreshold(int highWaterMark, int lowWaterMark, int threshold) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.clearSessionInfo();
            LOGGER.debug("Update Threshold received, threshold: " + threshold);
        }
        this.threshold.set(threshold);
        if (serviceEnablerInfo != null)
            serviceEnablerInfo.setMaxConnections(threshold);
    }

    public String getProtocol() {
        return PROTOCOL;
    }

    // ===== End of ServiceEnablerOperate methods =====


    // ===== IServiceRequestManagerController methods =====

    public synchronized boolean addSession() {
        if (currentSessions.incrementAndGet() <= threshold.get()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Session added. Current sessions: " + currentSessions.get() +
                        " Threshold: " + threshold.get());
            }
            if (serviceEnablerInfo != null) {
                serviceEnablerInfo.incrementCurrentConnections(CallType.SERVICE_REQUEST,
                        CallDirection.INBOUND);
                serviceEnablerInfo.incrementNumberOfConnections(CallType.SERVICE_REQUEST,
                        CallResult.CONNECTED, CallDirection.INBOUND);
            }
            return true;
        } else {
            currentSessions.decrementAndGet();
            return false;
        }
    }

    public synchronized void removeSession() {
        if (serviceEnablerInfo != null) {
            serviceEnablerInfo.decrementCurrentConnections(CallType.SERVICE_REQUEST,
                    CallDirection.INBOUND);
        }
        currentSessions.decrementAndGet();

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Session removed. Current sessions: " + currentSessions.get() +
                    " Threshold: " + threshold);

        getCurrentState().removeSession();
    }

    public int getCurrentSessions() {
        return currentSessions.get();
    }

    public void updateStatistics(CallResult callResult, CallDirection callDirection) {
        if (serviceEnablerInfo != null) {
            serviceEnablerInfo.incrementNumberOfConnections(CallType.SERVICE_REQUEST,
                    callResult, callDirection);
        }
    }

    public AdministrativeState getCurrentState() {
        return currentState.get();
    }

    public void setOpenedState() {
        currentState.set(openedState);
    }

    public void setClosingUnforcedState() {
        currentState.set(closingUnforcedState);
    }

    public void setClosingForcedState() {
        currentState.set(closingForcedState);
        eventDispatcher.fireEvent(new ServiceClosed());
    }

    public void setClosedState() {
        currentState.set(closedState);
    }

    public void openCompleted() {
        if (serviceEnablerInfo != null) {
            serviceEnablerInfo.opened();
        }
    }

    public void closeCompleted() {
        if (serviceEnablerInfo != null) {
            serviceEnablerInfo.closed();
        }
    }

    /**
     * Only for basic test!
     * Reset the controller to the initial state. Closed with no calls added
     * and threshold = 0.
     */
    public void clear() {
        currentState.set(closedState);
        currentSessions.set(0);
        threshold.set(0);
    }

    // ===== End of IServiceRequestManagerController methods =====


    public String toString() {
        return PROTOCOL + ":" + host + ":" + port;
    }
}
