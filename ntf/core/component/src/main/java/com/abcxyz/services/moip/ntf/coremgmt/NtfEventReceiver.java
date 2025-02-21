package com.abcxyz.services.moip.ntf.coremgmt;

import com.abcxyz.services.moip.ntf.event.NtfEvent;

/**
 * event receiver for catching event from scheduler or remote MRD
 *
 */
public interface NtfEventReceiver {

    public void sendEvent(NtfEvent event);
}