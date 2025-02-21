package com.mobeon.masp.servicerequestmanager.states;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.servicerequestmanager.ServiceRequestManagerController;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManagerController;

/**
 * @author mmawi
 */
public class ClosedState implements AdministrativeState {
    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private IServiceRequestManagerController srmController;

    public ClosedState(IServiceRequestManagerController srmController) {
        this.srmController = srmController;
    }

    public void closeForced() {
        if (log.isInfoEnabled())
            log.info("Close (forced) received in " + this + ", close is considered competed.");
        srmController.closeCompleted();
    }

    public void closeUnforced() {
        if (log.isInfoEnabled())
            log.info("Close (unforced) received in " + this + ", close is considered completed.");
        srmController.closeCompleted();
    }

    public void open() {
        if (log.isInfoEnabled())
            log.info("Open received in " + this + ".");
        srmController.setOpenedState();
        srmController.openCompleted();
    }

    public void removeSession() {
        if (log.isInfoEnabled())
            log.info("Remove session should not happen in " + this +
                    ", it is ignored since it will not affect this state.");
    }

    public String toString() {
        return "Closed state";
    }
}
