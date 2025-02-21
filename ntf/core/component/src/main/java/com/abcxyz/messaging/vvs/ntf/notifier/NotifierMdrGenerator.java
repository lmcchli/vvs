/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierMdrGenerator;
import com.mobeon.ntf.meragent.MerAgent;


public class NotifierMdrGenerator implements INotifierMdrGenerator {
    private static NotifierMdrGenerator instance = null;
    
    private MerAgent merAgent = null;
    
    private NotifierMdrGenerator() {
        merAgent = MerAgent.get();
    }
    
    public static NotifierMdrGenerator get() {
        if(instance == null) {
            instance = new NotifierMdrGenerator();
        }
        return instance;
    }

    @Override
    public void generateMdrDelivered(String receiver, int portType, String reasonDetail) {
        merAgent.generateMdrDelivered(receiver, portType, reasonDetail);
    }

    @Override
    public void generateMdrExpired(String receiver, int portType, String reasonDetail) {
        merAgent.generateMdrExpired(receiver, portType, reasonDetail);
    };

    @Override
    public void generateMdrFailed(String receiver, int portType, String reasonDetail, String message) {
        merAgent.generateMdrFailed(receiver, portType, reasonDetail, message);
    }

    @Override
    public void generateMdrDiscarded(String receiver, int portType, String reasonDetail, String message) {
        merAgent.generateMdrDiscarded(receiver, portType, reasonDetail, message);
    }
    
    @Override
    public void generateMdrPhoneOnDelivered(String receiver) {
        merAgent.generateMdrPhoneOnDelivered(receiver);

    }
    

}
