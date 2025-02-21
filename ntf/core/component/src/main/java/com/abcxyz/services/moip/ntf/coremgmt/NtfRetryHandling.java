package com.abcxyz.services.moip.ntf.coremgmt;

import java.util.Properties;

import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 *
 * Interface defines event persistence handling inside NTF
 *
 * @author lmchuzh
 *
 */
public interface NtfRetryHandling
{

	/**
	 * 
	 * return this retry's service name
	 * @return
	 */
	public String getEventServiceName();

	/**
	 * schedule a retry event, retry time and service event type will be retrieved
	 * from retry schema in the handler. Automatically generates a unique event ID
	 *
	 * @param properties
	 * @return
	 */

	public NtfEvent scheduleEvent(Properties properties);
	/**
	 * schedule a retry event for a specific NTF service event,
	 * retry time and service type will be retrieved from its handler
	 *
	 * @param uid: unique ID identifies event ID
	 * @param properties: event properties
	 * @return
	 */
	public NtfEvent scheduleEvent(String uid, Properties properties);


     /*
      *  schedule first retry
      *
      * @param event
      * @return
      */
    public String scheduleEvent(NtfEvent event);
    public String scheduleEvent(NtfEvent event, long delay);


    /**
     * cancel event
     *
     * @param eventId
     */
    public void cancelEvent(String eventId);
    
    /**
     * Schedules a reminder trigger event for the Notification Reminder feature.
     * @param event - the notification event for the current new deposit
     * @return scheduled EventId string
     */
    public String scheduleReminderTriggerEvent(NtfEvent event);
    
    /**
     * Cancels the given reminder trigger event.
     * @param eventId - the EventId string identifying the event to cancel
     */
    public void cancelReminderTriggerEvent(String eventId);
    
    /**
     * Reschedules the given reminder trigger event to (current time + reminder interval).
     * @param oldEventId the EventId string identifying the event to reschedule
     * @return new EventId string
     */
    public String rescheduleReminderTriggerEvent(String oldEventId);

    /**
     * Schedules a delayed AutoUnlockPin unlock event for the AutoUnlockPin feature.
     * @param event - the event for this notification
     * @param user TODO
     * @return scheduled EventId string
     */
    public String scheduleAutoUnlockPinEvent(NtfEvent event, UserInfo user);
    
    /**
     * Schedules a AutoUnlockPin SMS notification event for the AutoUnlockPin feature.
     * @param event - the event for this notification
     * @return scheduled EventId string
     */
    public String scheduleAutoUnlockPinSmsEvent(NtfEvent event);
    
}
