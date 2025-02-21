/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierManagementCounter;
import com.mobeon.ntf.management.ManagementCounter;
import com.mobeon.ntf.management.ManagementInfo;


public class NotifierManagementCounter implements INotifierManagementCounter {

    private static NotifierManagementCounter instance = null;

    private NotifierManagementCounter() {
    }
    
    public static NotifierManagementCounter get() {
        if(instance == null) {
            instance = new NotifierManagementCounter();
        }
        return instance;
    }

    @Override
    public void incrementSuccessCounter(String counterServiceName) {
        ManagementInfo.get().getCounter(counterServiceName, ManagementCounter.CounterType.SUCCESS).incr();        
    }

    @Override
    public void incrementFailCounter(String counterServiceName) {
        ManagementInfo.get().getCounter(counterServiceName, ManagementCounter.CounterType.FAIL).incr();        
    }

}
