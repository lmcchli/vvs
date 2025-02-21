package com.abcxyz.messaging.vvs.ntf.notifier;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.phonestatus.ANotifierPhoneOnReceiver;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.event.PhoneOnEvent;
import com.mobeon.ntf.event.PhoneOnEventListener;


/**
 * Wrapper class to hide the internal NTF interface PhoneOnEventListener class from the plug-in.
 */
public class NotifierPhoneOnListener implements PhoneOnEventListener {   

    private static LogAgent log = NtfCmnLogger.getLogAgent(NotifierPhoneOnListener.class);
    private ANotifierPhoneOnReceiver notifierPhoneOnReceiver = null;

    
    public NotifierPhoneOnListener(ANotifierPhoneOnReceiver phoneOnReceiver) {
        notifierPhoneOnReceiver = phoneOnReceiver;
    }
    
    @Override
    public void phoneOn(PhoneOnEvent phoneOnEvent) {
        log.debug("Received phone on event: " + phoneOnEvent.toString());
        notifierPhoneOnReceiver.phoneOn(new NotifierPhoneOnEvent(phoneOnEvent));            
    }
}
