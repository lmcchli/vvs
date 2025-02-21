package com.mobeon.masp.execution_engine.runtime.wrapper;

import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.operateandmaintainmanager.CallState;
import com.mobeon.masp.operateandmaintainmanager.CallType;

/**
 * @author David Looberger
 */
public class SessionInfoHelper {
    private static final ILogger log = ILoggerFactory.getILogger(SessionInfoHelper.class);

    public static CallState getCallState(Connection.State state) {
        if (state == Connection.State.ALERTING) {
            return CallState.ALERTING;
        } else if (state == Connection.State.CONNECTED) {
            return CallState.CONNECTED;
        }  else if (state == Connection.State.DISCONNECTED) {
            return CallState.DISCONNECTED;
        }  else if (state == Connection.State.FAILED) {
            return CallState.FAILED;
        }  else if (state == Connection.State.PROGRESSING) {
            return CallState.PROGRESSING;
        }  else if (state == Connection.State.RECORDING_CONNECTED) {
            return CallState.RECORD;
        }  else if (state == Connection.State.RECORDING_ALERTING) {
            return CallState.RECORD;
        }  else if (state == Connection.State.ERROR) {
            return CallState.ERROR;
        }  else {
            // TODO: What should the "default/initial" state be?
            if (log.isDebugEnabled()) log.debug("Unknown state: " + state);
            return null;
        }
    }

    /**
     * Return an ID to be used in monitor (OM manager). We used to use the session ID but in order to save
     * some characters in the monitor GUI we use the session ID without prefix.
     * @return the ID to be used in monitor
     */
    public static String getMonitorId(ExecutionContext ex){
        return ex.getSession().getUnprefixedId();
    }

    public static CallType getCallType(CallProperties.CallType callType) {
        if (callType == CallProperties.CallType.VOICE) {
            return CallType.VOICE;
        } else if (callType == CallProperties.CallType.VIDEO) {
            return CallType.VIDEO;
        } else if (callType == CallProperties.CallType.UNKNOWN) {
            return CallType.VOICE_VIDEO_UNKNOWN;
        }
        return CallType.VOICE_VIDEO_UNKNOWN;
    }
}
