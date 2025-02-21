/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.wrapper;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.IOutboundMediaStream.PlayOption;
import com.mobeon.masp.stream.RecordingProperties;

public class Call {
    private Connection connection;
    private static ILogger log = ILoggerFactory.getILogger(Call.class);
    private boolean isPlaying = false;

    // this flag is set to false when we are playing and should be set
    // by the outside to true when a play event is received.
    private boolean hasReceivedPlayEvent = true;
    private boolean isRecording = false;
    private ExecutionContext context;

    public void play(IMediaObject[] obj, long offset) {
        Object callId = connection.getBridgePartyId();
        if (log.isDebugEnabled()) log.debug("Starting play using callId " + callId);

        setIsPlaying(true);
        setHasReceivedPlayEvent(false);
        connection.play();
        // TODO: can we be sure there is a stream at this moment?
        connection.getCall().play(callId, obj, PlayOption.WAIT_FOR_AUDIO, offset);
    }

    public void play(IMediaObject obj, long offset) {
        Object callId = connection.getBridgePartyId();
        if (log.isDebugEnabled()) log.debug("Starting play using callId " + callId);

        setIsPlaying(true);
        setHasReceivedPlayEvent(false);
        connection.play();
        // TODO: can we be sure there is a stream at this moment?
        connection.getCall().play(callId, obj, PlayOption.WAIT_FOR_AUDIO, offset);

    }

    /**
     * Records a MediaObject.
     *
     * @param obj the MediaObject to record.
     * @return false if fatal error, otherwise true
     */
    public boolean record(IMediaObject obj, RecordingProperties properties) {

        // The callId to supply is according to Malin Flodin
        // going to change name to like "request id" and shall be used
        // to understand what record that has finished, if there are several
        // simultaneous records. But not needed for now.
        Object callId = connection.getBridgePartyId();
        try {
            setIsRecording(true);
            if (log.isDebugEnabled()) log.debug("Staring record using callId " + callId);

            connection.getCall().record(callId, obj, properties);
            // stream.record(callId, obj, properties);
        } catch (IllegalStateException e) {
            setIsRecording(false);
            return false;
        } catch (IllegalArgumentException e) {
            setIsRecording(false);
            return false;
        } catch (UnsupportedOperationException e) {
            setIsRecording(false);
            return false;
        }
        return true;
    }

    public boolean stopRecord() {
        setIsRecording(false);

        Object callId = connection.getBridgePartyId();
        if (log.isDebugEnabled()) log.debug("Stopping record using callId " + callId);

        connection.getCall().stopRecord(callId);

        return true;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public boolean stopPlay() {
        setIsPlaying(false);
        Object callId = connection.getBridgePartyId();
        if (log.isDebugEnabled()) log.debug("Stopping play using callId " + callId);
        setIsPlaying(false);
        connection.getCall().stopPlay(callId);
        return true;

    }

    public void play(String text, long offset) {
        // TODO: Not implemented. Is this ever used??
    }

    public void setIsPlaying(boolean value) {

        /*
        SessionInfo sessionInstance = getSessionInstance();
        if(sessionInstance != null){
            try {
                if (value) {
                    sessionInstance.setOutboundActivity(CallActivity.PLAY);
                } else {
                    sessionInstance.setOutboundActivity(CallActivity.IDLE);
                }
        }
            catch (IlegalSessionInstanceException e) {}
        }
        */
        isPlaying = value;
    }

    /*
    private SessionInfo getSessionInstance() {
        SessionInfo sessionInstance = null;
        SessionInfoFactory sessionInfoFactory = context.getSessionInfoFactory();
        if(sessionInfoFactory == null) {
            if (log.isDebugEnabled()) log.debug("sessionInfoFactory was null");
        } else {
            sessionInstance = sessionInfoFactory.getSessionInstance(SessionInfoHelper.getMonitorId(context));
            if(sessionInstance == null) {
                if (log.isDebugEnabled()) log.debug("sessionInstance was null");
            }
        }
        return sessionInstance;
    }
    */

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void setIsRecording(boolean value) {
        /*
        SessionInfo sessionInstance = getSessionInstance();
        if(sessionInstance != null){
            try {
                if (value) {
                    sessionInstance.setInboundActivity(CallActivity.RECORD);
                } else {
                    sessionInstance.setInboundActivity(CallActivity.IDLE);
                }
            }
            catch (IlegalSessionInstanceException e) { }
        }
        */
        isRecording = value;
    }

    public boolean getIsRecording() {
        return isRecording;
    }

    public void setContext(ExecutionContext ex) {
        context = ex;
    }

    public IInboundMediaStream getInboundStream() {
        return connection.getCall().getInboundStream();
    }

    public boolean hasReceivedPlayEvent() {
        return hasReceivedPlayEvent;
    }

    public void setHasReceivedPlayEvent(boolean hasReceivedPlayEvent) {
        this.hasReceivedPlayEvent = hasReceivedPlayEvent;
    }
}
