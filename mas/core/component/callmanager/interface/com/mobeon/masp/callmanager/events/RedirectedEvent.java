package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.InboundCall.RedirectStatusCode;


/**
 * Event fired by Call Manager when an inbound call has sucessfully been redirected
 * @author lmcraby
 *
 */
public class RedirectedEvent implements Event {
    
    private final Call call;
    private final RedirectStatusCode reason;
    
    public RedirectedEvent(Call call, RedirectStatusCode reason) {
        this.call = call;
        this.reason = reason;
    }

    public Call getCall() {
        return call;
    }

    public RedirectStatusCode getReason() {
        return reason;
    }

}
