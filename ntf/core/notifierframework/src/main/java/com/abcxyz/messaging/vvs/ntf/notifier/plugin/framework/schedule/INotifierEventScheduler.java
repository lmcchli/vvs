/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule;

import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierUtil;


/**
 * The INotifierEventScheduler interface defines the methods that the Notifier plug-in can invoke to schedule and cancel events.
 * <p>
 * Scheduled events can contain information for an event in the form of properties, allowing the delay of the processing of an event to a later time.
 * <p>
 * Scheduled events can also be used as a back up or retry.
 * Once the event has been successfully processed, the scheduled back up or retry event can be cancelled.
 * <p>
 * These scheduled events are saved persistently on the file system so they will survive a NTF restart.
 * <p>
 * The Notifier plug-in needs to schedule only the initial event.  Subsequent retries are taken care of by the Notifier scheduling mechanism.
 * When a scheduled event is fired, prior to calling {@link ANotifierEventHandler#eventFired(INotifierEventInfo)}, the Notifier scheduling mechanism
 * will schedule the next retry according to the retry schema for the event service.
 */
public interface INotifierEventScheduler {

    /**
     * Registers an event service with its name, retry schema and callback event handler.
     * <p>
     * Before an event can be scheduled for an event service, the event service must be registered.
     * @param serviceName the name of the event service.  This name must be unique to this event service.
     * @param notifierEventRetryInfo the ANotifierEventRetryInfo object containing the details of the retry schema
     * @param notifierEventHandler the ANotifierEventHandler which will be called when a scheduled event is fired
     */
    public void registerEventService(String serviceName, ANotifierEventRetryInfo notifierEventRetryInfo, ANotifierEventHandler notifierEventHandler);

    /**
     * Schedules an event for the specified event service at the first retry time.
     * The first retry time is taken from the retry schema with which the event service was registered.
     * @param serviceName the name of the service under which the event will be scheduled
     * @param id the unique id for this event.  This id can be generated using {@link INotifierUtil#getUniqueEventSchedulingId}.
     *           Please refer to the sample code provided in the VVS NTF Notifier SDK.
     * @param props the properties containing information regarding the scheduled event.
     *              These properties can be used to process the event when it is fired.
     * @return INotifierEventInfo object containing the information about the scheduled event, or null if the event could not be scheduled
     */
    public INotifierEventInfo scheduleEvent(String serviceName, String id, Properties props);

    /**
     * Schedules an event for the specified event service at the specified time.
     * @param serviceName the name of the service under which the event will be scheduled
     * @param id the unique id for this event.  This id can be generated using {@link INotifierUtil#getUniqueEventSchedulingId}.
     *           Please refer to the sample code provided in the VVS NTF Notifier SDK.
     * @param props the properties containing information regarding the scheduled event.
     *              These properties can be used to process the event when it is fired.
     * @param when the specified time at which the event should fire for the first time
     * @return INotifierEventInfo object containing the information about the scheduled event, or null if the event could not be scheduled
     */
    public INotifierEventInfo scheduleEvent(String serviceName, String id, Properties props, long when);

    /**
     * Schedules an event for the specified event service at the specified time, but adjusts the expiry time based on the
     * initial when instead of current time.
     * @param serviceName the name of the service under which the event will be scheduled
     * @param id the unique id for this event.  This id can be generated using {@link INotifierUtil#getUniqueEventSchedulingId}.
     *           Please refer to the sample code provided in the VVS NTF Notifier SDK.
     * @param props the properties containing information regarding the scheduled event.
     *              These properties can be used to process the event when it is fired.
     * @param when the specified time at which the event should fire for the first time
     * @return INotifierEventInfo object containing the information about the scheduled event, or null if the event could not be scheduled
     */
    public INotifierEventInfo scheduleEventWithRelativeExpiry(String serviceName, String id, Properties props, long when);


    /**
     * Cancels the scheduled event with the specified event id.
     * @param eventId the id of the event to cancel.  This id should originate from a call to {@link INotifierEventInfo#getEventId()}.
     */
    public void cancelEvent(String eventId);

}
