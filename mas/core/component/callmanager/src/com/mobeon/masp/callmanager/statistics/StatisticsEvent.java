package com.mobeon.masp.callmanager.statistics;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.operateandmaintainmanager.CallType;
import com.mobeon.masp.operateandmaintainmanager.CallDirection;

/**
 * @author mmawi
 */
public class StatisticsEvent implements Event {
    private CallType callType;
    private CallDirection callDirection;

    public StatisticsEvent(CallType callType, CallDirection callDirection) {
        this.callType = callType;
        this.callDirection = callDirection;
    }

    public CallType getCallType() {
        return callType;
    }

    public CallDirection getCallDirection() {
        return callDirection;
    }

    public String toString() {
        return "StatisticsEvent: <CallType = " + callType +
                ">, <Direction = " + callDirection + ">";
    }
}
