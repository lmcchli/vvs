/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.ntf.slamdown;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.trafficeventsender.mfs.IMfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.out.sms.InfoResultHandler;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.util.threads.NtfThread;

/**
 * SlamdownSender sends the Slamdown information to the NTF SMS client.
 */
public class SlamdownSender implements com.mobeon.ntf.Constants {

    private SMSOut _smsOut;
    private LogAgent log;
    private static SlamdownSenderWorker[] slamdownSenderWorker;
    private static IMfsEventManager _mfsEventManager;
    private ManagedArrayBlockingQueue<Object> slamdownWorkerQueue;
    private ManagedArrayBlockingQueue<Object> slamdownSenderQueue;
    private int slamdownMcnSenderNumberOfWorkers = 10;
    static int reportCount = 0; //used to report slamdown queue size periodically. 

    protected SlamdownSender(ManagedArrayBlockingQueue<Object> slamdownWorkerQueue) {
         log = NtfCmnLogger.getLogAgent(SlamdownSender.class);
         _smsOut = SMSOut.get();

         // Slamdown Main WorkerQueue
         this.slamdownWorkerQueue = slamdownWorkerQueue;

         // SlamdownSender thread's shared workerQueue
         slamdownSenderQueue = new ManagedArrayBlockingQueue<Object>(Config.getSlamdownMcnSenderQueueSize());

         // SlamdownSender threads
         slamdownMcnSenderNumberOfWorkers = Config.getSlamdownMcnSenderWorkers();
         slamdownSenderWorker = new SlamdownSenderWorker[slamdownMcnSenderNumberOfWorkers];
         for (int i=0; i<slamdownMcnSenderNumberOfWorkers; i++) {
             slamdownSenderWorker[i] = new SlamdownSenderWorker("SlamdownSenderWorker-" + i);
         }
    }

    public static void setMfsEventManager(IMfsEventManager manager){
    	_mfsEventManager = manager;
    }

    public void send(SlamdownList list) {
        try {
            slamdownSenderQueue.put(list);
        } catch (Throwable t) {
            log.info("send: queue full or state locked while handling event, will retry");
        }
    }

    private class SlamdownSenderWorker extends NtfThread {

        

        private Object SendSlamDownListCounter; //counter

        public SlamdownSenderWorker(String threadName) {
            super(threadName);
            setDaemon(true);
            start();
        }

        /**
         * The shutdown loop stops after the ntfRun method is finished.
         *
         * @return true always (i.e. this thread has not shutdown activity)
         */
        public boolean shutdown() {
            if (isInterrupted()) {
                return true;
            } //exit immediately if interrupted..

            if (slamdownSenderQueue.size() == 0)
            {
                    //give a short time for new items to be queued in workers, to allow other threads to empty there queues.
                    if (slamdownSenderQueue.isIdle(2,TimeUnit.SECONDS)) {
                        return true;
                    }
                    else
                    {
                        if (slamdownSenderQueue.waitNotEmpty(2, TimeUnit.SECONDS)) {
                            return(ntfRun());
                        } else
                        {
                            return true;
                        }

                    }
            } else {
                return(ntfRun());
            }
        }

        public boolean ntfRun() {
            SlamdownList list = null;

            try {
                Object obj = slamdownSenderQueue.poll(10, TimeUnit.SECONDS); //timeout after 2 seconds to check management state.
                   
                    if (reportCount++ == 100) {
                        reportCount = 0;
                        log.info("queueSize = " + slamdownSenderQueue.size());
                    }
                    if (obj == null) { return false; }
                    if (!(obj instanceof SlamdownList)) {
                        log.warn("Invalid object on slamdown sender queue: " + obj.getClass());
                        return false;
                    }
                    
                list = (SlamdownList) obj;

                synchronized (list) {
                    if (list.getCurrentState() != SlamdownList.STATE_DONE) {
                        log.debug("SlamdownSenderWorker-" + getName() + " to send Slamdown information ");
                        sendSlamdownList(list);
                    }
                }

            } catch (OutOfMemoryError me) {
                try {
                    ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                    log.error("NTF out of memory, shutting down... ",me);
                    shutdown();
                } catch (OutOfMemoryError e2) {;} //ignore
                return true;
            } catch (Exception e) {
                log.error("SlamdownSender exception: ", e);
                if (list != null) {
                    // Inject the result to SlamdownWorker thread via SlamdownResultHandler
                    SlamdownResultHandler slamdownResultHandler = new SlamdownResultHandler();
                    slamdownResultHandler.retry(list);
                }
            }
          return false;

        }

        private void sendSlamdownList(SlamdownList sl) {

            if (sl != null) {
                SlamdownResultHandler slamdownResultHandler = new SlamdownResultHandler();

                // Check if the Slamdown/Mcn file exists
                if(_mfsEventManager == null) {
                    _mfsEventManager = MfsEventFactory.getMfsEvenManager();
                }

                File[] files= sl.getFileList();

                if (files == null || files.length == 0) {
                    log.debug("SendSlamdownList: Notification already processed for " + sl.getSubscriberNumber() + " : " + sl.getNotificationNumber());
                    slamdownResultHandler.allOk(sl, 0);
                    return;
                }


                try {
                    if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                        SendSlamDownListCounter = CommonOamManager.profilerAgent.enterCheckpoint("NTF.SendSlamDownList");
                    }

                    log.debug("Sending " + sl.getType()+ " to " + sl.getSubscriberNumber() + " : " + sl.getNotificationNumber());

                    String formatHeader;
                    String formatFooter;
                    String formatBody[];
                
                String charsetname = Config.getCharsetEncoding();
                if (charsetname.equals("")) charsetname = null;
                SlamdownPayload payload = null;

                    if (sl.getNotificationType()==SlamdownList.NOTIFICATION_TYPE_SLAMDOWN) {
                        // Slamdown case
                    if (charsetname == null) {
                        formatHeader = SlamdownFormatter.formatHeader(sl);
                        formatFooter = SlamdownFormatter.formatFooter(sl);
                        formatBody = SlamdownFormatter.formatBody(sl);
                        payload = new SlamdownPayload(formatHeader, formatBody, formatFooter);

                        StringBuffer sb = new StringBuffer();
                        for (String s:formatBody) {sb.append(s);}
                        log.debug("Slamdown message:\nHeader: " + formatHeader + "Body  : " + sb + "Footer: " + formatFooter);  
                    } else {
                        payload = SlamdownFormatter.getSlamdownBytePayload(charsetname, sl);
                    }
                } else {
                        // Mcn case
                        McnFormatter.Template template = McnFormatter.Template.MCN;
                        log.debug("sendSlamdownList MCN hasMcnSubscribedService: " + sl.getUserInfo().hasMcnSubscribedService());
                        if (Config.isMcnSubscribedEnabled() && sl.getUserInfo().hasMcnSubscribedService()) {
                            template = McnFormatter.Template.MCN_SUBSCRIBED;
                        }
                        McnFormatter formatter = McnFormatter.getInstance(template);
                    
                    if (charsetname == null) {
                        formatHeader = formatter.formatHeader(sl);
                        formatFooter = formatter.formatFooter(sl);
                        formatBody = formatter.formatBody(sl);
                        payload = new SlamdownPayload(formatHeader, formatBody, formatFooter);

                        StringBuffer sb = new StringBuffer();
                        for (String s:formatBody) {sb.append(s);}
                    log.debug("Mcn message:\nHeader: " + formatHeader + "Body  : " + sb + "Footer: " + formatFooter);
                    } else {
                        payload = formatter.getMcnBytePayload(charsetname, sl);
                    }
                }

                /**
                 * Before sending the SMS Slamdown/Mcn notification, the files must be renamed as "slamdowninformationf_<date>"
                 * The "f" tag means final (file is now handled as an SMS-info notification).
                 */
                sl.renameSlamdownMcnFilesAsHandled();

                    // Release lock file on slamdownmcn_phoneon.lock
                    try {
                    _mfsEventManager.releaseLockFile(sl.getNotificationNumber(), SlamdownList.SLAMDOWNMCN_PHONE_ON_LOCK_FILE, sl.getPhoneOnLockId(), sl.getInternal());
                } catch (Exception e) {
                    log.warn("Exception trying to release lock file " + SlamdownList.SLAMDOWNMCN_PHONE_ON_LOCK_FILE + " for " + sl.getSubscriberNumber() + " : " + sl.getNotificationNumber(), e);
                }

                    _smsOut.sendInfo(new SMSAddress(Config.getTypeOfNumber(),
                            Config.getNumberingPlanIndicator(),
                            sl.getNumber()),
                            sl.getValidity(),
                        payload,
                            sl,
                            slamdownResultHandler);
                } finally {
                    if (SendSlamDownListCounter != null) {
                        CommonOamManager.profilerAgent.exitCheckpoint(SendSlamDownListCounter);
                    }
                }
            }
        }
    }

    private class SlamdownResultHandler implements InfoResultHandler {

        public void allOk(SlamdownList slamdownList, int okCount) {
            log.debug("SlamdownResultHandler: " + slamdownList.getType() + " SMS sent OK to " + slamdownList.getSubscriberNumber() + " : " + slamdownList.getNotificationNumber());

            synchronized (slamdownList) {
                if (isSlamdownListWaitingSmsInfoState(slamdownList)) {
                    slamdownList.setCurrentState(SlamdownList.STATE_SENDING_INFO);
                    slamdownList.setCurrentEvent(SlamdownList.EVENT_SMS_INFO_RESPONSE_SUCCESSFUL);
                    try {
                        slamdownWorkerQueue.offer(slamdownList, 500, TimeUnit.MILLISECONDS);
                    } catch (Throwable t) {
                        log.warn("SlamdownResultHandler: " + slamdownList.getType() + " SMS sent OK response is DROPPED since queue is full: " + slamdownList.getSubscriberNumber() + " : " + slamdownList.getNotificationNumber());
                    }
                }
            }
        }

        public void noneOk(SlamdownList slamdownList) {
            log.debug("SlamdownSender: " + slamdownList.getType() + " SMS noneOk for " + slamdownList.getSubscriberNumber() + " : " + slamdownList.getNotificationNumber());

            synchronized (slamdownList) {
                if (isSlamdownListWaitingSmsInfoState(slamdownList)) {
                    slamdownList.setCurrentState(SlamdownList.STATE_SENDING_INFO);
                    slamdownList.setCurrentEvent(SlamdownList.EVENT_CLIENT_FAILED);
                    try {
                        slamdownWorkerQueue.offer(slamdownList, 500, TimeUnit.MILLISECONDS);
                    } catch (Throwable t) {
                        log.warn("SlamdownResultHandler: " + slamdownList.getType() + " SMS noneOk response is DROPPED since queue is full: " + slamdownList.getSubscriberNumber() + " : " + slamdownList.getNotificationNumber());
                    }
                }
            }
        }

        public void retry(SlamdownList slamdownList) {
            log.debug("SlamdownSender: " + slamdownList.getType() + " SMS retry for " + slamdownList.getSubscriberNumber() + " : " + slamdownList.getNotificationNumber());

            synchronized (slamdownList) {
                if (isSlamdownListWaitingSmsInfoState(slamdownList)) {
                    slamdownList.setCurrentState(SlamdownList.STATE_SENDING_INFO);
                    slamdownList.setCurrentEvent(SlamdownList.EVENT_CLIENT_RETRY);
                    try {
                        slamdownWorkerQueue.offer(slamdownList, 500, TimeUnit.MILLISECONDS);
                    } catch (Throwable t) {
                        log.warn("SlamdownResultHandler: " + slamdownList.getType() + " SMS retry response is DROPPED since queue is full: " + slamdownList.getSubscriberNumber() + " : " + slamdownList.getNotificationNumber());
                    }
                }
            }
        }

        public void failed(SlamdownList slamdownList) {
            log.debug("SlamdownSender: " + slamdownList.getType() + " SMS failed for " + slamdownList.getSubscriberNumber() + " : " + slamdownList.getNotificationNumber());

            synchronized (slamdownList) {
                if (isSlamdownListWaitingSmsInfoState(slamdownList)) {
                    slamdownList.setCurrentState(SlamdownList.STATE_SENDING_INFO);
                    slamdownList.setCurrentEvent(SlamdownList.EVENT_CLIENT_FAILED);
                    try {
                        slamdownWorkerQueue.offer(slamdownList, 500, TimeUnit.MILLISECONDS);
                    } catch (Throwable t) {
                        log.warn("SlamdownResultHandler: " + slamdownList.getType() + " SMS failed response is DROPPED since queue is full: " + slamdownList.getSubscriberNumber() + " : " + slamdownList.getNotificationNumber());
                    }
                }
            }
        }

        public void result(SlamdownList slamdownList, boolean[] result, int okCount) {
            // This method interprets 'partlyFaied' response from SMSInfoLister as successful
            allOk(slamdownList, okCount);
        }

        private boolean isSlamdownListWaitingSmsInfoState(SlamdownList slamdownList) {
            boolean result = false;
            String subscriber = slamdownList.getSubscriberNumber() + " : " + slamdownList.getNotificationNumber();

            if (slamdownList.getSchedulerIds().getSmsUnitEventId() != null && slamdownList.getSchedulerIds().getSmsUnitEventId().length() > 0) {
                log.warn("SlamdownResultHandler: SMS-Info result received for subscriber " + subscriber + " while pending SMS-Unit eventId found: " + slamdownList.getSchedulerIds().getSmsUnitEventId());
            }

            if (slamdownList.getSchedulerIds().getSmsType0EventId() != null && slamdownList.getSchedulerIds().getSmsType0EventId().length() > 0) {
                log.warn("SlamdownResultHandler: SMS-Info result received for subscriber " + subscriber + " while pending SMS-PhoneOn eventId found: " + slamdownList.getSchedulerIds().getSmsType0EventId());
            }

            if (slamdownList.getSchedulerIds().getSmsInfoEventId() != null && slamdownList.getSchedulerIds().getSmsInfoEventId().length() > 0) {
                result = true;
            }

            return result;
        }
    }
}
