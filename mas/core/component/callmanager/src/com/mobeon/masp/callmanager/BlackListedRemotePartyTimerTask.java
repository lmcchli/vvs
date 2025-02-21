/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import java.util.TimerTask;

/**
 * A timer task for a timer controlling how long a remote party is black listed.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class BlackListedRemotePartyTimerTask extends TimerTask {
    private final String remotePartyId;

    public BlackListedRemotePartyTimerTask(String remotePartyId) {
        this.remotePartyId = remotePartyId;
    }

    public void run() {
        CMUtils.getInstance().getRemotePartyController().
                removeBlackListedRemoteParty(remotePartyId);
    }
}
