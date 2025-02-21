/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule;


/**
 * The ANotifierEventHandler abstract class defines the methods that the NTF Notifier scheduler mechanism will invoke 
 * to allow the Notifier plug-in to handle scheduled events that have fired.
 * <p>
 * The Notifier plug-in concrete class that implements this abstract class should handle the event fired.
 */
public abstract class ANotifierEventHandler {

    /**
     * The response used by the Notifier plug-in to acknowledge it has received the event fired signal.  
     * This response does not cause the next scheduled retry for this event to be cancelled.
     */
    public static final int NOTIFIER_EVENT_HANDLE_RESULT_OK = 1;
    
    /**
     * The response used by the Notifier plug-in to cancel the next scheduled retry for this event.
     */
    public static final int NOTIFIER_EVENT_HANDLE_RESULT_STOP_RETRIES = 3;

    
    /**
     * Handles the event fired.  This is the callback method when an event is fired by the Notifier scheduler.
     * @param notifierEventInfo INotifierEventInfo object containing the information about the fired event
     * @return NOTIFIER_EVENT_HANDLE_RESULT_OK to acknowledge the firing of the event, or
     *         NOTIFIER_EVENT_HANDLE_RESULT_STOP_RETRIES to cancel the next scheduled retry for the fired event
     */
    public int eventFired(INotifierEventInfo notifierEventInfo) {
        return NOTIFIER_EVENT_HANDLE_RESULT_OK;
    }

}
