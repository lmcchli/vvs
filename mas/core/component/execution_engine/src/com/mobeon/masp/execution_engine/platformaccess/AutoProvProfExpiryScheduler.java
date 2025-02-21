package com.mobeon.masp.execution_engine.platformaccess;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.handling.AbstractEventStatusListener;
import com.abcxyz.messaging.scheduler.handling.AppliEventHandler;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccessException;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryUpdater;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.masp.callmanager.notification.OutboundNotification;

public class AutoProvProfExpiryScheduler extends AbstractEventStatusListener{

	private static ILogger logger = ILoggerFactory.getILogger(AutoProvProfExpiryScheduler.class);

    private static final String AUTOPROV_SERVICE_NAME = "AutoProv-Profile-Expires";
	private static final int SCHEDULER_PRECISION = 60 * 1000;
	public static final String MUID= "muid";
	public static final String USER_AGENT = "userAgentNumber";
	public static final String EXPIRY_EVENT_ID = "ExpiryEventId";
	public static final String FORCE = "force";
	private static AutoProvProfExpiryScheduler instance = null;
    int maximumNumberOfTries = 6;
    
	AppliEventHandler eventHandler;

	private AutoProvProfExpiryScheduler() {

        RetryEventInfo info = new RetryEventInfo(AUTOPROV_SERVICE_NAME);
        // Client which will schedule passes 'when'; retry is only used to make sure
        // the file containing the event will correctly be deleted when expiry occurs
        info.setEventRetrySchema("1440 CONTINUE");
		eventHandler = new AppliEventHandler(info, this);
	}

	public static AutoProvProfExpiryScheduler getInstance() {
		if (instance == null) {
			instance = new AutoProvProfExpiryScheduler();
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
		// The expiry of this event means that the autoprovisioned subscriber's account has
		// not been accessed for long enough that it must be autodeleted

		//TODO MUST FIND A WAY TO PASS ENOUGH INFO SO THAT WE CAN DOUBLE-CHECK THIS SUBSCRIBER'S
		//     COS 'ON-THE-FLY' BEFORE DELETING IT TO MAKE SURE THAT THE AUTODELETION FLAG IS
		//     STILL SET FOR ITS COS...
		
		// if all goes well, the default will be to stop retrying since we will have
		// deleted the subscriber altogether...
		int result = EventHandleResult.STOP_RETRIES;

        try {
        	String muid = prop.getProperty(MUID);
			String userAgent = prop.getProperty(USER_AGENT);
			logger.debug("handleExpiry(): muid: " + muid + ", userAgent: " + userAgent);
	        String fileName = OutboundNotification.getAutodeletionFileName(userAgent);
			logger.debug("handleExpiry(): File is: " + fileName);
			MfsEventManager mfsEventManager = MfsEventFactory.getMfsEvenManager();

			if (mfsEventManager.fileExists(userAgent, fileName, true)) {
				Properties oldProp = mfsEventManager.getProperties(userAgent, fileName);
				String storedEventId = oldProp.getProperty(EXPIRY_EVENT_ID);

				boolean shouldProcessFiredEvent = CommonMessagingAccess.getInstance().compareEventIds(eventInfo, storedEventId);
                if (!shouldProcessFiredEvent) {
                    return EventHandleResult.STOP_RETRIES;
                }

				logger.debug("handleExpiry(): Validated expiry of Autoprov; proceding with " +
				             "Autodeletion of subscriber " + userAgent);
				if (!mfsEventManager.removeFile(userAgent, fileName, true)) {
					// can't remove file; normally this shouldn't cause any problem
					// since autodeletion will remove the whole directory anyway but
					// just in case, remember to return "OK" to retry...
					result = EventHandleResult.OK;
				}
				
				//TODO ONLY DEPROVISION IF THE SUBSCRIBER'S COS STILL SAYS YES!!!

		    	URI keyId = null;
		    	try {
		    		keyId = new URI(muid);
		    	}
		    	catch (URISyntaxException e) {
		            String message = "Cannot create URI from muid; cannot proceed with Autodeletion for muid " +
		    	                     muid + ", userAgent" + userAgent;
		            logger.error(message);
		            return EventHandleResult.STOP_RETRIES;
		    	}
		    	
				logger.debug("handleExpiry(): calling deleteProfile with keyId: " + keyId);
				try {
					DirectoryUpdater.getInstance().deleteProfile("subscriber", keyId);			
				}
				catch (DirectoryAccessException e) {
		            String message = "Failed to delete subscriber profile with muid " + muid +
		            		         ", userAgent " + userAgent;
		            logger.error(message);
		            return EventHandleResult.STOP_RETRIES;
				}
				
				
			} else {
			    logger.debug("handleExpiry(): File does not exist.");
			    result = EventHandleResult.STOP_RETRIES;
			}
			
        } catch (Exception e) {
            String message = "Cannot proceed with Autodeletion for muid " + prop.getProperty(MUID);
            if (eventInfo.getNumberOfTried() >= maximumNumberOfTries ||
            		eventInfo.getNextEventInfo() == null || eventInfo.isLastExpire()) {
	            logger.error(message + ", will not retry");
	            result = EventHandleResult.STOP_RETRIES;
	        } else {
                logger.warn(message + ", will retry");
	            result = EventHandleResult.OK;
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

	public String scheduleExpiryTimer(String userAgentNumber, String muid, long expires){
	    Properties  properties = new Properties();
		properties.put(AutoProvProfExpiryScheduler.MUID, muid);
		properties.put(AutoProvProfExpiryScheduler.USER_AGENT, userAgentNumber);
		properties.put(AutoProvProfExpiryScheduler.FORCE, "true");

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
