/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2010.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.mobeon.ntf.out.vvm;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.handling.AppliEventHandler;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.EventsStatusListener;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryUpdater;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.cmnaccess.EventInfoPersistence;
import com.mobeon.common.cmnaccess.EventInfoPersistence.EventInfoTypes;

/**
 * Controls the setting of a subscriber's "VVM System Deactivated" MCD flag. A subscriber is "VVM System Deactivated" if, after a
 * timeout period, its client phone has not reacted to a notification of a new message. On the other hand, it becomes
 * "VVM activated" as soon as an event of "subscriber activity detected" is received.
 * 
 * <p>
 * There are other details for the conditions to flip the flag. See the code for details.
 * </p>
 * 
 * <p>
 * Reference: FP 105 65-0334/09378 - Visual Voicemail SIM swap
 * </p>
 * 
 * @author lmcyvca
 * 
 * @deprecated Class is kept to process legacy Visual Voicemail SIM swap notification schedule.
 */
public class VVMSystemDeactivator {

    private static final String MOIP_VVM_SYSTEM_ACTIVATED = "MOIPVvmSystemActivated";
    private AppliEventHandler eventHandler;
    private EventInfoPersistence persistor;
    private final static String VVM_TIMEOUT_TYPE = "vvmTimeoutType";
    private final static LogAgent logger = NtfCmnLogger.getLogAgent(VVMSystemDeactivator.class);

    /**
     * @param inactivityTimeoutInMinutes
     *        the subscriber will be considered "VVM system deactivated" if it does not answer "new VVM message" notifications
     *        within this timeout period, in minutes. A value of 0 or lower disables this feature.
     */
    public VVMSystemDeactivator(long inactivityTimeoutInMinutes) {
        logger.debug("Creating VVM System Deactivator with inactivity timeout = " + inactivityTimeoutInMinutes + " minutes");

        persistor = new EventInfoPersistence(EventInfoTypes.VVMTimeout, inactivityTimeoutInMinutes * 60 * 1000);

        // For format of retry schema, see class EventRetryTimerSchema
        RetryEventInfo info = new RetryEventInfo(VVM_TIMEOUT_TYPE, "", 0); // no retries, no expiry
        eventHandler = new AppliEventHandler(info, new CountdownReachedListener());
    }

    /**
     * A callback for the messaging core scheduler.
     * 
     * @author lmcyvca
     */
    private class CountdownReachedListener implements EventsStatusListener {

        @Override
        public long getNextRetryTime(AppliEventInfo eventInfo) {
            return 0;
        }

        @Override
        public int eventFired(AppliEventInfo eventInfo) {
            String msid = eventInfo.getEventKey();
            logger.debug("Legacy VVM inactivity timeout reached for subscriber msid " + msid + ". Delete file.");
            
            try {
                persistor.delete(msid);
                logger.debug("Persisted countdown event file deleted successfully.");
            } catch (MsgStoreException e) {
                logger.debug(
                        "Failed to delete persisted countdown event.  Ignoring error because event was cancelled anyway.",
                        e);
                // Do nothing - we no longer need it anyway
            }
            
            
//          /*
//          * The original event that eventually led to the VVM system deactivation may not have been received by the
//          * subscriber if he had no filters defined. Verify this and send him a fallback notification SMS if required.
//          */
//            Properties props = eventInfo.getEventProperties();
//            boolean otherNotificationsWereSent = false;
//            if (props != null) {
//                String otherNotifString = props.getProperty(OTHER_NOTIFICATIONS_WERE_SENT, "false");
//                otherNotificationsWereSent = Boolean.parseBoolean(otherNotifString);
//            }
//            
//            if (!otherNotificationsWereSent && getSubVvmSystemActivatedFlag(msid)) { 
//                                
//                try {
//                    String eventId = eventInfo.getEventId();
//                    NtfEvent event = NtfEventGenerator.generateEvent(eventId);
//                    
//                    UserInfo userInfo = UserFactory.findUserByTelephoneNumber(event.getRecipient());
//                    if (userInfo == null) {
//                        logger.error("Unable to find subscriber with msid " + msid + " to notify that VVM System deactivated.");
//                        return EventHandleResult.OK;
//                    }
//                    
//                    NotificationEmail email = new NotificationEmail(event);
//                    email.init();
//                    
//                    NotificationGroup ng = new NotificationGroup(NtfMain.getEventHandler(), email, logger, MerAgent.get());
//                    FallbackUtil.doLevelTwoScheduledFallback(userInfo, ng);
//                } catch (MsgStoreException e) {
//                    logger.error("Unable to notify subscriber with msid " + msid + " that VVM System deactivated.", e);
//                }
//            }
            
            
            
            
            return EventHandleResult.OK;
        }

        @Override
        public void reportEventScheduleFail(AppliEventInfo eventInfo) {
            // Do nothing
        }

        @Override
        public void reportEventCancelFail(AppliEventInfo eventInfo) {
            // Do nothing
        }

        @Override
        public void reportCorruptedEventFail(String eventId) {
            // Do nothing
        }
    }

// deprecated
//
//    /**
//     * Informs this class that an event requiring a reaction from the VVM client was sent to the client. This method will start the
//     * inactivity timeout countdown when the right conditions are met. When the countdown reaches 0, the subcriber will be set
//     * "VVM System Deactivated" in MCD, thus disabling VVM functionality (SIM Swapped to non-VVM phone).
//     * 
//     * @param event
//     *        The NTF event that was received that requires client reaction
//     * @param userInfo
//     *        The subscriber info associated to the VVM DEPOSIT event.
//     * @param otherNotificationsWereSent true if any other notifications were sent for the event
//     */
//    public void eventRequiringClientReactionWasSent(NtfEvent event, UserInfo userInfo, boolean otherNotificationsWereSent) {
//        try {
//            logger.debug("### Entering eventRequiringClientReactionWasSent()");
//            String msid = getRecipientMsid(event);
//            
//            if (msid != null && userInfo != null) {
//                if (inactivityTimeoutInMinutes > 0 && !countdownAlreadyStarted(msid) && subHasVvmEnabled(userInfo)
//                        && subPhoneIsTurnedOn(userInfo)) {
//                    // Start countdown
//
//                    long when = System.currentTimeMillis() + (inactivityTimeoutInMinutes * 60 * 1000);
//                    Properties props = event.getPersistentProperties();
//                    props.put(OTHER_NOTIFICATIONS_WERE_SENT, Boolean.toString(otherNotificationsWereSent));
//                    AppliEventInfo eventInfo = eventHandler.scheduleEvent(when, msid, VVM_TIMEOUT_TYPE, props);
//
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("Scheduling VVM activity timeout for msid " + msid + ".  When: " + new Date(when));
//                    }
//
//                    try {
//                        saveCountdownEvent(eventInfo, msid);
//                    } catch (MsgStoreException e) {
//                        logger.error("Failed to save countdown event for msid " + msid
//                                + ". Cancelling countdown.  Subscriber will keep VVM functionality.", e);
//                        eventHandler.cancelEvent(eventInfo);
//                    }
//                }
//            }
//        } finally {
//            logger.debug("### Leaving eventRequiringClientReactionWasSent()");
//        }
//    }
//
//    /**
//     * @return the recipient msid from the given event
//     */
//    private String getRecipientMsid(NtfEvent event) {
//        String msid = "";
//
//        if (event != null) {
//            MessageInfo msgInfo = event.getMsgInfo();
//
//            if (msgInfo != null && msgInfo.rmsa != null) {
//                msid = msgInfo.rmsa.getId();
//            }
//        }
//
//        return msid;
//    }
//    
//    /**
//     * Informs this class that some VVM client activity was detected. This causes the countdown timer to be cancelled, and the
//     * subscriber to be set to "VVM System Activated" in MCD (SIM swapped to VVM phone).
//     * 
//     * @param msid
//     *        The msid of the subscriber for whom VVM client activity was detected.
//     */
//    public void vvmClientActivityWasDetected(String msid) {
//        try {
//            logger.debug("### Entering vvmClientActivityWasDetected()");
//
//            if (msid != null) {
//                // Cancel countdown
//                try {
//                    String eventId = persistor.getEvent(msid);
//                    setSubVvmSystemActivatedFlag(msid, eventId, true, false);
//
//                    if (eventId != null) {
//                        AppliEventInfo eventInfo = new AppliEventInfo();
//                        eventInfo.setEventId(eventId);
//                        eventHandler.cancelEvent(eventInfo);
//                        persistor.delete(msid);
//                        logger.debug("Event cancelled for msid " + msid + ": " + eventInfo);
//
//                        try {
//                            deleteCountdownEvent(msid);
//                            logger.debug("Persisted countdown event file deleted successfully.");
//                        } catch (MsgStoreException e) {
//                            logger.debug(
//                                    "Failed to delete persisted countdown event.  Ignoring error because event was cancelled anyway.",
//                                    e);
//                            // Do nothing - we no longer need it anyway
//                        }
//                    } else {
//                        /*
//                         * Event not found. Ok to ignore because this method gets called for every client activity. The event gets
//                         * cancelled on the first call, so for subsequent calls, it's normal there's no event to cancel.
//                         */
//                    }
//                } catch (MsgStoreException e) {
//                    logger.error("Failed to find persisted event ID for msid " + msid
//                            + ". Cannot cancel event.  Subscriber will be 'VVM system deactivated' when countdown reaches 0.", e);
//                }
//            }
//        } finally {
//            logger.debug("### Leaving vvmClientActivityWasDetected()");
//
//        }
//    }
//
//    private void saveCountdownEvent(AppliEventInfo eventInfo, String msid) throws MsgStoreException {
//        logger.debug("Saving countdown event: msid=" + msid + ", event=" + eventInfo);
//        persistor.save(eventInfo, msid);
//        logger.debug("Countdown event was successfully saved");
//    }
//
//    private void deleteCountdownEvent(String msid) throws MsgStoreException {
//        logger.debug("Deleting persisted countdown event: msid=" + msid);
//        persistor.delete(msid);
//        logger.debug("Persisted countdown event was successfully deleted");
//    }
//
//    private boolean countdownAlreadyStarted(String msid) {
//        boolean isStarted = false;
//
//        try {
//            isStarted = persistor.getEvent(msid) != null;
//        } catch (MsgStoreException e) {
//            logger.error("Could not determine whether countdown was already started.  Assuming already started = " + isStarted, e);
//        }
//
//        logger.debug("Countdown already started = " + isStarted + " for msid " + msid);
//
//        return isStarted;
//    }
//
//    /**
//     * This method determine the phone status on or off by doing a HLR lookup or from an SMS-0 bounce. The way is contained in a
//     * confg parameter in NTF
//     * 
//     * @param userInfo
//     *        The subscriber profile
//     * @return true is phone is on, false otherwise
//     */
//    private boolean subPhoneIsTurnedOn(UserInfo userInfo) {
//        boolean isPhoneOn = false; // play it safe by default - phone is off so we won't schedule the timeout and thus keep VVM
//                                   // functionality enabled
//        String phoneNumber = userInfo.getTelephoneNumber();
//
//        if (System.getProperty("forcephoneon", "false").equals("true")) {
//            logger.debug("subPhoneIsTurnedOn(): System property forcephoneon is true. Phone ON detection overridden.");
//            isPhoneOn = true;
//        } else {
//            // Check if we already have the information in the cache
//            PhoneStatus phoneStatus = PhoneStatus.getPhoneStatus(userInfo.getTelephoneNumber());
//            if (System.getProperty("forcelookupforphoneondetection", "false").equals("true")) {
//                // For test purpose only. Display the actual info and force a lookup
//                logger.debug("subPhoneIsTurnedOn(): phoneStatus.isPhoneOn() would return " + phoneStatus.isPhoneOn().toString());
//            } else {
//                if (phoneStatus.isPhoneOn() != PhoneStatus.State.NONE) {
//                    return (phoneStatus.isPhoneOn() == PhoneStatus.State.YES);
//                }
//            }
//
//            // We don't have it so we need to make a lookup
//
//            // Check which method we have to use to detect the phone is ON or OFF
//            // and run detection based on the value of the parameter:
//            // - SMSType0
//            // - any other will be assuned as HLR ATI without alert SC.
//            String lookupMode = Config.getSimSwapPhoneOnMode();
//            logger.debug("Lookup using " + lookupMode + " method.");
//
//            PhoneOnOff phone = PhoneOnOff.create(lookupMode);
//            isPhoneOn = phone.isPhoneOn(userInfo);
//
//            // Persist the phone ON/OFF state in the cache
//            phoneStatus.setPhoneOn((isPhoneOn) ? PhoneStatus.State.YES : PhoneStatus.State.NO);
//        }
//
//        logger.debug("Sub phone is turned on = " + isPhoneOn + " for phone number " + phoneNumber);
//
//        return isPhoneOn;
//    }
//
//    private boolean subHasVvmEnabled(UserInfo userInfo) {
//        boolean hasVvmEnabled = userInfo.hasVvmService() && userInfo.isVVMNotificationAllowed();
//
//        logger.debug("Sub has VVM enabled = " + hasVvmEnabled + " for phone number " + userInfo.getTelephoneNumber());
//
//        return hasVvmEnabled;
//    }
//
//    /**
//     * Set a subscriber's VVM system deactivated flag in MCD.  If the flag changes, listeners are notified.
//     * 
//     * @param msid
//     *        The msid of the subscriber for whom to set the flag
//     * @param eventId
//     *        The ID of the original NtfEvent passed to {@link #eventRequiringClientReactionWasSent(NtfEvent, UserInfo, boolean)}
//     * @param newSystemActivatedFlag
//     *        false to set the sub "VVM system deactivated", true to set it "VVM system activated"
//     * @param otherNotificationsWereSent true if any other notifications were sent for the given eventId
//     */
//    private void setSubVvmSystemActivatedFlag(String msid, String eventId, boolean newSystemActivatedFlag, boolean otherNotificationsWereSent) {
//        boolean oldSystemActivated = getSubVvmSystemActivatedFlag(msid);
//
//        logger.debug(MOIP_VVM_SYSTEM_ACTIVATED + ": OLD = " + (oldSystemActivated ? "yes" : "no") + "; NEW = " + (newSystemActivatedFlag ? "yes" : "no"));
//        
//        if (newSystemActivatedFlag != oldSystemActivated) {
//            DirectoryUpdater dirUpdater = DirectoryUpdater.getInstance();
//            String newSystemActivatedString = newSystemActivatedFlag ? "yes" : "no";
//            String values[] = { newSystemActivatedString };
//            KeyValues kv = new KeyValues(MOIP_VVM_SYSTEM_ACTIVATED, values);
//            Modification mod = new Modification(Modification.Operation.REPLACE, kv);
//            List<Modification> mods = new Vector<Modification>();
//            mods.add(mod);
//
//            try {
//                URI subMsidURI = new URI("msid", msid, null);
//                logger.debug("Updating MCD subscriber profile for URI " + subMsidURI + ". Setting " + MOIP_VVM_SYSTEM_ACTIVATED + " = "
//                        + newSystemActivatedString);
//                dirUpdater.updateProfile("subscriber", subMsidURI, mods);
//                logger.debug("MCD subscriber profile updated successfully");
//                if (vvmSystemDeactivatorListener != null) {
//                    NtfEvent event = NtfEventGenerator.generateEvent(eventId);
//                    vvmSystemDeactivatorListener.subVvmActivatedFlagWasModified(msid, event, otherNotificationsWereSent, oldSystemActivated, newSystemActivatedFlag);
//                }
//            } catch (URISyntaxException e) {
//                logger.error("Failed to update MCD subscriber profile for msid " + msid, e);
//            } catch (DirectoryAccessException e) {
//                logger.error("Failed to update MCD subscriber profile for msid " + msid, e);
//            }
//        }
//    }
//
    private boolean getSubVvmSystemActivatedFlag(String msid) {
        boolean isVvmSystemActivated = false;
        
        DirectoryUpdater dirUpdater = DirectoryUpdater.getInstance();
        
        if (dirUpdater != null) {
            IDirectoryAccessSubscriber sub = dirUpdater.lookupSubscriber("msid:" + msid);
            
            if (sub != null) {
                boolean[] values = sub.getBooleanAttributes(MOIP_VVM_SYSTEM_ACTIVATED);
                
                if (values != null && values.length > 0) {
                    isVvmSystemActivated = values[0];
                }
            }
        }
        
        return isVvmSystemActivated;
    }
}
