/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule;

import java.util.Properties;

/**
 * The INotifierEventInfo interface defines the methods that the Notifier plug-in can invoke to obtain
 * information about a scheduled event.
 * <p>
 * When an event is scheduled, a INotifierEventInfo object is returned to the Notifier plug-in.  
 * Since the event was just scheduled, the details of the events are known.  
 * In this case, the information that is typically retrieved from the INotifierEventInfo object is 
 * the event id of the newly scheduled event.  
 * The event id can be used to cancel this scheduled event.
 * <p>
 * When an event is fired, a INotifierEventInfo object is passed as an argument to the Notifier plug-in.
 * In this case, the information that is typically retrieved from the INotifierEventInfo object includes 
 * the event properties which can be used to process the event and 
 * the event id of the next retry so that the next retry can be cancelled upon successful processing of the event. 
 */
public interface INotifierEventInfo {

    /**
     * Gets the event id of the current scheduled event.
     * @return the event id of the current scheduled event, or null if the event could not be scheduled
     */
    public String getEventId();
    
    /**
     * Gets the event type.
     * <p>
     * For simplicity, the Notifier scheduling mechanism uses the event service name as the event type.
     * The event service name is specified when calling 
     * {@link INotifierEventScheduler#registerEventService(String, ANotifierEventRetryInfo, ANotifierEventHandler)}. 
     * 
     * @return the event type which is the same as the event service name
     */
    public String getEventType();
    
    /**
     * Gets the properties associated with the event.
     * @return the properties associated with the event
     */
    public Properties getEventProperties();
    
    /**
     * Returns whether this event is expired.  An event is expired when all retries have been exhausted.
     * <p>
     * An expiration of an event can be used as a trigger to perform any closing tasks associated with this event.  
     * For example, deletion of the notification payload file or generation of a MDR.
     * @return true if this event is expired; false otherwise.
     */
    public boolean isExpire();
    
    /**
     * Returns whether this is the last retry to signal the expiration of this event. 
     * @return true if this is the last try to signal the expiration of this event; false otherwise
     */
    public boolean isLastExpire();
    
    /**
     * Returns whether the next retry of this event has been scheduled.
     * @return true if the next retry of this event has been scheduled; false otherwise
     */
    public boolean isNextRetryScheduled();
    
    /**
     * Returns the event id of the next scheduled retry for this event.
     * @return the event id of the next scheduled retry, or null if there is no next scheduled retry
     */
    public String getNextEventId();
    
    /**
     * Returns the number of tries that has occurred for this event.
     * @return the number of tries that has occurred for this event
     */
    public int getNumberOfTried();
    
    /**
     * Returns whether the specified event id matches the event id of this current event.
     * <p>
     * When matching event ids, the event properties are not considered.
     * @param eventId the event id to verify
     * @return true if the specified event id matches the event id of this current event; false otherwise
     */
    public boolean isEventIdsMatching(String eventId);
}
