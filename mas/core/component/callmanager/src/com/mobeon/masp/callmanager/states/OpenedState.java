/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.states;

import com.mobeon.masp.callmanager.loadregulation.LoadRegulator;
import com.mobeon.masp.callmanager.loadregulation.LoadRegulationAction;
import com.mobeon.masp.callmanager.CallManagerController;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.events.CloseForcedEvent;
import com.mobeon.masp.callmanager.events.OpenEvent;
import com.mobeon.masp.callmanager.events.CloseUnforcedEvent;
import com.mobeon.masp.callmanager.events.UpdateThresholdEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * The administrative state Opened.
 *
 * @author Malin Flodin
 */
public class OpenedState implements AdministrativeState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final CallManagerController cmController;
    private final LoadRegulator loadRegulator;

    public OpenedState(CallManagerController cmController,
                       LoadRegulator loadRegulator) {
        this.cmController = cmController;
        this.loadRegulator = loadRegulator;
    }

    public synchronized void closeForced(CloseForcedEvent closeForcedEvent) {
        if (log.isInfoEnabled())
            log.info("Close request (forced) received in " + this + ". Unregistration " +
                    "towards all SSPs is initiated. All calls are instructed to " +
                    "disconnect.");

        cmController.unregisterAllSsps();

        if (loadRegulator.getCurrentCalls() > 0) {
            cmController.lockAllCalls(closeForcedEvent);
            cmController.setClosingForcedState();
        } else {
            cmController.closeCompleted();
            cmController.setClosedState();
        }
    }


    public synchronized void closeUnforced(CloseUnforcedEvent closeUnforcedEvent) {
       if (log.isInfoEnabled())
          log.info("Close request (unforced) received in " + this +
                        ". Unregistration towards all SSPs is initiated. " +
                        "Waiting for calls to finish before considered locked.");

       cmController.unregisterAllSsps();
       cmController.setClosingUnforcedRejectingState();


       if (loadRegulator.getCurrentCalls() == 0) {
                if (log.isInfoEnabled())
                   log.info("All calls have been disconnected in " + this +  ". The state is considered Closed.");
                cmController.closeCompleted();
                cmController.setClosedState();
        }
    }


    /*public synchronized void closeUnforced(CloseUnforcedEvent closeUnforcedEvent) {
        if (log.isInfoEnabled())
            log.info("Close request (unforced) received in " + this +
                    ". Unregistration towards all SSPs is initiated. " +
                    "Waiting for calls to finish before considered locked.");

        cmController.unregisterAllSsps();
        cmController.setClosingUnforcedRejectingState();
    } */


    public synchronized void open(OpenEvent openEvent) {
        if (log.isInfoEnabled())
            log.info("Open request received in " + this +
                    ". Open is considered completed due to the current state.");
        cmController.openCompleted();
    }

    public synchronized CALL_ACTION addInboundCall(String id) {
        if (log.isDebugEnabled())
            log.debug("Adding a new inbound call in " + this + ".");

        LoadRegulationAction action = loadRegulator.addCall(id);

        if (action == LoadRegulationAction.STOP_TRAFFIC) {
            if (log.isInfoEnabled())
                log.info("The new inbound call resulted in high water mark reached. " +
                        "Unregistration towards all SSPs is initiated.");
            cmController.unregisterAllSsps();
        } else if (action == LoadRegulationAction.REDIRECT_TRAFFIC) {
            if (log.isInfoEnabled())
                log.info("The max amount of calls is reached. Call is redirected.");
            return CALL_ACTION.REDIRECT_CALL;
        }

        return CALL_ACTION.ACCEPT_CALL;
    }

    public synchronized CALL_ACTION checkCallAction() {
        LoadRegulationAction action = loadRegulator.checkCallAction();
        
        if (action == LoadRegulationAction.REDIRECT_TRAFFIC) {
            if (log.isDebugEnabled())
                log.debug("The max amount of calls is reached, a new call would be redirected.");
            return CALL_ACTION.REDIRECT_CALL;
        }

        if (log.isDebugEnabled())
            log.debug("A new inbound call would be accepted in " + this + ".");
        return CALL_ACTION.ACCEPT_CALL;
    }

    public synchronized CALL_ACTION addOutboundCall(String id) {
        if (log.isDebugEnabled())
            log.debug("Adding a new outbound call in " + this + ".");

        LoadRegulationAction action = loadRegulator.addCall(id);
        if (action == LoadRegulationAction.START_TRAFFIC) {
            if (log.isDebugEnabled())
                log.debug("Registration towards all SSPs is initiated in " +
                        this + ".");
            cmController.registerAllSsps();
        } else if (action == LoadRegulationAction.STOP_TRAFFIC) {
            if (log.isInfoEnabled())
                log.info("The new outbound call resulted in high water mark reached. " +
                        "Unregistration towards all SSPs is initiated.");
            cmController.unregisterAllSsps();
        } else if (action == LoadRegulationAction.REDIRECT_TRAFFIC) {
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
        LoadRegulationAction action = loadRegulator.removeCall(id);
        if (action == LoadRegulationAction.START_TRAFFIC) {
            if (log.isInfoEnabled())
                log.info("Removing the call resulted in low water mark reached. " +
                        "Registration towards all SSPs is initiated.");
            cmController.registerAllSsps();
        }
    }

    public synchronized void updateThreshold(UpdateThresholdEvent thresholdEvent) {
        if (log.isDebugEnabled())
            log.debug("Updating threshold in " + this + ": " + thresholdEvent);

        LoadRegulationAction action =
                loadRegulator.updateThreshold(
                        thresholdEvent.getHighWaterMark(),
                        thresholdEvent.getLowWaterMark(),
                        thresholdEvent.getThreshold());

        if (action == LoadRegulationAction.START_TRAFFIC) {
            if (log.isInfoEnabled())
                log.info("Updating threshold causes low water mark to be reached. " +
                        "Registration towards all SSPs is initiated.");
            cmController.registerAllSsps();
        } else if (action == LoadRegulationAction.STOP_TRAFFIC) {
            log.info("Updating threshold causes high water mark to be reached. " +
                    "Unregistration towards all SSPs is initiated.");
            cmController.unregisterAllSsps();
        }

        CMUtils.getInstance().getServiceEnablerInfo().
                setMaxConnections(loadRegulator.getConfiguredMax());
    }

    public String toString() {
        return "Opened state";
    }
}
