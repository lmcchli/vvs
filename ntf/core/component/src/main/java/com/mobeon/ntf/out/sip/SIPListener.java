package com.mobeon.ntf.out.sip;

import com.mobeon.common.storedelay.*;

/**
 * Main class for sip handling.
 * Receives timed events or notifications about an ongoing
 * sip attempt.
 * Checks what is to be done next with the sip depending
 * on the incoming event/notification.
 */
public class SIPListener implements DelayListener
{

    /** Identity for this listener. */
    private String id;

    /**
     * Creates a new instance of SipListener.
     * The listener
     * @param id Identity for the listener
     */
    public SIPListener(String id)
    {
        this.id = id;
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
     * Handles one event for SIP Notify.
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
/** DBHANDLER NOT USED ANYMORE IN MIO.
        if (status >= 0) {
            // Convert info -> SIPInfo
            // Check if event is the expected one??? And if not???
            // warn if call reply and we are not at final operation???
            // or go to next state anyway if call reply???
            // save reply code for call reply, use it when we are at final??
            SIPInfo sipInfo = new SIPInfo(info);
            if( status == DelayListener.HANDLE_STATUS_RESTART ) {
                // restart of NTF, set status to new unless done.
                if( sipInfo.getStatus() != SIPInfo.STATUS_DONE ) {
                    sipInfo.setStatus(SIPInfo.STATUS_NEW);
                }
            }
            if( sipInfo.getStatus() == SIPInfo.STATUS_SENDING ) {
                sipInfo.setStatus(SIPInfo.STATUS_NEW);
            }
            //sipInfo.setStatus(status);
            SDLogger.log(SDLogger.DEBUG,
                         "SIPListener-Putting info to workqueue " + sipInfo);
            workQueue.putObject(sipInfo);
        } else {

            SDLogger.log(SDLogger.INFO,
                         "SIPListener got error code " + status);
            if (status == HANDLE_STATUS_ERR_RESCHEDULE) {
                // Assume we could not schedule to an existing sipNotify attempt
                SDLogger.log(SDLogger.INFO,
                             "SIPListener-Rescheduling, " +
                             "assumes existing attempt is on");
                delayer.schedule(null, info);
            } else if (status == HANDLE_STATUS_ERR_NOTIFY ) {
                // assume we wanted to notify about an expired event.

                SDLogger.logObject(SDLogger.INFO,
                            "SIPListener-Notifying, " +
                            "assumes notify for expired event", info );
            } else {
                SDLogger.logObject(SDLogger.ERROR,
                                   "SIPListener - Remove sip notify attempt due to errors ",
                                   info);
                delayer.cleanInfo(info.getKey(), info.getType());
            }

        }
*/
    }

}
