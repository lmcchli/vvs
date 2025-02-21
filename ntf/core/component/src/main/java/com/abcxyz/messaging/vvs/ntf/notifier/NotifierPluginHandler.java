/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.ANotifierPlugin;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierIncomingSignalInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierPluginException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse.NotifierHandlingActions;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse.NotifierHandlingTypes;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.Config;


public class NotifierPluginHandler extends ANotifierPlugin {

    private static final String CUSTOM_NOTIFIER_PLUGIN_CLASS_NAME = "com.abcxyz.messaging.vvs.ntf.notifier.plugin.custom.NotifierPlugin";

    private static LogAgent log = NtfCmnLogger.getLogAgent(NotifierPluginHandler.class);
    
    private static NotifierPluginHandler instance = null;        
    
    private static Vector<ANotifierPlugin> notifPlugins = null;
    private static boolean hasPlugins = false;
    
    
    public static boolean hasPlugins() {
        return hasPlugins;
    }

    private NotifierPluginHandler() {
        loadPlugins();
    }
    
    public static NotifierPluginHandler get() {
        if(instance == null) {
            instance = new NotifierPluginHandler();
        }
        return instance;
    }
    
    private void loadPlugins() {

        HashMap<String,String> plugins = Config.getNotifierPlugins();
        if (plugins != null ) {
            Class<?> pluginClass = null;
            String plugInName="";
            String className="";
            boolean tableHasValues = true;
            int plugInsLoaded = 0;
            ANotifierPlugin notifierPlugin = null;
            if (plugins.size() == 0 ) {
                //If table empty then assume only legacy plug-in
                notifPlugins = new Vector<ANotifierPlugin>(1);
                plugins.put("LegacyPlugin",CUSTOM_NOTIFIER_PLUGIN_CLASS_NAME);
                tableHasValues = false;
            } else
            {
                notifPlugins = new Vector<ANotifierPlugin>(plugins.size());
            }
            Iterator<String> iter = plugins.keySet().iterator();
            while (iter.hasNext()) {
                try {    
                    plugInName=iter.next();
                    className = plugins.get(plugInName);
                    pluginClass = Class.forName(className);
                    log.info("Attempting to load plug-in: " + plugInName + "> + class: <" + pluginClass + ">" );
                    notifierPlugin = (ANotifierPlugin) pluginClass.newInstance();
                    notifierPlugin.initialize(NotifierNtfServicesManager.get());
                    log.info("Succesfully loaded plugin: <" + plugInName + ">");
                    notifPlugins.add(notifierPlugin);
                    hasPlugins = true;
                    notifierPlugin = null;                    
                    plugInsLoaded++;
                } catch (ClassNotFoundException e) {
                    if (!tableHasValues && CUSTOM_NOTIFIER_PLUGIN_CLASS_NAME.equals(plugins.get(plugInName)) ) {
                        //legacy - if not defined in table, then it's not an error if plug-in does not exist.
                        log.debug("plugin: <" + className + "> class not found: " + e.getMessage());
                    } else {
                        log.error("plugin: <" + className + "> class not found: " + e.getMessage());
                    }
                } catch (InstantiationException e) {
                    log.error("plugin: <" + className + "> could not be instantiated (InstantiationException): " + e.getMessage());                
                } catch (IllegalAccessException e) {
                    log.error("plugin: <" + className + "> could not be instantiated (IllegalAccessException): " + e.getMessage());                
                } catch (NotifierPluginException e) {
                    notifierPlugin = null;
                    log.error("plugin: <" + className + "> could not be initialized: " + e.getMessage());  
                }                
            }
            if (tableHasValues) {
                if (plugInsLoaded > 0 && plugins.size() == plugInsLoaded)
                    log.info("All plug-ins were succesfully loaded");
                else if(plugInsLoaded > 0)
                    log.error("Only loaded " + plugInsLoaded + " notifier plug-ins, out of " + plugins.size());
                else
                    log.error("No notifer plug-ins were succesfully loaded out of a possible " + plugins.size());               
            }   
        } else {
            hasPlugins = false;
            //we should not get null here as Config.getNotifierPlugins() should return an empty hash if no items.
            log.error("No plug-ins were loaded - due to an unexpected null plug-in list");
        }
        
    }
    
    /*
     * These are the ANotifierPlugin class functions which are then passed on to the real plug-in's if they exist.
     */
    
    /*
     * determine if notification is handled by any of the loaded plug-ins. 
     */
    public boolean isHandlingNotificationEvent(String eventType, Properties eventProperties) {
        if (hasPlugins == false) {
            return false;
        } else
        {
            Iterator<ANotifierPlugin> iter = notifPlugins.iterator();
            while(iter.hasNext()) {
                if (iter.next().isHandlingNotificationEvent(eventType, eventProperties) == true ) {
                    return true;
                }
            }
            return false;
        }
    }

    /*
     * Handles an incoming notification signal if applicable.
     * <p>
     * This plug-in can indicate it is handling the notification signal completely, partially or not at all by setting the appropriate {@link NotifierHandlingTypes} value in the NotifierIncomingSignalResponse. 
     * <p>
     * This plug-in can also defer the decision to handle the notification signal by setting the appropriate {@link NotifierHandlingActions} value in the NotifierIncomingSignalResponse.
     *  
     * @param signalInfo the INotifierIncomingSignalInfo containing the information about the incoming notification signal, such as the event properties
     * @return NotifierIncomingSignalResponse indicating whether the plug-in is handling the notification signal completely, partially or not at all
     */
    public NotifierIncomingSignalResponse handleNotification(INotifierIncomingSignalInfo signalInfo) {
        if (hasPlugins == true) { 
            //try all until we find the first one that is fully or partially handling, retrun with result.
            Iterator<ANotifierPlugin> iter = notifPlugins.iterator();
            while(iter.hasNext()) {
                NotifierIncomingSignalResponse incomingSignalResponse = iter.next().handleNotification(signalInfo);
                if (!incomingSignalResponse.getHandlingType().equals(NotifierIncomingSignalResponse.NotifierHandlingTypes.NONE)) {
                    return(incomingSignalResponse);
                }
            } 
        }
        
        //if none are handling then return None.
        NotifierIncomingSignalResponse incomingSignalResponse = new NotifierIncomingSignalResponse();
        incomingSignalResponse.addHandlingType(NotifierHandlingTypes.NONE);
        incomingSignalResponse.setAction(NotifierHandlingActions.OK);
        return incomingSignalResponse;
    }
    
    /**
     * Refreshes the configuration used by this plug-in.
     * This method is invoked by the NTF component when a configuration refresh has been triggered.
     * <p>
     * This plug-in should retrieve new INotifierConfigManager objects and then update any configuration values stored in memory.  
     * @return true if this plug-in successfully refreshed its configuration, false otherwise
     */
    public boolean refreshConfig() {
        boolean result = true; 
        if (hasPlugins == true) { 
            //inform all plug-ins to refresh config.
            Iterator<ANotifierPlugin> iter = notifPlugins.iterator();
            while(iter.hasNext()) {
                //if any are false - all are false.
                result = iter.next().refreshConfig() && result;
            }
        }
        return result;
    }
    
    /**
     * Informs the plug-in of a change of state of NTF such as shutting down, lock etc..
     * This method is invoked by the NTF component when a change of NTF state has occurred.
     * <p>
     * This plug-in should take the appropriate action on state change i.e clean up before shutdown, wait for unlock etc.
     */
    public void stateChange(INotifierNtfAdminState state) {
        if (hasPlugins == true) { 
            //inform all plug-ins of state change.
            Iterator<ANotifierPlugin> iter = notifPlugins.iterator();
            while(iter.hasNext()) {
                //if any are false - all are false.
                iter.next().stateChange(state);
            }
        }       
    }
}
