package com.mobeon.masp.execution_engine.platformaccess;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.SubscriptionState;

import java.util.Properties;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.handling.AbstractEventStatusListener;
import com.abcxyz.messaging.scheduler.handling.AppliEventHandler;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.masp.callmanager.notification.OutboundNotification;

public class SubscriptionExpiryScheduler extends AbstractEventStatusListener {

	private static ILogger logger = ILoggerFactory.getILogger(SubscriptionExpiryScheduler.class);

    private static final String SUBSCRIPTION_SERVICE_NAME = "Subscription-Expires";
	private static final int SCHEDULER_PRECISION = 60 * 1000;
	public static final String USER_AGENT = "userAgentNumber";
	public static final String EXPIRY_EVENT_ID = "ExpiryEventId";
	public static final String MAILBOX_ID = "mailboxId";
	public static final String CALLED_NUMBER = "callednumber";
	public static final String FORCE = "force";
	private static SubscriptionExpiryScheduler instance;
    int maximumNumberOfTries = 10;

	AppliEventHandler eventHandler;

	private SubscriptionExpiryScheduler() {
        ConfigManager localConfig = CommonOamManager.getInstance().getLocalConfig();

        String subscriptionExpiryIntervalInMin = localConfig.getParameter(MoipMessageEntities.sipNotifyExpiryIntervalInMin);
	    RetryEventInfo info = new RetryEventInfo(SUBSCRIPTION_SERVICE_NAME);
        info.setEventRetrySchema(subscriptionExpiryIntervalInMin + "m CONTINUE");
		eventHandler = new AppliEventHandler(info, this);

        maximumNumberOfTries = localConfig.getIntValue(MoipMessageEntities.sipNotifyExpiryRetries);
	}

	public static SubscriptionExpiryScheduler getInstance() {
		if (instance == null) {
			instance = new SubscriptionExpiryScheduler();
		}
		return instance;
	}

	public AppliEventHandler getEventHandler() {
		return this.eventHandler;
	}

	public int eventFired(AppliEventInfo eventInfo) {
        logger.debug("Received event from scheduler: " + eventInfo.getEventId() );

        if (eventInfo.getNumberOfTried() >= maximumNumberOfTries) {
            logger.info("Expiry event " + eventInfo.getEventId() + ", will not retry (number of expiry retries reached");
            return EventHandleResult.STOP_RETRIES;
        }

        return handleExpiry(eventInfo.getEventProperties(), eventInfo);
	}

	public int handleExpiry(Properties prop, AppliEventInfo eventInfo) {
        int result = EventHandleResult.OK;

        try {
			String userAgent = prop.getProperty(USER_AGENT);
			String mailboxId = prop.getProperty(MAILBOX_ID);
	        String fileName = OutboundNotification.getSubscriptionFileName(userAgent);
			logger.debug("handleExpiry() File is: " + fileName);
			MfsEventManager mfsEventManager = MfsEventFactory.getMfsEvenManager();

			if (mfsEventManager.fileExists(mailboxId, fileName, true)) {
				Properties oldProp = mfsEventManager.getProperties(mailboxId, fileName);
				SubscriptionState ssHeader = new SubscriptionState();
				ssHeader.setState(SubscriptionState.TERMINATED);
				ssHeader.setReasonCode("timeout");
				oldProp.put(SIPHeader.SUBSCRIPTION_STATE, ssHeader.toString());
				String storedEventId = oldProp.getProperty(EXPIRY_EVENT_ID);

				boolean shouldProcessFiredEvent = CommonMessagingAccess.getInstance().compareEventIds(eventInfo, storedEventId);
                if (!shouldProcessFiredEvent) {
                    return EventHandleResult.STOP_RETRIES;
                }

				logger.debug("Canceling the subscription timer (in file) for " + mailboxId);
				mfsEventManager.storeProperties(mailboxId, fileName, oldProp);

	    		MessageInfo msgInfo = new MessageInfo();
	    		msgInfo.rmsa = new MSA(MfsEventManager.getMSID(mailboxId));
    			MfsEventManager.getMfs().notifyNtf(EventTypes.MAILBOX_UPDATE, msgInfo, mailboxId, prop);
			} else {
			    logger.debug("handleExpiry(): File does not exist.");
			    result = EventHandleResult.STOP_RETRIES;
			}
        } catch (Exception e) {
            String message = "Cannot cancel SipSubscriptiondelete for mailboxId " + prop.getProperty(MAILBOX_ID);
            if (eventInfo.getNumberOfTried() >= maximumNumberOfTries || eventInfo.getNextEventInfo() == null || eventInfo.isLastExpire()) {
	            logger.error(message + ", will not retry");
	            result = EventHandleResult.STOP_RETRIES;
	        } else {
                logger.warn(message + ", will retry");
	        }
		}

        return result;
	}

    /* (non-Javadoc)
	 * @see com.abcxyz.messaging.scheduler.handling.EventsStatusListener#reportCorruptedEventFail(java.lang.String)
	 */
	@Override
	public void reportCorruptedEventFail(String eventId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Report corrupted event fail: " + eventId);
		}
	}

	/* (non-Javadoc)
	 * @see com.abcxyz.messaging.scheduler.handling.EventsStatusListener#reportEventCancelFail(com.abcxyz.messaging.scheduler.handling.AppliEventInfo)
	 */
	@Override
	public void reportEventCancelFail(AppliEventInfo eventInfo) {
		if (logger.isDebugEnabled()) {
			logger.debug("Report cancel event fail: " + eventInfo);
		}
	}

	public void reportEventScheduleFail(AppliEventInfo eventInfo) {
		if (logger.isDebugEnabled()) {
			logger.debug("Report scheduled event fail: " + eventInfo);
		}
	}

	public String scheduleExpiryTimer(String mailboxId, String userAgentNumber, long expires){
	    Properties  properties = new Properties();
		properties.put(SubscriptionExpiryScheduler.USER_AGENT, userAgentNumber);
		properties.put(SubscriptionExpiryScheduler.MAILBOX_ID, mailboxId);
		properties.put(SubscriptionExpiryScheduler.CALLED_NUMBER, mailboxId);
		properties.put(SubscriptionExpiryScheduler.FORCE, "true");

		AppliEventInfo eventInfo = eventHandler.scheduleEvent(expires + SCHEDULER_PRECISION,
		        CommonMessagingAccess.getUniqueId(),
				EventTypes.INTERNAL_TIMER.getName(),
				properties);

		return eventInfo.getEventId();
	}

	public void cancelExpiry(String expiryEventId){
		AppliEventInfo previous = new AppliEventInfo();
		previous.setEventId(expiryEventId);
		eventHandler.cancelEvent(previous);
	}
}

