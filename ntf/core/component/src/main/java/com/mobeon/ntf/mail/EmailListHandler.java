/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.mail;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.text.Template;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.management.ManagementCounter;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.time.NtfTime;
import com.mobeon.ntf.util.PersistentQueue;
import com.mobeon.ntf.util.PersistentObject;
import java.util.*;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;



/**
 * EmailListHandler polls a vector for new email from a list.
 * <BR>
 *
    State of this thread<UL>
        <LI>0 - Not running
        <LI>1 - Run called
        <LI>2 - Sleeping
        <LI>3 - Waiting
        <LI>4 - Joining
        <LI>5 - Run exited
        <LI>8 - Looking for new notifications
        <LI>9 - Forwarding new notifications to notification processor
        <LI>15 - In handle new notif loop
        <LI>16 - Forwarding new message
        </UL>
    */
public class EmailListHandler extends MailboxPoller {

    /** Log file handler*/
    private final static Logger log = Logger.getLogger(EmailListHandler.class); 
    /** The next step in the flow of processing notifications, where we
        send notifications */
    private EmailStore nextNotifProcessor;

    /** Number of instances that have been created from this class */
    private int instanceNumber;

    /** If traffic is low, the run method sleeps until nextWakeupTime */
    private int nextWakeupTime;
    /** Number of pending listed email not compleated.*/
    private int pendingListedEmail = 0;



    /** When the MailboxPoller is started, it shall look for all notification
        emails. After that, it shall only look for unseen notifications.
        firstTimeSinceStart remembers the state. */
    private boolean firstTimeSinceStart = true;

    private ManagementCounter queueCounter;

    /** The time to wait for an answer. If no answer has been done before this, then reset the counter */
    public static int MAXREPLYTIME = 60000;

    private long lastSentTime = 0;

    private CleanerThread cleanerThread = null;
    private boolean runCleaner = true;

    private PersistentQueue queue;

    private HashMap uidMappings;
    private int uidCounter = -1;
    

    /**
     * Constructor.
     *@param imapThreads the thread group for this poller thread.
     *@param notifProcessor the NTF part that shall process the found mail.
     *@param instanceNumber id for this EmailListHandler instance.
     *@param ncl is requested to delete any "very old messages" found.
     */
    public EmailListHandler(ThreadGroup imapThreads,
                            EmailStore notifProcessor,
                            int instanceNumber) {
        super(imapThreads, "EmailListHandler-" + instanceNumber);
        this.instanceNumber = instanceNumber;
        nextNotifProcessor = notifProcessor;
        
        //queueCounter = ManagementInfo.get().getInternalQueues("EmailList");
        queue = PersistentQueue.getQueue("EmailList");
        uidMappings = new HashMap();
        cleanerThread = new CleanerThread();
        cleanerThread.start();
        lastSentTime = System.currentTimeMillis();
    }

    /**
     *This should only be called from test code. And from internal when
     *NTF is shutting down.
     */
    public void stopCleaner() {
        runCleaner = false;
    }

    public boolean shutdown() {
        ntfRun();
        return true;

    }

    public boolean ntfRun() {
        if( pendingListedEmail > 0 && (lastSentTime + MAXREPLYTIME) < System.currentTimeMillis() ) {
            pendingListedEmail = 0;
        }
        if(queue.getQueueSize() > 0 && pendingListedEmail < Config.getSlamdownConn()){
            PersistentObject obj = queue.get();
            forwardListedEmail(obj);
        } else {
            try {
                synchronized(this) {
                    wait(MAXREPLYTIME);
                }

            } catch (InterruptedException e) { ; }
        }
        return false;
    }

    public int getPendingCount() {
        return pendingListedEmail;
    }

    public int getEmailCount() {
        return queue.getQueueSize();
    }

    public int getTotalCount() {
        return queue.getTotalCount();
    }

    public void resetPendingCount() {
        log.logMessage("Resetting pending count", log.L_DEBUG);
        synchronized(this) {
            pendingListedEmail = 0;
            lastSentTime = System.currentTimeMillis();
            this.notifyAll();
        }
    }

    /**
     * MailstorePoller tells EmailListHandler that a listgenerated notification is completed.
     *@param msgId the UID of the completed message.
     */
    public void notifCompleted(int msgId) {
        synchronized(this){
            if(pendingListedEmail > 0) pendingListedEmail--;

            Integer uidInteger = new Integer(msgId);
            PersistentObject obj = (PersistentObject) uidMappings.remove(uidInteger);
            if( obj != null ) {
                queue.remove(obj);
            }
            this.notifyAll();
        }
        log.logMessage("Number of (internal) pending list messages: " + pendingListedEmail, log.L_DEBUG);
    }

    /**
     * Tells the EmailListHandler that a listgenerated notification needs to be retried at a
     * later time. <B>Listgenerated messages will not be retried<B>
     *@param uid the UID of the message.
     *@param retryAddresses the receivers of the notification that have not yet
     * been notified.
     */
    public void notifRetry(int uid, String retryAddresses) {
        synchronized(this){
            if(pendingListedEmail > 0) pendingListedEmail--;
            Integer uidInteger = new Integer(uid);
            PersistentObject obj = (PersistentObject) uidMappings.remove(uidInteger);
            if( obj != null ) {
                queue.retry(obj);
            }
            this.notifyAll();
        }
        log.logMessage("Number of (internal) pending list messages: " + pendingListedEmail, log.L_DEBUG);
    }

    public void addEmailList(Vector v){
        log.logMessage("Adding list with " + v.size() + " receivers.", log.L_DEBUG);
        for( int i=0;i<v.size();i++ ) {
            PersistentObject obj = (PersistentObject) v.get(i);
            queue.add(obj);
        }

        queueCounter.incr(v.size());
        synchronized(this){this.notifyAll();}
    }

    /** Inserts a listed mail into the NotificationHandler.*/
    private void forwardListedEmail(PersistentObject obj){
        int msgSize = 0;
        NotificationEmail e;
        if(obj != null){
            if (obj instanceof MwiOffInfo) {
                e = createMwiOffMessage((MwiOffInfo) obj);
            } else if (obj instanceof SlamdownInfo) {
                e = createSlamdownMessage((SlamdownInfo) obj);
            } else {
                log.logMessage("Bad object in email list, type= " + obj.getClass().getName(), log.L_DEBUG);
                return;
            }
            log.logMessage("Forwarding message from list to: " + e.getReceiver(), log.L_DEBUG);
            pendingListedEmail++;
            lastSentTime = System.currentTimeMillis();
            nextNotifProcessor.putEmail(e);
            queueCounter.decr();
        }

    }

    private int getNextUid() {
        if( uidCounter >= 0) {
            uidCounter = -1;
        }
        return uidCounter--;
    }
    private NotificationEmail createSlamdownMessage(SlamdownInfo info){
        int id = getNextUid();
        uidMappings.put(new Integer(id), info);
        NotificationEmail ne = null;

       /* ne = new NotificationEmail(id,
            "From: " + info.getMessage() + "(" + info.getMessage() + ")\r\n"
            + "To: " + info.getMail() + "\r\n"
            + "Subject: ipms/message\r\n"
            + "Ipms-Notification-Type: ntf.internal.slamdown\r\n"
            + "Ipms-Notification-Content: ntf.internal." +
                (info.getCallType() ==  com.mobeon.ntf.Constants.NTF_VIDEO ? "video" : "voice") + "slamdown\r\n"
            + "\r\n"
            + "You recevied a call from " + info.getMessage() + ", no message was left.\r\n"
        );*/

        return ne;
    }


    private NotificationEmail createMwiOffMessage(MwiOffInfo info) {
        int id = getNextUid();
        uidMappings.put(new Integer(id), info);
        NotificationEmail ne = null;
        /*NotificationEmail ne =
            new NotificationEmail(id,
            "From: <sink@mobeon.com>\r\n"
            + "To: notification.off@" + Config.getImapHost() + "\r\n"
            + "Subject: ipms/message\r\n"
            + "Ipms-Notification-Type: mvas.subscriber.logout\r\n"
            + "Ipms-Notification-Content: " + info.getTelephoneNumber() + "\r\n"
            + "\r\n"

        );*/

        return ne;
    }

    private class CleanerThread extends Thread {
        public CleanerThread() {
            super("EmailListHandler:Cleaner");
        }

        public void run() {
            log.logMessage("Starting " + getName() + " thread", Logger.L_VERBOSE );
            while( runCleaner ) {
                try {
                    long nextTestTime = lastSentTime + MAXREPLYTIME;
                    long now = System.currentTimeMillis();
                    long sleepTime = nextTestTime - now;

                    if( now > nextTestTime ) {
                        if( pendingListedEmail != 0 )
                            resetPendingCount();
                        sleepTime = MAXREPLYTIME;
                    }

                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    log.logMessage("EmailListHandler.CleanerThread.run " + NtfUtil.stackTrace(e), Logger.L_VERBOSE );
                }
            }
            log.logMessage("Ending " + getName() + " thread", Logger.L_VERBOSE);
        }
    }

}
