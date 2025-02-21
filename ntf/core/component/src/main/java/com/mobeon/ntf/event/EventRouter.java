/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.event;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import java.util.*;

/**
 * EventRouter distributes phone on events from the SMSC to the subscribers.
 */
public class EventRouter implements PhoneOnEventListener {

    HashMap<String, PhoneOnEventListener> phoneOnEventListener = new HashMap<String, PhoneOnEventListener>();
    LogAgent log = NtfCmnLogger.getLogAgent(EventRouter.class);
    private static EventRouter s_inst = null;

    static {
        s_inst = new EventRouter();
    }

    public static EventRouter get() {
        return s_inst;
    }

    public EventRouter() {
    }

    public synchronized void register(PhoneOnEventListener listener) {
        phoneOnEventListener.put(listener.toString(), listener);
        log.debug("EventRouter " + listener + " registered, total " + phoneOnEventListener.size());
    }

    public void phoneOn(PhoneOnEvent e) {
        Iterator<Map.Entry<String,PhoneOnEventListener>> it = phoneOnEventListener.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,PhoneOnEventListener> mapEntry = it.next();
            log.debug("EventRouter distributing phoneOn event to " + mapEntry.getKey());
            PhoneOnEventListener p = mapEntry.getValue();
            p.phoneOn(e);
        }
    }
}
