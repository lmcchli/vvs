/* **********************************************************************
 * Copyright (c) ABCXYZ 2009. All Rights Reserved.
 * Reproduction in whole or in part is prohibited without the
 * written consent of the copyright owner.
 *
 * ABCXYZ MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ABCXYZ SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * **********************************************************************/
package com.abcxyz.services.moip.ntf.coremgmt;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.MsgStoreServer;
import com.abcxyz.messaging.mrd.operation.InformEventReq;
import com.abcxyz.messaging.scheduler.handling.AppliEventHandler;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.AbstractEventStatusListener;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.EventProperties;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.coremgmt.reminder.SmsReminder;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventGenerator;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.ntf.NtfEventHandler;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.out.autounlockpin.AutoUnlockPin;
import com.mobeon.ntf.out.autounlockpin.AutoUnlockPinUtil;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * Class implements NTF retry event handling using scheduler.
 */
public class NtfRetryEventHandler extends AbstractEventStatusListener implements NtfRetryHandling, EventSentListener {

	protected AppliEventHandler eventHandler;
	protected AppliEventHandler reminderEventHandler = null;
	protected AppliEventHandler autoUnlockPinUnlockEventHandler = null;
	protected AppliEventHandler autoUnlockPinSmsEventHandler = null;

	protected NtfEventReceiver eventReceiver;
    private static LogAgent logger = NtfCmnLogger.getLogAgent(NtfRetryEventHandler.class);

    protected AtomicLong numOfScheduledEvent = new AtomicLong(0);
    protected AtomicLong numOfCancelledEvent = new AtomicLong(0);
    protected AtomicLong numOfFiredNotifEvent = new AtomicLong(0);
    protected AtomicLong numOfFiredExpireEvent = new AtomicLong(0);

    protected AtomicLong numOfResponseOK = new AtomicLong(0);
    protected AtomicLong numOfResponseNormalRetry = new AtomicLong(0);
    protected AtomicLong numOfResponseFailed = new AtomicLong(0);
    protected AtomicLong numOfResponseTemporary = new AtomicLong(0);

    protected String lastBackup;


    protected NtfRetryEventHandler() {

    }

	/**
	 * instantiate retry handlers for a specific event type.
	 * @param initialNotifRetryInfo schema for initial notifications (notify because of new message deposit)
	 */
	public NtfRetryEventHandler(RetryEventInfo initialNotifRetryInfo) {
	    RetryEventInfo reminderRetryInfo = SmsReminder.getReminderTriggerRetryEventInfo();
	    init(initialNotifRetryInfo, reminderRetryInfo);
	    
        //instantiate scheduler event handler that handles autounlockpin unlock retries
        RetryEventInfo aupUnlockRetryInfo = AutoUnlockPin.getAutoUnlockPinUnlockRetryEventInfo();
        autoUnlockPinUnlockEventHandler = new AppliEventHandler(aupUnlockRetryInfo, this);
        
        //instantiate scheduler event handler that handles autounlockpin sms notification retries
        RetryEventInfo aupSmsRetryInfo = AutoUnlockPin.getAutoUnlockPinSmsRetryEventInfo();
        autoUnlockPinSmsEventHandler = new AppliEventHandler(aupSmsRetryInfo, this);
	}
	
	/**
	 * register initial notification schema and reminder schema to schedulers
	 * @param initialNotifRetryinfo schema for initial notifications (notify because of new message deposit)
	 * @param reminderRetryInfo schema for reminder notifications (notify because still have new messages in mailbox)
	 */
	protected void init(RetryEventInfo initialNotifRetryinfo, RetryEventInfo reminderRetryInfo) {
	    //instantiate scheduler event handler that handles reminder notification retries
	    reminderEventHandler = new AppliEventHandler(reminderRetryInfo, this);
        if (logger.isDebugEnabled()) {
            logger.debug("init: initialized reminderEventHandler with schema: " + 
                    reminderRetryInfo.getEventRetrySchema() + "; expiration: " + reminderRetryInfo.getExpireTimeInMinute());
        }

        init(initialNotifRetryinfo);
	}
	
	/**
	 * register initial notification schema to scheduler
	 * @param info schema for initial notifications (notify because of new message deposit)
	 */
	protected void init(RetryEventInfo info) {

		//instantiate scheduler event handler that handles initial notification retries
		eventHandler = new AppliEventHandler(info, this);
		
        eventReceiver = NtfEventHandlerRegistry.getNtfEventReceiver(getEventServiceName());

	}

	public String getEventServiceName() {
		return eventHandler.getEventServiceName();

	}

	/**
	 * schedule a backup event for the current event service
	 */
	public NtfEvent scheduleEvent(Properties properties) {
		String uid  = MsgStoreServer.getAnyMsgId();
		return scheduleEvent(uid, properties);
	}

	/**
	 * schedule a backup event for the current event service
	 */
	public NtfEvent scheduleEvent(String uid,  Properties properties) {

		long when = System.currentTimeMillis() + eventHandler.getFirstRetryTimer();

		AppliEventInfo eventInfo = eventHandler.scheduleEvent(when, uid, NtfEventTypes.EVENT_TYPE_NOTIF.getName(), properties);

		//generate NTF event
		NtfEvent ntfEvent = NtfEventGenerator.generateEvent(eventInfo.getEventId());

        numOfScheduledEvent.incrementAndGet();
        this.lastBackup = eventInfo.getEventId();

		return ntfEvent;
	}

    @Override
    public String scheduleEvent(NtfEvent event) {
        return scheduleEvent(event, 0);
    }

	/**
	 * schedule a new event from a NtfEvent
	 *
	 * NtfEvent's internal data: eventTypeKey, MessageInfo, and Properties will be used for constructing the event
	 *
	 */
	@Override
	public String scheduleEvent(NtfEvent event, long delay) {
		long when = System.currentTimeMillis() + eventHandler.getFirstRetryTimer() + delay;
		AppliEventInfo eventInfo = null;

		if (event.isEventServiceType(NtfEventTypes.DEFAULT_NTF.getName())) {
		    eventInfo = eventHandler.scheduleEvent(when, event.getEventUid(), NtfEventTypes.EVENT_TYPE_NOTIF.getName(), event.getPersistentProperties());
		} else if (event.isEventServiceType(NtfEventTypes.MWI_OFF_UNSUBSCRIBED.getName())) {
		    /*
		     * MWI OFF UNSUBSCRIBED event has some property data (ex. MOIPFilter)
		     * that contains separators (control characters: ";", "=") that can be mixed up
		     * when saving the event into the persistent storage and retrieved later.
		     * For this reason, we need to encode.
		     * This is done only on this event to keep backward compatibility.
		     */
            Properties props = new Properties();
            try {
                Properties eventProps = event.getPersistentProperties();
                Set<String> keys = eventProps.stringPropertyNames();

                for (String key : keys) {
                    String value = eventProps.getProperty(key);
                    
                    if (key.contains(InformEventReq.DELIMITER)) {
                        key = URLEncoder.encode(key, InformEventReq.ENCODING);
                    }
                    if (value.contains(InformEventReq.DELIMITER)) {
                        value = URLEncoder.encode(value, InformEventReq.ENCODING);
                    }
                    props.setProperty(key, value);
                }
            } catch (UnsupportedEncodingException ex) {
                // Encoding not supported.
                if (logger.isDebugEnabled()) {
                    logger.debug("Caught unsupported encoding exception, while encoding properties" +
                            " for event of type " + event.getEventTypeKey());
                }
                
                // Transfer properties without encoding
                props = event.getPersistentProperties();
            }
		    eventInfo = eventHandler.scheduleEvent(when, event.getEventUid(), event.getEventServiceTypeKey(), props);
		} else {
		    eventInfo = eventHandler.scheduleEvent(when, event.getEventUid(), event.getEventServiceTypeKey(), event.getPersistentProperties());
		}

        numOfScheduledEvent.incrementAndGet();
        this.lastBackup = eventInfo.getEventId();

		return eventInfo.getEventId();
	}

	

	@Override
	public void cancelEvent(String eventId) {
		if (eventId == null || eventId.isEmpty()) {
			return;
		}

		AppliEventInfo eventInfo = new AppliEventInfo();
		eventInfo.setEventId(eventId);
		this.numOfCancelledEvent.incrementAndGet();
		this.eventHandler.cancelEvent(eventInfo);
	}

	@Override
	public String scheduleReminderTriggerEvent(NtfEvent event) {
	    String eventId = null;
	    if(reminderEventHandler != null){
	        if(event != null){
	            long when = System.currentTimeMillis() + reminderEventHandler.getFirstRetryTimer();
	            AppliEventInfo eventInfo = reminderEventHandler.scheduleEvent(when, event.getRecipient() + NtfEvent.getUniqueId(), NtfEventTypes.EVENT_TYPE_REMINDER.getName(), event.getReminderProperties());
	            eventId = eventInfo.getEventId();
	        }
	        else{
	            logger.debug("scheduleReminderTriggerEvent: Reminder trigger backup not scheduled because new message notification event is null.");
	        }
	    }
	    else{
            logger.debug("scheduleReminderTriggerEvent: Reminder trigger backup not scheduled because reminderEventHandler is null.");
	    }
	    return eventId;
	}

	@Override
	public void cancelReminderTriggerEvent(String eventId) {
	    if(reminderEventHandler != null){
	        if(eventId != null){
	            AppliEventInfo eventInfo = new AppliEventInfo();
	            eventInfo.setEventId(eventId);
	            reminderEventHandler.cancelEvent(eventInfo);
	        }
	        else{
	            logger.debug("cancelReminderTriggerEvent: Reminder trigger backup not cancelled because event id is null.");
	        }
	    }
	    else{
	        logger.debug("cancelReminderTriggerEvent: Reminder trigger backup not cancelled because reminderEventHandler is null.");
	    }
	}
    
    @Override
    public String rescheduleReminderTriggerEvent(String oldEventId){
        String newEventId = null;
        if(reminderEventHandler == null){
            //Cannot cancel oldEventId either since reminderEventHandler is null; return oldEventId to caller to indicate nothing was done.
            logger.error("rescheduleReminderTriggerEvent: Reminder trigger backup not rescheduled because reminderEventHandler is null.");
            newEventId = oldEventId;
        }
        else{
            if(oldEventId == null){
                logger.debug("rescheduleReminderTriggerEvent: Reminder trigger backup not rescheduled because event id is null.");
            }
            else{
                try {
                    EventID oldID = new EventID(oldEventId);
                    if(EventID.EVENT_TYPE_EXPIRY.equals(oldID.getServiceSpecificType())){
                        logger.debug("rescheduleReminderTriggerEvent: Reminder trigger backup not rescheduled because it is the expiry event; the original expiry is kept.");
                        newEventId = oldEventId;
                    }
                    else{
                        //Since reminders are scheduled at the same interval until expiration, just get the first retry timer
                        long when = System.currentTimeMillis() + reminderEventHandler.getFirstRetryTimer();
                        String expiryTime = oldID.getProperty(EventProperties.EXPIRY_TIME);
                        Long expiry = Long.parseLong(expiryTime);

                        if(when < oldID.getEventTime() + 1000){
                            logger.debug("rescheduleReminderTriggerEvent: New timestamp (" + when + " millisec) is within one second of the old timestamp (" + oldID.getEventTime() + " millisec); no need to reschedule.");
                            newEventId = oldEventId;
                        }
                        else if((when + 60 * 1000) > expiry){
                            logger.debug("rescheduleReminderTriggerEvent: Reminder trigger backup not rescheduled because the new time + 1 minute buffer (" + (when + 60 * 1000) + 
                                    " millisec) will be past the event expiry (" + expiry + " millisec).  " +
                                    "Since the event expiry should be the next event to fire, cancel the original backup and initiate the reminder trigger clean up by returning null.");
                            cancelReminderTriggerEvent(oldEventId);
                        }
                        else{
                            AppliEventInfo oldEventInfo = new AppliEventInfo();
                            oldEventInfo.setEventId(oldEventId);
                            AppliEventInfo newEventInfo = reminderEventHandler.rescheduleEvent(oldEventInfo, when);

                            if(newEventInfo.getEventId() != null){
                                logger.debug("rescheduleReminderTriggerEvent: Reminder trigger backup successfully rescheduled: " + newEventInfo.getEventId());
                                newEventId = newEventInfo.getEventId();
                            }
                            else{
                                logger.error("rescheduleReminderTriggerEvent: Reminder trigger backup rescheduling failed; keep original backup: " + oldEventId);
                                newEventId = oldEventId;                            
                            }
                        }
                    }
                } catch (InvalidEventIDException e) {
                    //Cannot cancel since the event id is invalid; just log and return null to remove invalid event id from persistence.
                    logger.error("rescheduleReminderTriggerEvent: Reminder trigger backup not rescheduled because event id is invalid: " + e.toString());
                } catch (NumberFormatException e){
                    //Leave oldEventId since we cannot reschedule it.  Without a valid expiry time, the expiry event will not be scheduled 
                    //but at least there will still be a reminder trigger alive until then.
                    logger.error("rescheduleReminderTriggerEvent: Reminder trigger backup not rescheduled because event id has an invalid expiry time; keep original backup.  NumberFormatException: " + e.getMessage());
                    newEventId = oldEventId;
                }
            }
        }
        return newEventId;
    }
    
    @Override
    public String scheduleAutoUnlockPinEvent(NtfEvent event, UserInfo user) {
        String eventId = null;
        if(autoUnlockPinUnlockEventHandler != null){
            if(event != null){

                Properties props = event.getEventProperties();
                
                
                long lockoutTime = -1;
                String lockTimeAsString = "" + props.get(MoipMessageEntities.AUTO_UNLOCK_PIN_LOCKTIME);
                try {
                    lockoutTime = AutoUnlockPinUtil.parseEventTime(lockTimeAsString);
                } catch (ParseException e) {

                    logger.error("scheduleAutoUnlockPinEvent: Could not parse mandatory attribute " + MoipMessageEntities.AUTO_UNLOCK_PIN_LOCKTIME, e);
                    String oldEventId = event.getReferenceId();
                    cancelEvent(oldEventId);
                    
                    //Generate MDR failure
                    MerAgent.get().aupUnlockFailed(event.getRecipient());
                }
                
                if(lockoutTime != -1) {
                  
                    long unlockCosDelay = user.getAutoUnlockPinDelay() * 3600000; // hours to ms
                    long when = lockoutTime + unlockCosDelay;
                    
                    Properties unlockProps = event.getAutoUnlockPinProperties();
                    unlockProps.put(NtfEvent.AUTO_UNLOCK_PIN_LOCKTIME, lockTimeAsString);
                    
                    AppliEventInfo eventInfo = autoUnlockPinUnlockEventHandler.scheduleEvent(
                                                    when, event.getRecipient() + NtfEvent.getUniqueId(),
                                                    NtfEventTypes.EVENT_AUTO_UNLOCK_PIN_L2.getName(), unlockProps);
                    eventId = eventInfo.getEventId();
                }
                
            }
            else{
                logger.debug("scheduleAutoUnlockPinEvent: AutoUnlockPin Unlock backup not scheduled because notification event is null.");
            }
        }
        else{
            logger.debug("scheduleAutoUnlockPinEvent: AutoUnlockPin Unlock backup not scheduled because autoUnlockPinUnlockEventHandler is null.");
        }
        return eventId;
    }
    
    @Override
    public String scheduleAutoUnlockPinSmsEvent(NtfEvent event) {
        String eventId = null;
        if(autoUnlockPinSmsEventHandler != null){
            if(event != null){
                // Schedule retry for AutoUnlockPin SMS
                Properties props = event.getAutoUnlockPinProperties();
                props.put(NtfEvent.AUTO_UNLOCK_PIN_LOCKED, "0");
                
                AppliEventInfo eventInfo = autoUnlockPinSmsEventHandler.scheduleEvent(
                                                    event.getRecipient() + NtfEvent.getUniqueId(), 
                                                    NtfEventTypes.EVENT_AUTO_UNLOCK_PIN_L2.getName(), props);
                eventId = eventInfo.getEventId();
                
                /**
                 * Cancel retry for AutoUnlockPin Unlock
                 * 
                 * AutoUnlockPin Unlock event should always be canceled even if AutoUnlockPin SMS can't be scheduled since Unlock event
                 * will be discarded on retry if the user has already been unlocked.
                 */
                String unlockEventId = event.getReferenceId();
                logger.debug("AutoUnlockPin:handleAutoUnlockPinUnlock: Unlock success for " + event.getRecipient() + ", canceling event retry " + unlockEventId);
                cancelEvent(unlockEventId); 

            }
            else{
                logger.debug("scheduleAutoUnlockPinSmsEvent: AutoUnlockPin Sms backup not scheduled because notification event is null.");
            }
        }
        else{
            logger.debug("scheduleAutoUnlockPinSmsEvent: AutoUnlockPin Sms backup not scheduled because autoUnlockPinSmsEventHandler is null.");
        }
        return eventId;
    }
    
	@Override
	public int eventFired(AppliEventInfo eventInfo) {
        int result = EventHandleResult.OK;
	    try {
	        //send to receiver the new NTF event
	        if (eventInfo.getEventId() == null) {
	            return 0;
	        }

	        NtfEvent ntfEvent = NtfEventGenerator.generateEvent(eventInfo.getEventId());
	        if (ntfEvent == null) {
	            return 0;
	        } else if (ntfEvent.isEventServiceType(NtfEventTypes.MWI_OFF_UNSUBSCRIBED.getName())) {
	            /*
	             *  Decode event property values for the MWI OFF Unsubscribed event
	             *  because is may contains control characters used as separator.
	             *  Encoding has been performed before saving this event to the 
	             *  scheduler.
	             */
	            try {
    	            Properties props = ntfEvent.getPersistentProperties();
    	            Set<String> keys = props.stringPropertyNames();
                    Matcher matcher = Pattern.compile("%[\\dA-Fa-f]{2}").matcher("");
    	            for (String key : keys) {
    	                boolean update = false;
    	                String value = props.getProperty(key);
    	                
    	                // Check if key has encoding characters
    	                matcher.reset(key);
    	                if (matcher.find()) {
    	                    key = URLDecoder.decode(key, InformEventReq.ENCODING);
    	                    update = true;
    	                }
    
                        // Check if value has encoding characters
    	                matcher.reset(value);
                        if (matcher.find()) {
                            value = URLDecoder.decode(value, InformEventReq.ENCODING);
                            update = true;
                        }
                        
                        if (update == true) {
                            ntfEvent.setProperty(key, value);
                        }
    	            }
                } catch (UnsupportedEncodingException ex) {
                    // Nothing really to do except logging.
                    if (logger.isDebugEnabled()) {
                        logger.debug("Caught unsupported encoding exception, while decoding ntf event" +
                                " for event of type " + ntfEvent.getEventTypeKey());
                    }
                }

	        }

	        if (shouldProcessFiredEvent(ntfEvent, eventInfo)) {
                NtfRetryHandling schedulerHandler = NtfEventHandlerRegistry.getEventHandler(ntfEvent.getEventServiceTypeKey());
                if (schedulerHandler == null) {
                    logger.error("NtfRetryEventHandler Unable to get NtfRetryHandling");
                    return EventHandleResult.STOP_RETRIES;
                }

                // Keep reference of the next backed-up event
                if (eventInfo.getNextEventInfo() == null || eventInfo.isExpire() || eventInfo.isLastExpire()) {
                    if (eventInfo.getNextEventInfo() != null) {
                        ntfEvent.keepReferenceID(eventInfo.getNextEventInfo().getEventId());
                    }
	                ntfEvent.setExpiry();
	                this.numOfFiredExpireEvent.incrementAndGet();
	            } else {
	                ntfEvent.keepReferenceID(eventInfo.getNextEventInfo().getEventId());
	                this.numOfFiredNotifEvent.incrementAndGet();
	            }

                String profilerText = ntfEvent.isExpiry() ? "Expiry" : "Retry";
                boolean storedInQueue = NtfMessageService.get().getQueue().offer(ntfEvent);
                if (!storedInQueue) {
                    logger.warn("NtfRetryEventHandler: Not stored in ntfMessageServiceQueue (full), eventId: " + ntfEvent.getReferenceId() + ", will retry");
                    if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                        NtfMessageService.profilerAgentCheckPoint("NTF.2.NREH." + profilerText + ".NotQueued.#" + eventInfo.getNumberOfTried());
                    }
                } else {
                    if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                        NtfMessageService.profilerAgentCheckPoint("NTF.2.NREH." + profilerText + ".Queued.#" + eventInfo.getNumberOfTried());
                    }
                }

                // Test purpose 
	            this.lastBackup = eventInfo.getEventId();

	        } else {
	            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    NtfMessageService.profilerAgentCheckPoint("NTF.2.NREH.ShouldNotProcess");
                }
	            result = EventHandleResult.STOP_RETRIES;
	        }

	    } catch (Exception e) {
	        String message = "Exception for event " + eventInfo.getEventId();
	        if (eventInfo.getNextEventInfo() == null || eventInfo.isLastExpire()) {
	            logger.error(message + ", will not retry. ", e);
	            result = EventHandleResult.STOP_RETRIES;
	        } else {
	            logger.warn(message + ", will retry. ", e);
	        }
	    }

	    return result;
	}

	private boolean shouldProcessFiredEvent(NtfEvent ntfEvent, AppliEventInfo eventInfo){
	    boolean shouldProcess = true;
	    if(ntfEvent.isReminder()){
	        shouldProcess = SmsReminder.shouldProcessFiredReminderEvent(ntfEvent, eventInfo);
	    }
	    return shouldProcess;
	}

	@Override
	public void reportCorruptedEventFail(String eventId) {
	}

	@Override
	public void reportEventCancelFail(AppliEventInfo eventInfo) {
	}

	@Override
	public void reportEventScheduleFail(AppliEventInfo eventInfo) {
	}

	@Override
	public void sendStatus(NtfEvent event, SendStatus status) {

	    NtfEventHandler ntfEventHandler = (NtfEventHandler)NtfEventHandlerRegistry.getNtfEventReceiver();

	    try {
            if (logger.isDebugEnabled()) {
                logger.debug("sendStatus received: " + status.toString());
            }
	        if (status == SendStatus.NORMAL_RETRY) {
	            numOfResponseNormalRetry.incrementAndGet();
	            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    ntfEventHandler.profilerAgentCheckPoint("NTF.NREH.4.NormalRetry");
                }
	            return;
	        }

	        if (status != SendStatus.TEMPORARY_ERROR) {
	            if (status == SendStatus.OK) {
	                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                        ntfEventHandler.profilerAgentCheckPoint("NTF.NREH.3.OK");
                    }
	                numOfResponseOK.incrementAndGet();
	            } else if (status == SendStatus.PERMANENT_ERROR) {
	                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                        ntfEventHandler.profilerAgentCheckPoint("NTF.NREH.6.Failed");
                    }
	                numOfResponseFailed.incrementAndGet();
	            }

	            //cancel event
	            if (event.getReferenceId() != null) {
	                AppliEventInfo info = new AppliEventInfo();
	                info.setEventId(event.getReferenceId());
	                eventHandler.cancelEvent(info);
	                if (logger.isDebugEnabled()) {
	                    logger.debug("NotifEventHandler cancelled event: " + event.getReferenceId());
	                }
	            }
	        } else {
	            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    ntfEventHandler.profilerAgentCheckPoint("NTF.NREH.5.TemporaryError");
                }
	            numOfResponseTemporary.incrementAndGet();
	        }

	    } finally {
	        logger.info("Ntf Counters (NREH): " +
	                "EventFiredNotif: " + numOfFiredNotifEvent.get() +
	                ", EventFiredExpiry: " + numOfFiredExpireEvent.get() +
	                ", ResponseOk: " + numOfResponseOK.get() +
	                ", ResponseNormalRetry: " + numOfResponseNormalRetry.get() +
	                ", ResponseTemporary: " + numOfResponseTemporary.get() +
	                ", ResponseFailed: " + numOfResponseFailed.get());
	    }
	}

    public long getNumOfScheduledEvent() {
        return numOfScheduledEvent.get();
    }

    public long getNumOfCancelledEvent() {
        return numOfCancelledEvent.get();
    }

    public long getNumOfFiredNotifEvent() {
        return numOfFiredNotifEvent.get();
    }

    public long getNumOfExpiredEvent() {
        return numOfFiredExpireEvent.get();
    }

    public long getNumOfResponseOK() {
        return numOfResponseOK.get();
    }

    public long getNumOfResponseNormalRetry() {
        return numOfResponseNormalRetry.get();
    }

    public long getNumOfResponseFailed() {
        return numOfResponseFailed.get();
    }

    public long getNumOfResponseTemporary() {
        return numOfResponseTemporary.get();
    }

    public String getLastBackup() {
        return lastBackup;
    }
}
