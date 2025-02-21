/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * This is an RTSP request message for setting up an MRCP/RTSP session.
 */
public class SetupRequest extends RtspRequest {
    /**
     * The constructor.
     * Intializes an RTSP session by defining the kind of requested service
     * and a requested service port.
     * @param isRecognition true => recognize (ASR), false => syntesize (TTS).
     * @param rtpPort client port (RTP==rtpPort, RTCP=rtpPort+1).
     */
    public SetupRequest(boolean isRecognition, int rtpPort) {
        super("SETUP", isRecognition);
        int rtcpPort = rtpPort+1;
        if (isRecognition) {
            // TODO: is there a workaound for this recognize does not need client_port? 
            rtpPort = 5554;
            rtcpPort = rtpPort+1;
            // TODO: it works but is ugly
            setHeaderField("Transport", "RTP/AVP;client_port=" + rtpPort + "-" + rtcpPort);
        } else {
            setHeaderField("Transport", "RTP/AVP;client_port=" + rtpPort + "-" + rtcpPort);
        }
    }
}
