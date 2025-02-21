/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.mail;

import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotifCompletedListener;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.ErrorLogLimiter;
import com.mobeon.ntf.util.time.NtfTime;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.management.ManagementStatus;
import java.util.*;

/****************************************************************
 * MailstorePoller polls a number of mailboxes for new mail using one instance
 * of MailboxPoller for each mailbox.
 * <BR>
 *
 */

public class MailstorePoller implements
                                 NotifCompletedListener,
                                 MailMemoryManager {
    /** Log file handler*/
    private final static Logger log = Logger.getLogger(MailstorePoller.class); 
    public static final int yes= 0;
    public static final int no= 1;
    public static final int never= 2;
    /**Allow 5 minutes of processing current notifications after polling is stopped*/
    private static final int imapShutdownTime= 5 * 60 * 1000;


    private ErrorLogLimiter errlim = new ErrorLogLimiter(60, 300);

    /** Number of mailboxes*/
    private int mailboxCount;

    /** MailboxPollers*/
    private MailboxPoller pollers[];
    /***/
    int [] mailpollerMsgCount = new int[1];//Config.getImapThreads()*3];

    /** EmailListHandler*/
    private EmailListHandler emailListHandler;

    /** Hash of sizes of mails in memory, keyed by UID*/
    private HashMap /*of Long*/ mailSizes;

    /** Total size of all mails in memory*/
    private long mailMemoryUsed;

    /** Total size allowed for mails in memory*/
    private long mailMemory;

    /** Thread group for threads related to IMAP */
    ThreadGroup imapThreads;

    private int lastReportTime = 0;

    public void notifCompleted(int msgId, int poller) {
            releaseMemory(msgId, poller);
            pollers[poller].notifCompleted(msgId);
    }

    public void notifRetry(int msgId, int poller, String retryAddresses) {
            releaseMemory(msgId, poller);
            pollers[poller].notifRetry(msgId, retryAddresses);
    }

    public void notifRenew(int msgId, int poller) {
            pollers[poller].notifRenew(msgId);
    }

    public void notifRelease(int msgId, int poller) {
            releaseMemory(msgId, poller);
    }

    /****************************************************************
         * The class MessageId serves as a mailstore-unique message identifier, by
         * adding a folder id to the folder-unique message-id
         */
        private class MessageId {
            private int uid;
            private int folderId;

            public MessageId(int uid, int folderId) {
                this.uid= uid;
                this.folderId= folderId;
            }

            public int hashCode(){
            return uid^folderId;
            }

            public boolean equals(Object o){
                if(o == null) return false;
                if(!(o instanceof MessageId)) return false;
                MessageId temp = (MessageId)o;
                if(temp.uid != uid) return false;
                return temp.folderId == folderId;
            }
        }


    public long getMailMemoryUsed() {
        return mailMemoryUsed;
    }


    public int getMailMemoryCount() {
        return mailSizes.size();
    }


    public synchronized int reserveMemory(int poller, int msgId, int mailSize) {
        if (mailSize > mailMemory) return never;
        if (mailSize > mailMemory - mailMemoryUsed) return no;

        mailMemoryUsed+= mailSize;
        log.logMessage("MailstorePoller: reserved " + mailSize + " bytes (total " + mailMemoryUsed + ")", log.L_DEBUG);
        mailSizes.put(new MessageId(msgId, poller), new Integer(mailSize));

        return yes;
    }


    public synchronized void releaseMemory(int msgId, int poller) {
        if (msgId < 0) {
            return;
        }

        Integer mailSize= (Integer)(mailSizes.remove(new MessageId(msgId, poller)));
        if (mailSize != null) {
            mailMemoryUsed-= mailSize.intValue();
            log.logMessage("MailstorePoller: released " + mailSize.intValue() + " bytes (total " + mailMemoryUsed + ")",log.L_DEBUG);
        } else {
            log.logMessage("MailstorePoller: Trying to release memory for unknown message (" + msgId + "," + poller + ")", log.L_ERROR);
        }
    }

    public synchronized void reportCountStatus(int poller, int new_messages, int total){
        lastReportTime = NtfTime.now;
        int num = poller;
        mailpollerMsgCount[(poller*3)] = poller;
        mailpollerMsgCount[((poller*3)+1)] = new_messages;
        mailpollerMsgCount[((poller*3)+2)] = total;
        if(num == 0){
            String s = "";
            int i = 0;
            while(i < mailpollerMsgCount.length){
                s += mailpollerMsgCount[i];
                s += "(" + mailpollerMsgCount[(++i)] +",";
                s += mailpollerMsgCount[(++i)] + ")";
                i++;
            }
            log.logMessage(s, log.L_DEBUG);
        }
    }

    public int getTimeSinceLastReport() {
        return NtfTime.now - lastReportTime;
    }

    public synchronized void notifyStatus() {
        boolean allDown = true;
        for( int i=0;i<(pollers.length-1);i++ ) {
            if( pollers[i].getStatus() == true ) {
                allDown = false;
                break;
            }
        }
        
        // This class is not used anymore but kept as historical reference
        /*
        if( allDown ) {
            ManagementStatus mStatus = ManagementInfo.get().getStatus(IServiceName.STORAGE, Config.getImapHost() );
            if( mStatus != null ) {
                mStatus.setHostName(Config.getImapHost());
                mStatus.setPort(Config.getImapPort());
                mStatus.setZone(Config.getLogicalZone());
                mStatus.down();
            }
        } else {
            ManagementStatus mStatus = ManagementInfo.get().getStatus(IServiceName.STORAGE, Config.getImapHost() );
            if(mStatus != null ) {
                mStatus.up();
            }

        }*/
    }


    public MailstorePoller(EmailStore notifProcessor) {

        log.logMessage("MailstorePoller: starting mailbox pollers", log.L_VERBOSE);
        //mailboxCount = Config.getImapThreads();
        pollers= new MailboxPoller[mailboxCount+1];
        imapThreads= new ThreadGroup("imap");
        mailSizes= new HashMap();
        //mailMemory= Config.getMailMemoryMegabyte();
        /*
        for(int i = 0; i < Config.getImapThreads()*3; i++)
            mailpollerMsgCount[i] = -1;
        */
        emailListHandler = new EmailListHandler(imapThreads, notifProcessor, mailboxCount);
        emailListHandler.setDaemon(true);
        emailListHandler.start();

        for (int i= 0; i < mailboxCount; i++) {
            pollers[i]= new MailboxPoller(imapThreads, notifProcessor, i, this, this, emailListHandler, this);
            pollers[i].start();
        }
        pollers[mailboxCount] = emailListHandler;
        lastReportTime = NtfTime.now;
        //ManagementInfo.get().setMailstorePoller( this );
        notifyStatus();
    }

    /**
     * Constructor used for testing.
     */
    public MailstorePoller() {
    }

    public void reportError(String msg, Exception e, boolean unexpected) {
        errlim.report(msg, e, unexpected);
    }
}

