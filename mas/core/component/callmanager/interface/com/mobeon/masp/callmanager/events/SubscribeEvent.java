package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.callmanager.Call;

public class SubscribeEvent implements Event{
	
    private final Call call;

    public SubscribeEvent(Call call) {
        this.call = call;
    }

    public Call getCall() {
        return call;
    }

    public String toString() {
        return "SubscribeEvent <Call=" + call + ">";
    }

}
