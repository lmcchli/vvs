/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.eventnotifier.Event;

/**
 * This event indicates that a recognition, by some reason, has failed.
 */
public class RecognitionFailedEvent implements Event {
    private String reason;

    /**
     * The constructor.
     * @param reason the reason why recognize failed.
     */
    public RecognitionFailedEvent(String reason) {
        this.reason = reason;
    }

    /**
     * Getter for the reason of recognition failure.
     * @return the reason of failure.
     */
    public String getReason() {
        return reason;
    }
}
