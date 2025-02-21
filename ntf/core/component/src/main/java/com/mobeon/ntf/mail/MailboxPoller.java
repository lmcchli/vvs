 /**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail;

 import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotifCompletedListener;
import com.mobeon.ntf.mail.imap.ImapClient;
import com.mobeon.ntf.management.ManagementCounter;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.util.threads.NtfThread;
import com.mobeon.ntf.util.time.NtfTime;
import org.eclipse.angus.mail.imap.IMAPFolder;

 import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.OrTerm;
import jakarta.mail.search.SearchException;

import java.util.*;

/**
 * MailboxPoller polls one mailbox for new mail.
 * it also handles all other aspects of the mailbox, such as deleting messages
 * that have been processed and retrying failed messages.
 * It is implemented for IMAP mailboxes. If more mailbox types should be needed,
 * this class should be separated into one MailboxPoller with general
 * functionality, and one IMAPMailboxPoller that knows about IMAP-specific
 * things.
 *
 * MailboxPoller marks messages that shall be retried by setting their
 * <I>FLAGGED</I> flag, and retries when the time comes by clearing their
 * <I>SEEN</I> flag.
 * <BR>
 *
    State of this thread<UL>
        <LI>0 - Not running
        <LI>1 - Run called
        <LI>2 - Sleeping
        <LI>3 - Waiting
        <LI>4 - Joining
        <LI>5 - Run exited
        <LI>6 - Flagging deleted
        <LI>7 - Expunging
        <LI>8 - Looking for new notifications
        <LI>9 - Forwarding new notifications to notification processor
        <LI>10 - Cleaning very old messages
        <LI>11 - In delete-flag loop
        <LI>12 - In very old loop
        <LI>13 - Deleting a very old message
        <LI>14 - Searching unflagged
        <LI>15 - In handle new notif loop
        <LI>16 - Forwarding new message
        <LI>17 - Size of new message
        <LI>19 - Parsing new message
        <LI>20 - Putting new message
        <LI>21 - Sending to postmaster
        </UL>
    */
public class MailboxPoller extends NtfThread {

    /** Log file handler*/
    private final static Logger log = Logger.getLogger(MailboxPoller.class);
    private ManagementCounter newCounter = null;
    private ManagementCounter retryCounter = null;
    /** The next step in the flow of processing notifications, where we
        send notifications */
    private EmailStore nextNotifProcessor;

    /** Number of messages left to delete before an expunge is needed */
    private int deletesToExpunge;

    /** The last time an expunge operation was done*/
    private int lastExpungeTime;

    /** Number of instances that have been created from this class */
    private int instanceNumber;

    /** If traffic is low, the run method sleeps until nextWakeupTime */
    private int nextWakeupTime;

    /** The next time a check for very old messages shall be done*/
    private int nextVeryOldTime;

    /** The next time to retry failed messages*/
    private int nextRetryTime;

    /** True during the time when NTF processes failed messages in addition to
     * new ones */
    private boolean retryNow;

    /** The NotifCompletedListener is notified when a very old mail is found.*/
    private NotifCompletedListener completedListener;

    /** The MailMemoryManager is notified when memory for mail is needed or
     * released.*/
    private MailMemoryManager memoryManager;

    private EmailListHandler emailListManager;

    /** When the MailboxPoller is started, it shall look for all notification
        emails. After that, it shall only look for unseen notifications.
        firstTimeSinceStart remembers the state. */
    private boolean firstTimeSinceStart = true;

    /** completedNotif contains completed notifications that can be
        delete-flagged */
    private Vector<Integer> completedNotif = new Vector<Integer>();

    /**
     * unseeMessages contains messages that shall be marked as unseen
     */
    private Vector<Integer> unseeMessages = new Vector<Integer>();

    /** Inbox to poll for notifications*/
    private Folder inbox = null;

    /**Referense to mailstorepoller*/
    private MailstorePoller msp;

    /** Client for communicating with the IMAP server */
    private ImapClient client;

    /** Unique log id for reduced logging */
    private int memoryFullLogId;

    /** The sequence number where the search for new mails shall start
        the next time */
    private int nextSeq;

    /** The condition of the connection to MS */
    private boolean connectionStatus = true;

    private LinkedList<Integer> retriedMessages;

    /* A table to store message ID for messages that are forwarded to method
     * forwardNotif()
     */
    private static Hashtable<String, Integer> messageIDs = new Hashtable<String, Integer>();

    private static boolean runCleanMessageIDs = true;

    /** Search term to find unseen messages */
    private static final AndTerm normalNew =
        new AndTerm
        (new FlagTerm(new Flags(Flags.Flag.DELETED), false), //Not deleted
         new FlagTerm(new Flags(Flags.Flag.SEEN), false)); // and unseen
    /** Search term to find unseen messages and messages flagged for retry */
    private static final AndTerm retryNew =
        new AndTerm
        (new FlagTerm(new Flags(Flags.Flag.DELETED), false), //Not deleted
         new OrTerm
         (new FlagTerm(new Flags(Flags.Flag.SEEN), false), // and unseen
          new FlagTerm(new Flags(Flags.Flag.FLAGGED), true))); //or retry

    public MailboxPoller(ThreadGroup imapThreads,
                         String threadName){
        super(threadName);
    }
    /**
     * Constructor.
     *@param imapThreads the thread group for this poller thread.
     *@param notifProcessor the NTF part that shall process the found mail.
     *@param instanceNumber id for this MailboxPoller instance.
     *@param ncl is requested to delete any "very old messages" found.
     *@param mmm manages the amount of memory available for mail.
     */
    public MailboxPoller(ThreadGroup imapThreads,
                         EmailStore notifProcessor,
                         int instanceNumber,
                         NotifCompletedListener ncl,
                         MailMemoryManager mmm,
                         EmailListHandler sm,
                         MailstorePoller msp) {
        super("MailboxPoller-" + instanceNumber);
        this.instanceNumber = instanceNumber;
        completedListener = ncl;
        memoryManager = mmm;
        emailListManager = sm;
        nextNotifProcessor = notifProcessor;
        this.msp = msp;
        client = new ImapClient(instanceNumber, this);
        memoryFullLogId = log.createMessageId();
        //nextVeryOldTime = NtfTime.now + Config.getVeryOldMessage() / 10;
        nextRetryTime = NtfTime.now + 1;//Config.getRetryInterval();
        //retryCounter = ManagementInfo.get().getNotifForRetry("MailboxPoller-" + instanceNumber);
        //newCounter = ManagementInfo.get().getNotifInQueue("New-" + instanceNumber);
        retriedMessages = new LinkedList<Integer>();
        // This class is not used anymore but kept as historical reference
/*
        if(Config.isMessageIDValidateOn()) {
            CleanMessageIDs.startThread();
        }
        */
    }

    /**
     * Used for Junit test only.
     * Starts the CleanMessageIDs thread.
     */
    public static void startCleanMessageIDs() {
        CleanMessageIDs.startThread();
    }

    /**
     * Used for Junit test only.
     * Stops the CleanMessageIDs thread.
     */
    public static void stopCleanMessageIDs() {
        runCleanMessageIDs = false;
    }

    /**
     * Used for Junit test only
     */
    public static void testSetMessageID(String messageId) {
        setMsgNotif(messageId);
    }

    /**
     * Used for Junit test only
     */
    public static boolean testCheckMessageID(String msgid) {
        return isMsgNotif(msgid);
    }
    /**
     * MailstorePoller tells MailboxPoller that a notification is completed, so
     * the mail can be deleted.
     *@param msgId the UID of the completed message.
     */
    public void notifCompleted(int msgId) {
        completedNotif.add(new Integer(msgId));
    }

    /**
     * MailstorePoller tells MailboxPoller that a notification is completed, so
     * the mail can be deleted.
     *@param msg the completed message.
     */
    public void notifCompleted(Message msg) {
        long uid;

        if (inbox == null) { inbox = client.getFolder(); }
        try {
            uid = ((IMAPFolder) inbox).getUID(msg);
            notifCompleted((int) uid);
        } catch (MessagingException e) {
            log.logMessage("MailboxPoller: exception when getting message UID " + e, log.L_VERBOSE);
            msp.reportError("notifCompleted() getting message UID", e, false);
            inbox = null;
        }
    }

    /**
     * Tells the mailboxPoller that a notification needs to be retried at a
     * later time.
     *@param uid the UID of the message.
     *@param retryAddresses the receivers of the notification that have not yet
     * been notified.
     */
    public synchronized void notifRetry(int uid, String retryAddresses) {

        log.logMessage("Message " + uid + " to " + retryAddresses + " will be retried", Logger.L_DEBUG);
        if( retryNow ) {
            retriedMessages.addLast(new Integer(uid));
        } else {
            try {
                if (inbox == null) { inbox = client.getFolder(); }

                Message m = ((IMAPFolder) inbox).getMessageByUID(uid);
                if (m != null) {
                    m.setFlag(Flags.Flag.FLAGGED, true);
                }


            } catch (IllegalWriteException e) {
                log.logMessage("Failed to set retry receivers, store does not implement modification of existing headers. " + e, Logger.L_ERROR);
                inbox = null;
            } catch (IllegalStateException e) {
                log.logMessage("Failed to set retry receivers, read-only folder. " + e, Logger.L_ERROR);
                inbox = null;
            } catch (MessagingException e) {
                log.logMessage("Failed to set retry receivers. " + e, Logger.L_VERBOSE);
                msp.reportError("notifRetry()", e, false);
                inbox = null;
            }
        }

    }

    /**
     * Tells that a mail should be marked as unseen so it will be found again and be notified on.
     * @param uid the UID of the message.
     */
    public void notifRenew(int uid) {
        unseeMessages.add(new Integer(uid));
    }


    /**
     *marks messages to flagged in the store.
     *
     */
    private synchronized void setRetryFlags() {
        try {
            if (inbox == null) { inbox = client.getFolder(); }
            Iterator<Integer> iter = retriedMessages.iterator();
            while( iter.hasNext() ) {
                Integer integer = iter.next();
                int uid = integer.intValue();

                Message m = ((IMAPFolder) inbox).getMessageByUID(uid);
                if (m != null) {
                    m.setFlag(Flags.Flag.FLAGGED, true);
                }
            }

        } catch (IllegalWriteException e) {
            log.logMessage("Failed to set retry receivers, store does not implement modification of existing headers. " + e, Logger.L_ERROR);
            inbox = null;
        } catch (IllegalStateException e) {
            log.logMessage("Failed to set retry receivers, read-only folder. " + e, Logger.L_ERROR);
            inbox = null;
        } catch (MessagingException e) {
            log.logMessage("Failed to set retry receivers. " + e, Logger.L_VERBOSE);
            msp.reportError("setRetryFlags() failed", e, false);
            inbox = null;
        }

        retriedMessages.clear();
        retryNow = false;
    }


    public void ntfThreadInit() {
        unseeMessages(); //Make old, half-processed messages
        //new again at start-up
    }

    public boolean ntfRun() {
        nextWakeupTime = NtfTime.now + 1;//Config.getImapPollInterval();
        boolean foundNewMail = false;
        nextSeq = 1; //Start searching from the start of inbox
        do {
            try {
                handleVeryOld();
                handleDelete();
                handleExpunge();
                handleRetry();
                handleUnseeMessages();
                foundNewMail = handleNewNotif();

            } catch (Exception e) {
                log.logMessage("MailboxPoller got unexpected: " + NtfUtil.stackTrace(e), Logger.L_VERBOSE);
                msp.reportError("ntfRun()", e, true);
                inbox = null;
            }
        } while (foundNewMail);
        setRetryFlags();
        if (client.getConnectionAge() > 1) {//Config.getImapMaxConnectionTime()) {
            log.logMessage("Periodic refresh of IMAP connection", Logger.L_VERBOSE);
            inbox = client.getFolder(); //Reconnect periodically
        }
        try {
            sleep(Math.max(1000 * (nextWakeupTime - NtfTime.now), 50));
        } catch (InterruptedException e) { ; }
        return false;
    }

    public boolean shutdown() {
        nextWakeupTime = NtfTime.now + 1;//Config.getImapPollInterval();
        try {
            handleVeryOld();
            handleDelete();
            handleExpunge();

        } catch (Exception e) {
            log.logMessage("MailboxPoller got unexpected exception: " + NtfUtil.stackTrace(e), Logger.L_ERROR);
            inbox = null;
        }
        try {
            sleep(Math.max(1000 * (nextWakeupTime - NtfTime.now), 50));
        } catch (InterruptedException e) { ; }
        return (memoryManager.getMailMemoryCount() == 0);
    }

    /**
     * unseeMessages is called at start-up to make messages seen by previous
     * invocations of NTF messages unseen again, so this NTF invocation will
     * process them. This is necessary, since if they are seen but not deleted,
     * the previous NTF invocation did not complete these notifications.
     */
    private void unseeMessages() {
        int count; //Total number of messages to process
        int i = 0;
        Message[] msgs; //holds a temporary batch of Messages
        Message msg; //Message being processed
        int next = 0; //Number of the first message in the next batch
        int batchSize; //Number of messages to process in this batch
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.FLAGS);

        log.logMessage("Making all messages unseen", log.L_DEBUG);
        try {
            if (inbox == null) { inbox = client.getFolder(); }
        }
        catch(NullPointerException npe) {
            //This just happens in the list handler and is not relevant
            return;
        }
        try {
            count = inbox.getMessageCount();
            log.logMessage("Starting with " + count + " messages in the inbox.", Logger.L_VERBOSE);
        } catch (MessagingException e) {
            log.logMessage("Exception counting messages: " + e, Logger.L_VERBOSE);
            msp.reportError("unseeMessages() counting messages", e, false);
            inbox = null;
            count = 0;
        }
        while (next < count) {
            if (inbox == null) { inbox = client.getFolder(); }

            batchSize = java.lang.Math.min(count - next, 1);//Config.getImapBatchSize());
            try {
                msgs = inbox.getMessages(next + 1 /*Number 1 and up*/, batchSize + next);

                inbox.fetch(msgs, fp);
                batchSize = msgs.length; //Update, in case we got fewer messages than requested
                for (i = 0; i < batchSize; i++) {
                    msg = msgs[i];
                    if ((msg != null)
                        && !msg.isExpunged()
                        && !msg.isSet(Flags.Flag.DELETED)) {
                        msg.setFlag(Flags.Flag.SEEN, false);
                        msg.setFlag(Flags.Flag.FLAGGED, false);
                    }
                }
            } catch (IllegalStateException e) {
                log.logMessage("MailboxPoller: inbox not open when fetching messages " + e, Logger.L_VERBOSE);
                msp.reportError("unseeMessages() getting messages in wrong state", e, false);
                inbox = null;
            } catch (MessagingException e) {
                log.logMessage("MailboxPoller: exception when fetching messages " + e, Logger.L_VERBOSE);
                msp.reportError("unseeMessages() getting messages", e, false);
                inbox = null;
            }
            next += i; //Normally i=batchSize, but not if an exception was thrown
        }
    }

    /**
     *handleVeryOld looks for very old messages that NTF has left behind
     *in the inbox. Such messages are deleted with an error message in the log
     *file. NTF should not leave any messages but if it does, this function
     *will ensure that the mailboxes will not grow forever.
     */
    //A first attempt to search using the SearchTerm ReceivedDateTerm failed,
    //since the comparisons LT and NE never found any messages, while the others
    //always found all messages.
    private void handleVeryOld() {
        if (NtfTime.now < nextVeryOldTime) { return; }

        //nextVeryOldTime = NtfTime.now + Config.getVeryOldMessage() / 10;

        FlagTerm seen = new FlagTerm(new Flags(Flags.Flag.SEEN), true);
        FlagTerm undeleted = new FlagTerm(new Flags(Flags.Flag.DELETED), false);
        AndTerm flags = new AndTerm(seen, undeleted);
        Date oldDate = new Date((NtfTime.now - 1) * 1000L); //Config.getVeryOldMessage()
        Message[] oldMessages;
        int uid;

        log.logMessage("Cleaning the inbox from very old messages received before "
                       + oldDate, Logger.L_VERBOSE);

        if (inbox == null) { inbox = client.getFolder(); }
        try {
            oldMessages = inbox.search(flags);
            for (int i = 0; i < oldMessages.length; i++) {
                if ((oldMessages[i].getReceivedDate()).before(oldDate)) {
                    try {
                        if ((oldMessages[i] != null)
                            && (!oldMessages[i].isExpunged())) {
                            uid = (int) (((IMAPFolder) inbox).getUID(oldMessages[i]));
                            log.logMessage("Deleting very old message: " + uid
                                           + " received on "
                                           + oldMessages[i].getReceivedDate(),
                                           Logger.L_ERROR);
                            completedListener.notifCompleted(uid, instanceNumber);
                        }
                    } catch (MessagingException e) {
                        log.logMessage("Failed to get UID for very old message " + e, Logger.L_ERROR);
                        inbox = null;
                    }
                }
            }
        } catch (SearchException e) {
            log.logMessage("Failed to search very old messages " + e, Logger.L_ERROR);
        } catch (IllegalStateException e) {
            log.logMessage("Failed to search very old messages " + e, Logger.L_VERBOSE);
            msp.reportError("handleVeryOld() search in wrong state", e, false);
            inbox = null;
        } catch (MessagingException e) {
            log.logMessage("Failed to search very old messages " + e, Logger.L_VERBOSE);
            msp.reportError("handleVeryOld() failed to search", e, false);
            inbox = null;
        }
    }

    /**
     * Delete all messages in the delete queue.
     */
    private void handleDelete() {
        int uid;

        if (completedNotif.isEmpty()) { return; }

        log.logMessage("Deleting " + completedNotif.size() + " messages", Logger.L_DEBUG);
        if (inbox == null) { inbox = client.getFolder(); }
        try {
            while (!completedNotif.isEmpty()) {
                uid = completedNotif.firstElement().intValue();

                // get a reference to the message
                Message msg = ((IMAPFolder) inbox).getMessageByUID(uid);

                if (msg == null) {
                    log.logMessage(": failed to set delete flag, message-id not found."
                                   , Logger.L_VERBOSE);
                } else if (msg.isExpunged()) {
                    log.logMessage(": failed to set delete flag, message already expunged."
                                   , Logger.L_VERBOSE);
                } else /*Normal case*/ {
                    log.logMessage("Setting delete flag for " + uid, Logger.L_DEBUG);
                    msg.setFlag(Flags.Flag.DELETED, true);
                }

                --deletesToExpunge;
                completedNotif.removeElementAt(0);
            }
        } catch (MessagingException e) {
            log.logMessage(": exception when setting delete flag " + e, Logger.L_VERBOSE);
            msp.reportError("handleDelete() setting delete flag", e, false);
            inbox = null;
        }
    }

    /**
     * Expunge deleted message if time has come or many messages have
     * been deleted.
     */
    private void handleExpunge() {
        if (deletesToExpunge <= 0
            || NtfTime.now > lastExpungeTime
            + Config.getMaxTimeBeforeExpunge()) {
            log.logMessage("Expunging", Logger.L_DEBUG);
            if (inbox == null) { inbox = client.getFolder(); }

            try {
                client.expunge();
                // This class is not used anymore but kept as historical reference
                deletesToExpunge = 1;//Config.getDeletesBeforeExpunge();
                lastExpungeTime = NtfTime.now;
            } catch (MessagingException e) {
                log.logMessage(": exception while doing expunge: " + e, Logger.L_VERBOSE);
                msp.reportError("handleExpunge()", e, false);
                inbox = null;
            }
        }
    }


    /**
     * handleRetry looks for flagged messages, and make the ready for retry by
     * clearing their seen flag.
     */
    private void handleRetry() {
        if (NtfTime.now < nextRetryTime) { return; }

        log.logMessage("Activating retry of failed messages", Logger.L_DEBUG);
        nextRetryTime = NtfTime.now + 1;//+ Config.getRetryInterval();

        retryNow = true;
    }


    private void handleUnseeMessages() {
        int uid;

        if (unseeMessages.isEmpty()) { return; }

        log.logMessage("Marking " + unseeMessages.size() + " messages as unseen", Logger.L_DEBUG);
        if (inbox == null) { inbox = client.getFolder(); }
        try {
            while (!unseeMessages.isEmpty()) {
                uid = unseeMessages.firstElement().intValue();

                // get a reference to the message
                Message msg = ((IMAPFolder) inbox).getMessageByUID(uid);

                if (msg == null) {
                    log.logMessage(": failed to remove seen flag, message-id not found."
                                   , Logger.L_VERBOSE);
                } else if (msg.isExpunged()) {
                    log.logMessage(": failed to remove seen flag, message already expunged."
                                   , Logger.L_VERBOSE);
                } else /*Normal case*/ {
                    log.logMessage("Removing seen flag for " + uid, Logger.L_DEBUG);
                    msg.setFlag(Flags.Flag.SEEN, false);
                }

                unseeMessages.removeElementAt(0);
            }
        } catch (MessagingException e) {
            log.logMessage(": exception when removing seen flag " + e, Logger.L_VERBOSE);
            msp.reportError("handleUnseeMessages() removing seen flag", e, false);
            inbox = null;
        }
    }

    /**
     * Handle new mails.
     *@return true if new mails were found.
     */
    private boolean handleNewNotif() {
        Message[] unseenMsg = null;
        int i;
        int count = 0; //Number of messages in inbox

        if (inbox == null) { inbox = client.getFolder(); }
        try {
            count = inbox.getMessageCount();
            try {
                int unreadCount = inbox.getUnreadMessageCount();
                int deleteCount = inbox.getDeletedMessageCount();
                newCounter.set(unreadCount);
                retryCounter.set(count - (unreadCount+deleteCount));
            } catch (MessagingException me) {
                log.logMessage("MailboxPoller.handleNewNotif" + me, Logger.L_VERBOSE);
                msp.reportError("handleNewNotif() counting messages", me, false);
            }
            if (count <= 1) { //Config.getImapBatchSize()) {
                unseenMsg = serverSearchNew(count);
            } else {
                unseenMsg = ntfSearchNew(count);
            }
            msp.reportCountStatus(instanceNumber, unseenMsg.length, count);
        } catch (MessagingException e) {
            log.logMessage("MailboxPoller: exception doing message count " + e, Logger.L_VERBOSE);
            msp.reportError("handleNewNotif() searching messages", e, false);
            inbox = null;
        }

        if (unseenMsg != null && unseenMsg.length > 0) {
            log.logMessage("Processing " + unseenMsg.length + " new messages", Logger.L_DEBUG);

            for (i = 0; i < unseenMsg.length; i++) {
                    if (!forwardNotif(unseenMsg[i])) {
                        break; //Folder closed, message objects invalid
                    }
            }
            return (unseenMsg.length > 0);
        }
        return false;
    }

    /**
     * The mailbox is not so full, so let the server search for new messages.
     *@param count the total number of mails in the mailbox.
     *@return an array with the new messages.
     */
    private Message[] serverSearchNew(int count) {
        // UID is defined as long (64 bits) in JavaMail.
        // UID is 32 bits in RFC2060, but to uniqly identify a message over multiple
        // sessions a combination of UID and UIDVALIDITY (32 bits) is needed (64 bits).
        // NSSDK never uses UIDVALIDITY and only uses 32 bits for UID.
        // since NSSDK is used for the MIME message, uid is cast to int before
        // it is returned to NSSDKMailboxConnection.
        Message[] msgs;

        if (inbox == null) { inbox = client.getFolder(); }

        try {
            if (retryNow) {
                msgs = inbox.search(retryNew);
            } else {
                msgs = inbox.search(normalNew);
            }
            return msgs;
        } catch (SearchException e) {
            log.logMessage("Server-search new messages failed " + e, Logger.L_ERROR);
        } catch (IllegalStateException e) {
            log.logMessage("Server-search new messages failed " + e, Logger.L_VERBOSE);
            msp.reportError("serverSearchNew() in wrong state", e, false);
        } catch (MessagingException e) {
            log.logMessage("Server-search new messages failed " + e, Logger.L_VERBOSE);
            msp.reportError("serverSearchNew()", e, false);
        }

        inbox = null;
        return new Message[0];
    }

    /**
     * There are many mails in the inbox, so read them a few at a time and keep
     * the new ones until enough new messages have been found.
     *@param count the total number of mails in the mailbox.
     *@return an array with the new messages.
     */
    private Message[] ntfSearchNew(int count) {
        // UID is defined as long (64 bits) in JavaMail.
        // UID is 32 bits in RFC2060, but to uniqly identify a message over multiple
        // sessions a combination of UID and UIDVALIDITY (32 bits) is needed (64 bits).
        // NSSDK never uses UIDVALIDITY and only uses 32 bits for UID.
        // since NSSDK is used for the MIME message, uid is cast to int before
        // it is returned to NSSDKMailboxConnection.
        long uid;
        int i = 0;
        Vector<Message> found = new Vector<Message>();
        Message[] msgs; //holds a temporary batch of Messages
        int batchSize;
        // Use FetchProfile to specify what items you want to retrieve
        FetchProfile fp = new FetchProfile();
        fp.add(UIDFolder.FetchProfileItem.UID);
        fp.add(FetchProfile.Item.FLAGS);

        while (nextSeq <= count && found.size() < 1//Config.getImapBatchSize()
               && isRunning()) {
            if (inbox == null) { inbox = client.getFolder(); }

            batchSize = java.lang.Math.min
                (count - (nextSeq - 1), 1);//Config.getImapBatchSize());
            try {
                //Scan a batch of messages and collect the unseen ones in found
                msgs = inbox.getMessages(nextSeq, batchSize + nextSeq - 1);
                inbox.fetch(msgs, fp);
                batchSize = msgs.length; //Update, in case we got fewer messages than requested
                if (batchSize == 0) { break; } //We can not loop around reading 0 messages
                i = 0;
                while (i < batchSize && found.size() < 1//Config.getImapBatchSize()
                       && isRunning()) {
                    if ((msgs[i] != null)
                        && (!msgs[i].isExpunged())
                        && (!msgs[i].isSet(Flags.Flag.DELETED))) {
                        if (retryNow && msgs[i].isSet(Flags.Flag.FLAGGED)) {
                            msgs[i].setFlag(Flags.Flag.FLAGGED, false);
                            msgs[i].setFlag(Flags.Flag.SEEN, false);
                        }
                        if (!msgs[i].isSet(Flags.Flag.SEEN)) {
                            uid = ((IMAPFolder) inbox).getUID(msgs[i]);
                            found.add(msgs[i]);
                        }
                    }
                    ++i;
                }
            } catch (IllegalStateException e) {
                log.logMessage("MailboxPoller: inbox not open when fetching messages " + e, Logger.L_VERBOSE);
                msp.reportError("ntfSearchNew() fetching messages in wrong state", e, false);
                inbox = null;
            } catch (MessagingException e) {
                log.logMessage("MailboxPoller: exception when fetching messages " + e, Logger.L_VERBOSE);
                msp.reportError("ntfSearchNew() fetching messages", e, false);
                inbox = null;
            }
            nextSeq += i; //If loop was interrupted by exception or found filled
        }
        msgs = new Message[found.size()];
        for (int q = 0; q < found.size(); q++) {
            msgs[q] = found.get(q);
        }
        return msgs;
    }


    private boolean isRunning() {
        return !ManagementInfo.get().isExit()
            && ManagementInfo.get().isAdministrativeStateUnlocked();
    }

    public void reportError(String msg, Exception e, boolean unexpected) {
        msp.reportError(msg, e, unexpected);
    }

    public void statusDown() {
        if( connectionStatus == true ) {
            connectionStatus = false;
            msp.notifyStatus();
        }
    }

    public void statusUp() {
        if( connectionStatus == false ) {
            connectionStatus = true;
            msp.notifyStatus();
        }
    }

    public boolean getStatus() {
        return connectionStatus;
    }


    public boolean forwardNotif(Message msg) {
        return forwardNotif(msg, 0);
    }

    /**
     * Forwards a notification to the next step in the processing chain.
     * It also wraps the message in an NSSDKNotificationEmail object.
     *@param msg The new notification
     *@return true iff the message was properly handled or was impossible to handle.
     */
    public boolean forwardNotif(Message msg, int msgUid) {
        NotificationEmail email = null;
        int uid = msgUid;
        int messageSize;

        if (msg == null) {
            log.logMessage("Can not forward a null message to the notification processor", Logger.L_ERROR);
            return true;
        }

        if (inbox == null && msgUid != 0) { inbox = client.getFolder(); }

        try {
            if( uid == 0 ) {
                uid = (int) (((IMAPFolder) inbox).getUID(msg));
            }
            messageSize = msg.getSize();
            if (messageSize < 0) {
                //Guess it means the folder is inaccessible. Check it by trying
                //a dummy operation on the folder to provoke an Exception.
                log.logMessage("Message size could not be determined", Logger.L_ERROR);
                Folder dummy = msg.getFolder();
                if (dummy != null && dummy.hasNewMessages()) {
                    log.logMessage("Could execute operation on the inbox", Logger.L_VERBOSE);
                }

                //Too bad, no Exception, then we dont not know what causes the
                //unknown size, so just guess a really big message size and go on.
                log.logMessage("Unknown size was NOT caused by lost connection", Logger.L_ERROR);
                //messageSize = (int) (Config.getMailMemoryMegabyte() / 3);
            }
            int resp = memoryManager.reserveMemory(instanceNumber, uid, messageSize);
            switch (resp) {
            case MailstorePoller.yes:
                if (log.isActive(memoryFullLogId)) {
                    log.logReducedOff(memoryFullLogId, "OK Memory available again", Logger.L_VERBOSE);
                }
                break; //Memory available, just carry on
            case MailstorePoller.no:
                log.logReduced(memoryFullLogId, "Currently not enough memory to process mail", Logger.L_VERBOSE);
                Thread.sleep(500);
                return true; //Not enough memory now, skip this message
            case MailstorePoller.never:
                log.logMessage("Mail " + uid + " in gnotification mailbox " + instanceNumber + "is too large to handle", Logger.L_ERROR);
                sendMailTooLargeMessage(uid, messageSize);
                msg.setFlag(Flags.Flag.SEEN, true);
                if (retryNow) { msg.setFlag(Flags.Flag.FLAGGED, false); }
                return true; //Never enough memory
            default:
                log.logMessage("Bad response from reserveMemory " + resp, Logger.L_ERROR);
            }
            /*
            This call is to be removed, we no longer need to support this
            email = new NotificationEmail(uid, messageSize, msg);
            if (Config.isMessageIDValidateOn() && isMsgNotif(email.getMessageId())) {
                //email.setDuplicated();
                 Don't remove if there is an autoforward address in the mail
                if(!isAutoforwardAddressInMail(email)) {
                    log.logMessage("Message with ID " + email.getMessageId() + " is a duplicate," +
                               " stop processing the message", log.L_VERBOSE);
                    msg.setFlag(Flags.Flag.DELETED, true);
                    memoryManager.releaseMemory(uid, instanceNumber);
                    return true;
                }
            }
            if (Config.isMessageIDValidateOn()) {
                setMsgNotif(email.getMessageId());
            }*/
            if(email.isSlamdown()) {
                log.logMessage("Forwarding slamdown message " + uid, Logger.L_DEBUG);
                //emailListManager.addEmailList(email.getSlamdownInfo());//not used anymore
                memoryManager.releaseMemory(uid, instanceNumber);
                msg.setFlag(Flags.Flag.DELETED, true);

            } else {

                if (email != null) {
                    log.logMessage("Forwarding " + messageSize + "-byte message " + uid, Logger.L_DEBUG);
                    nextNotifProcessor.putEmail(email);
                    msg.setFlag(Flags.Flag.SEEN, true);
                    if (retryNow) { msg.setFlag(Flags.Flag.FLAGGED, false); }
                }
            }
            return true;
        } catch (FolderClosedException e) {
            log.logMessage("MailboxPoller: exception when getting message data or properties " + e, Logger.L_VERBOSE);
            msp.reportError("forwardNotif() getting message data or properties from closed folder", e, false);
            return false;
        } catch (MessagingException e) {
            log.logMessage("MailboxPoller: exception when getting message data or properties " + e, Logger.L_VERBOSE);
            msp.reportError("forwardNotif() getting message data or properties", e, false);
            inbox = null;
            return true;
        } catch (Exception e) {
            log.logMessage("MailboxPoller got unexpected exception: " + NtfUtil.stackTrace(e), Logger.L_VERBOSE);
            msp.reportError("forwardNotif()", e, true);
            inbox = null;
            return true;
        }
    }

    /**
     * Sends a mail to the postmaster that the mailbox has a mail that can not
     * be parsed.
     *@param uid the UID of the bad mail.
     */
    private void sendParsingErrorNotification(int uid) {
        sendToPostmaster("Unparseable mail in notification mailbox " + instanceNumber,
                         "Mail " + uid + " in notification mailbox "
                         + instanceNumber
                         + " has a bad format, and can not be handled."
                         + "\n\nIt must be examined and manually deleted.", null);
    }


    /**
     * Send a mail to the postmaster about a mail that is too large to ever be
     * handled by NTF.
     *@param uid the UID of the big mail.
     *@param size the size of the big mail.
     */
    private void sendMailTooLargeMessage(int uid, int size) {
        sendToPostmaster("Too large mail in notification mailbox "
                         + instanceNumber,
                         "Mail " + uid + " in notification mailbox "
                         + instanceNumber + " is too large ("
                         + size
                         + " bytes).\n\nIt must be examined and manually deleted."
                         , null);
    }


    /**
     * Sends a mail to the postmaster.
     *@param subject subject of the new mail.
     *@param text body text of the new mail.
     *@param org the original mail the new mail is about.
     */
    private void sendToPostmaster(String subject, String text, Multipart org) {
        try {
            log.logMessage("Sending error mail to "// + Config.getImapPostmaster()
                           + " Subject='" + subject + "'", Logger.L_VERBOSE);
            MimeMessage msg =
                new MimeMessage(Session.getDefaultInstance(new Properties()));
            String hostname;
            try {
                //hostname = Config.getImapHost();
            } catch (Exception e) { hostname = "unknown"; }
            msg.setFrom(new InternetAddress("notification\u0040"));// + hostname));
            //msg.addRecipient(Message.RecipientType.TO, new InternetAddress(Config.getImapPostmaster()));
            msg.setSubject(subject);
            MimeMultipart body = new MimeMultipart();
            if (text != null) { //Add text to message body
                MimeBodyPart part = new MimeBodyPart();
                part.setText(text);
                body.addBodyPart(part);
            }
            if (org != null) { //Add original mail to message body
                MimeBodyPart part = new MimeBodyPart();
                part.setContent(org);
                body.addBodyPart(part);
            }
            msg.setContent(body);

            Transport.send(msg);
        } catch (MessagingException e) {
            //log.logMessage("Failed to send error message to postmaster" + Config.getImapPostmaster() + e, log.L_ERROR);
        }
    }

    private static boolean isMsgNotif(String messageId) {
        synchronized(messageIDs) {
            return messageIDs.containsKey(messageId);
        }
    }

    private static void setMsgNotif(String messageId) {
        synchronized(messageIDs) {
            int time = NtfTime.now;
            if (messageId != null && ! messageIDs.containsKey(messageId)) {
                messageIDs.put(messageId, new Integer(time));
                log.logMessage("Store Message-Id: " + messageId, Logger.L_DEBUG);
            }
        }
    }

    /*private boolean isAutoforwardAddressInMail(NotificationEmail email) {
        return email.getAutoForwardedAddresses().size() > 0 ? true : false;
    }*/

    private String arrayToString(String[] temp) {
        if (temp == null) { return null; }
        if (temp.length == 0) { return ""; }
        if (temp.length == 1) { return temp[0]; }

        String all = temp[0];
        for (int i = 1; i < temp.length; i++) {
            all += "," + temp[i];
        }
        return all;
    }

    private static class CleanMessageIDs extends Thread {
        private static CleanMessageIDs _cleanMsgIds = null;

        public CleanMessageIDs() {
            super("MailboxPoller:CleanMessageIDs");
        }

        public static void startThread() {
            if (_cleanMsgIds == null) {
                _cleanMsgIds = new CleanMessageIDs();
                _cleanMsgIds.start();
            }
        }

        public void run() {
            int deltaTime = 0;
            int maxStoreTime = 60; //sec
            int sleepTime = 1000; //millisec
            int imapPollInterval = 1;//Config.getImapPollInterval();

            if (imapPollInterval <= 60) {
                maxStoreTime = imapPollInterval/2 + 1;
            }

            log.logMessage("Thread started", Logger.L_VERBOSE );
            log.logMessage("Message IDs will be stored in " +
                            maxStoreTime + " seconds", Logger.L_DEBUG);
            Enumeration<String> msgIds = null;
            while( runCleanMessageIDs ) {
                try {
                    synchronized(messageIDs) {
                        msgIds = messageIDs.keys();

                        while(msgIds.hasMoreElements()) {
                            String messageId = msgIds.nextElement();
                            Integer storedTime = messageIDs.get(messageId);
                            deltaTime = NtfTime.now - storedTime.intValue();
                            if(deltaTime > maxStoreTime) {
                                messageIDs.remove(messageId);
                                log.logMessage("Delete Message-Id: " +
                                               messageId, Logger.L_DEBUG);
                            }
                        }
                    }
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    log.logMessage("MailboxPoller:CleanMessageIDs.run " + NtfUtil.stackTrace(e), Logger.L_VERBOSE );
                }
            }
            log.logMessage("Thread stopped", Logger.L_VERBOSE );
        }
    }

}
