package com.abcxyz.services.moip.ntf.coremgmt;

import com.abcxyz.services.moip.ntf.event.NtfEvent;

/**
 * call back from NTF message sending for handling events
 */
public interface EventSentListener {


    public enum SendStatus {
        OK,
        PERMANENT_ERROR,
        TEMPORARY_ERROR,
        NO_MORE_RETRY,
        NORMAL_RETRY,
        RETRY_LATER  //case if the retry time needs to be adjusted
    };


    /**
     * report back sending status from message sender
     *
     */
    void sendStatus(NtfEvent event, SendStatus status);
}