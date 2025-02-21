/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms;

import java.util.Arrays;
import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierIncomingSignalInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfNotificationInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse.NotifierHandlingActions;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.NotifierIncomingSignalResponse.NotifierHandlingTypes;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile.NotificationType;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.NotifierDatabaseException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.INotifierMessageGenerator;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierProfiler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierUtil;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.NotifierTypeEvent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.CPHRNotifTYPE;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateType.NotifierTypeNotificationNumber;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.database.NotifierDatabaseHelper;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule.InvalidEventException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule.NotifierEvent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule.NotifierEventHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.send.NotifierNotificationInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.send.sms.NotifierSenderSms;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util.NotifierConfig;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handle Notification
 * 
 * Responsible to handle notification along with the worker.
 * <p>
 * The NotifierHandler class determines if an incoming notification signal should be handled
 * by this plug-in then it delegates the actual handling of the signal to a NotifierWorker thread by
 * creating a NotifierEvent for the signal and putting it on the NotifierWorkers' queue.
 * <p>
 * This class shows how to accept to handle an incoming notification signal completely, partially or not at all.
 */
public class NotifierHandler {

	// Get access to the NTF services for this class.
    private static INotifierLogger log = TemplateSmsPlugin.getLoggerFactory().getLogger(NotifierHandler.class);
    private INotifierUtil notifierUtil = TemplateSmsPlugin.getUtil();
    private INotifierProfiler notifierProfiler = TemplateSmsPlugin.getProfiler();
    private INotifierMessageGenerator notifierMessageGenerator = TemplateSmsPlugin.getMessageGenerator();

    private static NotifierHandler _inst = null;
    private NotifierWorker[] notifierWorkers;
    private BlockingQueue<NotifierEvent> workingQueue;
    private boolean isStarted = false;

    private NotifierHandler() {
        try {
            // Create a working queue from which the Notifier workers will get work.
            workingQueue = new LinkedBlockingQueue<NotifierEvent>(NotifierConfig.getNotifierEventQueueSize());

            // Create Notifier workers which will handle the notifications
            int numberOfWorkers = NotifierConfig.getNotifierEventWorkers();
            if (numberOfWorkers>0 ) {


                // Instantiate the NotifierSenderSms object which will send the notifications
                new NotifierSenderSms(workingQueue);

                createWorkers(numberOfWorkers);
                isStarted=true;
            } else {
                log.warn("Notifier service disable");
            }

        } catch (Exception e) {
            log.error("Unable to start template Notifier service due to exception: ",e);
        }
    }

    public static NotifierHandler get() {
        if(_inst == null) {
            _inst = new NotifierHandler();
        }
        return _inst;
    }

    public BlockingQueue<NotifierEvent> getWorkingQueue() {
        return workingQueue;
    }

    /**
     * Create the workers
     */
    private void createWorkers(int numberOfWorkers) {
        notifierWorkers = new NotifierWorker[numberOfWorkers];

        for (int i = 0; i<numberOfWorkers; i++) {
            notifierWorkers[i] = new NotifierWorker(workingQueue, "NotifierWorker-" + i);
            notifierWorkers[i].setDaemon(true);
            notifierWorkers[i].start();
        }
    }

    /**
     * This method is invoked to determine whether this plug-in should
     * take over an incoming notification signal from the NTF component.
     * <PRE>
     * The NotifierIncomingSignalResponse contains the:
     * 1) Action that must be performed by the NTF component:
     *    Supported actions:
     *    - OK:     The plug-in has handled (taken over) successfully this signal or that the plug-in has nothing to handle. 
     *    - RETRY:  The plug-in requires the NTF component to retry this signal later. 
     * 2) NotifierHandlingTypes
     *    Supported handled types:
     *    - NONE:   Signal is not handled by Notifier plug-in, NTF component must handle.
     *    - ALL:    Signal is handled completely by Notifier plug-in.
     *    - SMS:    Signal is handled by Notifier plug-in (SMS part), NTF component must handle the remaining.
     *</PRE>
     * @param signalInfo INotifierIncomingSignalInfo object containing all information about the incoming signal for sending a notification
     * @return NotifierIncomingSignalResponse
     */
    public NotifierIncomingSignalResponse handleNotifier(INotifierIncomingSignalInfo signalInfo) {
        NotifierIncomingSignalResponse incomingSignalResponse = new NotifierIncomingSignalResponse();

        // Get the event properties which contain the information for the incoming signal.
        Properties eventProperties = signalInfo.getNotificationEventProperties();
        INotifierNtfNotificationInfo ntfNotifInfo = TemplateSmsPlugin.getNtfNotificationInfo(eventProperties);
        String subscriberNumber = ntfNotifInfo.getReceiverTelephoneNumber();
        
        // Determine if the Notifier plug-in must handle the incoming signal.
        NotifierInfo notifierInfo = isHandledByNotifierHandler(signalInfo.getServiceType(), eventProperties);
        if (notifierInfo == null) {
            log.debug("plug-in will not handle incoming signal: " + signalInfo.getServiceType());

            // Since Notifier does not need to handle this event, must return NotifierHandlingActions.OK
            incomingSignalResponse.addHandlingType(NotifierHandlingTypes.NONE);
            incomingSignalResponse.setAction(NotifierHandlingActions.OK);
            return incomingSignalResponse;
        }

        // Entry found in plug-in configuration for the notification event type in the incoming signal.
        int countReadOnly = 0;
        int countDiscarded = 0;
        int countProcessed = 0;

        String notifierTypeName = notifierInfo.typeName;
        CPHRNotifTYPE cphrNotifType = notifierInfo.cphrNotifType;
        TemplateType templateEntry = notifierInfo.templateEntry;

        // Set the handledNotification so that the NTF component knows which one the plug-in takes over
        switch (cphrNotifType) {
            case MAILBOXSUBSCRIBER:
            case SUBSCRIBERONLY:
                incomingSignalResponse.addHandlingType(NotifierHandlingTypes.ALL);
                break;
            case NONSUBSCRIBER:
            	log.error("cphrType not currently handled:" + cphrNotifType.getName());
            	 incomingSignalResponse.addHandlingType(NotifierHandlingTypes.NONE);
                 return incomingSignalResponse;
            default:
                log.error("Received invalid handledNotification " + cphrNotifType.getName());
                incomingSignalResponse.addHandlingType(NotifierHandlingTypes.NONE);
                return incomingSignalResponse;
        }

        try {
            if (!isStarted) {
                log.error("Received event but service is not started, will ask NTF component to retry");
                incomingSignalResponse.setAction(NotifierHandlingActions.RETRY);
                if (notifierProfiler.isProfilerEnabled()) {
                    NotifierHelper.profilerCheckPoint("NTF.TNP.1.NH.NotStarted");              
                }                
                return incomingSignalResponse;
            }

            // The plug-in gets the subscriber profile from the MiO database.
            ANotifierDatabaseSubscriberProfile subscriberProfile = null; 
            try {
                subscriberProfile = NotifierHelper.getSubscriberProfile(subscriberNumber, notifierTypeName, eventProperties);
            } catch (NotifierDatabaseException nde) {
                incomingSignalResponse.setAction(NotifierHandlingActions.RETRY);
                log.error("NotifierDatabaseException while looking up subscriber, will ask NTF component to retry: " + nde.getMessage());
                if (notifierProfiler.isProfilerEnabled()) {
                    NotifierHelper.profilerCheckPoint("NTF.TNP.1.NH.SubP.Excep." + notifierTypeName);
                }

                return incomingSignalResponse;
            }

            if(subscriberProfile == null) { //TODO for non subscriber type..
                incomingSignalResponse.setAction(NotifierHandlingActions.OK);
                log.error("Received event but subscriber " + subscriberNumber + " not found in database, ignoring event and sending response: " + incomingSignalResponse);
                if (notifierProfiler.isProfilerEnabled()) {
                    NotifierHelper.profilerCheckPoint("NTF.TNP.1.NH.SubP.NotFound." + notifierTypeName);
                }                
                return incomingSignalResponse;
            }
            
            // Verify if the type is disabled for the subscriber's COS.
            String cosName = NotifierDatabaseHelper.getCosName(subscriberProfile);
            if (!templateEntry.isEnabledforCos(cosName)) {
                log.debug("Notifier type " + notifierTypeName + " is disabled for COS " + cosName);

                // Since Notifier does not need to handle this event, must return NotifierHandlingActions.OK
                incomingSignalResponse.setAction(NotifierHandlingActions.OK);
                return incomingSignalResponse;
            }            

            // Get the notification phone numbers to send to.
            String[] notificationNumbers = null;
            try {
                notificationNumbers = getNotificationNumbers(ntfNotifInfo, subscriberProfile, templateEntry, cphrNotifType);
            } catch(NotifierMfsException e) {
                incomingSignalResponse.setAction(NotifierHandlingActions.RETRY);
                log.error("NotifierMfsException for " + subscriberNumber + " while retrieving notification numbers, will retry: " + e.getMessage());
                if (notifierProfiler.isProfilerEnabled()) {
                    NotifierHelper.profilerCheckPoint("NTF.TNP.1.NH.SubP.NotifNumEx." + notifierTypeName);
                }                
                return incomingSignalResponse;
            }
            
            log.debug("Notification numbers retrieved for " + subscriberNumber + " " + notifierTypeName + ":" + Arrays.toString(notificationNumbers));
            if(notificationNumbers == null || notificationNumbers.length == 0) {
                incomingSignalResponse.setAction(NotifierHandlingActions.OK);
                if (notifierProfiler.isProfilerEnabled()) {
                    NotifierHelper.profilerCheckPoint("NTF.TNP.1.NH.SubP.NoNotifNum." + notifierTypeName);
                }                
                log.warn("Received event but no notification number found, ignoring event and sending response: " + incomingSignalResponse);
                return incomingSignalResponse;
            }
            
            // Create a Notifier event for each notification number to put on the Notifier workers' queue.
            for (String notificationNumber : notificationNumbers) {
                // Normalize B number
                notificationNumber = notifierUtil.getNormalizedTelephoneNumber(notificationNumber);

                // Normalize A number (sender), if found in the properties
                String sender = eventProperties.getProperty(NotifierConstants.SENDER_PHONE_NUMBER_PROPERTY);
                if (sender != null && !sender.isEmpty()) {
                    sender = notifierUtil.getNormalizedTelephoneNumber(sender);
                    eventProperties.setProperty(NotifierConstants.SENDER_PHONE_NUMBER_PROPERTY, sender);
                }

                NotifierEvent notifierEvent = null;
                try {
                    notifierEvent = new NotifierEvent(templateEntry, cphrNotifType, subscriberNumber, notificationNumber, eventProperties);
                } catch(NotifierMfsException e) {
                    switch(e.getNotifierMfsExceptionCause()) {
                        case FILE_DOES_NOT_EXIST:
                            incomingSignalResponse.setAction(NotifierHandlingActions.OK);
                            log.error("NotifierMfsException (file does not exist) for " + subscriberNumber + ":" + notificationNumber + ", will ignore event: " + e.getMessage());
                            if (notifierProfiler.isProfilerEnabled()) {
                                NotifierHelper.profilerCheckPoint("NTF.TNP.1.NH.Mfs.FDNExcep." + notifierTypeName);
                            }
                            return incomingSignalResponse;
                        case FILE_PARSING_ERROR:
                            //Delete payload file for some events where there is a payload file.
                            boolean isPayloadFileRemoved = NotifierHelper.removePayloadFile(notificationNumber, eventProperties);
                            if(isPayloadFileRemoved) {
                                incomingSignalResponse.setAction(NotifierHandlingActions.OK);
                                log.error("NotifierMfsException (parsing error) for " + subscriberNumber + ":" + notificationNumber + ", payload file deleted, will ignore event: " + e.getMessage());
                            } else {
                                incomingSignalResponse.setAction(NotifierHandlingActions.RETRY);
                                log.error("NotifierMfsException (parsing error) for " + subscriberNumber + ":" + notificationNumber + ", unable to delete payload file, will retry event: " + e.getMessage());
                            }
                            if (notifierProfiler.isProfilerEnabled()) {
                                NotifierHelper.profilerCheckPoint("NTF.TNP.1.NH.Mfs.FPExcep." + notifierTypeName);
                            }                            
                            return incomingSignalResponse;
                        case NO_CAUSE_SPECIFIED:
                        case FILE_NOT_ACCESSIBLE:
                            log.error("NotifierMfsException (temporary or unknown error) for " + subscriberNumber + ":" + notificationNumber + ": " + e.getMessage());
                            break;
                    }
                    // Stop looping through notification numbers because the same MFS error will probably occur again if we try again right away.
                    break;
                }
                
                //To avoid repeated database lookups, save the subscriber profile in the notifier event.
                //Trade-off of this is that any subscriber profile updates will not be seen for the current notification.
                notifierEvent.setSubscriberProfile(subscriberProfile);             

                /**
                 * Validate if the subscriber's storage is READ-ONLY (using the notification number).
                 * This is needed since scheduled events are stored in a .status file under the notification number.
                 * Even if the event is expired, let the scheduler retry so that any files associated with the current notification can be deleted properly.
                 */
                if (!notifierUtil.isFileStorageOperationsAvailable(notificationNumber)) {
                    log.warn("Storage currently not available for " + notifierEvent.getIdentity() + ", will let NTF component retry.");
                    countReadOnly++;
                    if (notifierProfiler.isProfilerEnabled()) {
                        NotifierHelper.profilerCheckPoint("NTF.TNP.1.NH.ReadOnly." + notifierTypeName);
                    }                    
                    continue;
                }
                
                //At this point, the decision has been made to handle at least part of the signal.
                //Now the plug-in code should delegate the actual sending of the notification to a plug-in thread
                //so that the NTF thread that is being used to execute this code will be freed.
                if (processIncomingNotifierEvent(notifierEvent, signalInfo.isExpiry())) {
                    countProcessed++;
                }
            }

            if (countProcessed > 0) {
                // At least 1 notifierEvent has been triggered (and/or discarded) for a given notification number, consider the notification successful.
                if (countReadOnly > 0) {
                    log.info("NotifierEvent " + notifierTypeName + " considered successful for " + subscriberNumber + " even if at least one notification number was is 'read only'" +
                            " since, at least, one notification number HAS been notified");
                }

                if (countDiscarded > 0) {
                    log.info("NotifierEvent " + notifierTypeName + " considered successful for " + subscriberNumber + " even if notification was discarded for at least one" +
                            " notification number since discarded also means processed");
                }
                
                /**
                 * For this sample plug-in, in the case of having multiple notification numbers, if one is successful
                 * and another one is not (has been asked to retry by the processIncomingNotifierEvent method),
                 * the notifier is considered successful.
                 */
                log.info("NotifierEvent " + notifierTypeName + " successful for " + subscriberNumber);
                incomingSignalResponse.setAction(NotifierHandlingActions.OK);
            } else {
                log.info("NotifierEvent " + notifierTypeName + " will retry for " + subscriberNumber);
                incomingSignalResponse.setAction(NotifierHandlingActions.RETRY);
            }

        } catch (Throwable e) {
            String message = "NotifierHandler exception for " + subscriberNumber;
            log.error(message, e);

            incomingSignalResponse.setAction(NotifierHandlingActions.RETRY);
        }

        return incomingSignalResponse;
    }

    

    private boolean processIncomingNotifierEvent(NotifierEvent notifierEvent, boolean isExpiry) {
        boolean scheduled = false;
        
             log.debug("New Notifier event to be processed for " + notifierEvent.getIdentity());

            //Since the plug-in is taking over at least part of the incoming signal, it is responsible for sending the notification it agreed to send.
            //For this sample plug-in, this includes scheduling a backup/retry so that the notification will not be lost if the NTF component shuts down
            //before the notification can be sent out.
            //An "initial" event is scheduled since this notification is entering the initial state of being sent.
            scheduled = NotifierEventHandler.get().scheduleInitial(notifierEvent);
            if (scheduled) {
                // Set notifierEvent event
                if (isExpiry) {
                    notifierEvent.setNotifierTypeEvent(NotifierTypeEvent.EVENT_SCHEDULER_EXPIRY);
                } else {
                    notifierEvent.setNotifierTypeEvent(NotifierTypeEvent.EVENT_NEW_NOTIF);
                }

                //Try to put the notifierEvent on the NotifierWorkers' queue.
                //This delegation of the handling of the notification will free the NTF thread that is being used
                //to execute this code.
                boolean storedInQueue = workingQueue.offer(notifierEvent);
                if (storedInQueue) {
                    log.debug("New Notifier event to be processed for " + notifierEvent.getIdentity());
                    if (notifierProfiler.isProfilerEnabled()) {
                        NotifierHelper.profilerCheckPoint("NTF.TNP.3.NH.PI.NotBlock.Queued." + notifierEvent.getNotifierTypeName());
                    }
                } else {
                    log.debug("NotifierWorkingQueue full while receiving a new notification for " + notifierEvent.getIdentity() + ", will retry");
                    if (notifierProfiler.isProfilerEnabled()) {
                        NotifierHelper.profilerCheckPoint("NTF.TNP.3.NH.PI.NotBlock.NotQueued." + notifierEvent.getNotifierTypeName());
                    }

                }
            } else {
                log.debug("Unable to schedule Initial event for " + notifierEvent.getIdentity() + ", will retry ");
                if (notifierProfiler.isProfilerEnabled()) {
                    NotifierHelper.profilerCheckPoint("NTF.TNP.3.NH.PI.NotBlock.NotSched." + notifierEvent.getNotifierTypeName());
                }
            }
        
        return scheduled;
    }

    public NotifierInfo isHandledByNotifierHandler(String receivedEventType, Properties eventProperties) {
        NotifierInfo result = null;
        
        receivedEventType = receivedEventType.toLowerCase();
        
        if (!TemplateType.isTemplateTypeDefined(receivedEventType)) {
        	if (!doesCphrTemplateExist(receivedEventType)) {
            	log.debug("event Type: " + receivedEventType + " does not have a  defined phrase in .cphr files, will not be handled by this plugin.");
            	return null;
            } else
            {
            	TemplateType.addTemplateTypeFromDefault(receivedEventType); //create the eventType.
            }        	
        }

        log.debug(receivedEventType + " will be handled by template SMS plug-in.");
        
        TemplateType templateEntry = TemplateType.getTemplateType(receivedEventType);            	
    	CPHRNotifTYPE cphrNotifType = templateEntry.getCphrNotifType();
        
		result = new NotifierInfo();
		result.typeName = receivedEventType;
		result.cphrNotifType = cphrNotifType;
		result.templateEntry = templateEntry; 
		return result;
    }
    
    private boolean doesCphrTemplateExist(String receivedEventType) {
    	return notifierMessageGenerator.doesCphrTemplateExist(receivedEventType);
	}

	public class NotifierInfo {
        public String typeName;
        public String cphrTemplateName;
        public CPHRNotifTYPE cphrNotifType;
        public TemplateType templateEntry;
    }

    public boolean isStarted() {
        return  isStarted;
    }

    private String[] getNotificationNumbers(INotifierNtfNotificationInfo ntfNotifInfo, ANotifierDatabaseSubscriberProfile subscriberProfile, TemplateType notifierType, CPHRNotifTYPE cphrNotifType ) throws InvalidEventException, NotifierMfsException {
        
        String[] numbers = null;

        switch (cphrNotifType){
            case MAILBOXSUBSCRIBER: //deliberate fall through
            case SUBSCRIBERONLY:          
                // Check where the notification number should be taken from.
                switch (notifierType.getNotifierTypeNotificationNumber()){
                    case RECIPIENT:
                        // Use receiver number directly as notification number
                        numbers = new String[1];
                        numbers[0] = ntfNotifInfo.getReceiverTelephoneNumber();
                        return numbers;
                    case DELIVERY_PROFILE:      
                        // Get the notification number(s) from the delivery profile associated with the subscriber.
                        if (subscriberProfile == null) {
                            log.error("getNotificationNumbers, subscriberProfile is null");
                            return new String[0];
                        } 
                        numbers = subscriberProfile.getSubscriberNotificationNumbers(NotificationType.SMS, new NotifierNotificationInfo(ntfNotifInfo.getNotificationEventProperties()));
                        return numbers;
                    default:
                        log.error("getNotificationNumbers, NotifierTable.notificationNumber value cannot be recognized: " + notifierType.getNotifierTypeNotificationNumber());
                        throw new InvalidEventException("Invalid event notificationNumber value");
                }               
            case NONSUBSCRIBER:
                if (notifierType.getNotifierTypeNotificationNumber() == NotifierTypeNotificationNumber.DELIVERY_PROFILE) {
                	log.warn("Cannot specifiy a delivery profile as recipient for non subscriber, taking recipient instead");
                    numbers = new String[1];
                    numbers[0] = ntfNotifInfo.getReceiverTelephoneNumber();
                    return numbers;                	
                }
            default:
                throw new InvalidEventException("Invalid event handlednotification type");

        }
    }

}
