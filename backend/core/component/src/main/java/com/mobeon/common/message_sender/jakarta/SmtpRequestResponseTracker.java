/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.message_sender.jakarta;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;

/**
 * @author Håkan Stolt
 */
public class SmtpRequestResponseTracker implements ProtocolCommandListener {

    private String latestRequest;
    private String latestResponse;

    /**
     * This method is invoked by a ProtocolCommandEvent source after
     * sending a protocol command to a server.
     * <p/>
     *
     * @param event The ProtocolCommandEvent fired.
     */
    public void protocolCommandSent(ProtocolCommandEvent event) {
        latestRequest = event.getMessage().trim();
    }

    /**
     * This method is invoked by a ProtocolCommandEvent source after
     * receiving a reply from a server.
     * <p/>
     *
     * @param event The ProtocolCommandEvent fired.
     */
    public void protocolReplyReceived(ProtocolCommandEvent event) {
        latestResponse = event.getMessage().trim();
    }

    public String getLatestRequest() {
        return latestRequest;
    }

    public String getLatestResponse() {
        return latestResponse;
    }

    @Override
    public String toString() {
        return "[latestRequest=" + latestRequest + ",latestResponse=" + latestResponse + "]";
    }

}
