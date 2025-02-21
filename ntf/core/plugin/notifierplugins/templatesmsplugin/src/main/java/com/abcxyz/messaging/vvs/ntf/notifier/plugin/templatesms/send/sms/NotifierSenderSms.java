/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.send.sms;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.ANotifierNotificationInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.INotifierMessageGenerator;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.NotifierMessageGenerationException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.NotifierSendException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.INotifierSenderSms;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierProfiler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierUtil;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.NtfThread;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.NotifierHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateSmsPlugin;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.NotifierTypeEvent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule.NotifierEvent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.send.NotifierNotificationInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util.NotifierConfig;

/**
 * The NotifierSenderSms class sends notifications to the SMSC.
 */
public class NotifierSenderSms {

    private static INotifierLogger logger = TemplateSmsPlugin.getLoggerFactory().getLogger(NotifierSenderSms.class);
    private INotifierMessageGenerator notifierMessageGenerator = TemplateSmsPlugin.getMessageGenerator();
    private INotifierSenderSms notifierSender = TemplateSmsPlugin.getSender();
    private INotifierProfiler notifierProfiler = TemplateSmsPlugin.getProfiler();
    
    private static NotifierSenderSms _inst;
    private NotifierSenderWorker[] notifierSenderWorker;    
    private BlockingQueue<NotifierEvent> notifierWorkerQueue;
    private BlockingQueue<NotifierEvent> notifierSenderQueue;
    private int notifierSenderNumberOfWorkers = 10;

    public NotifierSenderSms(BlockingQueue<NotifierEvent> notifierWorkerQueue) {

         this.notifierWorkerQueue = notifierWorkerQueue;
         this.notifierSenderQueue = new LinkedBlockingQueue<NotifierEvent>(NotifierConfig.getNotifierSenderSmsQueueSize());
         this.notifierSenderNumberOfWorkers = NotifierConfig.getNotifierSenderSmsWorkers();
         this.notifierSenderWorker = new NotifierSenderWorker[notifierSenderNumberOfWorkers];
         for (int i=0; i<notifierSenderNumberOfWorkers; i++) {
             this.notifierSenderWorker[i] = new NotifierSenderWorker("NotifierSenderWorker-" + i);
         }
         _inst = this;
    }

    public static NotifierSenderSms get() {
        return _inst;
    }    

    public void send(NotifierEvent notifierEvent) {
        boolean storedInQueue = notifierSenderQueue.offer(notifierEvent);
        if (!storedInQueue) {
            logger.warn("NotifierSenderSms.send: Not stored in notifierSenderQueue (full), will retry");
        }
    }

    /**
     * NotifierSenderWorker class
     */
    private class NotifierSenderWorker extends NtfThread {

    	private INotifierUtil notifierUtil = TemplateSmsPlugin.getUtil();
		private INotifierLogger logger;


		public NotifierSenderWorker(String threadName) {
            super(TemplateSmsPlugin.getLoggerFactory(), TemplateSmsPlugin.getnotifierServicesManager(), threadName);
            logger = loggerFactory.getLogger(NotifierSenderWorker.class);
            setDaemon(true);
            start();
        }


        private void sendNotifSms(NotifierEvent notifierEvent) {
        	try {

        		if (!notifierUtil.isFileStorageOperationsAvailable(notifierEvent.getNotificationNumber())) {
        			//don't try to send if file system is read only. let it retry instead..
        			logger .warn("sendNotifSms: File system is read only (Geo Fail Over?) for Event:" + notifierEvent.getIdentity()  );
        			return;
        		}
        		sendNotifierEvent(notifierEvent);
        	} catch (NotifierMessageGenerationException e) {
        		switch (e.getMessageGenerationExceptionCause()) {
        		case NO_CAUSE_SPECIFIED:
        		case PAYLOAD_FILE_DOES_NOT_EXIST:
        			logger.error("sendNotifSms: NotifierMessageGenerationException for " + notifierEvent.getIdentity() + "; will not be retried: " + e.getMessage());
        			handlePermanentError(notifierEvent);
        			break;
        		case PAYLOAD_FILE_NOT_ACCESSIBLE:
        			// Do nothing; will retry
        			logger.error("sendNotifSms: NotifierMessageGenerationException for " + notifierEvent.getIdentity() + "; will be retried: " + e.getMessage());
        			break;
        		}
        	} catch (NotifierSendException e) {
        		switch (e.getNotifierSendExceptionCause()) {
        		case SUBSCRIBER_DATABASE_PROFILE:
        			//Subscriber database profile no longer allows the sending of the notification (e.g. notifications are disabled, the notification number is no longer valid).
        			logger.error("sendNotifSms: NotifierSendException while sending " + notifierEvent.getNotifierTypeName() + " notification for " + notifierEvent.getIdentity() + "; will not be retried: " + e.getMessage());
        			handlePermanentError(notifierEvent);
        			break;
        		case MESSAGE_STORE_ERROR:
        		case NO_CAUSE_SPECIFIED:
        			// Do nothing; will retry
        			logger.error("sendNotifSms: NotifierSendException while sending " + notifierEvent.getNotifierTypeName() + " notification for " + notifierEvent.getIdentity() + "; will be retried: " + e.getMessage());
        			break;
        		}
        	}
        }
        
        
        private void sendNotifierEvent(NotifierEvent notifierEvent) throws NotifierMessageGenerationException, NotifierSendException {            
            Object perf = null;
            try {
                if (notifierProfiler.isProfilerEnabled()) {
                    perf = notifierProfiler.enterProfilerPoint("NTF.Trace.2.3.NSS.Send.Generic");
                }
                ANotifierNotificationInfo notificationInfo = new NotifierNotificationInfo(notifierEvent.getEventProperties());
                ANotifierDatabaseSubscriberProfile subscriberProfile = notifierEvent.getSubscriberProfile();
                NotifierSendInfoSms notifierSendInfoSms = new NotifierSendInfoSms(notifierEvent, notifierWorkerQueue);
                String cphrTemplate = notifierEvent.getCphrtemplateName();

                // the event will be sent according to the .cphr files template name specified in the template notification.
                String charsetname = NotifierConfig.getCharsetEncoding();
                if(charsetname != null) {
                    byte[] messageBytes = notifierMessageGenerator.generateNotificationMessageAsBytes(charsetname, cphrTemplate, notificationInfo, subscriberProfile, false);
                    notifierSender.sendNotificationSms(notifierSendInfoSms, messageBytes);
                } else {
                    String messageString = notifierMessageGenerator.generateNotificationMessageAsString(cphrTemplate, notificationInfo, subscriberProfile, false);
                    notifierSender.sendNotificationSms(notifierSendInfoSms, messageString);
                }
            } finally {
                if (perf != null) {
                    notifierProfiler.exitProfilerPoint(perf);
                }
            }          
        }
        
        
        private void handlePermanentError(NotifierEvent notifierEvent) {
            logger.debug("Handling permanent error; injecting EVENT_CLIENT_FAILED into queue.");
            notifierEvent.setNotifierTypeEvent(NotifierTypeEvent.EVENT_CLIENT_FAILED);
            boolean storedInQueue = NotifierHandler.get().getWorkingQueue().offer(notifierEvent);
            if (!storedInQueue) {
                logger.warn("NotifierSenderSms.handlePermanentError: Not stored in workingQueue (full), " + notifierEvent.getIdentity() + " (" + notifierEvent.getNotifierTypeName() + ") response dropped.");
            }
        }

		@Override
		protected boolean ntfRun() {
            NotifierEvent notifierEvent = null;

                try {
                	
                	if (managmentInfo.isAdministrativeStateShutdown()) {
                		//don't wait if shutting down.
                		notifierEvent = notifierSenderQueue.poll(); 
                	} else { 
                		//Time out regularly to check if we are to lock or shutdown.
                		notifierEvent = notifierSenderQueue.poll(5,TimeUnit.SECONDS);
                	}
                	if (notifierEvent == null) {
                		//go to check NTF state..
                		return false;
                	}

                    synchronized (notifierEvent) {
                        logger.debug("NotifierSenderWorker-" + getName() + " to send " + notifierEvent.getNotifierTypeName() + " for " + notifierEvent.getIdentity());
                        sendNotifSms(notifierEvent);
                    }

                } catch (InterruptedException ie) {
                	logger.info("Thread interrupted, exiting on request from ManagementInfo: " + getName());
                	return true;
                	
                } catch (Exception e) {
                    logger.error("NotifierSenderWorker-" + getName() + ": unexpected Exception: ", e);
                    if (notifierEvent != null) {
                        logger.error("Exception for " + notifierEvent.getIdentity() + " while in " + notifierEvent.getStatus(), e);
                    }
                }
                return false;
		}
		
		@Override
		protected boolean shutdown() {
			
			if (isInterrupted()) {
				return true;
			} //exit immediately if interrupted..
			if (notifierSenderQueue.isEmpty()) {
				//wait a short while see if queue has new items, if not exit.
				try { Thread.sleep(1000); } catch (InterruptedException i) {return true;}
				if (notifierSenderQueue.isEmpty()) {
					return true; //exit now.
				}
				return ntfRun(); //process the remaining items before shutdown.
			} else
			{
				return true;
			}
		}
    }
}
