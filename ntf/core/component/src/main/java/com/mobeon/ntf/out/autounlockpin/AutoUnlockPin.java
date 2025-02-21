/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2013.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.mobeon.ntf.out.autounlockpin;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.abcxyz.messaging.common.mcd.KeyValues;
import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccessException;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryUpdater;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventReceiver;
import com.abcxyz.services.moip.ntf.coremgmt.NtfRetryHandling;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventGenerator;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.userinfo.UserInfo;



/**
 * 
 * Handle AutoUnlockPin CAI3G Unlocking and SMS notification for the AutoUnlockPin feature.
 * 
 * The AutoUnlockPin reset the subscriber MOIPBadLoginCount to 0 after a CoS defined amount of time and 
 * sends an SMS notification to the subscriber that his mailbox has been unlocked.
 * 
 */
public class AutoUnlockPin {

    private static LogAgent log = NtfCmnLogger.getLogAgent(AutoUnlockPin.class);
    
    //First call to this class is from NtfRetryEventHandler constructor; 
    //so, initialize retryHandler later when NtfRetryEventHandler has been created and is in the NtfEventHandlerRegistry.
    protected static NtfRetryHandling retryHandler = null; 

    /**
     * Creates the RetryEventInfo for auto unlock pin unlock according to the configuration settings.
     * @return RetryEventInfo  retry event info for auto unlock pin unlock
     */
    public static RetryEventInfo getAutoUnlockPinUnlockRetryEventInfo(){
        RetryEventInfo info = new RetryEventInfo(NtfEventTypes.AUTO_UNLOCK_PIN_L2.getName());
        info.setEventRetrySchema(Config.getNotifRetrySchema());
        info.setExpireTimeInMinute(Config.getNotifExpireTimeInMin());
        
        return info;
    }
    
    /**
     * Creates the RetryEventInfo for auto unlock pin sms according to the configuration settings.
     * @return RetryEventInfo  retry event info for auto unlock pin sms
     */
    public static RetryEventInfo getAutoUnlockPinSmsRetryEventInfo(){
        RetryEventInfo info = new RetryEventInfo(NtfEventTypes.AUTO_UNLOCK_PIN_L2.getName());
        info.setEventRetrySchema(Config.getNotifRetrySchema());
        info.setExpireTimeInMinute(Config.getNotifExpireTimeInMin());
        
        return info;
    }
    
    /**
     * Schedule delayed AutoUnlockPin Unlock event
     * @param email NotificationEmail
     * @param user UserInfo
     */
    public static void handleAutoUnlockPin(NotificationEmail email, UserInfo user) {
        
        log.debug("Entered handleAutoUnlockPin");
        NtfEvent event = email.getNtfEvent();
        
        String eventId = getRetryHandler().scheduleAutoUnlockPinEvent(event, user);
        if(eventId != null) {
            log.debug("handleAutoUnlockPinUnlock: Succeeded in scheduling auto unlock pin unlock event for " + event.getRecipient());
            String oldEventId = event.getReferenceId();
            getRetryHandler().cancelEvent(oldEventId);

        } else {
            log.warn("handleAutoUnlockPinUnlock: Failed to schedule auto unlock pin event for " + event.getRecipient() + ", will retry.");
        }

        
    }
    
    /**
     * Reset subscriber bad login count and send SMS notification
     *  
     * @param email NotificationEmail
     * @param user UserInfo
     */
    public static void handleAutoUnlockPinUnlock(NotificationEmail email, UserInfo user) {
        
        log.debug("Entered handleAutoUnlockPinUnlock");
        
        NtfEvent event = email.getNtfEvent();
        String recipient = event.getRecipient();

        if(!shouldUnlockBeProcessed(event, user)) {
            
            String eventId = event.getReferenceId();
            log.debug("handleAutoUnlockPinUnlock: Unlock for " + recipient + " should not be processed, canceling the event.");
            getRetryHandler().cancelEvent(eventId);
            
        } else if(resetBadLoginCount(event, user)) {
            /**
             * TUI mailbox successfully unlocked, send SMS to subscriber is enabled in his CoS
             */
            // Generate MDR Success
            MerAgent.get().aupUnlockDelivered(recipient);
            
            if(shouldSmsInfoBeProcessed(event, user)) {
                
                log.debug("AutoUnlockPin:handleAutoUnlockPinUnlock: Unlock success for " + recipient + ", scheduling SMS-Info.");
                
                // Schedule retry for AutoUnlockPin SMS
                String eventIdSmsInfo = getRetryHandler().scheduleAutoUnlockPinSmsEvent(event);
         
                // Send event to NTF event handler
                NtfEvent smsInfoEvent = NtfEventGenerator.generateEvent(eventIdSmsInfo);
                NtfEventReceiver eventReceiver = NtfEventHandlerRegistry.getNtfEventReceiver(NtfEventTypes.AUTO_UNLOCK_PIN_L2.getName());
                if (eventReceiver != null) {
                    eventReceiver.sendEvent(smsInfoEvent);
                }
   
            } else {
                
                //SMS will not be sent
                String eventId = event.getReferenceId();
                log.debug("AutoUnlockPin:handleAutoUnlockPinUnlock: Unlock success for " + recipient + " and SMS-Info should not be sent, canceling retry event " + eventId);
                getRetryHandler().cancelEvent(eventId);
            }
            
        } else {
            log.debug( "AutoUnlockPin:handleAutoUnlockPinUnlock: Failed to reset " + DAConstants.ATTR_BAD_LOGIN_COUNT + " for " 
                    + event.getRecipient() + ", Unlock will retry (level-2)");
        }
        
    }
    
    public static void handleAutoUnlockPinExpiry(NotificationEmail email) {
        NtfEvent event = email.getNtfEvent();
        getRetryHandler().cancelEvent(event.getReferenceId());
        
        //Generate MDR Expiry
        MerAgent.get().aupUnlockExpired(event.getRecipient());
    }
    
    private static boolean resetBadLoginCount(NtfEvent event, UserInfo user) {
        
        boolean result = true;
        
        DirectoryUpdater dirUpdater = DirectoryUpdater.getInstance();
        String msid = user.getMsid();

        String values[] = { "0" };
        KeyValues kv = new KeyValues(DAConstants.ATTR_BAD_LOGIN_COUNT, values);
        Modification mod = new Modification(Modification.Operation.REPLACE, kv);
        List<Modification> mods = new Vector<Modification>();
        mods.add(mod);

        try {
            URI subMsidURI = new URI("msid", msid, null);
            log.debug("Updating MCD subscriber profile for URI " + subMsidURI + ". Setting " + DAConstants.ATTR_BAD_LOGIN_COUNT + " = 0");
            dirUpdater.updateProfile("subscriber", subMsidURI, mods);
            log.debug("MCD subscriber profile updated successfully");
            
        } catch (URISyntaxException e) {
            log.error("Failed to update MCD subscriber profile for msid " + msid, e);
            result = false;
        } catch (DirectoryAccessException e) {
            log.error("Failed to update MCD subscriber profile for msid " + msid, e);
            result = false;
        }
        
        return result;
        
    }
    
    private static boolean shouldUnlockBeProcessed(NtfEvent event, UserInfo user) {
     
        // Check user still has feature in his CoS
        if( !user.hasAutoUnlockPinEnabled() ) {
            /**
             * MOIPPinAutoUnlockEnabled in user CoS is not set to yes,
             * Auto Unlock is not needed, cancel the event.
             */
            log.debug( "AutoUnlockPinHanlder:shouldUnlockBeProcessed: " + DAConstants.ATTR_AUTO_UNLOCK_PIN_ENABLED + 
                       " is not enabled for " + event.getRecipient());
            return false;
        }
        
        // Check if this is the latest pin lockout
        Date lastPinLockoutDate = user.getLastPinLockoutTime();
        long locktime = 0;
        try {
            locktime = AutoUnlockPinUtil.parseEventTime(event.getProperty(NtfEvent.AUTO_UNLOCK_PIN_LOCKTIME));
        } catch (ParseException e) {
            log.debug("AutoUnlockPinHanlder:shouldUnlockBeProcessed: " + NtfEvent.AUTO_UNLOCK_PIN_LOCKTIME + " could not be parsed from event properties.", e);
        }
        if (lastPinLockoutDate == null || locktime == 0 ||  locktime != lastPinLockoutDate.getTime()) {
            /**
             * user has already been unlocked by customer service or has been unlocked by customer service and then locked himself out again. 
             * Cancel this event, another Auto Unlock Pin event has already been scheduled to handle the second unlock if needed
             */
            if(log.isDebugEnabled()) {
                log.debug( "AutoUnlockPinHanlder:shouldUnlockBeProcessed: " + DAConstants.ATTR_USER_LAST_PIN_LOCKOUT_TIME + " [" + lastPinLockoutDate +
                        "] does not match event lockout time [" + locktime + "]");
            }
            return false;
            
        } 
        
        // Check if user is still locked
        int badLoginCount = user.getBadLoginCount();
        int maxLoginLockout = user.getMaxLoginLockout();
        
        if(badLoginCount < maxLoginLockout) {
            /**
             * user has been unlocked by customer service or the CoS has been updated with a higher MaxLoginLockout, 
             * Auto Unlock is not needed, cancel the event.
             */
            if(log.isDebugEnabled()) {
                log.debug( "AutoUnlockPinHanlder:shouldUnlockBeProcessed: Inbox already unlocked for " + event.getRecipient() + 
                           " (badLoginCount=" + badLoginCount + ", maxLoginLockout=" + maxLoginLockout + ").");
            }
            return false;
        } 
        
        
        return true;
    }
    
    private static boolean shouldSmsInfoBeProcessed(NtfEvent event, UserInfo user) {
               
        // Check user still has feature in his CoS
        if( !user.hasAutoUnlockPinSmsEnabled() ) {
            /**
             * MOIPPinAutoUnlockSmsEnabled in user CoS is not set to yes,
             * SMS-Info is not needed, cancel the event.
             */
            log.debug( "AutoUnlockPin:shouldSmsInfoBeProcessed: " + DAConstants.ATTR_AUTO_UNLOCK_PIN_SMS_ENABLED +
                       " is not enabled for " + event.getRecipient());
            return false;
        }
        
        return true;
    } 
    
    private static NtfRetryHandling getRetryHandler() {
        if(retryHandler == null){
            retryHandler = NtfEventHandlerRegistry.getEventHandler();
        }
        
        return retryHandler;
    }
    
   
}
