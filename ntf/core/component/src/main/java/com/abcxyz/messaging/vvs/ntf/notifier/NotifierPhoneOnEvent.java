package com.abcxyz.messaging.vvs.ntf.notifier;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.phonestatus.INotifierPhoneOnEvent;
import com.mobeon.ntf.event.PhoneOnEvent;


/**
 * Wrapper class to hide the implementation of the PhoneOnEvent class from the plug-in.
 */
public class NotifierPhoneOnEvent implements INotifierPhoneOnEvent {
    PhoneOnEvent phoneOnEvent = null;
    
    public NotifierPhoneOnEvent(PhoneOnEvent phoneOnEvent) {
        this.phoneOnEvent = phoneOnEvent;
    }
    
    @Override
    public String getPhoneNumber() {
        return phoneOnEvent.getAddress();
    }

    @Override
    public NotifierPhoneOnResult getResult() {
        NotifierPhoneOnResult notifierPhoneOnStatus = null;
        switch(phoneOnEvent.getResult()) {
            case PhoneOnEvent.PHONEON_OK:
                notifierPhoneOnStatus = NotifierPhoneOnResult.OK;
                break;
            case PhoneOnEvent.PHONEON_FAILED:
                notifierPhoneOnStatus = NotifierPhoneOnResult.FAILED;
                break;
            case PhoneOnEvent.PHONEON_FAILED_TEMPORARY:
                notifierPhoneOnStatus = NotifierPhoneOnResult.FAILED_TEMPORARY;
                break;
            case PhoneOnEvent.PHONEON_BUSY:
                notifierPhoneOnStatus = NotifierPhoneOnResult.BUSY;
                break;
            default:
                notifierPhoneOnStatus = NotifierPhoneOnResult.FAILED_TEMPORARY;
        }
        return notifierPhoneOnStatus;
    }
    
    public String toString() {
        return "{NotifierPhoneOnResult: " + getPhoneNumber() + " " + getResult().toString() + "}";
    }
    
}
