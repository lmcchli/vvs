/**
 * Copyright (c) 2009 Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf;

import com.abcxyz.services.moip.ntf.event.NtfEvent;

/****************************************************************
 * Interface for classes interested in knowing when the processing of
 * a notification is completed
 */
public interface NtfCompletedListener {

    /****************************************************************
     * notifCompleted is called to notify the listener that a notification is
     * completed and that all about it can be forgotten.
     * @param NtfEvent identifies the notification
     * returned to the listener with this call. Intended for routing
     * among internal components in the receiver
     */
    void notifCompleted(NtfEvent event);

    /****************************************************************
     * Called to notify the listener that a notification is
     * partly completed, but should be retried for some receivers.
     * @param NtfEvent identifies the notification
     * returned to the listener with this call. Intended for routing
     * among internal components in the receiver
     */
    void notifRetry(NtfEvent event);

    /****************************************************************
     * Called to notify the listener that a notification failed permanently
     * and no more retry should be performed.
     * @param NtfEvent identifies the notification
     * returned to the listener with this call. Intended for routing
     * among internal components in the receiver
     */
    void notifFailed(NtfEvent event);
}
