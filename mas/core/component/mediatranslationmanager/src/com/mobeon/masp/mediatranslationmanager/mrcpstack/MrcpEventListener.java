/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.mrcpstack;

import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.MrcpEventId;

/**
 * The MrcpEventlistener receives and handles incoming MRCP event messages.
 */
public interface MrcpEventListener {
    /**
     * This is the listener notification method.
     * @param event the MRCP event type ID.
     * @param reason the reason for the event.
     */
    public void handleMrcpEvent(MrcpEventId event, String reason);
}
