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

package com.mobeon.ntf.out.delayedevent;

import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.util.DesignSequenceDiagram;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.INotifierEventInfo;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.DelayedEvent;
import com.abcxyz.services.moip.ntf.event.DelayedEventFactory;
import com.abcxyz.services.moip.ntf.event.DelayedSMSReminder;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.management.ManagedLinkedBlockingQueue;
import com.mobeon.ntf.util.threads.NtfThread;


/**
 * This class takes an INotifierEventInfo object from the delayedEventsQueue, creates an DelayedEvent object from it,
 * then calls the corresponding delayed event handler based on the delayed event type.
 *
 * @author ewenxie
 * @author lmcmajo
 * @since vfe_nl33_mfd02  2015-07-14
 */
public class DelayedEventWorker extends NtfThread implements Constants {
    private ManagedLinkedBlockingQueue<INotifierEventInfo> delayedEventsQueue;
    private DelayedEventHandler delayHandler;
    private static LogAgent log =  NtfCmnLogger.getLogAgent(DelayedEventWorker.class);
    private static DelayedSmsReminderHandler delayedSmsHandler = DelayedSmsReminderHandler.get();

    public DelayedEventWorker(ManagedLinkedBlockingQueue<INotifierEventInfo> delayedEventsQueue, String threadName, DelayedEventHandler handler){
        super(threadName);
        this.delayedEventsQueue = delayedEventsQueue;
        this.delayHandler = handler;
    }

    /* (non-Javadoc)
     * @see com.mobeon.ntf.util.threads.NtfThread#ntfRun()
     */
    @Override
    public boolean ntfRun() {
        log.debug("Enter into DelayedEventWorker.ntfRun()");
        // Get an event from the delayedEventsQueue
        INotifierEventInfo eventInfo = null;
        try {
            eventInfo = delayedEventsQueue.take();
        } catch (InterruptedException ie) {
            return true;
        } //timeout in order to check for management status changes, locked, shutdown.

        if (eventInfo == null){
            return false;
        }

        log.debug("ntfRun(): eventInfo.getEventProperties()=" + eventInfo.getEventProperties());

        DelayedEvent delayedEvent = DelayedEventFactory.createDelayedEvent(eventInfo.getEventProperties());
        if (delayedEvent == null) {
            log.warn("ntfRun(): received invalid DelayedEvent, " + eventInfo.getEventProperties());
            return false;
        }

        log.debug("ntfRun(): " + DelayedEvent.DELAYED_EVENT_TYPE + " : " + delayedEvent.getDelayedEventType().type() + ", "
                + DelayedEvent.TRIGGER_TIME + " : " + delayedEvent.getTriggerTime() + ", " + DelayedEvent.ACTION + " : "
                + delayedEvent.getAction().action());

        // make sure the fired event is the one expected, as indicated in the status file.
        if (!delayHandler.doesPersitentIdMatchEventId(delayedEvent, eventInfo)) {
            log.debug("ntfRun(): EventId does not match, stop processing event " + delayedEvent.getMessageEventProperties());
            return false;
        }

        // write the eventId of the scheduled next retry for this event into the status file.
        log.debug("eventInfo.getNextEventId() = " + eventInfo.getNextEventId());
        delayHandler.updateSchedulerEventIdPersistenty(delayedEvent.getSubscriberNumber(), eventInfo.getNextEventId() , delayedEvent.getStatusFileName());

        if (eventInfo.isExpire()) { //this will happen if the expiration time specified in the delayed event retry schema is reached.
            log.warn("Failed to send DelayedEvent, expiration happened: " + delayedEvent.getMessageEventProperties());
            //CDR?
            delayHandler.cancelDelayedEvent(delayedEvent);
            return false;
        }


        switch (delayedEvent.getDelayedEventType()) {
            case DELAYEDSMSREMINDER:
                delayedSmsHandler.process((DelayedSMSReminder)delayedEvent);
                break;
            default:
                log.warn("ntfRun(): Unknown DeleaydEvent type " + delayedEvent.getDelayedEventType());
                return false;
        }

        return false;
    }

    /**
     * @override
     * The shutdown loop tries to drain the queue until forced to shutdown
     * A small guard time is included to allow other threads to drain into this one.
     * @return true when exit
     **/
    public boolean shutdown() {
        if (isInterrupted()) {
            return true;
        } // exit immediately if interrupted.

        if (delayedEventsQueue.size() == 0) {
            // give a short time for new items to be queued in workers, to allow other threads to empty there queues.
            if (delayedEventsQueue.isIdle(2, TimeUnit.SECONDS)) {
                DesignSequenceDiagram.printFullSequence();
                return true;
            } else {
                try {
                    if (delayedEventsQueue.waitNotEmpty(2, TimeUnit.SECONDS)) {
                        if (ntfRun() == true) {
                            DesignSequenceDiagram.printFullSequence();
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        DesignSequenceDiagram.printFullSequence();
                        return true;
                    }
                } catch (InterruptedException e) {
                    return false; // forced shutdown.
                }
            }
        } else {
            if (ntfRun() == true) {
                DesignSequenceDiagram.printFullSequence();
                return true;
            } else {
                return false;
            }
        }
    }

}
