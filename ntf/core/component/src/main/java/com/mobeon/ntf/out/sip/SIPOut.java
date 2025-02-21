/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.sip;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.SipMwiEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.ntf.event.SipMwiEvent;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.SIPFilterInfo;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationGroup;

public class SIPOut {

    private LogAgent log = NtfCmnLogger.getLogAgent(SIPOut.class);
    private ManagedArrayBlockingQueue<Object> queue;
    private SipMwiCallSpec caller;
    private boolean isStarted;
    private SIPWorker[] sipWorkers;
    private SipMwiEventHandler sipMwiEventHandler;
    private static SipMwiCallSpec sipMwiCallerStub;
    private static boolean merNotification = true;
    private static SIPOut instance = null;

    /**
     * Constructor
     */
    public SIPOut() {
        if (Config.getDoSipMwi()) {
            isStarted = start();
            log.debug("SIP MWI is active");
        } else {
            log.debug("SIP MWI is inactive, check Config.doSipMwi");
        }
        instance = this;
    }

    public static SIPOut get() {
        if (instance == null) {
            instance = new SIPOut();
        }
        return instance;
    }

    public void setCallerStub(SIPCaller caller) {
        this.caller = caller;
    }

    static public void setSipMwiCaller(SipMwiCallSpec caller) {
        sipMwiCallerStub = caller;
    }

    static public void setNoMerNotification() {
        merNotification = false;
    }

    private boolean start() {
        try {
            int NO_WORKERS = Config.getSipMwiWorkers();
            if(NO_WORKERS<=0)
            {
               return false;
            }

            // Create working queue for SipOut and SipWorkers
            queue = new ManagedArrayBlockingQueue<Object>(Config.getSipMwiQueueSize());

            // Create caller and workers
            if (sipMwiCallerStub == null) {
                caller = new SIPCaller();
            } else {
                caller = sipMwiCallerStub;
            }

            createWorkers(NO_WORKERS);

            // Create SipMwi event handler
            sipMwiEventHandler = (SipMwiEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SIPMWI.getName());
            sipMwiEventHandler.setWorkingQueue(queue);
            return true;
        } catch (Exception e) {
            if (log != null) {
                log.error("Could not start SIP notification out interface. Message: " + e);
            }
            return false;
        }
    }

    /**
     * Create the workers
     */
    private void createWorkers(int NO_WORKERS) {
        sipWorkers = new SIPWorker[NO_WORKERS];
        for (int i = 0; i<NO_WORKERS; i++) {
            sipWorkers[i] = new SIPWorker(queue, caller, "SIPWorker-" + i, merNotification);
            sipWorkers[i].setDaemon(true);
            sipWorkers[i].start();
        }
    }

    /**
     * This method is invoked by a notification handler thread to set out a SIP MWI notification.
     * @param user UserInfo
     * @param info SIPFilterInfo
     * @param inbox UserMailbox
     * @param recipientId RecipientId
     * @param messageInfo MessageInfo
     * @param ntfEvent NtfEvent
     * @return Number of SIP MWI notifications sent out
     */
    public int handleMWI(UserInfo user, SIPFilterInfo info, UserMailbox inbox, String recipientId, MessageInfo messageInfo, NtfEvent ntfEvent) {
        return handleMWI(user, info, recipientId, ntfEvent, null);
    }

    public int handleMWI(UserInfo user, SIPFilterInfo info, String recipientId, NtfEvent ntfEvent, NotificationGroup ng) {
        int count = 0;
        int countReadOnly = 0;

        try {
            String[] numbers = info.getNumbers();
            SipMwiEvent sipMwiEvent = null;

            if (!isStarted()) {
                String message = "SipMwi is not started, notification for " + recipientId;
                log.error(message);
                if (ng != null) {
                    ng.retry(user, Constants.NTF_SIPMWI, message);
                }
                return count;
            }

            if (numbers == null || numbers.length == 0) {
                String message = "No SipMwi notification number for " + recipientId;
                log.warn(message);
                if (ng != null) {
                    ng.failed(user, Constants.NTF_SIPMWI, message);
                }
                return count;
            }

            String subscriberNumber = CommonMessagingAccess.getInstance().denormalizeNumber(recipientId);
            if (subscriberNumber == null || subscriberNumber.isEmpty()) {
                String message = "No SipMwi subscriber number denormalized for " + recipientId;
                log.warn(message);
                if (ng != null) {
                    ng.failed(user, Constants.NTF_SIPMWI, message);
                }
                return count;
            }

            for (int i=0; i<numbers.length; i++) {
                // Create new event
                if (numbers[i] == null || numbers[i].isEmpty()) {
                    continue; //skip if a null entry.
                }
                
                sipMwiEvent = new SipMwiEvent(subscriberNumber, numbers[i], ntfEvent);

                // Validate if the subscriber's storage is READ-ONLY (using the notification number)
                if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(numbers[i])) {
                    log.warn("Storage currently not available to process SipMwi for " + sipMwiEvent.getIdentity() + ", will retry");
                    countReadOnly++;
                    continue;
                }

                // Add event to the scheduler (backup event)
                if (!sipMwiEventHandler.scheduleBackup(sipMwiEvent)) {
                    String message = "Unable to update persistent storage for " + sipMwiEvent.getIdentity() + ", will retry";
                    log.warn(message);
                    if (ng != null) {
                        ng.retry(user, Constants.NTF_SIPMWI, message);
                    }
                    return count;
                }

                // Add to the working queue for initial processing
                queue.put(sipMwiEvent);

                count++;
            }

            if (count > 0) {
                /**
                 * At least, one SipMwi event (for a given notification number) has been injected, consider the notification successful (level-2).
                 * Do not notify MER as the current success is only about injecting the event in level-3 OdlWorkers.
                 */
                if (ng != null) {
                    ng.ok(user, Constants.NTF_SIPMWI, false);
                }

                if (countReadOnly > 0) {
                    log.warn("SipMwi notification considered successful since, at least, one notification number has been notified for " + subscriberNumber);
                }
            } else {
                if (countReadOnly > 0) {
                    // No successful notification since at least, one notification number is read-only and none were successful, must retry
                    if (ng != null) {
                        ng.retry(user, Constants.NTF_SIPMWI, "SipMwi notification retry since " + subscriberNumber + " storage is found to be Read-Only mode");
                    }
                } else {
                    // No successful notification, consider the notification failed
                    if (ng != null) {
                        ng.failed(user, Constants.NTF_SIPMWI, "SipMwi notification failed for " + subscriberNumber);
                    }
                }
            }
        } catch (Exception e) {
            String message = "SipMwi exception for " + user.getTelephoneNumber();
            log.error(message, e);
            if (ng!= null) {
                ng.failed(user, Constants.NTF_SIPMWI, message);
            }
        }

        return count;
    }

    public boolean isStarted() {
        return isStarted;
    }
}
