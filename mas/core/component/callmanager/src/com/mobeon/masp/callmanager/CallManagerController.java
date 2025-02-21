/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import com.mobeon.masp.callmanager.events.CloseForcedEvent;

/**
 * Interface towards a Call Manager controller. This interface is used from the
 * administrative states when accessing the Call Manager controller.
 *
 * @author Malin Flodin
 */
public interface CallManagerController {

    public void setClosedState();
    public void setClosingForcedState();
    public void setClosingUnforcedRejectingState();
    public void setOpenedState();

    public void lockAllCalls(CloseForcedEvent closeForcedEvent);
    public void closeCompleted();
    public void openCompleted();

    public void registerAllSsps();
    public void unregisterAllSsps();
}
