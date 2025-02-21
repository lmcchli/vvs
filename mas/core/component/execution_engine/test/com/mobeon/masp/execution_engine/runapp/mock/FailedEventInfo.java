package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.callmanager.events.FailedEvent;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 4, 2006
 * Time: 3:00:08 PM
 * To change this template use File | Settings | File Templates.
 *
 *
 * this class contains some info that can be used when creating a FailedEvent.
 *
 */
public class FailedEventInfo {

    private FailedEvent.Reason reason;
    private CallDirection direction;
    private String message;
    private int networkStatusCode;

    public FailedEventInfo(FailedEvent.Reason reason,
                           CallDirection direction,
                           String message,
                           int networkStatusCode){
        this.reason = reason;
        this.direction = direction;
        this.message = message;
        this.networkStatusCode = networkStatusCode;
    }

    public FailedEvent.Reason getReason() {
        return reason;
    }

    public CallDirection getDirection() {
        return direction;
    }

    public String getMessage() {
        return message;
    }

    public int getNetworkStatusCode() {
        return networkStatusCode;
    }
}
