package com.mobeon.ntf.reminder;

import com.mobeon.ntf.Constants;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.Logger;
import com.mobeon.common.storedelay.DelayHandler;
import com.mobeon.common.storedelay.DelayInfo;

import java.util.Calendar;

public class ReminderHandler implements Constants {
    
    private final static Logger log = Logger.getLogger(ReminderHandler.class);

    private DelayHandler delayer;

    private ReminderWorker reminderWorkers[];
    private ReminderListener reminderListener;
    private ManagedArrayBlockingQueue<Object> queue;

    private static ReminderHandler inst = null;
    private boolean isStarted;

    public static ReminderHandler get() {
        return inst;
    }
    public static void setReminderHandler(ReminderHandler handler) {
        inst = handler;
    }

    public ReminderHandler(DelayHandler delayer) {

        // Instantiate single OdlNotificationFailed, OdlControl, single
        // OAProtocol OdlControl is listener to DelayLine, OdlNotificationFailed
        // is listener to timeout.
        if (delayer != null) {
            log.logMessage("Reminder is active", Logger.L_VERBOSE);
            this.delayer = delayer;

            start();

        } else {
            log.logMessage("Reminder is not active, check creation of delay handler", Logger.L_VERBOSE);
        }


    }

    public boolean start() {
        try {
            queue = new ManagedArrayBlockingQueue<Object>(250);
            makeWorkersAndListener();
            delayer.registerInterest(ReminderInfo.DELAY_TYPE_REMINDER, reminderListener);
            setStarted(true);
            return true;
        }  catch (Exception e) {
            if (log != null) {
                log.logMessage("Could not start reminder interface. Message: " + e, Logger.L_ERROR);
            }
            return false;
        }
    }




    /**
     * Create the worker and all listeners.
     */
    private void makeWorkersAndListener()
    {
        int NO_WORKERS = 2;
        ReminderCallerImpl caller = new ReminderCallerImpl();
        reminderWorkers = new ReminderWorker[NO_WORKERS];
        for (int i = 0; i<NO_WORKERS; i++) {
            reminderWorkers[i] = new ReminderWorker(delayer, queue,
                                                    "ReminderWorker-" + i, caller);
            reminderWorkers[i].setDaemon(true);
            reminderWorkers[i].start();
        }
        reminderListener = new ReminderListener("reminderlistener", queue);

    }

    /**
     * used for testing only. Set this to use a specific caller impl.
     * @param caller ReminderCaller
     */
    public void setReminderCaller(ReminderCaller caller) {
        for( int i=0;i<reminderWorkers.length; i++ ) {
            ReminderWorker worker = reminderWorkers[i];
            worker.setCaller(caller);
        }
    }

    /**
     * Cancel one reminder attempt
     */
    public boolean cancel(UserInfo user) {
        if (!Config.isUnreadMessageReminder() ||  !isStarted() || !user.hasUnreadMessageReminder()) {
            return false;
        }

        String userEmail = user.getMail();
        log.logMessage("Begin operation to cancel reminder notification " +
                       " for mail " + userEmail, Logger.L_DEBUG);

        delayer.cleanInfo(userEmail, ReminderInfo.DELAY_TYPE_REMINDER );
        return true;
    }




    /**
     * Add entry if not exist. Set counter to 0 if exist. Always set reminder time to now +
     * configurable time (often 24h).
     * @param user the user that shall have the reminder
     *
     * @return true if the entry is successfully scheduled
     */
    public boolean doReminder(UserInfo user) {
        if( !Config.isUnreadMessageReminder() ) {
            //log.logMessage("doReminder: reminder not active in configuration parameter unreadmessageremindertype ", log.L_DEBUG);
            return false;
        }
        if (!isStarted()) {
            log.logMessage("doReminder: reminder notification, but reminder is not started.", Logger.L_ERROR);
            return false;
        }

        if( !user.hasUnreadMessageReminder()) {
            log.logMessage("doReminder: user " + user.getMail() + " does not have unreadmessage reminder", Logger.L_DEBUG);
            return false;
        }

        log.logMessage("doReminder: retrieved reminder request for " + user.getMail(), Logger.L_DEBUG);


        String userEmail = user.getMail();
        String userDN    = user.getFullId();
        Calendar now = Calendar.getInstance();
        long nowMS = now.getTimeInMillis();
        int nextTime = Config.getUnreadMessageReminderInterval();
        Calendar next = now;
        next.add(Calendar.SECOND, nextTime);

        String key = userEmail;
        DelayInfo storedInfo = delayer.getInfo(key, ReminderInfo.DELAY_TYPE_REMINDER);
        if( storedInfo != null ) {
            ReminderInfo newInfo = new ReminderInfo(storedInfo);
            newInfo.setRetryAttempts((short) 0);
            DelayInfo dInfo = newInfo.getPersistentRepresentation();
            delayer.schedule(next, dInfo);
        } else {
            ReminderInfo newInfo = new ReminderInfo(userEmail,
                    nowMS, userDN);
            DelayInfo dInfo = newInfo.getPersistentRepresentation();
            delayer.schedule(next, dInfo);
        }


        return true;
    }


    /**
     * Implements superclass exists.
     * Always return true since this is only used to see if a
     * outdial info can be removed and its better to do the
     * removal attempt anyway.
     */
    public boolean exists() {
        if (!isStarted()) { return false; }
        return true;
    }


    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }
}
