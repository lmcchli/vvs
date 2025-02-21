/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.states;

import com.mobeon.masp.callmanager.CallManagerController;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.events.CloseForcedEvent;
import com.mobeon.masp.callmanager.events.OpenEvent;
import com.mobeon.masp.callmanager.events.CloseUnforcedEvent;
import com.mobeon.masp.callmanager.events.UpdateThresholdEvent;
import com.mobeon.masp.callmanager.loadregulation.LoadRegulator;
import com.mobeon.masp.callmanager.loadregulation.states.NormalLoadState;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * The administrative state Closed.
 *
 * @author Malin Flodin
 */
public class ClosedState implements AdministrativeState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final CallManagerController cmController;
    private final LoadRegulator loadRegulator;

    public ClosedState(CallManagerController cmController,
                       LoadRegulator loadRegulator) {
        this.cmController = cmController;
        this.loadRegulator = loadRegulator;
    }

    public synchronized void closeForced(CloseForcedEvent closeForcedEvent) {
        if (log.isInfoEnabled())
            log.info("Close request (forced) received in " + this + ". Close is " +
                    "considered completed immediately due to the current state.");
        cmController.closeCompleted();
    }

    public synchronized void closeUnforced(CloseUnforcedEvent closeUnforcedEvent) {
        if (log.isInfoEnabled())
            log.info("Close request (unforced) received in " + this + ". Close is " +
                    "considered completed immediately due to the current state.");
        cmController.closeCompleted();
    }

    public synchronized void open(OpenEvent openEvent) {
        if (log.isDebugEnabled())
            log.debug("Open request received in " + this + ".");

        if (loadRegulator.getCurrentState() instanceof NormalLoadState) {
            if (log.isInfoEnabled())
                log.info("Open request received in " + this +
                        " and the load situation is normal so " +
                        "registration towards all SSPs is initiated.");
            cmController.registerAllSsps();

        } else {
            if (log.isInfoEnabled())
                log.info("Open request received in " + this +
                        " but the load situation is high. No registration towards " +
                        "the SSPs is initiated until the load situation has improved.");
        }

        cmController.openCompleted();
        cmController.setOpenedState();
    }

    public synchronized CALL_ACTION addInboundCall(String id) {
        if (log.isDebugEnabled())
            log.debug("New inbound call is rejected in " + this + ".");
        return CALL_ACTION.REJECT_CALL;
    }

    public synchronized CALL_ACTION checkCallAction() {
        if (log.isDebugEnabled())
            log.debug("A new inbound call would be rejected in " + this + ".");
        return CALL_ACTION.REJECT_CALL;
    }

    public synchronized CALL_ACTION addOutboundCall(String id) {
        if (log.isDebugEnabled())
            log.debug("New outbound call is rejected in " + this + ".");
        return CALL_ACTION.REJECT_CALL;
    }

    public synchronized void removeCall(String id) {
        if (log.isDebugEnabled())
            log.debug("Removing a call in " + this +
                    " suggests implementation error and is ignored.");
    }

    public void updateThreshold(UpdateThresholdEvent thresholdEvent) {
        if (log.isDebugEnabled())
            log.debug("Updating threshold in " + this + ": " + thresholdEvent);

        loadRegulator.updateThreshold(
                        thresholdEvent.getHighWaterMark(),
                        thresholdEvent.getLowWaterMark(),
                        thresholdEvent.getThreshold());

        CMUtils.getInstance().getServiceEnablerInfo().
                setMaxConnections(loadRegulator.getConfiguredMax());
    }

    public String toString() {
        return "Closed state";
    }
}
