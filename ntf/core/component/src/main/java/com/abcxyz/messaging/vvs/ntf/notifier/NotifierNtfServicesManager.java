/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.cancel.CancelSmsPluginDistributor;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfNotificationInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfServicesManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierStateManagement;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.ICancelSmsEventRouter;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.INotifierDatabaseAccess;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.INotifierMfsEventManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.phonestatus.INotifierPhoneOnRouter;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.INotifierEventScheduler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.INotifierMessageGenerator;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.INotifierSenderSms;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierConfigManagerFactory;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLoggerFactory;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierManagementCounter;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierMdrGenerator;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierProfiler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierUtil;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.IntfServiceTypePidLookup;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.out.sms.ServiceTypePidLookupImpl;


public class NotifierNtfServicesManager implements INotifierNtfServicesManager {
    
    private static  NotifierNtfServicesManager instance = null;
    
    private NotifierNtfServicesManager() {        
    }
    
    public static NotifierNtfServicesManager get() {
        if(instance == null) {
            instance = new NotifierNtfServicesManager();
        }
        return instance;
    }


    @Override
    public INotifierNtfNotificationInfo getNtfNotificationInfo(Properties eventProperties) {
        return new NotifierNtfNotificationInfo(eventProperties);
    }

    @Override
    public INotifierConfigManagerFactory getConfigManagerFactory() {
        return NotifierConfigManagerFactory.get();
    }

    @Override
    public INotifierDatabaseAccess getDatabaseAccess() {
        return NotifierDatabaseAccess.get();
    }

    @Override
    public INotifierEventScheduler getEventScheduler() {
        return new NotifierEventScheduler();
    }

    @Override
    public INotifierLoggerFactory getLoggerFactory() {
        return NotifierLoggerFactory.get();
    }

    @Override
    public INotifierManagementCounter getManagementCounter() {
        return NotifierManagementCounter.get();
    }

    @Override
    public INotifierProfiler getProfiler() {
        return NotifierProfiler.get();
    }
    
    @Override
    public INotifierMdrGenerator getMdrGenerator() {
        return NotifierMdrGenerator.get();
    }

    @Override
    public INotifierMessageGenerator getMessageGenerator() {
        return NotifierMessageGenerator.get();
    }

    @Override
    public INotifierMfsEventManager getMfsEventManager() {
        return NotifierMfsEventManager.get();
    }

    @Override
    public INotifierPhoneOnRouter getPhoneOnRouter() {
        return NotifierPhoneOnRouter.get();
    }

    @Override
    public INotifierSenderSms getSender() {
        return NotifierSenderSms.get();
    }

    @Override
    public INotifierUtil getUtil() {
        return NotifierUtil.get();
    }

    @Override
    public INotifierStateManagement getNotifierStateManagement() {
        return ManagementInfo.get();
    }

    @Override
    public ICancelSmsEventRouter getCancelRouter() {
        return CancelSmsPluginDistributor.get();
    }

    @Override
    public IntfServiceTypePidLookup getNtfServiceTypePidLookup() {
        return ServiceTypePidLookupImpl.get();
    }

}
