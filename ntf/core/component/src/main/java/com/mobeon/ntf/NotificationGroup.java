/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackHandler;
import com.abcxyz.services.moip.ntf.coremgmt.reminder.SmsReminder;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.util.logging.loggingOutputStream;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.userinfo.UserInfo;

import java.util.*;


/**
 * NotificationGroup collects the details about a group of notifications,
 * i.e. notifications about a common content to a number of receivers.
 * It keeps information about the<UL>
 * <LI>content (i.e. the notification email)
 * <LI>receivers (i.e. the email recipients)
 * <LI>status (i.e. the feedback about deliveries to different delivery interfaces)
 * </UL>of the notification. Based on the status, this class also decides if all
 * notification in the group are completed and can be removed from persistent
 * storage or if it must be retried at a later time.
 * <P>
 * A notification may be sent to many out interfaces (outdial, SMS...).
 * <P>
 * An event is reported to MER for each successful delivery or when all deliveries
 * have failed. If some deliveries fail and the others are retried, no MER event is
 * reported until a retry succeeds or the notification fails by reaching the
 * expiry time.
 * <P>
 * NotificationGroup, as it keeps track of all receivers for a notification, also
 * helps ensuring that each unique user is only notified once per mail, even if
 * he is addressed with different mail addresses.
 *
 * <H3>Sequence</H3>
 * NotificationGroup has two interfaces, one used to build the NotificationGroup by adding
 * content and receiver information to it, and one used to report feedback from
 * the deliveries. The timing between the two interfaces is independent, except
 * that you can of course not start reporting about a user before it is known to
 * NotificationGroup. The operations within the "build" interfaces must come in
 * a certain order though: <OL>
 * <LI>Create a NotificationGroup instance.
 * <LI>Add a user.
 * <LI>Set the number of delivery interface responses expected for the user.
 * <LI>Repeat until all users have been added.
 * <LI>Tell the NotificationGroup that no more users will be added.
 * </OL>
 *
 * Note that this allows feedback to arrive before the expected number of
 * responses is known. This is OK.
 */
public class NotificationGroup implements com.mobeon.ntf.out.FeedbackHandler {

    private NotificationEmail email; //The email that caused the notification
    private boolean moreUsers = true; //Have all users been added?
    private Hashtable<UserInfo, FeedbackEntry> users;
    private int result = Constants.FEEDBACK_STATUS_UNKNOWN;
    private NtfCompletedListener eventHandler;
    private MerAgent mer;
    private LogAgent log;
    private boolean merEvent; /*true if MER events should be sent*/
    private boolean done = false;
    private boolean isNotificationProcessedAtNtfLevel3 = false;
    private Boolean reminderTriggerStarted = false;
    private Timer timer;

    private static int created = 0; //This many mails have started processing
    private static int released = 0; //This many mails have completed processing
    private static int retried = 0; //This many mails have been set for retrying
    private static int failed = 0; //This many mails have failed and been
                                   //discarded

    /**
     * Constructor that creates a NotificationGroup for a notification mail.
     *@param eventHandler handles the deletion of mails when notification is
     * done.
     *@param email the notification email.
     *@param log where to write log messages.
     *@param mer where to send MER events.
     */
    public NotificationGroup(NtfCompletedListener eventHandler,
                             NotificationEmail email,
                             LogAgent log,
                             MerAgent mer) {
        this.log = log;
        this.mer = mer;
        this.eventHandler = eventHandler;
        this.email = email;
        users = new Hashtable<UserInfo, FeedbackEntry>();
        merEvent = true;

        timer = new Timer();

        timer.schedule(new TimerTask() {
          @Override
          public void run() {
            forceCleanup();
          }
        }, 10 * 60 * 1000 );

        create();
    }


    /**
     * noMerEvent tells the NotificationGroup not to send any events to
     * MER. This is used e.g. for MWI off notifications.
     */
    public void noMerEvent() {
        merEvent = false;
    }

    /**
     * addUser adds another user that shall be notified from the email.
     *@param user the user. The same user must not be added more than once.
     */
    public synchronized void addUser(UserInfo user) {
        users.put(user, new FeedbackEntry());
    }

    /**
     * noMoreUsers tells NotificationGroup that no more users will be added, so
     * when all registered users are done, the notification is done.
     */
    public synchronized void noMoreUsers() {
        moreUsers = false;
        checkAllDone();
    }

    /**
     * setOutCount tells NotificationGroup how many delivery interfaces a user will
     * be notified on, so it is possible to tell if all responses have been
     * collected. Each call to setOutCount increments the number of expected
     * responses from delivery interfaces.
     *@param user the identity of the user.
     *@param c the number of delivery interfaces sent to.
     */
    public synchronized void setOutCount(UserInfo user, int c) {
        if (user == null) { return; }
        FeedbackEntry fe = users.get(user);
        if (fe == null) { return; }

        fe.addcount(c); //Add! The count is 0 initially, but might be
                       //negative now if we have already had responses
        fe.updateCount(); //update the count with the temp count.
        if (fe.getCount() == 0) { //All responses have arrived,
                             //or none is expected, must clean up now.
            userDone(user, false);
        }
    }

    /**
     *Same as setOutCount except that userDone is not called.
     *@param user the identity of the user.
     *@param c the number of delivery interfaces sent to.
     */
    public synchronized void increaseTempCount(UserInfo user, int c) {
        if (user == null) { return; }
        FeedbackEntry fe = users.get(user);
        if (fe == null) { return; }

        fe.AddtempCount(c);
    }

    /**
     * Get the number of remaining delivery interfaces (after setOutCount has
     * been called, that is. Before that it returns the negative of the number of
     * delivery interfaces that have already reported results.)
     *@param user the user to get the count for.
     *@return the number of remaining delivery interfaces the user expects
     * feedback from.
     */
    public int getOutCount(UserInfo user) {
        FeedbackEntry fe = users.get(user);
        if (fe == null) {
            return 0;
        } else {
            return fe.getCount();
        }

    }

    /**
     * Tells if the NotificationGroup contains a particular user.
     *@param user the user to look for.
     *@return true if the user is in the NotificationGroup.
     */
    public boolean containsUser(UserInfo user) {
        return users.containsKey(user);
    }

    /**
     * Returns the email this NotificationGroup is about.
     *@return the email in the NotificationGroup.
     */
    public NotificationEmail getEmail() {
        return email;
    }

    /**
     * Set the result
     * @param result result of notification.
     */
    public void setResult(int result) {
        this.result = result;
    }

    /* Methods to receive feedback from the delivery interfaces */
    public synchronized void ok(UserInfo user, int notifType) {
        ok(user, notifType, true);
    }

    /**
     * This method tells NotificationGroup that this user was successfully notified by one
     * of the delivery interfaces. If there is more than one delivery interface,
     * this method will be called several times for the same user. Each
     * invocation results in a MER event. At the first invocation, the user is
     * considered as successfully notified and removed from the list of users to notify.
     *@param user the user that was successfully notified.
     *@param notifType the delivery interface that successfully notified the user.
     */
    public synchronized void ok(UserInfo user, int notifType, boolean sendToMer) {

        isNotificationProcessedAtNtfLevel3 =
            (notifType == Constants.NTF_SLAM) ||
            (notifType == Constants.NTF_FAX_PRINT_NOTIF) ||
            (notifType == Constants.NTF_MCNNOTIF) ||
            (notifType == Constants.NTF_ODL) ||
            (notifType == Constants.NTF_VVM) ||
            (notifType == Constants.NTF_SIPMWI) ||
            (notifType == Constants.NTF_VVM) ||
            (notifType == Constants.NTF_DELAYED_NOTIFY);

        if (!isNotificationProcessedAtNtfLevel3) {
            log.debug(Constants.notifTypeStrings[notifType] + " delivery OK for subscriber " + user.getTelephoneNumber());
        }

        /**
         * No MER notification (when successful) for Outdial, Slamdown/MCN notification or Visual Voice Mail (VVM)
         * since their respective level-3 scheduling will handle the MDR generation.
         */
        if (isNotificationProcessedAtNtfLevel3) {
            userDone(user, true);
            return;
        }

        if (merEvent && sendToMer) {
            try {
                if(notifType == Constants.NTF_SMS && email.isAutoUnlockPinL2()) {
                    mer.aupSmsDelivered(user.getTelephoneNumber());
                } else if (Config.shouldUseCallerInEventDescription() && notifType == Constants.NTF_SMS) {
                   /**
                    * The callerNumber used to generate SMS MDR is the denormalized one contained in the MFS C1.from field.
                    * This value is used regardless of the senderVisibility since the information is used for MDR purposes
                    */
                   String callerNumber = CommonMessagingAccess.getInstance().denormalizeNumber(email.getSender());
                   mer.notificationDelivered(user.getTelephoneNumber(), notifType, callerNumber);
                } else if(notifType == Constants.NTF_FAX_RECEIPT_SMS){
                   mer.faxprintRecieptNotificationDelivered(user.getTelephoneNumber(),Constants.NTF_FAX_RECEIPT_SMS);
                } else {
                    mer.notificationDelivered(user.getTelephoneNumber(), notifType);
                }
            } catch (Exception e) {
                log.error("Unable to generate MDRs, keep processing the notification for " + user.getTelephoneNumber());
            }
        }

        if ((notifType == Constants.NTF_SMS || notifType == Constants.NTF_FLS) && email.isEventTypeNotif()) {
            synchronized(reminderTriggerStarted) {
                if (reminderTriggerStarted == false) {
                    SmsReminder.startNextReminderTrigger(user, email, Constants.FEEDBACK_STATUS_OK);
                    reminderTriggerStarted = true;
                }
            }
        }
        userDone(user, true);
    }

    /**
     * Failed tells NotificationGroup that notification of this user failed on one
     * of the delivery interfaces.
     *@param user the user whose delivery failed.
     *@param notifType the delivery interface reporting failure.
     *@param msg a message describing why the delivery failed.
     */
    public synchronized void failed(UserInfo user, int notifType, String msg) {
        String s = Constants.notifTypeStrings[notifType] + " delivery failed for subscriber " + user.getTelephoneNumber();
        log.debug(msg == null ? s : s + " \"message: " + msg + "\"");
        handleResultReason(user, notifType, Constants.FEEDBACK_STATUS_FAILED, msg);
    }

    /**
     * Expired tells NotificationGroup that the notification expired before it
     * was delivered to the user.
     *@param user the user whose notification expired.
     *@param notifType the delivery interface reporting expiry.
     */
    public synchronized void expired(UserInfo user, int notifType) {
        log.debug(Constants.notifTypeStrings[notifType] + " delivery expired for subscriber " + user.getTelephoneNumber());
        handleResultReason(user, notifType, Constants.FEEDBACK_STATUS_EXPIRED, "Notification Expired");
    }

    /**
     * Expired tells NotificationGroup that the notification expired before it
     * was delivered to the user.
     *@param user the user whose notification expired.
     *@param notifType the delivery interface reporting expiry.
     */
    public synchronized void expired(UserInfo user, int notifType, String msg) {
        log.debug(Constants.notifTypeStrings[notifType] + " delivery expired for subscriber " + user.getTelephoneNumber());
        handleResultReason(user, notifType, Constants.FEEDBACK_STATUS_EXPIRED, msg);
    }

    /**
     * Retry tells NotificationGroup that notification of this user failed
     * temporarily on one of the delivery interfaces, and that it may help
     * to retry later.
     *@param user the user
     *@param notifType the delivery interface that failed.
     *@param msg a message describing why the notification failed.
     */
    public void retry(UserInfo user, int notifType, String msg) {
        String s = Constants.notifTypeStrings[notifType] + " delivery failed temporarily for subscriber " + user.getTelephoneNumber();
        log.debug(msg == null ? s : s + " \"message: " + msg + "\"");
        handleResultReason(user, notifType, Constants.FEEDBACK_STATUS_RETRY, msg);
    }

    /**
     * Handle one result for a user.
     * This is a helper for the feedback functions (ok, failed,
     * expired, retry). It determines what to do about a report.
     * If the user is not found (probably due to aleady succeding in
     * an A-level notification nothing is done.
     * If this is the last feedback the method userDone is called.
     *@param user the user whose delivery was reported.
     *@param notifType the type of output interface that was reported.
     *@param reason how the delivery went.
     *@param msg explaining text.
     */
    private void handleResultReason(UserInfo user, int notifType, int reason, String msg) {
        if ((notifType == Constants.NTF_SMS || notifType == Constants.NTF_FLS) && email.isEventTypeNotif()) {
            synchronized(reminderTriggerStarted) {
                if (reminderTriggerStarted == false) {
                    SmsReminder.startNextReminderTrigger(user, email, reason);
                    reminderTriggerStarted = true;
                }
            }
        }

        FeedbackEntry fe = users.get(user);
        if (fe == null) { return; } //User already done

        /** Fallback handling.  Specifically for SMS notification, level-2 */
        if (notifType == Constants.NTF_SMS && (reason == Constants.FEEDBACK_STATUS_FAILED || reason == Constants.FEEDBACK_STATUS_EXPIRED)
                && ( email.isEventTypeNotif() || email.isReminderNotification() ) ) {
            NtfEvent event = email.getNtfEvent();

            /** Case of a SMS notification which is already in fallback mode */
            if (event.isFallback()) {
                log.debug("SMS notification " + Constants.STATUS_STRING[reason] + " while in fallback mode.  No other fallback will be triggered for " + event.getRecipient() + ", " + event.getReferenceId());
            } else {
                log.debug("SMS notification " + Constants.STATUS_STRING[reason] + ".  Fallback mechanism will be invoke for " + event.getRecipient() + " to figure out if a fallback notification must be invoked");

                /** Inject the failed notification in the fallback mechanism to figure out if a fallback notification SHALL be invoked */
                boolean result = FallbackHandler.get().fallback(Constants.NTF_SMS, event);
                if (!result) {
                    log.error("Fallback mechanism was not triggered because of queue full, SMS notification failed for subscriber " + event.getRecipient());
                }
            }
        }

        if (fe.getBestResultALevel() > reason) {
            fe.setBestResultALevel(reason);
            fe.setTypeALevel(notifType);
            fe.setFailedMessageALevel(msg);
        }


        if (fe.decCount() == 0) {
            userDone(user, false);
        }
    }

    /**
     * Should be called when all feedback for a user has been received.
     * Marks the user as done and checks if the entire NotificationGroup is done.
     *@param user the user that is done.
     *@param forceOk true if userDone was confirmed by an OK notification succeeded.
     *       Then the whole group is ok and we do not have to care about earlier (and future) results.
     */
    private void userDone(UserInfo user, boolean forceOk) {
        //If an output interface has succeeded (i.e. OK), no event created in MER.
        if (!forceOk) {
            // In all other cases (RETRY, FAILED and EXPIRED), action must be taken
            // and an event is created in MER.
            // If all output interfaces reported failure, the latest one is reported.
            FeedbackEntry fe = users.get(user);
            if (fe == null) { return; } //Some interfaces reported OK, so the user is already done
            result = fe.getBestResultALevel();

            // The remaining checks is to determine the kind of report
            // and the message to send with it, note that the type and
            // message is approximations anyway since they only tell
            // about the last received notification with a failure.
            try {
            if (fe.getBestResultALevel() == Constants.FEEDBACK_STATUS_FAILED) {
                if (merEvent) {
                    if( email.isSlamdown() ) {
                        mer.slamdownInfoFailed(user.getTelephoneNumber(), fe.getTypeALevel(), fe.getFailedMessageALevel() );
                    } else if(email.isFaxDeliveryReceipt()){
                        mer.faxprintRecieptNotificationFailed(user.getTelephoneNumber(),  fe.failedMessageALevel,Constants.NTF_FAX_RECEIPT_SMS );
                    } else if(email.isAutoUnlockPinL2()) {
                        mer.aupSmsFailed(user.getTelephoneNumber(), fe.getFailedMessageALevel());
                    } else {
                        mer.notificationFailed(user.getTelephoneNumber(), fe.getTypeALevel(), fe.getFailedMessageALevel());
                    }
                }
            } else if (fe.getBestResultALevel() == Constants.FEEDBACK_STATUS_EXPIRED) {
                if (merEvent) {
                    if( email.isSlamdown() ) {
                        mer.slamdownInfoExpired(user.getTelephoneNumber(), fe.getTypeALevel() );
                    } else if(email.isFaxDeliveryReceipt()){
                        mer.faxprintRecieptNotificationExpired(user.getTelephoneNumber() ,Constants.NTF_FAX_RECEIPT_SMS);
                    } else if(email.isAutoUnlockPinL2()) {
                        mer.aupSmsExpired(user.getTelephoneNumber());
                    } else {
                        mer.notificationExpired(user.getTelephoneNumber(), fe.getTypeALevel() );
                    }

                }
            }
            } catch (Exception e) {
                log.error("Unable to generate MDRs, keep processing the notification for " + user.getTelephoneNumber());
            }
        } else {
            result = Constants.FEEDBACK_STATUS_OK;
        }

        users.remove(user); //The user may already be removed, but that is OK.
        checkAllDone();
    }

    /**
     * Checks if all users (for a given notification) are done, and send the appropriate feedback.
     */
    private void checkAllDone() {
        if (done) { return; }

        if (!isNotificationProcessedAtNtfLevel3) {
            log.debug("NotificationGroup - checkAllDone: " + (moreUsers ? "more" : "no more") + " users, " + users.size() + " pending");
        }

        if (!moreUsers && users.isEmpty()) {
            done = true;
            if (result == Constants.FEEDBACK_STATUS_RETRY) {
                eventHandler.notifRetry(email.getNtfEvent());
            } else if (result == Constants.FEEDBACK_STATUS_FAILED) {
                eventHandler.notifFailed(email.getNtfEvent());
            } else {
                eventHandler.notifCompleted(email.getNtfEvent());
            }
            timer.cancel();
            release();
        }
    }

    /**
     * Signal that a new NotificationGroup has been created.
     */
    private static synchronized void create() {
        created++;
    }

    /**
     * Signal that a NotificationGroup is no longer used.
     */
    private static synchronized void release() {
        released++;
    }

    /** getFailed tells how many notification emails have failed and been
     *discarded by NTF.
     *@return number of notification that NTF has discarded.
     */
    public static int getFailed() {
        return failed;
    }

    /** getCreated tells how many notification emails have been set for retry.
     *@return number of retried notification emails.
     */
    public static int getRetried() {
        return retried;
    }

    /** getCreated tells how many notification emails have entered NTF.
     *@return number of notification that NTF has started handling.
     */
    public static int getCreated() {
        return created;
    }

    /**
     * getReleased tells how many notification emails NTF have completed
     * handling.
     *@return number of notifications that NTF has completed handling.
     */
    public static int getReleased() {
        return released;
    }

    /**
     * Create a string with the user information of this NotificationGroup.
     *@return a one-line representation of this NotificationGroup.
     */
    public String toString() {
        String s = "";

        for (Enumeration<UserInfo> e = users.keys(); e.hasMoreElements();) {
            UserInfo key = e.nextElement();
            FeedbackEntry fe = users.get(key);
            s += key.getTelephoneNumber() + " " + fe.toString();
        }
        return "{NotificationGroup: " + s + "}";
    }

    /**
     * FeedbackEntry keeps feedback information for one user.
     */
    private class FeedbackEntry {
        /** Number of remaining feedback calls. */
    	private int count;
        /** Best result for A level notifications.*/
    	private int bestResultALevel;
        /** Output interface for the last failure, A level */
    	private int typeALevel;
        /** Message for the latest failure, A Level, Remembered for use in MER event.*/
    	private String failedMessageALevel;
        /** Temporary count */
    	private int tempCount;

        FeedbackEntry() {
            count = 0;
            bestResultALevel = Constants.FEEDBACK_STATUS_UNKNOWN;
            typeALevel = 0;
            failedMessageALevel = null;
            tempCount = 0;
    	}

    	public void AddtempCount(int c) {
    		tempCount+=c;
    	}

    	public int getCount() {
    		if (count >= 0)
    		{return count;}
    		else
    		{return 0;}
    	}

    	public void updateCount() {
    		count+=tempCount;
    	}

    	public void addcount(int c) {
    		count+=c;
    	}

    	public int decCount() {
    		count--;
    		if (count >= 0)
    		{return count;}
    		else
    		{return 0;}
    	}

    	public int getBestResultALevel() {
    		return bestResultALevel;
    	}

    	public void setBestResultALevel(int bestResultALevel) {
    		this.bestResultALevel = bestResultALevel;
    	}

    	public int getTypeALevel() {
    		return typeALevel;
    	}

    	public void setTypeALevel(int typeALevel) {
    		this.typeALevel = typeALevel;
    	}

    	public String getFailedMessageALevel() {
    		return failedMessageALevel;
    	}

    	public void setFailedMessageALevel(String failedMessageALevel) {
    		this.failedMessageALevel = failedMessageALevel;
    	}

    	public String toString()
    	{
    		StringBuilder s = new StringBuilder();
    		s.append("count: ").append(count)
    		.append(" A: ").append(bestResultALevel)
    		.append(" T: ").append(typeALevel);
    		return s.toString();
        }

    }

    private void forceCleanup() {
        if (!(!moreUsers && users.isEmpty())) {
            noMoreUsers();
            Iterator<UserInfo> iter = users.keySet().iterator();
            while (iter.hasNext()) {
                UserInfo info = iter.next();
                userDone(info,true);
            }
            log.warn("NotificationGroup: Notifications still pending when forced closed, forcing finish");
        }

    }

    protected void finalize() throws Throwable {
         try {
           forceCleanup();
         } finally {
             super.finalize();
         }
     }
}
