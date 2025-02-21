/**
 * Copyright (c) 2010 Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.sms;

import java.util.concurrent.ConcurrentHashMap;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.out.vvm.VvmEvent;
import com.mobeon.ntf.out.vvm.VvmHandler;
/**
 * Listener for handling VVM responses.
 */
public class VvmSmsListener extends AbstractSMSResultHandler {
	
    private LogAgent log = NtfCmnLogger.getLogAgent(VvmSmsListener.class);
	
	/**
	 * SMSListenerEvent is used to keep event information.
	 */
	private class SMSListenerEvent extends SMSResultAggregator {
		private String notificationNumber;
		private VvmEvent vvmEvent;
		
		SMSListenerEvent(String notificationNumber, VvmEvent vvmEvent) {
			this.notificationNumber = notificationNumber;
			this.vvmEvent = vvmEvent;
		}
		
		String getNotificationNumber() {
			return notificationNumber;
		}
		
		VvmEvent getVvmEvent() {
			return vvmEvent;
		}
	}
	
	/** Event map */
	private ConcurrentHashMap<Integer, SMSListenerEvent> events = new ConcurrentHashMap<Integer, SMSListenerEvent>();

	/**
	 * Adds an event to this listener.
	 * 
	 * @param id Event unique ID.
	 * @param number Recipient phone number.
	 * @param vvmEvent Vvm Event.
	 */
	public void add(int id, String number, VvmEvent vvmEvent) {
		SMSListenerEvent event = new SMSListenerEvent(number, vvmEvent); 
		events.put(id, event);
	}

	/* (non-Javadoc)
	 * @see com.mobeon.common.sms.SMSResultHandler#ok(int)
	 */
	@Override
	public void ok(int id) {
		handleEvent(id, "OK", null, VvmEvent.VVM_EVENT_SMS_UNIT_SUCCESSFUL);
	}

	/* (non-Javadoc)
	 * @see com.mobeon.common.sms.SMSResultHandler#failed(int, java.lang.String)
	 */
	@Override
	public void failed(int id, String errorText) {
		handleEvent(id, "FAILED", errorText, VvmEvent.VVM_EVENT_SMS_UNIT_FAILED);
	}

	/* (non-Javadoc)
	 * @see com.mobeon.common.sms.SMSResultHandler#retry(int, java.lang.String)
	 */
	@Override
	public void retry(int id, String errorText) {
		handleEvent(id, "RETRY", errorText, VvmEvent.VVM_EVENT_SMS_UNIT_RETRY);
	}
	
	private void handleEvent(int id, String eventName, String errorText, int eventValue) {
		SMSListenerEvent event = events.remove(id);
		if (event == null) {
			if (log.isDebugEnabled()) {
			    log.debug("Received VVM SMS response " + eventName + " but could not find event in cache, dicarding.");
			}
			return;
		}

		String notificationNumber = event.getNotificationNumber();
		VvmEvent vvmEvent = event.getVvmEvent();
	
		if (log.isDebugEnabled()) {
			String msg = "Received VVM SMS response " + eventName + " for " + notificationNumber + " : " + vvmEvent.getNotificationType().getName();

			if (errorText != null) {
				msg = msg + " with message \"" + errorText + "\"";
			}
			log.debug(msg);
		}

		if(isVvmEventWaitingSmsInfoState(vvmEvent)) {
		    vvmEvent.setCurrentState(VvmEvent.STATE_SENDING_INFO);
		    vvmEvent.setCurrentEvent(eventValue);
		    
		    //add to worker queue
	        if(!VvmHandler.get().getWorkingQueue().offer(vvmEvent)) {
	            log.warn("VvmSmsListener: VVM SMS " + eventName + " response is DROPPED since queue is full: " + notificationNumber + " : " + vvmEvent.getNotificationType().getName());
	        }
		}
	}
	
	private boolean isVvmEventWaitingSmsInfoState(VvmEvent vvmEvent) {
        boolean result = false;
        String subscriberNumber = vvmEvent.getSubscriberNumber();

        if (vvmEvent.getSchedulerIds().getSendingUnitPhoneOnEventId() != null && vvmEvent.getSchedulerIds().getSendingUnitPhoneOnEventId().length() > 0) {
            log.warn("VvmSmsListener: SMS-Info result received for subscriber " + subscriberNumber + " while pending " + 
                      VvmEvent.STATE_STRING[VvmEvent.STATE_SENDING_PHONE_ON] + " eventId found: " + vvmEvent.getSchedulerIds().getSendingUnitPhoneOnEventId());
        }

        if (vvmEvent.getSchedulerIds().getWaitingPhoneOnEventId() != null && vvmEvent.getSchedulerIds().getWaitingPhoneOnEventId().length() > 0) {
            log.warn("VvmSmsListener: SMS-Info result received for subscriber " + subscriberNumber + " while pending " + 
                    VvmEvent.STATE_STRING[VvmEvent.STATE_WAITING_PHONE_ON] + " eventId found: " + vvmEvent.getSchedulerIds().getWaitingPhoneOnEventId());
        }
        
        if (vvmEvent.getSchedulerIds().getDeactivatorEventId() != null && vvmEvent.getSchedulerIds().getDeactivatorEventId().length() > 0) {
            log.warn("VvmSmsListener: SMS-Info result received for subscriber " + subscriberNumber + " while pending " + 
                    VvmEvent.STATE_STRING[VvmEvent.STATE_WAITING_PHONE_ON] + " eventId found: " + vvmEvent.getSchedulerIds().getDeactivatorEventId());
        }


        if (vvmEvent.getSchedulerIds().getSmsInfoEventId() != null && vvmEvent.getSchedulerIds().getSmsInfoEventId().length() > 0) {
            result = true;
        }

        return result;
    }

    @Override
    protected SMSResultAggregator getEvent(int id) {
        return events.get(new Integer(id));
    }    
}
