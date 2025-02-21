/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.servicerequestmanager;

import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerOperate;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerInfo;
import com.mobeon.masp.operateandmaintainmanager.CallDirection;
import com.mobeon.masp.operateandmaintainmanager.CallResult;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.servicerequestmanager.states.AdministrativeState;

/**
 * Interface for a <code>ServiceEnablerOperate</code> for a
 * {@link ServiceRequestManager}.
 * <p/>
 * The controller is responsible for counting the number of concurrent XMP
 * requests handled by the <code>ServiceRequestManager</code>.
 *
 * @author mmawi
 */
public interface IServiceRequestManagerController extends ServiceEnablerOperate {
    void setPort(int port);

    void setHost(String host);

    void setServiceEnablerInfo(ServiceEnablerInfo serviceEnablerInfo);

    void setEventDispatcher(IEventDispatcher eventDispatcher);

    /**
     * Try to increase the number of current sessions.
     *
     * @return <code>false</code> if the threshold is reached,
     * <code>true</code> if it was OK to add the session.
     */
    public boolean addSession();

    /**
     * Decrease the number of current sessions.
     */
    public void removeSession();

    public int getCurrentSessions();

    public void updateStatistics(CallResult callResult, CallDirection callDirection);

    public void setOpenedState();
    public void setClosingUnforcedState();
    public void setClosingForcedState();
    public void setClosedState();
    public void openCompleted();
    public void closeCompleted();

    /**
     * @return the current administrative state.
     */
    public AdministrativeState getCurrentState();

    /**
     * Reset the controller.
     * Use only in basic test!
     */
    public void clear();
}
