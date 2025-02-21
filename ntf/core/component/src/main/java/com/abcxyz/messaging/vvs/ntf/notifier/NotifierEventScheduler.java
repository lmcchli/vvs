/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.EventHandleResult;
import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.EventProperties;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.scheduler.handling.AbstractEventStatusListener;
import com.abcxyz.messaging.scheduler.handling.AppliEventHandler;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.EventsStatusListener;
import com.abcxyz.messaging.scheduler.handling.LocalEventHandler;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.ANotifierEventHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.ANotifierEventRetryInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.INotifierEventInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.INotifierEventScheduler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;


public class NotifierEventScheduler extends AbstractEventStatusListener implements INotifierEventScheduler {

    /*Special class to override expiry time to be relative to the first specified trigger time instead of now
     * For when you want to add a retry schema to an initial delay.
     */
    private class NtfAppliEventHandler extends AppliEventHandler {

        private long expireTime;

        public NtfAppliEventHandler(RetryEventInfo info, EventsStatusListener eventListener) {
            super(info, eventListener);
            expireTime = info.getExpireTimeInMinute()*60*1000;
        }

        public AppliEventInfo scheduleEventWithRelativeExpriry(long when, String id, String type, Properties properties)
        {
            if (properties == null) {
                properties = new Properties();
            }

            if (expireTime != 0) {
                String time;
                if (when == 0) {
                    time = Long.toString(System.currentTimeMillis() + expireTime);
                } else {
                    //set expire time relative to the initial trigger time, which effectively becomes the first try before the schema
                    time = Long.toString(when + expireTime);
                }
                properties.put(EventProperties.EXPIRY_TIME, time);
            } else {
                properties.put(EventProperties.EXPIRY_TIME, "0");
            }

            //first try for schema will be after when
            if (when > System.currentTimeMillis()) {
                properties.setProperty(EventProperties.NUM_TRIED, "0");
            } else {
                properties.setProperty(EventProperties.NUM_TRIED, "1");
            }

            String[] props = retrieveProperties(properties);

            String eventId = eventHandler.scheduleFast(when, id, type, props);

            AppliEventInfo eventInfo = new AppliEventInfo();
            eventInfo.setEventId(eventId);
            eventInfo.setEventType(type);
            eventInfo.setEventKey(id);
            eventInfo.setEventProperties(properties);

            return eventInfo;
        }

    }

    private static LogAgent log = NtfCmnLogger.getLogAgent(NotifierEventScheduler.class);
    private static final String SERVICE_NAME = "Notifier";

    /** Handles all event cancellations. */
    AppliEventHandler cancelEventHandler = null;

    /** The map of schedulers, keyed by scheduler name. */
    private HashMap<String, Scheduler> schedulers;


    public NotifierEventScheduler() {
        RetryEventInfo cancelRetryEventInfo = new RetryEventInfo(SERVICE_NAME);
        cancelEventHandler = new AppliEventHandler(cancelRetryEventInfo, this);

        schedulers = new HashMap<String, Scheduler>();
    }

    @Override
    public void registerEventService(String serviceName, ANotifierEventRetryInfo notifierEventRetryInfo, ANotifierEventHandler notifierEventHandler) {
        log.debug("Registering service with notifierEventRetryInfo: " + notifierEventRetryInfo);

        RetryEventInfo retryInfo = new RetryEventInfo(notifierEventRetryInfo.getEventServiceName());
        retryInfo.setEventRetrySchema(notifierEventRetryInfo.getEventRetrySchema());
        retryInfo.setExpireTimeInMinute(notifierEventRetryInfo.getExpireTimeInMinute());
        retryInfo.setExpireRetryTimerInMinute(notifierEventRetryInfo.getExpireRetryTimerInMinute());
        retryInfo.setMaxExpireTries(notifierEventRetryInfo.getMaxExpireTries());

        schedulers.put(serviceName, new Scheduler(new NtfAppliEventHandler(retryInfo, this), notifierEventHandler));
    }

    @Override
    public INotifierEventInfo scheduleEvent(String serviceName, String id, Properties props) {
        log.debug("Scheduling event with: serviceName=" + serviceName + " id=" + id + " properties=" + props);
        INotifierEventInfo eventInfo = null;
        Scheduler scheduler = schedulers.get(serviceName);
        if(scheduler != null) {
            AppliEventInfo appliEventInfo = scheduler.appliEventHandler.scheduleEvent(id, serviceName, props);
            eventInfo = new NotifierEventInfo(appliEventInfo);
        } else {
            log.error("Could not find specific scheduler for service " + serviceName);
        }
        return eventInfo;
    }

    @Override
    public INotifierEventInfo scheduleEvent(String serviceName, String id, Properties props, long when) {
        log.debug("Scheduling event with: serviceName=" + serviceName + " id=" + id + " properties=" + props + " when=" + when);
        INotifierEventInfo eventInfo = null;
        Scheduler scheduler = schedulers.get(serviceName);
        if(scheduler != null) {
            AppliEventInfo appliEventInfo = scheduler.appliEventHandler.scheduleEvent(when, id, serviceName, props);
            eventInfo = new NotifierEventInfo(appliEventInfo);
        } else {
            log.error("Could not find specific scheduler for service " + serviceName);
        }
        return eventInfo;
    }

    @Override
    public INotifierEventInfo scheduleEventWithRelativeExpiry(String serviceName, String id, Properties props, long when) {
        log.debug("Scheduling event with: serviceName=" + serviceName + ", id=" + id + ", properties=" + props + ", when=" + when);
        INotifierEventInfo eventInfo = null;
        Scheduler scheduler = schedulers.get(serviceName);
        if(scheduler != null) {
            AppliEventInfo appliEventInfo = scheduler.appliEventHandler.scheduleEventWithRelativeExpriry(when, id, serviceName, props);
            eventInfo = new NotifierEventInfo(appliEventInfo);
        } else {
            log.error("Could not find specific scheduler for service " + serviceName);
        }
        return eventInfo;
    }

    @Override
    public void cancelEvent(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return;
        }

        log.debug("Cancelling event: " + eventId);
        AppliEventInfo eventInfo = new AppliEventInfo();
        eventInfo.setEventId(eventId);
        cancelEventHandler.cancelEvent(eventInfo);
    }

    @Override
    public int eventFired(AppliEventInfo eventInfo) {
        log.debug("Event fired: " + eventInfo.getEventId());
        int result = EventHandleResult.OK;
        try {
            EventID eventID = new EventID(eventInfo.getEventId());
            Scheduler scheduler = schedulers.get(eventID.getServiceName());
            if(scheduler != null) {
                result = scheduler.notifierEventHandler.eventFired(new NotifierEventInfo(eventInfo));
            } else {
                log.error("Could not find specific scheduler to handle event fired for service " + eventID.getServiceName());
            }
        } catch (InvalidEventIDException e) {
            log.error("Fired event id is invalid: " + eventInfo.getEventId());
            result = EventHandleResult.STOP_RETRIES;
        }
        return result;
    }


    private class Scheduler {
        /** Handles the scheduling of events */
        private NtfAppliEventHandler appliEventHandler;

        /** Handles the firing of scheduled events */
        private ANotifierEventHandler notifierEventHandler;


        public Scheduler(NtfAppliEventHandler appliEventHandler, ANotifierEventHandler notifierEventHandler) {
            this.appliEventHandler = appliEventHandler;
            this.notifierEventHandler = notifierEventHandler;
        }
    }

}
