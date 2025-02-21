/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.outdial;

import java.io.FilenameFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.OdlEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.ntf.event.OdlEvent;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.commands.CommandHandler;
import com.mobeon.common.commands.CommandException;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager.FileStatusEnum;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.NotificationOut;
import com.mobeon.ntf.event.EventRouter;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.userinfo.OdlFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;

import java.io.IOException;
import java.util.*;

public class OutdialNotificationOut extends NotificationOut implements Constants {

    private static OutdialNotificationOut instance = null;
    private PhoneOnListener phoneOnListener;
    private PhoneOnSender   phoneOnSender;
    private Map<String, CommandHandler> commandHandlers;
    private OdlWorker odlWorkers[];
    private OdlCallSpec caller;
    private ManagedArrayBlockingQueue<Object> queue;
    private OdlEventHandler eventHandler;
    private IEventStore eventStore;
    private static LogAgent logger = NtfCmnLogger.getLogAgent(OutdialNotificationOut.class);
    private static boolean merNotification = true;

    // Call public methods can lead to unexpected results if the config does not activate outdial.
    public OutdialNotificationOut() {
        super("outdial");

        if (Config.getDoOutdial()) {
            if (logger.isDebugEnabled())
                { logger.debug("Outdial is active");}
        	eventStore = OdlFactory.getInstance().createEventStore();
            phoneOnListener = new PhoneOnListener(eventStore);
            phoneOnSender = new PhoneOnSender();
            isStarted = start();
            phoneOnSender.start();

            
            phoneOnListener.setDaemon(true);

            phoneOnListener.start();
            
            EventRouter.get().register(phoneOnListener);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Outdial is not active, check Config.doOutdial");
            }
        }
        instance = this;
    }

    public static OutdialNotificationOut get() {
        if (instance == null) {
            instance = new OutdialNotificationOut();
        }
        return instance;
    }

    public ManagedArrayBlockingQueue<Object> getQueue() {
        return queue;
    }

    public IEventStore getEventStore() {
        return eventStore;
    }

    public Map<String, CommandHandler> getCommandHandlers() {
        return commandHandlers;
    }

    public static void setMer(boolean mer) {
        merNotification = mer;
    }

    public static boolean getMer() {
        return merNotification;
    }

    private static OdlCallSpec callerStub;
    static public void setOdlCaller(OdlCallSpec _caller) {
    	callerStub = _caller;
    }

    public boolean start() {
        try {
            int NO_WORKERS = Config.getOutdialWorkers();
            if (NO_WORKERS<=0) {
                logger.warn("Outdial service not stared no workers configured");
                return false;
            }

            makeCommandHandlers();
            queue = new ManagedArrayBlockingQueue<Object>(Config.getOutdialQueueSize());
            if (callerStub == null) {
                caller = new OdlCaller();
            } else {
            	caller = callerStub;
            }
            phoneOnListener.setQueue(queue);
            phoneOnSender.setQueue(queue);
            makeWorkersAndListener();
            eventHandler = (OdlEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.OUTDIAL.getName());
            eventHandler.keepEventStore(eventStore);
            eventHandler.keepOdlQueue(queue);

            return true;
        } catch (CommandException ce) {
            if (log != null) {
                logger.error("Could not start outdial, command handler problem : " + ce);
            }
            return false;
        } catch (java.io.IOException e) {
            if (log != null) {
                logger.error("Could not start outdial notification out interface. Message: " + e);
            }
            return false;
        }
    }

    /**
     * Create and initiate command handlers.
     * For now only one handler, in the file
     * outdial-default.cfg.
     */
    private void makeCommandHandlers() throws CommandException, IOException
    {
        commandHandlers = new HashMap<String, CommandHandler>();
        String defaultConfig = Config.getNtfHome();
        defaultConfig += "/cfg";
        File cfgDir = new File(defaultConfig);

        if (cfgDir.isDirectory()) {
            File[] outdialFiles = cfgDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.startsWith("outdial-")) && (name.endsWith(".cfg"));
                }
            });

            for (int i=0; i<outdialFiles.length ; i++) {
                File outdialFile = outdialFiles[i];
                String name = outdialFile.getName();
                String configName = name.substring(8,name.length()-4);
                logger.debug("Making outdial config from file " + name + " stored with schema name " + configName);

                Properties props = new Properties();
                try {
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(outdialFile));
                    props.load(bis);
                    CommandHandler handler = new CommandHandler(props);
                    commandHandlers.put(configName, handler);
                } catch (CommandException ce) {
                    logger.error("CommandException: Could not retreive outdial configuration from file: " + name, ce);
                    throw ce;
                } catch (IOException ioe) {
                    logger.error("IOException: Could not retreive outdial configuration from file: " + name, ioe);
                    throw ioe;
                }
            }
        }

        if (!commandHandlers.containsKey("default")) {
            logger.error("Default outdial configuration not found or loaded!");
        }
    }

    /**
     * Create the worker and all listeners.
     */
    private void makeWorkersAndListener()
    {
        int NO_WORKERS = Config.getOutdialWorkers();
        odlWorkers = new OdlWorker[NO_WORKERS];
        for (int i = 0; i<NO_WORKERS; i++) {
            odlWorkers[i] = new OdlWorker(commandHandlers, queue, caller, phoneOnSender, "OdlWorker-" + i, eventStore);
            odlWorkers[i].setDaemon(true);
            odlWorkers[i].start();
        }
    }

    /**
     * Cancel from superclass, use cancel(notifNumber,userEmail) instead
     */
    public boolean cancel(String notifNumber)
    {
        logger.debug("Notification out, Can not cancel with only notifnumber " + notifNumber);
        return false;
    }

    /**
     * notify from NTF notification handler to send an out-dial call
     */
    public int notify(UserInfo user, OdlFilterInfo info, String recipientId) {
        return notify(user, info, recipientId, null);
    }

    public int notify(UserInfo user, OdlFilterInfo info, String recipientId, NtfEvent ntfEvent) {
        return notify(user, info, recipientId, ntfEvent, null);
    }

    public int notify(UserInfo user, OdlFilterInfo info, String recipientId, NtfEvent ntfEvent, NotificationGroup ng) {
        int count = 0;
        int countReadOnly = 0;
        
        if (info == null || user == null) {
            return 0;
        }

        try {
            // Lets make sure the recipientId is not an URI but just the telephone number
            String denormalizedRecipientId = CommonMessagingAccess.getInstance().denormalizeNumber(recipientId);

            logger.debug("Received notification for " + denormalizedRecipientId);

            if (!isStarted()){
                String message = "Outdial is not started, notification for " + denormalizedRecipientId;
                logger.error(message);
                if (ng != null) {
                    ng.retry(user, NTF_ODL, message);
                }
                return count;
            }

            if (!user.isOutdialUser()){
                String message = "No Outdial in CoS for " + denormalizedRecipientId;
                logger.debug(message);
                if (ng != null) {
                    ng.failed(user, NTF_ODL, message);
                }
                return count;
            }

            String[] numbers = info.getNumbers();
            if (numbers == null || numbers.length == 0) {
                String message = "No Outdial notification number for " + denormalizedRecipientId;
                logger.warn(message);
                if (ng != null) {
                    ng.failed(user, NTF_ODL, message);
                }
                return count;
            }

            // For each notification number
            for (int i = 0; i < numbers.length; i++) {

                boolean newNotification = true;
                boolean performCall = true;

                // Validate if the subscriber's storage is READ-ONLY (using the notification number)
                if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(numbers[i])) {
                    logger.warn("Storage currently not available to process Outdial for " + denormalizedRecipientId + " : " + numbers[i]);
                    countReadOnly++;
                    continue;
                }

                /**
                 * New notification for a given subscriber number (mailbox), retrieve only the event
                 * related to the given subscriber and notification number combination.
                 *
                 * In the case of one notification number having multiple subscriber numbers (multiple mailbox),
                 * the given subscriber number (mailbox) must be retrieved in order to Outdial the appropriate mailbox.
                 *
                 * Again, in the case of one notification number having multiple subscriber numbers (multiple mailbox),
                 * if there is already a pending notification (ie, waiting phone-on for subscriber1-notification1), still
                 * a new event and a new persistent file must be stored for the new subscriber2-notification1 case.
                 * This will allow the unique notification number to be notified about both subscriber1 (mailbox1) and
                 * subscriber2 (mailbox2).
                 */
                OdlEvent event = eventStore.get(denormalizedRecipientId, numbers[i]);
                if (event != null) {
                    if (event.getRecipentId().equalsIgnoreCase(denormalizedRecipientId) && event.getTelNumber().equalsIgnoreCase(numbers[i])) {

                        // There is already an event pending found in persistent storage
                        newNotification = false;

                        /**
                         * Retrieving stored eventIds from status file (persistent storage).
                         * Status file can be obsolete based on Config.getOutdialStatusFileValidityInMin()
                         * in which case obsolete eventIds still must be cancelled (scheduler cleanup)
                         * and keep going with the business logic as if no eventIds were found.
                         */
                        FileStatusEnum fileStatus = eventStore.fileExistsValidation(event, Config.getOutdialStatusFileValidityInMin());
                        if (fileStatus.equals(FileStatusEnum.FILE_EXISTS_AND_INVALID)) {
                            logger.info("OutdialNotificationOut ignoring eventIds found in storage since status file is obsolete for " + event.getRecipentId() + " " + numbers[i]);

                            // Cancel obsolete eventIds
                            if (event.getReferenceId() != null && event.getReferenceId().length() > 0) {
                                eventHandler.cancelEvent(event.getReferenceId());
                                event.keepReferenceID(null);
                            }

                            if (event.getSchedulerIdReminder() != null && event.getSchedulerIdReminder().length() > 0) {
                                eventHandler.cancelReminderTriggerEvent(event.getSchedulerIdReminder());
                                event.setSchedulerIdReminder(null);
                            }
                        }

                        // Retrieve the schedulerIds
                        EventID schedulerId = null;
                        EventID schedulerIdReminder = null;
                        try {
                            if (event.getReferenceId() != null && event.getReferenceId().length() > 0) {
                                schedulerId = new EventID(event.getReferenceId());
                            }
                            if (event.getSchedulerIdReminder() != null && event.getSchedulerIdReminder().length() > 0) {
                                schedulerIdReminder = new EventID(event.getSchedulerIdReminder());
                            }
                        } catch (InvalidEventIDException ite) {
                            logger.error("Invalid EventId for subscriber " + event.getIdentity(), ite);
                            continue;
                        }

                        // Lookup schedulerId for pending START, LOGIN, WAIT, WAITON or CALL timer
                        if (schedulerId != null) {
                            if (schedulerId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_START.getName())) {
                                // Process this new notification (the retry might be in a while), this notification is the latest.
                                logger.debug("New Outdial notification for " + event.getIdentity() + " while waiting for Start retry, execute this deposit notification.");

                                // Inject a new deposit (restart the pending START timer)
                                eventHandler.rescheduleStartRetry(event);
                                performCall = true;
                            } else if (schedulerId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_LOGIN.getName())) {
                                // Process this new notification (the retry might be in a while), this notification is the latest.
                                logger.debug("New Outdial notification for " + event.getIdentity() + " while waiting for Login retry, execute this deposit notification.");

                                // There is already a LOGIN timer, if the subscriber is still logged-in, let the current LOGIN timer retry.
                                event.setFromLogin(true);

                                // Inject a new deposit (keep the current LOGIN timer - this deposit inherits the remaining timer)
                                performCall = true;
                            } else if (schedulerId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_WAIT.getName())) {
                                // Depending on the configuration, execute or discard this new deposit notification when waiting.
                                performCall = Config.getOutdialWhenInWaitState();
                                logger.debug("New Outdial notification for " + event.getIdentity() + " while waiting for Wait retry, " + (performCall ? "execute" : "discard") + " this new notification.");
                            } else if (schedulerId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_WAITON.getName())) {
                                // Depending on the configuration, execute or discard this new deposit notification when waiting on a Phone On.
                                performCall = Config.getOutdialWhenInWaitOnState();
                                logger.debug("New Outdial notification for " + event.getIdentity() + " while waiting for PhoneOn retry, " + (performCall ? "execute" : "discard") + " this new notification.");
                            } else if (schedulerId.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_CALL.getName())) {
                                // Discard this new deposit notification since there is already a call going on with MAS/XMP.
                                logger.debug("New Outdial notification for " + event.getIdentity() + " while waiting for Call retry, discard this new notification.");
                                performCall = false;
                            } else {
                                logger.error("New Outdial notification for " + event.getIdentity() + " while invalid schedulerId, execute this deposit notification.");
                                event.keepReferenceID(null);
                                newNotification = true;
                            }
                        }

                        // Lookup schedulerIdReminder for pending REMINDER timer
                        else if (schedulerIdReminder != null && schedulerIdReminder.getServiceName().equalsIgnoreCase(NtfEventTypes.EVENT_TYPE_ODL_REMINDER.getName())) {

                            // Do not cancel the current schedulerIdReminder since this new deposit could go faulty (on the out.interface) and perform a call.
                            logger.debug("New Outdial notification for " + event.getIdentity() + " while waiting for Reminder retry, execute this deposit notification.");

                            // Inject a new deposit (starting point is START)
                            performCall = true;
                        } else {
                            // Persistent storage found without any timer scheduled
                            logger.debug("New Outdial notification while " + event.getIdentity() + " found without any timer scheduled, execute this deposit notification");

                            // Inject a new deposit (starting point is START)
                            newNotification = true;
                        }
                    } else {
                        logger.debug("No event found in persistent storage for " + denormalizedRecipientId + " : " + numbers[i] + ", a non-related event was found for " + event.getRecipentId() + " : " + event.getTelNumber());
                    }
                } else {
                    logger.debug("No event found in persistent storage for " + denormalizedRecipientId + " : " + numbers[i]);
                }

                if (newNotification == true) {
                    event = new OdlEvent(denormalizedRecipientId, numbers[i], OdlConstants.EVENT_OUTDIAL_START, OdlConstants.EVENT_CODE_DEFAULT, ntfEvent);
                    logger.debug("New Outdial notification for " + event.getIdentity());

                    // Schedule a START timer
                    eventHandler.scheduleStartRetry(event);

                    // Store event
                    eventStore.put(numbers[i], event);
                    queue.put(event);

                } else if (performCall == true) {
                    logger.debug("Outdial event found in storage and call again for new Outdial notification for " + denormalizedRecipientId + " : " + numbers[i]);

                    // No need to schedule a START timer since it has been re-scheduled

                    // Reset the operation & code
                    event.setOdlTrigger(OdlConstants.EVENT_OUTDIAL_START);
                    event.setOdlCode(OdlConstants.EVENT_CODE_DEFAULT);

                    // Reset the reminder so that this new deposit gets a new reminder expiry period
                    event.resetReminder();

                    // Store event
                    eventStore.put(numbers[i], event);
                    queue.put(event);
                } else {
                    logger.debug("Outdial event found in storage and this new outdial notification is discarded for " + denormalizedRecipientId + " : " + numbers[i]);
                }

                // Even if the new notification is discarded (because of already pending one), it's handled and count as 'processed'
                count++;
            }

            if (count > 0) {
                /**
                 * At least, one Outdial event (for a given notification number) has been injected, consider the notification successful (level-2).
                 * Do not notify MER as the current success is only about injecting the event in level-3 OdlWorkers.
                 */
                if (ng != null) {
                    ng.ok(user, NTF_ODL, false);
                }

                if (countReadOnly > 0) {
                    logger.warn("Outdial notification considered successful since, at least, one notification number has been notified for " + denormalizedRecipientId);
                }
            } else {
                if (countReadOnly > 0) {
                    // No successful notification since at least, one notification number is read-only and none were successful, must retry
                    if (ng != null) {
                        ng.retry(user, NTF_ODL, "Outdial notification retry since " + denormalizedRecipientId + " storage is found to be Read-Only mode");
                    }
                } else {
                    // No successful notification, consider the notification failed
                    if (ng != null) {
                        ng.failed(user, NTF_ODL, "Outdial notification failed for " + denormalizedRecipientId);
                    }
                }
            }
        } catch (Exception e) {
            String message = "Outdial exception for " + user.getTelephoneNumber();
            logger.error(message, e);
            if (ng!= null) {
                ng.failed(user, Constants.NTF_ODL, message);
            }
        }

        return count;
    }


    /**
     * Implements superclass exists.
     * Always return true since this is only used to see if a
     * outdial info can be removed and its better to do the
     * removal attempt anyway.
     */
    public boolean exists(String recipient) {
        if (!isStarted()) { return false; }
        return true;
    }

}
