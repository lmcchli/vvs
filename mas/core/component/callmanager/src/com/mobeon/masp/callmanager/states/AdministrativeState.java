package com.mobeon.masp.callmanager.states;

import com.mobeon.masp.callmanager.events.CloseForcedEvent;
import com.mobeon.masp.callmanager.events.CloseUnforcedEvent;
import com.mobeon.masp.callmanager.events.OpenEvent;
import com.mobeon.masp.callmanager.events.UpdateThresholdEvent;

/**
 * This class is an interface for an Administrative state such as Opened,
 * Closed, ClosingForced and ClosingUnforced states.
 *
 * @author Malin Flodin
 */
public interface AdministrativeState {

    public static enum CALL_ACTION {
        ACCEPT_CALL, REJECT_CALL, REDIRECT_CALL
    }

    public void closeForced(CloseForcedEvent closeForcedEvent);
    public void closeUnforced(CloseUnforcedEvent closeUnforcedEvent);
    public void open(OpenEvent openEvent);
    public CALL_ACTION addInboundCall(String id);
    public CALL_ACTION checkCallAction();
    public CALL_ACTION addOutboundCall(String id);
    public void removeCall(String id);
    public void updateThreshold(UpdateThresholdEvent thresholdEvent);
}
