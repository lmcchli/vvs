/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms;

import java.io.File;
import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.ANotifierPlugin;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierIncomingSignalInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfNotificationInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfServicesManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierStateManagement;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse.NotifierHandlingActions;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse.NotifierHandlingTypes;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierPluginException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.ICancelSmsEventRouter;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.INotifierDatabaseAccess;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.INotifierMfsEventManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.INotifierEventScheduler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.INotifierMessageGenerator;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.INotifierSenderSms;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierConfigManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierConfigManagerFactory;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLoggerFactory;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierManagementCounter;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierMdrGenerator;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierProfiler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierUtil;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.IntfServiceTypePidLookup;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.cancel.CancelHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule.NotifierEventHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util.NotifierConfig;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util.NotifierConfigConstants;

/**
 * 
 * This class is the entry point into the templateSms plug-in:<BR>
 * - initialise and refresh the INotifierConfigManager object for its configuration file, templateSmsPlugin.conf<BR>
 * - gain access to the NTF services from the INotifierNtfServicesManager<BR>
 * 
 * It also delegates the decision of handling a incoming notification signal to another class, the NotifierHandler.
 */
public class TemplateSmsPlugin extends ANotifierPlugin {
    
    private static INotifierNtfServicesManager notifierNtfServicesManager = null;
    private static INotifierConfigManagerFactory notifierConfigManagerFactory = null;
    private static INotifierDatabaseAccess notifierDatabaseAccess = null;
    private static INotifierEventScheduler notifierEventScheduler = null;
    private static INotifierLoggerFactory notifierLoggerFactory = null;
    private static INotifierManagementCounter notifierManagementCounter = null;
    private static INotifierProfiler notifierProfiler = null;
    private static INotifierMdrGenerator notifierMdrGenerator = null;
    private static INotifierMessageGenerator notifierMessageGenerator = null;
    private static INotifierMfsEventManager notifierMfsEventManager = null;
    private static INotifierSenderSms notifierSenderSms = null;
    private static INotifierUtil notifierUtil = null;

    private static INotifierLogger log = null;
    private static INotifierConfigManager notifierPluginConfigManager = null;
    private static INotifierConfigManager ntfConfigManager = null;
	private static INotifierStateManagement notifierServicesManager = null;
	private static ICancelSmsEventRouter cancelSmsEventRouter = null;
	private static IntfServiceTypePidLookup ntfServiceTypePidLookup = null;
    

	private boolean isInitialized = false;
	

    @Override
    public void initialize(INotifierNtfServicesManager notifierNtfServicesMgr) throws NotifierPluginException {
        String initializeFailedErrorMsg = null;
        
        if(notifierNtfServicesMgr != null) {
            notifierNtfServicesManager = notifierNtfServicesMgr;
            notifierConfigManagerFactory = notifierNtfServicesManager.getConfigManagerFactory();
            notifierDatabaseAccess = notifierNtfServicesManager.getDatabaseAccess();
            notifierEventScheduler = notifierNtfServicesManager.getEventScheduler();
            notifierLoggerFactory = notifierNtfServicesManager.getLoggerFactory();
            notifierManagementCounter = notifierNtfServicesManager.getManagementCounter();
            notifierProfiler = notifierNtfServicesManager.getProfiler();
            notifierMdrGenerator = notifierNtfServicesManager.getMdrGenerator();
            notifierMessageGenerator = notifierNtfServicesManager.getMessageGenerator();
            notifierMfsEventManager = notifierNtfServicesManager.getMfsEventManager();
            notifierSenderSms = notifierNtfServicesManager.getSender();
            notifierUtil = notifierNtfServicesManager.getUtil();
            notifierServicesManager = notifierNtfServicesManager.getNotifierStateManagement();
            cancelSmsEventRouter = notifierNtfServicesManager.getCancelRouter();
            ntfServiceTypePidLookup = notifierNtfServicesManager.getNtfServiceTypePidLookup();

            if(notifierConfigManagerFactory != null &&
                    notifierDatabaseAccess != null &&
                    notifierEventScheduler != null &&
                    notifierLoggerFactory != null &&
                    notifierManagementCounter != null &&
                    notifierProfiler != null &&
                    notifierMdrGenerator != null &&
                    notifierMessageGenerator != null &&
                    notifierMfsEventManager != null &&
                    notifierSenderSms != null &&
                    notifierUtil != null) {

                if(log == null) {
                    log = notifierLoggerFactory.getLogger(TemplateSmsPlugin.class);
                }

                //Initialise plug-in.
                if(refreshConfig()) {
                    NotifierHandler.get();
                    NotifierEventHandler.get();
                    isInitialized = true;
                    log.debug("Notifier plugin initialized.");
                } else {
                    initializeFailedErrorMsg = "Template SMS plug-in not initialized due to bad configuration.";
                }
                
            } else {
                initializeFailedErrorMsg = "Template SMS plug-in not initialized due to unavailable NTF service(s).";
            }
        } else {
            initializeFailedErrorMsg = "Template SMS plug-in not initialized due to null NotifierNtfServicesManager instance.";
        }
        
        if(initializeFailedErrorMsg != null) {
            if(log != null) {
                log.error(initializeFailedErrorMsg);
            }
            throw new NotifierPluginException(initializeFailedErrorMsg);
        }
    }
    
    @Override
    public boolean isHandlingNotificationEvent(String eventType, Properties eventProperties) {
        boolean isHandlingEvent;
        //Events cannot be handled unless the support services are initialised.
        if(isInitialized) {
            isHandlingEvent = NotifierHandler.get().isHandledByNotifierHandler(eventType, eventProperties) != null;
        } else {
            if(log != null) {
                log.debug("Template SMS plug-in not handling " + eventType);
            }
            isHandlingEvent = false;
        }
        return isHandlingEvent;
    }

    /* (non-Javadoc)
     * @see com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.ANotifierPlugin#handleNotification(com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierIncomingSignalInfo)
     * 
     * This method checks to see if we are handling the notification type and handles if it is the case.
     * 
     * It should not hold the thread open for to long, as it will hold up the default notifiers in NTF.
     */
    @Override
    public NotifierIncomingSignalResponse handleNotification(INotifierIncomingSignalInfo signalInfo) {    
        NotifierIncomingSignalResponse incomingSignalResponse;
        //Events cannot be handled unless the support services are initialised.
        if(isInitialized) {
            incomingSignalResponse = NotifierHandler.get().handleNotifier(signalInfo);
        } else {
            if (notifierProfiler.isProfilerEnabled()) {
                NotifierHelper.profilerCheckPoint("NTF.TNP.1.NP.NotInitialized");
            }
            if(log != null) {
                log.debug("Notifier plug-in not handling " + signalInfo.getServiceType());
            }    
            incomingSignalResponse = new NotifierIncomingSignalResponse();
            incomingSignalResponse.addHandlingType(NotifierHandlingTypes.NONE);
            incomingSignalResponse.setAction(NotifierHandlingActions.OK);
        }
        return incomingSignalResponse;
    }

    @Override
    public boolean refreshConfig() {
        boolean isRefreshed = false;
        String notifierPluginConfigFilePath = notifierConfigManagerFactory.getNtfConfigDirectory() + File.separator + NotifierConfigConstants.TEMPLATE_PLUGIN_CONFIG_FILE_NAME;
        INotifierConfigManager newNotifierPluginConfigManager = notifierConfigManagerFactory.getConfigManager(notifierPluginConfigFilePath);
        INotifierConfigManager newNtfConfigManager = notifierConfigManagerFactory.getNtfConfigManager();
        if(newNotifierPluginConfigManager != null && newNtfConfigManager != null) {
            notifierPluginConfigManager = newNotifierPluginConfigManager;
            ntfConfigManager = newNtfConfigManager;
            NotifierConfig.refreshConfig();
            if (TemplateType.refreshConfig()) {
            	CancelHandler.get().refreshConfig();
            	isRefreshed = true;
            }
        } else {
            log.error("Refresh config failed.");
        }
        return isRefreshed;
    }
    
    @Override
    public void stateChange(INotifierNtfAdminState state) {
    	//For now not using this, as we will implement the NTFthread to handle the state Changes.
        log.info("Received state change: " + state);
    }
    
    
    /*
     * Static methods to make the support services available to the plug-in classes.
     */
    
    public static INotifierNtfNotificationInfo getNtfNotificationInfo(Properties eventProperties) {
        return notifierNtfServicesManager.getNtfNotificationInfo(eventProperties);
    }

    public static INotifierConfigManagerFactory getConfigManagerFactory() {
        return notifierConfigManagerFactory;
    }

    public static INotifierConfigManager getNotifierPluginConfigManager() {
        return notifierPluginConfigManager;
    }

    public static INotifierConfigManager getNtfConfigManager() {
        return ntfConfigManager;
    }

    public static INotifierDatabaseAccess getDatabaseAccess() {
        return notifierDatabaseAccess;
    }

    public static INotifierEventScheduler getEventScheduler() {
        return notifierEventScheduler;
    }

    public static INotifierLoggerFactory getLoggerFactory() {
        return notifierLoggerFactory;
    }

    public static INotifierManagementCounter getManagementCounter() {
        return notifierManagementCounter;
    }

    public static INotifierProfiler getProfiler() {
        return notifierProfiler;
    }

    public static INotifierMdrGenerator getMdrGenerator() {
        return notifierMdrGenerator;
    }
    
    public static INotifierMessageGenerator getMessageGenerator() {
        return notifierMessageGenerator;
    }

    public static INotifierMfsEventManager getMfsEventManager() {
        return notifierMfsEventManager;
    }

    public static INotifierSenderSms getSender() {
        return notifierSenderSms;
    }

    public static INotifierUtil getUtil() {
        return notifierUtil;
    }
    
    public static ICancelSmsEventRouter GetCancelEventRegister() {
    	return cancelSmsEventRouter;
    }
    
    public static IntfServiceTypePidLookup getNtfServiceTypePidLookup() {
		return ntfServiceTypePidLookup;
	}


	public static INotifierStateManagement getnotifierServicesManager() {
		return notifierServicesManager;
	}	
}
