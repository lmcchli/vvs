/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2015.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.abcxyz.services.moip.ntf.coremgmt;

import java.util.Date;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.vvs.ntf.notifier.NotifierNtfServicesManager;
import com.abcxyz.messaging.vvs.ntf.notifier.NotifierUtil;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.ANotifierEventHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.INotifierEventInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.INotifierEventScheduler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.NotifierEventRetryInfo;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.DelayedEvent;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.management.ManagedLinkedBlockingQueue;
import com.mobeon.ntf.out.delayedevent.DelayedEventHandler;

/**
 * This class schedules a received delayed event from MAS to trigger at the specified triggertime,
 * and handles the delayed event fired by the scheduler.
 *
 * @author ewenxie
 * @author lmcmajo
 * @since vfe_nl33_mfd02  2015-07-14
 */
public class DelayedEventTriggerHandler extends ANotifierEventHandler {
    private static LogAgent log = NtfCmnLogger.getLogAgent(DelayedEventTriggerHandler.class);

    private static final String DELAYED_EVENT_TRIGGERED_NAME="DlydEvtTriggered";
    private static final int MAX_EXPIRE_RETRIES = 16; // 16 retries at 15 minutes interval equals 4 hrs, the length of a typical maintenance window.
    private static final int MAX_EXPIRE_RETRY_TIME = 15;
    private static final int MAX_DELAY_DAYS = 92; // delayed events after this number of days won't be scheduled.


    private static DelayedEventTriggerHandler inst=null;


    private ManagedLinkedBlockingQueue<INotifierEventInfo> delayedEventsQueue;


    private INotifierEventScheduler eventScheduler;

    private NotifierEventRetryInfo delayEventRetryInfo  = null;

    private DelayedEventTriggerHandler(){

        eventScheduler = NotifierNtfServicesManager.get().getEventScheduler();

        String retrySchema  = Config.getDelayedEventRetrySchema();
        long expireTime     = Config.getDelayedEventRetryExpireTime();
        delayEventRetryInfo  = new NotifierEventRetryInfo(DELAYED_EVENT_TRIGGERED_NAME , retrySchema, expireTime);
        delayEventRetryInfo.setExpireRetryTimerInMinute(MAX_EXPIRE_RETRY_TIME);
        delayEventRetryInfo.setMaxExpireTries(MAX_EXPIRE_RETRIES);

        eventScheduler.registerEventService(getEventServiceName(), delayEventRetryInfo , this);

    }

    public static  String getEventServiceName() {
        return DELAYED_EVENT_TRIGGERED_NAME;
    }

    /**
     * Schedules the received delayed event from MAS. If the triggertime is now or even in the past, the event will be scheduled
     *  to fire at the next retry time according to delayed event retry schema. Otherwise it will be scheduled to fire at the
     *  specified triggertime.
     *
     * @param delayedEvent the delayed event originates from MAS.
     * @return the eventId if the event is scheduled successfully, otherwise returns null.
     */
    public String scheduleDelayedEvent(DelayedEvent delayedEvent) {


        Date triggerDate = delayedEvent.getTriggerTime();
        INotifierEventInfo eventInfo = null;

        long currentTime = System.currentTimeMillis();

        if(isTriggerDateBeyondLimitation(triggerDate)){
            log.error("Delayed event after " + MAX_DELAY_DAYS + " days can't be scheduled!");
            return null;
        }

        if (triggerDate.getTime() <= currentTime) {
            //This could happen if the MAS couldn't send the delayed event to NTF for a long time, but the chance is
            //very very low, since the minimum time the subscriber can set to remind him is after 1 day.
            //Decision: late is better than never, so send the reminder NOW.
            log.info("The " + DelayedEvent.TRIGGER_TIME + " " + triggerDate + " isn't in the future, scheduling to trigger at next retry as specified in retry schema" );
            eventInfo = eventScheduler.scheduleEvent(getEventServiceName(), delayedEvent.getSubscriberNumber() + NotifierUtil.get().getUniqueEventSchedulingId(), delayedEvent.getEventProperties());
        } else {
            log.debug("The delayedevent will be triggered at " + triggerDate.toString());

            //FIXME should be persistent properties but need to set them up properly.
            //This is so we don't add extra stuff not used such as Description which is not needed.
            eventInfo = eventScheduler.scheduleEventWithRelativeExpiry(getEventServiceName(), delayedEvent.getSubscriberNumber() + NotifierUtil.get().getUniqueEventSchedulingId(), delayedEvent.getEventProperties(), triggerDate.getTime());
        }

        if (eventInfo != null) {
            return eventInfo.getEventId();
        }
        else {
            return null;
        }
    }

    private boolean isTriggerDateBeyondLimitation(Date triggerDate){
        //long currentTime = System.currentTimeMillis();
        //log.debug("triggerDate.getTime() = " + triggerDate.getTime() + ", currentTime = " + currentTime);

        //Here the l symbol after 86400000 is vital since it ensures the calculation is done with type long,
        //otherwise it will go beyond the range of type int
        if(triggerDate.compareTo(new Date(System.currentTimeMillis() + MAX_DELAY_DAYS * 86400000l)) > 0){
            return true;
        }
        else{
            return false;
        }
    }

    public void cancelDelayedEvent(String eventId) {
        log.debug("Entering cancelDelayedEvent()");
        eventScheduler.cancelEvent(eventId);
    }


    /**
     * Handles the event fired by putting it into delayedEventsQueue. This is the callback method when an event
     * is fired by the Notifier scheduler.
     *
     * @param   eventInfo INotifierEventInfo object containing the information about the fired event.
     * @return  If the delayedEventsQueue is already full and this event can't be added to this queue, returns
     *          {@link com.abcxyz.messaging.scheduler.EventHandleResult#ERR_QUEUE_FULL}, otherwise returns
     *          NOTIFIER_EVENT_HANDLE_RESULT_OK to acknowledge the firing of the event.
     */
    public int eventFired(INotifierEventInfo eventInfo) {

        int result = NOTIFIER_EVENT_HANDLE_RESULT_OK;

        if(!DelayedEventHandler.get().isStarted()){
            log.error("Received delayedevent but service is not available");
            return NOTIFIER_EVENT_HANDLE_RESULT_OK; //let it retry, maybe it's not fully up yet or another NTF can handle it.
        }

        boolean storedInQueue = delayedEventsQueue.offer(eventInfo);
        if(storedInQueue){
            log.debug("DelayedEventTriggerHandler.eventFired(): event is stored into delayedEventsQueue");
        }
        else{
            log.warn("DelayedEventTriggerHandler.eventFired(): event is not stored into delayedEventsQueue, since the queue is full, will retry");
            return EventHandleResult.ERR_QUEUE_FULL;
        }

        return result;
    }

    public void setDelayedEventsQueue(ManagedLinkedBlockingQueue<INotifierEventInfo> delayedEventsQueue) {
        this.delayedEventsQueue = delayedEventsQueue;
    }

    public static DelayedEventTriggerHandler get() {
        if (inst != null) {
            return inst;
        }

        inst = new DelayedEventTriggerHandler();
        return inst;
    }

}
