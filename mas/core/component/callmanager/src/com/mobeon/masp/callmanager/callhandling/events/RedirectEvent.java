package com.mobeon.masp.callmanager.callhandling.events;

import com.mobeon.masp.callmanager.InboundCall.RedirectStatusCode;
import com.mobeon.masp.callmanager.RedirectDestination;


public class RedirectEvent extends CallCommandEvent {

    private final RedirectDestination destination;
    private final RedirectStatusCode redirectCode;


    public RedirectEvent(RedirectDestination destination, RedirectStatusCode redirectCode) {
        this.destination = destination;
        this.redirectCode = redirectCode;
    }

    public RedirectDestination getDestination() {
        return destination;
    }

    public RedirectStatusCode getRedirectCode() {
        return redirectCode;
    }

    public String getReason() {
        return redirectCode.getReason();
    }

}
