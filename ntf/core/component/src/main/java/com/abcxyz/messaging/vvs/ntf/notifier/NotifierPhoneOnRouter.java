/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.phonestatus.ANotifierPhoneOnReceiver;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.phonestatus.INotifierPhoneOnRouter;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.event.EventRouter;


public class NotifierPhoneOnRouter implements INotifierPhoneOnRouter {

    private static LogAgent log = NtfCmnLogger.getLogAgent(NotifierPhoneOnRouter.class);    
    private static NotifierPhoneOnRouter instance = null;
    
    private NotifierPhoneOnRouter(){
    }
    
    public static NotifierPhoneOnRouter get() {
        if(instance == null) {
            instance = new NotifierPhoneOnRouter();
        }
        return instance;
    }
    
    @Override
    public void register(ANotifierPhoneOnReceiver phoneOnReceiver) {
        log.debug("Registering INotifierPhoneOnReceiver: " + phoneOnReceiver.toString());
        EventRouter.get().register(new NotifierPhoneOnListener(phoneOnReceiver));        
    }
      
}
