/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierIncomingSignalInfo;
import com.abcxyz.services.moip.ntf.event.NtfEvent;


public class NotifierIncomingSignalInfo implements INotifierIncomingSignalInfo {

    private NtfEvent ntfEvent = null;
    
    public NotifierIncomingSignalInfo(NtfEvent ntfEvent) {
        this.ntfEvent = ntfEvent;
    }
    
    @Override
    public String getServiceType() {
        return ntfEvent.getNtfEventType();
    }

    @Override
    public boolean isExpiry() {
        return ntfEvent.isExpiry();
    }

    @Override
    public Properties getNotificationEventProperties() {
        return ntfEvent.getMessageEventProperties();
    }

}
