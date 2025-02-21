package com.mobeon.masp.servicerequestmanager.states;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManagerController;

/**
 * @author mmawi
 */
public class ClosingUnforcedState implements AdministrativeState {
    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private IServiceRequestManagerController srmController;

    public ClosingUnforcedState(IServiceRequestManagerController srmController) {
        this.srmController = srmController;
    }

    public void closeForced() {
        if (log.isInfoEnabled())
            log.info("Close (forced) received in " + this + ".");
        srmController.setClosingForcedState();
    }

    public void closeUnforced() {
        if (log.isInfoEnabled())
            log.info("Close (unforced) received in " + this + ", it is ignored.");
    }

    public void open() {
        if (log.isInfoEnabled())
            log.info("Open received in " + this + ".");
        srmController.setOpenedState();
        srmController.openCompleted();
    }

    public void removeSession() {
        if (log.isInfoEnabled())
            log.info("Removed session received in " + this + ".");

        if (srmController.getCurrentSessions() < 1) {
            srmController.setClosedState();
            srmController.closeCompleted();
        }
    }

    public String toString() {
        return "Closing (unforced) state";
    }
}
