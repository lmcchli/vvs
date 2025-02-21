/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * Interface for receiving MRCP/RTSP requests.
 * <p>
 * A MessageReceiver which is registered/attached to a ServerMock
 * will be notified each time a request arrives to the handler.
 * @see com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspSession
 * @see com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.RtspMessage
 */
public interface RtspMessageReceiver {
    /**
     * When an MRCP/RTSP request arrives the message receiver is notified
     * through this method.
     *
     * @param message an MRCP/RTSP request message.
     */
    public void receive(RtspMessage message);
}
