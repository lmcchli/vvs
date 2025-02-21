/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.outdial;

import java.io.StringReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.common.hlr.HlrAccessManager;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.mnr.SubscriberInfo;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.ssmg.AnyTimeInterrogationResult;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateAttributesFilter;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scriptengine.tokens.CONCAT;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.ss7.ISs7Manager;
import com.abcxyz.services.moip.common.ss7.Ss7Exception;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.NtfRetryHandling;
import com.abcxyz.services.moip.ntf.coremgmt.OdlEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackHandler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.coremgmt.reminder.SmsReminder;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventGenerator;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.abcxyz.services.moip.ntf.event.OdlEvent;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.commands.Command;
import com.mobeon.common.commands.Operation;
import com.mobeon.common.commands.CommandHandler;
import com.mobeon.common.commands.CommandException;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.util.threads.NtfThread;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.UserInfo.NotifState;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.NtfCompletedListener;
import com.mobeon.ntf.NtfMain;
import com.mobeon.ntf.Constants.hlrFailAction;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.mail.MessageDepositInfo;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.out.FeedbackHandlerImpl;
import com.mobeon.ntf.phonedetection.PhoneStatus;

/**
 * Does the synchronization of an out-dial notification.
 * A number of workers can be started for one out-dial listener.
 * The workers get their workloads via a ManagedArrayBlockingQueue.
 */
class OdlWorker extends NtfThread {
    
    static CommandHandler NOOP_COMMAND_HANDLER;
    static {
        String s = "maxwaithours = 1\n"
            + "initialstate = 0\n"
            + "numberofstates = 2\n"
            + "default.900 = END/fallback\n"
            + "state.1.default = END/fallback\n";

        try {
            Properties p = new Properties();
            p.load(new StringReader(s));

            NOOP_COMMAND_HANDLER = new CommandHandler(p);
        } catch (Exception e) {
            System.err.println("OdlWorker-NOOP command handler: unexpected Exception:");
            e.printStackTrace();
        }
    }

    private static LogAgent logger = NtfCmnLogger.getLogAgent(OdlWorker.class);

    private Map<String, CommandHandler> commandHandlers;
    private ManagedArrayBlockingQueue<Object> queue;
    private OdlCallSpec caller;
    private OdlCallListener callListener;
    private PhoneOnSender phoneon;
    private MerAgent mer;
    private IEventStore eventStore;
    private OdlEventHandler eventHandler;
    private MfsEventManager mfsEventManager;


    /**
     * Creates a new instance.
     * @param commandHandlers maps names to command handlers
     * @param queue work queue where work items is found
     * @param caller Outgoing calls are made through this
     * @param phoneon Phone on requests are made through this
     * @param threadName Thread name.
     * @param eventStore ODL Event Store.
     */
    public OdlWorker(Map<String, CommandHandler> commandHandlers,
                     ManagedArrayBlockingQueue<Object> queue,
                     OdlCallSpec caller,
                     PhoneOnSender phoneon,
                     String threadName,
                     IEventStore eventStore)
    {
        super(threadName);
        this.commandHandlers = commandHandlers;
        this.queue = queue;
        this.caller = caller;
        this.callListener = new WorkerCallListener();
        this.phoneon = phoneon;
        if(phoneon == null) {
            
                logger.debug("OutdialWorker phoneon is null"); 
        }
        this.mer = MerAgent.get();
        this.eventStore = eventStore;
        eventHandler = (OdlEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.OUTDIAL.getName());
        this.mfsEventManager = MfsEventFactory.getMfsEvenManager();

    }

    /**
     * Do one step of the work.
     * @return False if the work should continue, true if the worker wants to stop.
     */
    public boolean ntfRun()
    {
        boolean status = false;

        OdlEvent info = (OdlEvent) queue.poll(10, TimeUnit.SECONDS);
        if (info != null) {
            synchronized (info) {
                try {
                    if (logger.isDebugEnabled())
                        logger.debug("OutdialWork run: " + info + this.getName());

                    UserInfo userInfo = getUserInfo(info);
                    if (userInfo == null) {
                        logger.error("Subscriber profile not found for " + info.getIdentity() + ", cancelling event " + info);
                        doClean(info, true);
                    } else {
                if ( !isTooOld(info, userInfo) && info.isEventServiceName(NtfEventTypes.EVENT_TYPE_ODL_WAITON.getName())
                        && !info.isExpiry() ) {
                    // handling an Odl WaitOn event before maxWaitHours
                    // means we need to retry asking for a phone on
                    // doWaiton() does just that and takes care of cancelling the existing event
                    logger.debug("OutdialWork run: calling doWaiton() on notTooOld ODL_WAITON event for " + info);
                    doWaiton(info, userInfo);
                }
                else if (isTooOld(info, userInfo) || info.isExpiry()) {
                            handleExpire(info);
                        } else {
                            // ??check using telephone number initially
                            boolean toContinue = checkUserLogin(info, info.getRecipentId());

                            if (toContinue) {
                                checkCommand(info, userInfo);
                                if (info.getOdlTrigger() == OdlConstants.EVENT_OUTDIAL_START) {
                                    if (logger.isDebugEnabled())
                                        logger.debug("OutdialWork run: handleStart");
                                    handleStart(info, userInfo);
                                } else if (info.isFromNotify()) {// when the event comes from scheduler
                                    if (logger.isDebugEnabled())
                                        logger.debug("OutdialWork run: handleNotification");
                                    handleNotification(info, userInfo);
                                } else {
                                    logger.debug("OutdialWork run: handleTriggerEvent");
                                    handleTriggedEvent(info, userInfo);
                                }
                            }
                        }
                        info.notifyObservers();
                    }
                } catch (CommandException ce) {
                    logger.error("OdlWorker-CommandException: " + ce.getMessage(), ce);
                    doFallback(info);
                startReminderTriggerScheduler(info);
                } catch (Exception e) {
                    logger.error("Unexpected exception in OdlWorker " + e.getMessage(), e);
                    doFallback(info);
                startReminderTriggerScheduler(info);
                } finally {
                    try {
                        long lockid = info.getPhoneOnLock();
                        logger.debug("PhoneOn lock: " + lockid);
                        if (lockid != 0) {
                            boolean internal = mfsEventManager.isInternal(info.getTelNumber());
                            ;
                            mfsEventManager.releaseLockFile(info.getTelNumber(), MfsEventStore.OUTDIAL_PHONE_ON_LOCK_FILE, lockid,
                                    internal);
                            info.setPhoneOnLock(0);
                        }
                    } catch (Exception e) {
                        logger.warn("Exception trying to release lock file " + MfsEventStore.OUTDIAL_PHONE_ON_LOCK_FILE + " for "
                                + info.getRecipentId() + " : " + info.getTelNumber(), e);
                    }
                }
            }
        }

        return status;
    }

    private boolean checkUserLogin(OdlEvent info, String recipientId) {
        //MfsEventManager mfs = MfsEventFactory.getMfsEvenManager(CommonMessagingAccess.getInstance(), CommonMessagingAccess.getInstance().getMcd());
        MfsEventManager mfs = MfsEventFactory.getMfsEvenManager();

        boolean login = mfs.loginFileExistsAndValidDate(recipientId, Config.getLoginFileValidityPeriod());

        boolean toContinue = true;
        if (!login) {
            // Subscriber is not logged-in
            if (info.isFromLogin()) {
                // Subscriber was previously logged-in (this invocation is coming from OdlEventHandler) but not anymore
                logger.debug("Subscriber " + info.getIdentity() + " is not logged in any more, cancel login retry: " + info.getReferenceId());
                info.setFromLogin(false);
                eventHandler.cancelEvent(info.getReferenceId());
                info.keepReferenceID(null);
            } else {
                logger.debug("Subscriber " + info.getIdentity() + " is not logged in, go forward with notfication");
            }
        } else {
            // Subscriber is currently logged-in
            if (!info.isFromLogin()) {
                // This invocation is not coming from OdlEventHandler-Login-Retry
                String eventId = info.getReferenceId();
                if (info.getOdlCode() != OdlConstants.EVENT_CODE_COMPLETED) {
                    toContinue = false;
                    eventHandler.scheduleLoginRetry(info);
                    logger.debug("Subscriber " + info.getIdentity() + " is currently logged in, start LOGIN timer: " + info.getReferenceId());
                }
                eventHandler.cancelEvent(eventId);
            } else {
                // Subscriber is still logged-in
                logger.debug("Subscriber " + info.getIdentity() + " is still logged in, will retry later: " + info.getReferenceId());
                toContinue = false;
            }
        }

        return toContinue;
    }

    /**
     * Find out if an ODl event is too old to continue.
     * @param info out dial info
     * @return true if the info is too old, false otherwise.
     */
    private boolean isTooOld(OdlEvent info, UserInfo userInfo)
    {
        long nowMS = System.currentTimeMillis();
        long startMS = info.getStartTime();
        CommandHandler handler = findCommandHandler(info, userInfo);

        long endAtMS;

        if (handler.getMaxWaitTime() != -1) {
        	endAtMS = startMS + handler.getMaxWaitTime();
        } else {
        	endAtMS = startMS + handler.getMaxWaitHours() * 60 * 60 * 1000;
        }

        return endAtMS < nowMS;
    }

    private void handleExpire(OdlEvent odlEvent) {

        logger.debug("Handling Expiry of " + odlEvent.getEventServiceName() + "-" + odlEvent.getEventServiceTypeKey() + " for subscriber " + odlEvent.getIdentity());

        if (odlEvent.isEventServiceName(NtfEventTypes.EVENT_TYPE_ODL_START.getName())) {
            // Start Reminder trigger timer
            startReminderTriggerScheduler(odlEvent);

            // Fallback to the configured notification type
            doFallback(odlEvent);

        } else if (odlEvent.isEventServiceName(NtfEventTypes.EVENT_TYPE_ODL_LOGIN.getName())) {
            // Start Reminder trigger timer
            startReminderTriggerSchedulerForLogin(odlEvent);

            // Fallback to the configured notification type
            doFallback(odlEvent);

        } else if (odlEvent.isEventServiceName(NtfEventTypes.EVENT_TYPE_ODL_WAIT.getName())) {
            // Fallback to the configured notification type
            doFallback(odlEvent);

        } else if (odlEvent.isEventServiceName(NtfEventTypes.EVENT_TYPE_ODL_WAITON.getName())) {
            // Cancel pending Reminder timer
            eventHandler.cancelReminderTriggerEvent(odlEvent.getSchedulerIdReminder());
            odlEvent.setSchedulerIdReminder(null);

            // Fallback to the configured notification type
            doFallback(odlEvent);

        } else if (odlEvent.isEventServiceName(NtfEventTypes.EVENT_TYPE_ODL_CALL.getName())) {
            if (odlEvent.isReminder()) {
                /**
                 * Notification expired while it was under a Reminder notification (not an original notification)
                 * There is already a schedulerIdReminder started, this timer is kept for user experience.
                 */
            } else {
                // Start Reminder trigger timer
                startReminderTriggerScheduler(odlEvent);
            }

            // Fallback to the configured notification type
            doFallback(odlEvent);

        } else if (odlEvent.isEventServiceName(NtfEventTypes.EVENT_TYPE_ODL_REMINDER.getName())) {
            // No fallback when Reminder expiry

            // Cancel all the timers and remove the persistent file
            doClean(odlEvent, true);
        } else {
            logger.error("Event expired not handled, will fallback.  EventServiceName: " + odlEvent.getEventServiceName() +
                    " EventServiceTypeKey: " + odlEvent.getEventServiceTypeKey() +
                    " EventTypeKey: " + odlEvent.getEventTypeKey());
            doFallback(odlEvent);
            startReminderTriggerScheduler(odlEvent);
        }
    }

    /**
     * Find out if the subscriber has unread messages with active outdial filter
     * @param info out dial info
     * @param userInfo the user information
     * @return MessageDepositInfo of highest priority if messages exist, null otherwise.
     */
    private MessageDepositInfo hasUnreadMessages(OdlEvent info, UserInfo userInfo) {
        //Check user quota. Maybe user has already called in.
        //We do not want to call the user if he has already retrieved new messages
        StateAttributesFilter stateAttributesFilter = new StateAttributesFilter();
        stateAttributesFilter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, MoipMessageEntities.MESSAGE_NEW);
        String msid = userInfo.getMsid();
        MSA msa = new MSA(msid);
        StateFile[] stateFiles = null;
        MessageDepositInfo msg = null;
        
        try {
            stateFiles = CommonMessagingAccess.getInstance().searchStateFiles(msa, stateAttributesFilter);
            if(stateFiles == null || stateFiles.length == 0){
                logger.debug("OdlWorker.hasUnreadMessages: subscriber has already retrieved messages");
                return null;
            }
            for (int i=0; i < stateFiles.length; i++)    {
                //find unread urgent message if any, this is used when fetching filter later..
                msg = new MessageDepositInfo(stateFiles[i], info.getRecipient());
                msg.init();
                if (msg.isUrgent());
                return msg; //if not return a typical msg or null.
            } 
             return msg;   
        } catch (MsgStoreException e1) {
            //error just continue
            logger.error("OdlWorker.hasUnreadMessages: error while counting unread messages: " + e1.getMessage(),e1);
        }
        return msg;
    }

    /**
     * @param msg MessagInfo to check for out-dial, if urgent will use urgent filters..
     * @param info The info about the current event.
     * @param userInfo The userInfo of the message to be sent.
     * @return a valid filter or null if none.
     * @throws CommandException - if a problem with HLR fail which cannot report.
     */
    private NotificationFilter getValidFilter(MessageDepositInfo msg, OdlEvent info, UserInfo userInfo) throws CommandException {

        //Make sure filter is still active, for example in business hours, roaming etc, disabled..
        NotificationFilter filter = null;
        GregorianCalendar now = new GregorianCalendar();
        boolean validFilterFound = false;

        filter = userInfo.getFilter();
        
        FeedbackHandlerImpl fh = new FeedbackHandlerImpl(); //to determine if HLR query went OK and what to do it not.
        if( filter.getOdlFilterInfo(msg, now, fh) != null ) {               
            validFilterFound = true;
        } else
        {
            switch (fh.getStatus()) {
                case Constants.FEEDBACK_STATUS_OK:
                    //no matching filter found, this includes urgent, roaming, business hours etc.
                    break; //normal case continue
                case Constants.FEEDBACK_STATUS_FAILED:
                case Constants.FEEDBACK_STATUS_RETRY:
                    logger.debug("HLR error when checking filters, will let state machine decide what to do.");;
                    handleHLRError(info, userInfo); //indicate problem to state machine..
                    return null;
                default:
                    logger.warn("Unexpected result while fetching filter assuming retry " + fh.getStatus());
                    handleHLRError(info, userInfo); //indicate problem to state machine..
                    return null;
            }
        }
        

        if(!validFilterFound) {
            logger.debug("OdlWorker.getValidFilter: outdial filter: invalid, probably out of business hours or filter(s) changed, roaming state changed or notif disabled, etc.");
            return null;
            
        } else {
            return filter;
        }
    }
    
	/**
	 * Checks if the subscriber is busy by using the ATI MAP command
	 * @param result AnyTimeInterrogationResult
	 * @param info OdlEvent
	 * @param userInfo UserInfo
	 * @return true if subscriber is busy, additionally sets the prepaid status
	 */
    private boolean isSubscriberBusy(AnyTimeInterrogationResult result, OdlEvent info, UserInfo userInfo) {

		AnyTimeInterrogationResult.SUBSCRIBER_STATE state = result.getSubscriberState();

		if(state.equals(AnyTimeInterrogationResult.SUBSCRIBER_STATE.CAMELBUSY)){
			if(logger.isDebugEnabled()){
				logger.debug("OdlWorker.doCall: subscriber is busy " + info.getTelNumber());
			}
			return true;
		} else {
			if(logger.isDebugEnabled()){
				logger.debug("OdlWorker.doCall: subscriber is not busy " + info.getTelNumber());
			}
		}

		return false;
    }

    /**
     * Find out how long until the out-dial will expire.
     * @param info out-dial info
     * @return the number of seconds left in out-dial notification.
     */
    /*private int getTimeToExpire(OdlEvent info, UserInfo userInfo)
    {
        Calendar now = Calendar.getInstance();
        long nowMS = now.getTime().getTime();
        long startMS = info.getStartTime();
        long expiredMS = nowMS - startMS;
        CommandHandler handler = findCommandHandler(info, userInfo);
        long totalMS = handler.getMaxWaitHours() * 60 * 60 * 1000;
        int timeLeft = (int) (totalMS - expiredMS) / 1000;
        if( timeLeft < 0 ) {
            timeLeft = 0;
        }
        return timeLeft;
    }*/

    /**
     * Take care of restarted events.
     * @param info OdlEvent
     * @param userInfo UserInfo
     * @throws CommandException if a problem is found in the state machine
     */
    private void handleStart(OdlEvent info, UserInfo userInfo) throws CommandException
    {
        logger.debug("OdlWorker Starting : " + info);
        getFirstCommand(info, userInfo);
        doNextCommand(info, userInfo);
    }

    /**
     * Take care of notifications.
     * @param info outdial info
     * @throws CommandException if a problem was found in the state machine
     */
    private void handleNotification(OdlEvent info, UserInfo userInfo)
        throws CommandException
    {
    	if (logger.isDebugEnabled()) {
    		logger.debug("OdlWorker handling notification <" +
    				info.getOdlTrigger() +
    				"> for " +
    				info);
    	}

    	String event = info.getOdlTrigger();
        Command currentCommand = info.getCommand();

        if (event == null) {
        	//wait time comes to here
            // Assume operation is done, go to next
        	if (logger.isDebugEnabled())
        		logger.debug("No event for notification : " + info);
            if (currentCommand != null)
            	currentCommand.operationDone();

            doNextCommand(info, userInfo);
        } else if (OdlConstants.EVENT_OUTDIAL_CALL_MADE.equals(event)) {
            if ((currentCommand != null) && (currentCommand.getCurrentOperation().getOpcode() == CommandHandler.OP_CALL)) {
                // We wanted to make a call and it was done, let state machine decide
                currentCommand.operationDone();

                // Send MER event about call status
                if ((info.getOdlCode() == OdlConstants.EVENT_CODE_COMPLETED) ||
                    (info.getOdlCode() == OdlConstants.EVENT_CODE_INITIATED)) {

                    if (OutdialNotificationOut.getMer()) {
                        mer.notificationDelivered(info.getRecipient(), Constants.NTF_ODL);
                    }
                    logger.info("ODL call made for: " + info.getIdentity());
                }

                doNextCommand(info, userInfo);
            } else {
                if ((info.getOdlCode() == OdlConstants.EVENT_CODE_COMPLETED) ||
                    (info.getOdlCode() == OdlConstants.EVENT_CODE_INITIATED)) {
                	//transition "default.200 = END/" comes to here
                	if (logger.isDebugEnabled())
                		logger.debug("Outdialworker: Unexpected Call sucessfully made, quit outdial: " + info.getIdentity());

                    mer.notificationDelivered(info.getRecipient(), Constants.NTF_ODL);
                    logger.info("ODL call finished successful for:" + info.getIdentity());

                    doClean(info);
                } else {
                	if (logger.isDebugEnabled())
                		logger.debug("OdlWorker: call made with response code: " + info.getOdlCode());
                    doNextCommand(info, userInfo);
                }
            }
        } else if (OdlConstants.EVENT_OUTDIAL_PHONEON.equals(event)) {
            if ((currentCommand != null) && (currentCommand.getCurrentOperation().getOpcode() == CommandHandler.OP_WAITON)) {
            	if (info.getOdlCode() == OdlInfo.EVENT_CODE_PHONEON) {
    				eventHandler.cancelEvent(info.getReferenceId());
    				info.keepReferenceID(null);
            		currentCommand.operationDone();
            		mer.phoneOnDelivered(info.getRecipient());
            		doNextCommand(info, userInfo);
            	}else if(info.getOdlCode() == OdlInfo.EVENT_CODE_BUSY){
    				eventHandler.cancelEvent(info.getReferenceId());
    				info.keepReferenceID(null);
            		while(currentCommand.getOperationCount() > 0){
            			currentCommand.operationDone();
            		}
            		doNextCommand(info, userInfo);

            	} else if (info.getOdlCode() == OdlInfo.EVENT_CODE_SS7_ERROR) {
            		if (logger.isDebugEnabled())
                		logger.debug("OdlWorker.handleNotification() odlCode=ss7error ");
            		eventHandler.cancelEvent(info.getReferenceId());
    				info.keepReferenceID(null);
            		while(currentCommand.getOperationCount() > 0){
            			currentCommand.operationDone();
            		}
            		mer.phoneOnFailed(info.getRecipient());
            		doNextCommand(info, userInfo);
            	} else if (info.getOdlCode() == OdlInfo.EVENT_CODE_DESTINATION_NOT_REACHABLE) {
                    mer.phoneOnFailed(info.getRecipient());
            	}
            } else {
            	if (logger.isDebugEnabled())
            		logger.debug("OdlWorker: Phone on result when not " + "expecting, continuing ordinary sequence");
                // Nothing to do, ordinary notification should be coming
            }
        } else if (OdlConstants.EVENT_OUTDIAL_START.equals(event)) {
            // This is the start of out-dial, no earlier commands have been done
            // Just start with doing the first command
        	//cancel start event

            getFirstCommand(info, userInfo);
            doNextCommand(info, userInfo);
        } else {
            logger.warn("OdlWorker: Continues, Unknown event name : " + info.getOdlCode());
            // Assume current operation done and continue
            if (currentCommand != null) currentCommand.operationDone();
            doNextCommand(info, userInfo);
        }
    }


    /**
     * Take care of out-dials that have waited for a time.
     * @param info out-dial info
     * @throws CommandException if a problem was found in the state machine (e.g. an
     *  event code that was not defined in the current state.)
     */
 /*   private void handleTimeBased(OdlEvent info, UserInfo userInfo)
        throws CommandException
    {
    	if (logger.isDebugEnabled())
    		logger.debug("Outdialworker, timebased for " + info);

        Command currentCommand = info.getCommand();
        Operation currOp = currentCommand.getCurrentOperation();
        if (currOp.getOpcode() == CommandHandler.OP_WAITTIME) {
            // ok this far..., find out what to do next
            currentCommand.operationDone();
            doNextCommand(info, userInfo);
        } else {
            logger.info("OdlWorker: Got time notif when not waiting for one, retries command");
            doNextCommand(info, userInfo);

        }
    }*/

    private void handleTriggedEvent(OdlEvent info, UserInfo userInfo) throws CommandException {
    	if (logger.isDebugEnabled())
    		logger.debug("Outdialworker, triggered event for " + info);

        Command currentCommand = info.getCommand();
        if (currentCommand == null) {
        	getFirstCommand(info, userInfo);
        	currentCommand = info.getCommand();
        }

        Operation currOp = currentCommand.getCurrentOperation();
        //keep current operation code
        info.setCurrentOperation(currOp.getOpcode());
    	if (logger.isDebugEnabled())
    		logger.debug("Outdialworker, triggered event for "+ info+ " , currentOp="+ currOp.toString());
        if (currOp.getOpcode() == CommandHandler.OP_WAITTIME) {
            // ok this far..., find out what to do next
            currentCommand.operationDone();
            doNextCommand(info, userInfo);
        } else if (currOp.getOpcode() == CommandHandler.OP_WAITON) {
        	if(info.getOdlCode() == OdlInfo.EVENT_CODE_BUSY){
            	while(currentCommand.getOperationCount() > 0){
                	currentCommand.operationDone();
                }
            	info.setOdlTrigger(OdlConstants.EVENT_OUTDIAL_BUSY);
                doNextCommand(info, userInfo);
            } else if (info.getOdlCode() == OdlInfo.EVENT_CODE_DEFAULT) {
                logger.info("OdlWorker: trggered event: " + info.getOdlTrigger() + " execute next command");
                doNextCommand(info, userInfo);
            } else if (info.getOdlCode() == OdlInfo.EVENT_CODE_PHONEON) {
                logger.info("OdlWorker: trggered event: " + info.getOdlTrigger() + " execute next command");
                currentCommand.operationDone();
                doNextCommand(info, userInfo);
            }
        } else {
            logger.info("OdlWorker: trggered event: " + info.getOdlTrigger() + " execute next command");
            doNextCommand(info, userInfo);
        }
    }

    
    private void sendRoamSMS(OdlEvent info)
    {
      //convert to NTF event
      NtfEvent event = NtfEventGenerator.generateEvent(NtfEventTypes.ROAMING.getName(), info.getMsgInfo(), info.getEventProperties(), info.getReferenceId());
      event.setRoaming(true);

      //schedule backup
      NtfRetryHandling handler = NtfEventHandlerRegistry.getEventHandler(event.getEventServiceTypeKey());

      if (handler != null) {
          String backupId = handler.scheduleEvent(event);
          event.keepReferenceID(backupId);

          if (logger.isDebugEnabled()) {
              logger.debug("OdlWorker.sendRoamSMS() Roaming scheduled backup: " + backupId);
          }
      }

      NtfEventHandlerRegistry.getNtfEventReceiver(NtfEventTypes.ROAMING.getName()).sendEvent(event);

      if (logger.isDebugEnabled()) {
          logger.debug("OdlWorker.sendRoamSMS() Sent roaming NtfEvent: " + event.getEventUid());
      }
        //No other operations should be done after a RoamSMS.
        doClean(info); 
    }
    /**
     * Do fallback handling when the ordinary command has failed in some way.
     * E.g. a CommandException has been thrown.
     */
    private void doFallback(OdlEvent info)
    {
        if (FallbackHandler.get() != null) {
            logger.info("Falling back for " + info);
            FallbackHandler.get().fallback(Constants.NTF_ODL, info);
        } else {
            logger.info("Fallback requested but not initialized for " + info);
        }

        // No other operations should be done after fallback
        doClean(info);
    }

    /**
     * Populate the info with first command to do.
     * @param info out-dial info
     * @throws CommandException if a problem was found in the state machine (e.g. an
     *  event code that was not defined in the current state.)
     */
    private void getFirstCommand(OdlEvent info, UserInfo userInfo)
        throws CommandException
    {
        CommandHandler cmdHandler = findCommandHandler(info, userInfo);
        info.setCurrentState(cmdHandler.getInitialState());
        Command nextCommand = cmdHandler.getCommand(cmdHandler.getInitialState(), OdlConstants.EVENT_CODE_DEFAULT);
        info.setCommand(nextCommand);

        if (logger.isDebugEnabled())
        	logger.debug("OdlWorker Found first command, info= " + info);
    }

    /**
     * Do the next command, if needed look up into command handler.
     * @param info out-dial info
     * @throws CommandException if a problem was found in the state machine (e.g. an
     *  event code that was not defined in the current state.)
     */
    private void doNextCommand(OdlEvent info, UserInfo userInfo) throws CommandException
    {
    	logger.debug("OdlWorker doNextCommand");
        Command currentCommand = info.getCommand();

        int nextState = currentCommand.getNextState();
        logger.debug("CurrentCommand.getNextState(): " +nextState);

        if ((nextState != CommandHandler.STATE_FINAL) && (currentCommand.getOperationCount() == 0)) {
            // All done with this command, find next
        	int eventCode = findEventCode(info);
        	if (logger.isDebugEnabled()) logger.debug("OdlWorker All done with this command, find next with eventCode: " + eventCode);
            CommandHandler cmdHandler = findCommandHandler(info, userInfo);
            Command nextCommand = cmdHandler.getCommand(nextState, eventCode);
            if (logger.isDebugEnabled()) logger.debug("Next Command: " + ", " + nextCommand.toString());
            info.setCommand(nextCommand);
            info.setCurrentState(nextState);
            currentCommand = nextCommand;
            nextState = currentCommand.getNextState();
        }

        /**
         * Now check if we are without commands.
         * If correctly configured, we are at final state (END/),
         * otherwise we got a command without operations, leading to a non final state.
         */
        if (currentCommand.getOperationCount() == 0) {
            if (nextState != CommandHandler.STATE_FINAL) {
                logger.info("OdlWorker No operations for command without going to final state");
            }
            logger.debug("Done with outdialchain: " + info);

            // Start a Reminder scheduler
            startReminderTriggerScheduler(info);

            doClean(info);
            return;
        }

        Operation currentOp = currentCommand.getCurrentOperation();
        info.setCurrentOperation(currentOp.getOpcode());
        info.notifyObservers();
        try {
            long lockid = info.getPhoneOnLock();
            logger.debug("PhoneOn lock: "+ lockid);
            if (lockid != 0) {
                boolean internal = mfsEventManager.isInternal(info.getTelNumber());;
                mfsEventManager.releaseLockFile(info.getTelNumber(), MfsEventStore.OUTDIAL_PHONE_ON_LOCK_FILE, lockid, internal);
                info.setPhoneOnLock(0);
            }
        } catch (Exception e) {
            logger.warn("Exception trying to release lock file " + MfsEventStore.OUTDIAL_PHONE_ON_LOCK_FILE + " for " + info.getRecipentId() + " : " + info.getTelNumber(), e);
        }

        logger.debug("OdlWorker - currentState: " + info.getCurrentState() +  " Op = " + currentOp + " nextstate: " + nextState);

        switch (currentOp.getOpcode()) {
            case CommandHandler.OP_CALL:
                doCall(info, userInfo);
                break;
            case CommandHandler.OP_WAITON:
                doWaiton(info, userInfo);
                break;
            case CommandHandler.OP_WAITTIME:
                doWaitTime(info, currentOp.getParam());
                break;
            case CommandHandler.OP_NOOP:
                doWaitTime(info, "120");
                break;
            case CommandHandler.OP_FALLBACK:
                startReminderTriggerScheduler(info);
                doFallback(info);
                break;
            case CommandHandler.OP_ROAMSMS:
                sendRoamSMS(info);
                break;
            default:
                logger.error("OdlWorker Unknown opcode in command" + info);

                doClean(info);
        }
    }

    /**
     * Remove this info from storage.
     * @param odlEvent OdlEvent.
     */
    private void doClean(OdlEvent odlEvent)
    {
        doClean(odlEvent, false);
    }

    /**
     * Remove this info from storage.
     * @param odlEvent OdlEvent.
     */
    private void doClean(OdlEvent odlEvent, boolean force)
    {
        if (odlEvent.getReferenceId() != null) {
            eventHandler.cancelEvent(odlEvent.getReferenceId());
            odlEvent.keepReferenceID(null);
        }

        if (force) {
            if (odlEvent.getSchedulerIdReminder() != null) {
                eventHandler.cancelReminderTriggerEvent(odlEvent.getSchedulerIdReminder());
                odlEvent.setSchedulerIdReminder(null);
            }
        }

        if (odlEvent.getReferenceId() == null && odlEvent.getSchedulerIdReminder() == null) {
            eventStore.remove(odlEvent);
        }
        logger.debug("OdlWorker - Cleaned: " + odlEvent);
    }

    /**
     * Attempt to make a call to user.
     * @param info Information about the call.
     * @throws CommandException CommandException
     */
    private void doCall(OdlEvent info, UserInfo userInfo) throws CommandException
    {
        if (userInfo != null) {

            //Check if user has unread messages.
            MessageDepositInfo msg = hasUnreadMessages(info, userInfo);
            if(msg == null){
                //no unread messages.
                doClean(info, true);
                return;
            }

            // Validate if the subscriber's storage is READ-ONLY (using the notification number) where the event is stored.
            if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(info.getTelNumber())) {
                logger.warn("Storage currently not available to process Outdial call for " + info.getRecipentId() + " : " + info.getTelNumber() + ", will retry");
                return;
            }

            Boolean isRoaming = false; //as reference so we can pass it by reference while checking filter.
            isRoaming = new Boolean(false);
            //Check if subscriber is roaming or busy and get postpaid/prepaid status
            if(CommonMessagingAccess.getInstance().getSs7Manager().useHlr()) {
                // If HLR access is enabled, query the HLR for subscriber status and then act upon the obtained status

                boolean isBusy = false;

                String method = CommonMessagingAccess.getInstance().getSs7Manager().getSubStatusHlrInterrogationMethod();

                // Perform an Anytime Interrogation to get subscriber roaming and busy status
                if(method.equalsIgnoreCase(ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_ATI)) {
                    if ((Config.getCheckBusy() || Config.getCheckRoaming() || Config.getSubscriberChargingModel())) {
                        AnyTimeInterrogationResult result;
                        try {

                            Object perf = null;
                            try {
                                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                                    perf = CommonOamManager.profilerAgent.enterCheckpoint("OdlWorker.doCall.requestATI");
                                }
                                result = CommonMessagingAccess.getInstance().getSs7Manager().requestATI(info.getTelNumber());
                            } finally {
                                if (perf != null) {
                                    CommonOamManager.profilerAgent.exitCheckpoint(perf);
                                }
                            }

                            if (result.isError()) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("OdlWorker.doCall() Ss7 error after  requestATI()");
                                }
                                handleHLRError(info, userInfo);
                                return;
                            }

                            //isRoaming[0] = CommonMessagingAccess.getInstance().getSs7Manager().isRoaming_ATI(result);
                            isBusy = isSubscriberBusy(result, info, userInfo);

                            if(Config.getSubscriberChargingModel()) {
                                //set prepaid/postpaid status
                                userInfo.setPrePaid(result.getAtiSubscriberInfoExt().isPrepay());
                            }

                            logger.debug("Info from ATI. isBusy: " + (isBusy ? "YES" : "NO") + 
                                    " ,isRoaming = " + (isRoaming ? "TRUE" : "NO"));

                        } catch (Ss7Exception e) {
                            logger.error("OdlWorker.doCall ss7 exception: " + e.getMessage(), e);
                            handleHLRError(info, userInfo);
                            return;
                        }
                    }

                    // Perform a Send Routing Information for Short Message (SRIforSM) to get subscriber roaming and busy status
                } else if(method.equalsIgnoreCase(ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_SRIFORSM)) {
                    if (Config.getCheckBusy() || Config.getCheckRoaming()) {
                        boolean isPhoneOn = false;
                        ISs7Manager ss7mgr = CommonMessagingAccess.getInstance().getSs7Manager();

                        if (ss7mgr != null) {
                            SubscriberInfo subInfo;
                            try {
                                subInfo = ss7mgr.getSubscriberInfo(info.getTelNumber());

                                if (subInfo != null && Config.getCheckBusy()) {
                                    isPhoneOn = subInfo.getSubscriberStatus();
                                    isBusy = !isPhoneOn; // If the phone is on, we assume it is not busy even if it is
                                }

                                if(logger.isDebugEnabled()) {
                                    logger.debug("Info from SRIforSM. Subscriber Status (phone is on): " + (isPhoneOn ? "ON" : "OFF") + 
                                            (subInfo != null ? " , Roaming status is (1=is roaming, 0=not roaming, -1=unknown): " + subInfo.getRoamingStatus() 
                                                    : " , Roaming status is: " + (isRoaming ? "ROAMING" : "NOT ROAMING")));
                                }

                            } catch (Ss7Exception e) {
                                logger.error("Could not determine whether subscriber's phone is turned on.  Assuming phone is "
                                        + (isPhoneOn ? "on" : "off"), e);
                            }
                        }
                    }                    
                } else if (method.equalsIgnoreCase(ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_CUSTOM)) { // VFE_NL MFD

                    if (Config.getCheckRoaming()) {
                        logger.debug("OdlWorker.doCall() HLR Access method = custom; to use HlrAccessManager");
                        //TODO should convert other method (if above) to use the phone status method but for now just VFE_NL, should be central.
                        //as would need to add busy, pre pay etc to  phoneStatus.
                        //but to deal with query failure retry method should be implemented at some point.
                        PhoneStatus.State status = PhoneStatus.getPhoneStatus(info.getRecipient()).isRoaming(); //should check with delivery profile number..
                        if (status == PhoneStatus.State.NONE || status == PhoneStatus.State.ERROR) {
                            hlrFailAction action = Config.getHLRRoamingFailureAction();
                            logger.info("doCall() " + NotificationConfigConstants.HLR_ROAM_FAILURE_ACTION + "- failure action:" + action );
                            switch (action) {
                                case RETRY:
                                case FAIL:
                                    logger.info("doCall() - Unknown hlr status, let outdial state machine handle.");
                                    handleHLRError(info,userInfo);
                                    return;
                                case ROAM:
                                    isRoaming = true;
                                    break;
                                case HOME: //assume home on fail
                                    isRoaming = true;
                                    break;
                                default:
                                    logger.warn("doCall() unknown failure action, let outdial state machine handle action: " + action);
                                    handleHLRError(info,userInfo);
                                    return;
                            } 
                        } else {                        
                            isRoaming = (status == PhoneStatus.State.YES);
                        }                                                 
                    }

                }


                if (!info.isFallback()) {
                    NotificationFilter filter = getValidFilter(msg,info,userInfo);

                    if (isRoaming && filter==null) {
                        //check if disabled due to roaming, if so send a specific event to state machine to decide how to handle.
                        //for example operator may want to send an SMS fall back or may want to wait 24 hrs or whatever.

                        NotificationFilter filt = userInfo.getFilter();
                        //if the user has an explicit roaming filter with ODL not defined or MoipUserNTD has roaming disabled.
                        //NOTE Feedback Handler is null as we have just checked above roaming for the current number, extremely unlikely to be error here.
                        //unless cache expired and now we have HLR issues. In the unlikely event of error this will just not send roaming disabled.
                        if (filt.isNotifTypeDisabledDueToRoaming(Constants.NTF_ODL,msg.getReceiverPhoneNumber(),null)) {
                            sendRoamingDisabled(info,userInfo);
                        }
                        return;
                    } else if(filter == null) { //no valid filters i.e notification currently disabled
                        sendDisabled(info,userInfo);
                        return;
                    }
                } else {
                    //if we are a fall-back we can only check if disabled for roaming vie the MoipUserNtd.
                    //as we don't have to be listed in the filter.
                    NotificationFilter filter = userInfo.getFilter();
                    NotifState state = filter.isNotifTypeDisabledOnUser(Constants.NTF_ODL, msg.getReceiverPhoneNumber());
                    switch (state) {
                        case DISABLED:
                            if (filter.isNotifTypeDisabledDueToRoaming(Constants.NTF_ODL, msg.getReceiverPhoneNumber(), null)) {                                                          
                                sendRoamingDisabled(info,userInfo);
                            } else {
                                sendDisabled(info,userInfo);   
                            }
                            return;
                        case ENABLED:
                            break;
                        case FAILED:
                        case RETRY: 
                            handleHLRError(info, userInfo); //indicate problem to state machine..
                            return;
                    }

                }

                if(Config.getCheckBusy() && isBusy){
                    info.setOdlTrigger(OdlConstants.EVENT_OUTDIAL_BUSY);
                    Command currentCommand = info.getCommand();
                    info.setOdlCode(OdlConstants.EVENT_CODE_BUSY);
                    String eventId = info.getReferenceId();
                    eventHandler.scheduleCallRetry(info);
                    if (eventId != null) {
                        // cancel previous event
                        eventHandler.cancelEvent(eventId);
                    }
                    while(currentCommand.getOperationCount() > 0){
                        currentCommand.operationDone();
                    }
                    doNextCommand(info, userInfo);
                    return;
                }

            } else {
                if (!info.isFallback()) {
                    NotificationFilter filter = getValidFilter(msg,info,userInfo);
                    if (filter == null) {
                        sendDisabled(info,userInfo);
                        return;
                    }   
                } else
                {
                    //if we are a fall-back we can only check if disabled
                    //as we don't have to be listed in the filter.
                    NotificationFilter filter = userInfo.getFilter();
                    //checking with null telephone number can not result in an a roaming check error, so we don't need to check here.
                    if (filter.isNotifTypeDisabledOnUser(Constants.NTF_ODL, null) == NotifState.DISABLED) {
                        sendDisabled(info,userInfo);
                        return;
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("OdlWorker executing call to " + info.getTelNumber());
            }
            String eventId = info.getReferenceId();
            eventHandler.scheduleCallRetry(info);
            if (eventId != null) {
                // cancel previous event
                eventHandler.cancelEvent(eventId);
            }
            caller.sendCall(info.getRecipentId(), info.getTelNumber(), userInfo, callListener);
        } else {
            logger.error("Outdial.doCall - No Userinfo found, stops :" + info);

            doClean(info);
        }
    }

    private void sendDisabled(OdlEvent info, UserInfo userInfo) throws CommandException {
        info.setOdlTrigger(OdlConstants.EVENT_OUTDIAL_CALL_MADE); //we tried to make a call but it's now disabled for whatever reason
        Command currentCommand = info.getCommand();
        info.setOdlCode(OdlConstants.EVENT_CODE_NOTIFDISABLED);
        String eventId = info.getReferenceId();
        eventHandler.scheduleCallRetry(info);
        if (eventId != null) {
            // cancel previous event
            eventHandler.cancelEvent(eventId);
        }
        while(currentCommand.getOperationCount() > 0){
            currentCommand.operationDone();
        }
        doNextCommand(info, userInfo);
        return;
        
    }
    
    private void sendRoamingDisabled(OdlEvent info, UserInfo userInfo) throws CommandException {
        info.setOdlTrigger(OdlConstants.EVENT_OUTDIAL_CALL_MADE); //we tried to make a call but it's now disabled due to roaming.
        Command currentCommand = info.getCommand();
        info.setOdlCode(OdlConstants.EVENT_CODE_ROAMINGDISABLED);
        String eventId = info.getReferenceId();
        eventHandler.scheduleCallRetry(info);
        if (eventId != null) {
            // cancel previous event
            eventHandler.cancelEvent(eventId);
        }
        while(currentCommand.getOperationCount() > 0){
            currentCommand.operationDone();
        }
        doNextCommand(info, userInfo);
        return;
        
    }
    

    private void handleHLRError(OdlEvent info, UserInfo userInfo) throws CommandException{
        info.setOdlTrigger(OdlConstants.EVENT_OUTDIAL_PHONEON);
        Command currentCommand = info.getCommand();
        info.setOdlCode(OdlConstants.EVENT_CODE_HLR_ERROR);
        String eventId = info.getReferenceId();
        if (eventId != null) {
            // cancel previous event
            if (logger.isDebugEnabled()) {
                logger.debug("OdlWorker.handleHLRError() eventId!=null call eventHandler to cancel event ");
            }
            eventHandler.cancelEvent(eventId);
        }
        while(currentCommand.getOperationCount() > 0){
            currentCommand.operationDone();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("OdlWorker.handleSs7Error() in case hlr error doNextCommant() command=");
        }
        doNextCommand(info, userInfo);
    }

    /**
     * @param info Out-dial info.
     * @throws CommandException CommandException
     */
    private void doWaiton(OdlEvent info, UserInfo userInfo) throws CommandException
    {
        if (userInfo != null && hasUnreadMessages(info, userInfo) != null) {
           	if (logger.isDebugEnabled()) {
           		logger.debug("OdlWorker waiting for phone on for : " + info);
           	}

           	String eventId = info.getReferenceId();

           	CommandHandler commandHandler = findCommandHandler(info, userInfo);

            	// use noResponseRetryPeriodHours in addition to maxWaitHours to figure out how long to wait ...
                long noResponseRetryPeriodMS;
                long maxWaitMS;
                
                if (commandHandler.getNoResponseRetryPeriodTime() != -1) {
                    noResponseRetryPeriodMS = commandHandler.getNoResponseRetryPeriodTime();
                } else {
                    noResponseRetryPeriodMS = commandHandler.getNoResponseRetryPeriodHours() * 60 * 60 * 1000;
                }
                
                if (commandHandler.getMaxWaitTime() != -1) {
                    maxWaitMS = commandHandler.getMaxWaitTime();
                } else {
                    maxWaitMS = commandHandler.getMaxWaitHours() * 60 * 60 * 1000;
                }
                
           	// Start Time-out timer.
                eventHandler.scheduleWaitOnRetry(info, noResponseRetryPeriodMS, maxWaitMS);            	
            	
           	if (eventId != null) {
               	// Cancel previous event
           		eventHandler.cancelEvent(eventId);
           	}

           	// Send phone on request.
           	if (!phoneon.request(userInfo, info)) {
           	  //go around again, as unable to queue, will go to phone on state...
                
           	    handleNotification(info, userInfo); 
           	}
        } else {
            logger.error("OdleWorker.doWaitOn: No user info found or no unread message with valid filter found, stops: " + info);
            doClean(info);
        }
    }


    /**
     * Get Userinfo from MCD for a Out-dial.
     *
     * @param info defines the out-dial.
     * @return Userinfo object of user we are doing out-dial for.
     */
    protected UserInfo getUserInfo(OdlEvent info)
    {
        return UserFactory.findUserByTelephoneNumber(info.getRecipient());
    }

    /**
     * Wait a given (or default) time.
     * If timeString can not be used the default of 120 seconds is used.
     * @param info defines the out-dial
     * @param timeString Length in seconds to wait (integer).
     */
    private void doWaitTime(OdlEvent info, String timeString)
    {
    	if (logger.isDebugEnabled())
    		logger.debug("OdlWorker doWaitTime - Time = " + timeString);

        int waitSeconds = 120;
        try {
            waitSeconds = Integer.parseInt(timeString);
        } catch (NumberFormatException nfe) {
            logger.warn("OdlWorker uses default time; Could not parse time " +  timeString);
        }

    	if (logger.isDebugEnabled())
    		logger.debug("OdlWorker Going to wait for " + waitSeconds + " seconds for: " + info);

    	String eventId = info.getReferenceId();
    	eventHandler.scheduleWaitRetry(info, waitSeconds);

    	if (eventId != null) {
        	// Cancel previous event
    		eventHandler.cancelEvent(eventId);
    	}
    }


    /**
     * Find the event code to use from an info.
     * @param info out-dial info
     * @return Event code for the info.
     */
    private static int findEventCode(OdlEvent info)
    {
        String event = info.getOdlTrigger();
        if (logger.isDebugEnabled())
    		logger.debug("OdlWorker.findEventCode(): event="+event);
        if (event == null) {
            // Time based, use default code
            return OdlConstants.EVENT_CODE_DEFAULT;
        } else if (OdlConstants.EVENT_OUTDIAL_CALL_MADE.equals(event)) {
            return info.getOdlCode();
        } else if (OdlConstants.EVENT_OUTDIAL_PHONEON.equals(event)) {
        	if(info.getOdlCode() == OdlConstants.EVENT_CODE_BUSY){
        		return OdlConstants.EVENT_CODE_BUSY;
        	}else if (info.getOdlCode() == OdlConstants.EVENT_CODE_HLR_ERROR){
        		return OdlConstants.EVENT_CODE_HLR_ERROR;
        	}else {
        		return OdlConstants.EVENT_CODE_PHONEON;
        	}
        } else if (OdlConstants.EVENT_OUTDIAL_START.equals(event)) {
            return OdlConstants.EVENT_CODE_DEFAULT;
        } else if (OdlConstants.EVENT_OUTDIAL_BUSY.equals(event)){
        		return OdlConstants.EVENT_CODE_BUSY;
        } else {
            logger.warn("OdlWorker: Unknown event name : " + event);
            return OdlConstants.EVENT_CODE_DEFAULT;
        }

    }


    /**
     * Find command handler to use.
     * Later we may need to get name for handler from the UserInfo, for
     * now we just look up the handler under the key "default".
     * @param info out dial info
     * @return the command handler to user
     */
    private CommandHandler findCommandHandler(OdlEvent info, UserInfo userInfo) {
        String schemaName = userInfo.getOutdialSchema();

        if ((schemaName == null) || (schemaName.length() == 0)) {
        	if (logger.isDebugEnabled())
        		logger.debug("No outdial schema name found, using default" + info);
            schemaName = "default";
        }

    	if (logger.isDebugEnabled())
    		logger.debug("Outdial - Using outdial schema: " + schemaName);

        CommandHandler handler = commandHandlers.get(schemaName);
        if (handler == null) {
            logger.info("OdlWorker Named outdial schema not found, using default" + schemaName);
            handler = commandHandlers.get("default");
        }
        if (handler == null) {
            logger.error("OdlWorker no outdial schema found, check <NTF_HOME>/cfg if there are any outdial configuration files.");
            return NOOP_COMMAND_HANDLER;
        }
        return handler;
    }

    /**
     * Checks if the current event has a valid command.
     * <p>
     * This command should be invoked when a OdlEvent is retrieve from a persistent store.
     * Because, if it has a state other than start, the current command is null and it causes
     * the other routines to complain about it.
     * </p>
     *
     * @param info Event to inspect.
     * @param userInfo User information.
     */
    private void checkCommand(OdlEvent info, UserInfo userInfo) {

    	if (info.getCommand() == null && !info.getOdlTrigger().equals(OdlConstants.EVENT_OUTDIAL_START)) {
    		String schemaName = userInfo.getOutdialSchema();

    		if ((schemaName == null) || (schemaName.length() == 0)) {
    			schemaName = "default";
    		}

    		CommandHandler handler = this.commandHandlers.get(schemaName);
    		if (handler == null) {
    			logger.warn("OdlEventHandler command handler not found: " + info.getIdentity() + " schema: " + schemaName);
    		} else {
    		    try {
    		        Command command = handler.getCommand(info.getCurrentState(), info.getOdlCode(), info.getOperationCode());
    		        info.setCommand(command);
    		    } catch (CommandException e) {
    		        logger.warn("Command handler get command exception: " + info.getIdentity() + " schema: "
    		                + schemaName + " exception: " + e.getMessage());
    		        if (logger.isDebugEnabled()) {
    		            logger.debug("", e);
    		        }
    		    } catch (Exception e) {
                    logger.error("checkCommand Exception: ",e);
    		    }
    		}
    	}
    }

    /**
     * Utility method to start Reminder trigger
     * @param odlEvent OdlEvent
     */
    private void startReminderTriggerScheduler(OdlEvent odlEvent) {

        if (!Config.isOutdialReminderEnabled()) {
            return;
        }

        if (odlEvent.isFallback()) {
            logger.debug("No OdlReminder trigger started for Subscriber " + odlEvent.getIdentity() + " since in fallback mode.");
            return;
        }

        logger.debug("Subscriber " + odlEvent.getIdentity() + ", startReminderTriggerScheduler, isReminder: " + odlEvent.isReminder() + ", " + odlEvent.getSchedulerIdReminder());

        String schedulerIdReminder = null;
        String previousSchedulerIdReminder = odlEvent.getSchedulerIdReminder();

        if (!odlEvent.isReminder()) {
            /**
             * This notification is coming from an original deposit (not a Reminder retry).
             * Might be while either there is no pending Reminder trigger event or there is one.
             * In case of no pending Reminder trigger, start a ReminderTrigger event.
             * In case of a pending one, it must be cancelled and a new one must be started in order for this new deposit
             * to get the full retry schema and expiry period.
             */
            
            /**
             * But before all this, make sure that the oudialNotificationType IS outdial...
             * From configuration, initial Outdial notification can be configured to send
             * a 'normal' and/or 'flash' sms as Reminder instead of Outdial...
             */
            String outdialReminderType = Config.getOutdialReminderType();
            if (!outdialReminderType.equalsIgnoreCase(Config.OUTDIAL_REMINDER_TYPE_OUTDIAL)) {
                // for now at least, the only other type is flssms - no need to pass it as parameter...
                scheduleOtherReminderType(odlEvent);
                return;
            }

            schedulerIdReminder = eventHandler.scheduleReminderTriggerEvent(odlEvent);
            logger.debug("Subscriber " + odlEvent.getIdentity() + " starts a new reminder timer: " + schedulerIdReminder);

            if (previousSchedulerIdReminder != null) {
                logger.debug("Cancelling previous ReminderTrigger timer for subscriber " + odlEvent.getIdentity() + ", " + previousSchedulerIdReminder);
                eventHandler.cancelReminderTriggerEvent(previousSchedulerIdReminder);
            }

            // Keep the schedulerIdReminder persistent
            odlEvent.setSchedulerIdReminder(schedulerIdReminder);
            odlEvent.notifyObservers();

        } else {
            // This notification is coming from a Reminder retry (not from an original deposit).
            if ((odlEvent.getOdlCode() == OdlConstants.EVENT_CODE_COMPLETED) ||
                (odlEvent.getOdlCode() == OdlConstants.EVENT_CODE_INITIATED)) {

                // Notification is successful
                if (previousSchedulerIdReminder != null) {
                    /**
                     * This notification was successful and the user got notified but the notification might have
                     * encountered some delay because of either internal retries or phone-on waiting period.
                     * Therefore, the previous Reminder scheduler must be re-adjusted, first for the user experience
                     * that must be maintained (Reminder retry interval) and second for the tracking of the number of retries
                     */
                    schedulerIdReminder = eventHandler.rescheduleReminderTriggerEvent(previousSchedulerIdReminder);
                    if (previousSchedulerIdReminder.equalsIgnoreCase(schedulerIdReminder)) {
                        /**
                         * If the rescheduled id is the same as the previous, this means that the backup event of previousSchedulerIdReminder
                         * is an expiry event (isExpire=true).  In this case, the previousSchedulerIdReminder is the one that shall be kept.
                         */
                        logger.debug("Subscriber " + odlEvent.getIdentity() + " successfuly notified, keeping the same reminder schedulerId " + previousSchedulerIdReminder);
                    } else {
                        /**
                         * The schedulerIdReminder might contain 2 values, either a new schedulerIdReminder value
                         * (in which case the next reminder scheduler id has been successfully rescheduled - generic case) or
                         * the schedulerIdReminder is null because the next retry would have happened after the expiry period
                         * (in which case the current notification is the last attempt, the schedulerId must be nullified)
                         */
                        logger.debug("Subscriber " + odlEvent.getIdentity() + " successfuly notified, reschedule the reminder timer: " + schedulerIdReminder);

                        // Keep the schedulerIdReminder persistent
                        odlEvent.setSchedulerIdReminder(schedulerIdReminder);
                        odlEvent.notifyObservers();
                    }
                } else {
                    logger.debug("Subscriber " + odlEvent.getIdentity() + " successfuly notified, no pending reminder timer");
                }
            } else {
                /**
                 * Notification is unsuccessful while system already had a Reminder timer for the subscriber.
                 * In this case, the previous schedulerIdReminder is kept and will retry at next retry.
                 */
                logger.debug("Subscriber " + odlEvent.getIdentity() + " was not successfuly notify, reminder will retry: " + previousSchedulerIdReminder);
            }
        }
    }


    /** Utility method to revert to another type of event (sms and/or fls) for Reminder.
     * Schedule a level 2 event for it.
     * 
     * @param odlEvent              The original outdial Event that made sending this reminder necessary
     */
    private void scheduleOtherReminderType(OdlEvent odlEvent) {
        // Get SMS Reminder's handler 
        NtfRetryHandling smsRetryHandler = NtfEventHandlerRegistry.getEventHandler(); 

        // Create an NTF (sms) Event out of the original ODL event - don't want to confuse the handler with OdlEvent properties...
        NtfEvent ntfEvent = new NtfEvent(NtfEventTypes.EVENT_TYPE_REMINDER.getName(), ((NtfEvent)odlEvent).getReminderProperties() );
        String mailboxTel = ntfEvent.getRecipient();

        // Schedule the newly created event 
        String newTriggerEventId = smsRetryHandler.scheduleReminderTriggerEvent(ntfEvent);        

        // And 'update persistently' so that the event will be properly processed when triggered 
        SmsReminder.updateExternallyScheduledReminderTrigger(mailboxTel, newTriggerEventId);
    }

    
    /**
     * Utility method to start Reminder trigger - For login case
     * @param odlEvent OdlEvent
     */
    private void startReminderTriggerSchedulerForLogin(OdlEvent odlEvent) {

        if (!Config.isOutdialReminderEnabled()) {
            return;
        }

        if (odlEvent.isFallback()) {
            logger.debug("No OdlReminder trigger started for Subscriber " + odlEvent.getIdentity() + " since in fallback mode.");
            return;
        }

        String schedulerIdReminder = null;
        String previousSchedulerIdReminder = odlEvent.getSchedulerIdReminder();

        if (previousSchedulerIdReminder == null) {
            /**
             * LOGIN expiry while there is no pending schedulerIdReminder going on, start one.
             * Start a new Reminder timer
             */
            schedulerIdReminder = eventHandler.scheduleReminderTriggerEvent(odlEvent);
            logger.debug("Subscriber " + odlEvent.getIdentity() + " starts a new reminder timer: " + schedulerIdReminder);
        } else {
            if (!odlEvent.isReminder()) {
                /**
                 * LOGIN expiry while there is already a pending schedulerIdReminder going on.
                 * Start a new Reminder timer and cancel the previous one.
                 */
                schedulerIdReminder = eventHandler.scheduleReminderTriggerEvent(odlEvent);
                logger.debug("Subscriber " + odlEvent.getIdentity() + " starts a new reminder timer: " + schedulerIdReminder);

                logger.debug("Cancelling previous ReminderTrigger timer for subscriber " + odlEvent.getIdentity() + ", " + previousSchedulerIdReminder);
                eventHandler.cancelReminderTriggerEvent(previousSchedulerIdReminder);
            } else {
                /**
                 * Notification is unsuccessful while system already had a Reminder timer for the subscriber.
                 * In this case, the previous schedulerIdReminder is kept and will retry at next retry.
                 */
                logger.debug("Subscriber " + odlEvent.getIdentity() + " was not successfuly notify, reminder will retry: " + previousSchedulerIdReminder);
            }
        }

        // Keep the schedulerIdReminder persistent
        odlEvent.setSchedulerIdReminder(schedulerIdReminder);
        odlEvent.notifyObservers();
    }

    // ==========================================================
    /**
     * Class to handle answers from out calls.
     */
    private class WorkerCallListener implements OdlCallListener {
        /**
         * Take care of a result for the given number.
         * A notification with relevant data is sent to the used delayer.
         * @param subscriberNumber mailbox
         * @param notificationNumber Phone number that was called
         * @param userInfo the user the call was for
         * @param code Reply code for the call attempt
         */
        public void handleResult(String subscriberNumber, String notificationNumber, UserInfo userInfo, int code) {
            if (logger.isDebugEnabled())
            	logger.debug("ODL Call Listener Notified: " + subscriberNumber + " : " + notificationNumber + " got code " + code);

            OdlEvent odlEvent = eventStore.get(subscriberNumber, notificationNumber);
            if (odlEvent != null) {
        		Operation currentOp = odlEvent.getCommand().getCurrentOperation();
        		if (currentOp != null && currentOp.getOpcode() == CommandHandler.OP_CALL) {
        		    odlEvent.setOdlTrigger(OdlInfo.EVENT_OUTDIAL_CALL_MADE);
        		    odlEvent.setOdlCode(code);
        		    odlEvent.setFromNotify(true);
        			//cancel ODL call retry event
        			eventHandler.cancelEvent(odlEvent.getReferenceId());
        			odlEvent.keepReferenceID(null);
        			odlEvent.notifyObservers();

                    try {
                        queue.put(odlEvent);
                    } catch (Throwable t) {
                        logger.info("handleResult: queue full or state locked while handling event, will retry");
                    }
        		}
            } else {
            	logger.info("ODL Call Listener Notified event not found for  " + subscriberNumber + " : " + notificationNumber + " got code " + code);
            }
        }
    }

    @Override
    public boolean shutdown() {
        if (isInterrupted()) {
            return true;
        } //exit immediately if interrupted..

        if (queue.size() == 0)
        {
                //give a short time for new items to be queued in workers, to allow other threads to empty there queues.
                if (queue.isIdle(2,TimeUnit.SECONDS)) {
                    return true;
                }
                else
                {
                    if (queue.waitNotEmpty(2, TimeUnit.SECONDS)) {
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
}
