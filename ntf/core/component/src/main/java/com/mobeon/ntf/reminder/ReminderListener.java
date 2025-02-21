package com.mobeon.ntf.reminder;

import com.mobeon.common.storedelay.*;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;

/**
 * Main class for outdial handling.
 * Receives timed events or notifications about an ongoing
 * outdial attempt.
 * Checks what is to be done next with the outdial depending
 * on the incoming event/notification, the outdial attempts
 * remaining operations and the attempts state.
 */
public class ReminderListener implements DelayListener

{

    /** Identity for this listener. */
    private String id;
    /** Queue where received events are put. */
    private ManagedArrayBlockingQueue<Object> workQueue;

    /**
     * Creates a new instance of OutDialListener.
     * The listener
     * @param id Identity for the listener
     * @param workQueue Queue to put received events in.
     */
    public ReminderListener(String id, ManagedArrayBlockingQueue<Object> workQueue)
    {
        this.id = id;
        this.workQueue = workQueue;
    }

    /**
     * Identity for this listener.
     * All listeners must return an identity string the string
     * must be unique amongs all listeners of the same class.
     * This is used to manage listener sets.
     * @return Id for the listener.
     */
    public String getListenerId()
    {
        return id;
    }


    /**
     * Handles one event for Outdials.
     * If the event does not have any errors it is put in the workqueue.
     * @param delayer The delayer that delivers the notification
     * @param info The associated information
     * @param status Status for notification
     * @param event Extra info about the notification
     */
    public void handle(DelayHandler delayer,
                       DelayInfo info,
                       int status,
                       DelayEvent event)
    {
        if (status >= 0) {
            // Convert info -> OdlInfo
            // Check if event is the expected one??? And if not???
            // warn if call reply and we are not at final operation???
            // or go to next state anyway if call reply???
            // save reply code for call reply, use it when we are at final??
            ReminderInfo reminderInfo = new ReminderInfo(info);
            SDLogger.log(SDLogger.DEBUG,
                         "ReminderListener- Putting info to workqueue " + reminderInfo);
            try {
                workQueue.put(reminderInfo);
            } catch (Throwable t) {
                SDLogger.log(SDLogger.INFO, "ReminderListener-handle: queue full or state locked while handling reminder event, will retry");
            }
        } else {

            SDLogger.log(SDLogger.INFO,
                         "ReminderListener got error code " + status);
            if (status == HANDLE_STATUS_ERR_SCHEDULE) {
                // Assume we could not schedule to an existing outdial attempt
                SDLogger.log(SDLogger.INFO,
                             "ReminderListener-Rescheduling, " +
                             "assumes existing attempt is on");
                delayer.reschedule(null, info);
            } else if (status == HANDLE_STATUS_ERR_RESCHEDULE) {
                // Assume we could not reschedule to an newly removed item
                SDLogger.logObject(SDLogger.INFO,
                             "ReminderListener-Rescheduling, " +
                             "assumes reschedule for newly removed item", info);

            } else if (status == HANDLE_STATUS_ERR_NOTIFY ) {
                // assume we wanted to notify about an expired event.

                SDLogger.logObject(SDLogger.INFO,
                            "ReminderListener-Notifying, " +
                            "assumes notify for expired event", info );
            } else {
                SDLogger.logObject(SDLogger.ERROR,
                                   "ReminderListener - Remove reminder attempt due to errors ",
                                   info);
                delayer.cleanInfo(info.getKey(), info.getType());
            }

        }
    }

}
