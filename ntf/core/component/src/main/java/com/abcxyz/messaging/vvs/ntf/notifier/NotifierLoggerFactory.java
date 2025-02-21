/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLoggerFactory;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;


public class NotifierLoggerFactory implements INotifierLoggerFactory {
    private static NotifierLoggerFactory instance = null;
    
    private NotifierLoggerFactory() {        
    }
    
    public static NotifierLoggerFactory get() {
        if(instance == null) {
            instance = new NotifierLoggerFactory();
        }
        return instance;
    }
    
    @Override
    public INotifierLogger getLogger(Class<?> theClass) {
        LogAgent logAgent = NtfCmnLogger.getLogAgent(theClass);
        return new NotifierLogger(logAgent);
    }

    @Override
    public INotifierLogger getLogger(String className) {
        LogAgent logAgent = NtfCmnLogger.getLogAgent(className);
        return new NotifierLogger(logAgent);
    }
    
}
