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
import com.mobeon.masp.callmanager.loadregulation.LoadRegulationAction;
import com.mobeon.masp.callmanager.loadregulation.states.NormalLoadState;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * The administrative state unforced Closing.
 * In this state new calls are rejected.
 *
 * @author Malin Flodin
 */
public class ClosingUnforcedRejectingState implements AdministrativeState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final CallManagerController cmController;
    private final LoadRegulator loadRegulator;

    public ClosingUnforcedRejectingState(CallManagerController cmController,
                                         LoadRegulator loadRegulator) {
        this.cmController = cmController;
        this.loadRegulator = loadRegulator;
    }

    public synchronized void closeForced(CloseForcedEvent closeForcedEvent) {
        if (log.isInfoEnabled())
            log.info("Close request (forced) received while closing in " + this +
                    ". All calls are instructed to disconnect.");

        if (loadRegulator.getCurrentCalls() > 0) {
            cmController.lockAllCalls(closeForcedEvent);
            cmController.setClosingForcedState();
        } else {
            if (log.isInfoEnabled())
                log.info("All calls have been disconnected in " + this +
                        ". The state is considered Closed.");
            cmController.closeCompleted();
            cmController.setClosedState();
        }
    }

    public synchronized void closeUnforced(CloseUnforcedEvent closeUnforcedEvent) {
        if (log.isInfoEnabled())
            log.info("Close request (unforced) received in " + this +
                    ". It is ignored.");
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
            log.debug("Adding a new outbound call in " + this + ".");

        // Accept call and use any of the previously registered SSPs for
        // the new call
        LoadRegulationAction action = loadRegulator.addCall(id);

        if (action == LoadRegulationAction.REDIRECT_TRAFFIC) {
            if (log.isDebugEnabled())
                log.debug("The new outbound call resulted in max load reached, " +
                        "the call will be rejected.");
            return CALL_ACTION.REJECT_CALL;
        }

        return CALL_ACTION.ACCEPT_CALL;
    }

    public synchronized void removeCall(String id) {
        if (log.isDebugEnabled())
            log.debug("Removing a call in " + this + ".");

        loadRegulator.removeCall(id);
        int activeCalls = loadRegulator.getCurrentCalls();
        if (activeCalls > 0) {
            if (log.isDebugEnabled())
                log.debug("There are still " + activeCalls + " active calls in " +
                        this + ".");
        } else {
            if (log.isInfoEnabled()) log.info("All calls have been disconnected in " + this +
                                              ". The shutdown is complete and the state is considered Locked.");
            cmController.closeCompleted();
            cmController.setClosedState();
        }
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
        return "Closing unforced rejecting state";
    }
}
