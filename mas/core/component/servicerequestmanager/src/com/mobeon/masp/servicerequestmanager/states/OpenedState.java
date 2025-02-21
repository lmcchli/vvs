package com.mobeon.masp.servicerequestmanager.states;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManagerController;

/**
 * @author mmawi
 */
public class OpenedState implements AdministrativeState {
    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private IServiceRequestManagerController srmController;

    public OpenedState(IServiceRequestManagerController srmController) {
        this.srmController = srmController;
    }

    public void closeForced() {
        if (log.isInfoEnabled())
            log.info("Close (forced) received in " + this + ".");
        if (srmController.getCurrentSessions() < 1) {
            srmController.setClosedState();
            srmController.closeCompleted();
        } else {
            srmController.setClosingForcedState();
        }
    }

    public void closeUnforced() {
        if (log.isInfoEnabled())
            log.info("Close (unforced) received in " + this + ".");
        if (srmController.getCurrentSessions() < 1) {
            srmController.setClosedState();
            srmController.closeCompleted();
        } else {
            srmController.setClosingUnforcedState();
        }
    }

    public void open() {
        if (log.isInfoEnabled())
            log.info("Open received in " + this + ", open is considered completed.");
        srmController.openCompleted();
    }

    public void removeSession() {
        if (log.isInfoEnabled())
            log.info("Remove session received in " + this +
                    ", it is ignored since it will not affect this state.");
    }

    public String toString() {
        return "Opened state";
    }
}
