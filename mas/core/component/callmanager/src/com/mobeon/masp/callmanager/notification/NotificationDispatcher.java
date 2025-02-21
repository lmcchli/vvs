/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.callmanager.notification;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sip.events.SipEventImpl;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.sip.message.SipMessage;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of all ongoing NOTIFY transactions.
 *
 * @author Mats Hägg
 */
public class NotificationDispatcher {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final ConcurrentHashMap<String, OutboundNotification> ongoingNotifications =
            new ConcurrentHashMap<String, OutboundNotification>();

    public NotificationDispatcher() {
    }

    /**
     * @return amount of ongoing notifications.
     */
    public int amountOfOngoingNotifications() {
        return ongoingNotifications.size();
    }

    /**
     * Clear all ongoing notifications.
     */
    public void clearOngoingNotifications() {
        ongoingNotifications.clear();
    }


    /**
     * Inserts the given {@link com.mobeon.masp.callmanager.notification.OutboundNotification}
     * in the map of ongoing notification procedures. If the notification or sipRequest is null,
     * no insertion is made.
     * @param notification The {@link com.mobeon.masp.callmanager.notification.OutboundNotification}
     * to insert. MUST NOT be null. If null, an error log is generated and no insertion is made.
     * @param sipRequest is used to retrieve the transaction id for the ongoing
     * NOTIFY transaction. MUST NOT be null. If null, an error log is generated and no insertion is made.
     */
    public void addOngoingNotification(OutboundNotification notification,
                                                    SipRequest sipRequest) {

        if ((notification == null) || (sipRequest == null)) {
            log.error("Trying to insert an Outbound Notification but parameter is null. " +
                    "Notification: " + notification +
                    ", SipRequest: " + sipRequest);
        } else {
            String transactionId = getTransactionId(sipRequest);
            if (transactionId == null) {
                log.warn("transactionId is null, cannot insert ongoing notification. " +
                        "Notification: " + notification +
                        ", SipRequest: " + sipRequest);
                return;
            }
            if (log.isDebugEnabled())
                log.debug("Inserting an ongoing notification procedure with id=" +
                        transactionId);
            ongoingNotifications.put(transactionId, notification);
        }
    }

    /**
     * Removes the given {@link com.mobeon.masp.callmanager.notification.OutboundNotification}
     * from the map of ongoing notifications. If the notification or
     * sipRequest is null, no removal is done.
     * @param notification The {@link com.mobeon.masp.callmanager.notification.OutboundNotification}
     * to remove. MUST NOT be null. If null, an error log is generated
     * and no removal is done.
     * @param sipRequest is used to retrieve the transaction id for the
     * ongoing NOTIFY transaction. MUST NOT be null. If null, an error log
     * is generated and no removal is done.
     */
    public void removeOngoingNotification(OutboundNotification notification,
                                                       SipRequest sipRequest) {

        if ((notification == null) || (sipRequest == null)) {
            log.error("Trying to remove an Outbound Notification but parameter is null. " +
                    "Notification: " + notification +
                    ", SipRequest: " + sipRequest);

        } else {
            String transactionId = getTransactionId(sipRequest);
            if (transactionId == null) {
                log.warn("transactionId is null, cannot remove ongoing notification. " +
                        "Notification: " + notification +
                        ", SipRequest: " + sipRequest);
                return;
            }

            if (log.isDebugEnabled())
                log.debug("Removing the ongoing notification procedure with id=" +
                        transactionId);

            ongoingNotifications.remove(transactionId, notification);

        }
    }

    /**
     * The given {@link com.mobeon.masp.callmanager.sip.events.SipEvent} is mapped to the
     * {@link com.mobeon.masp.callmanager.notification.OutboundNotification}
     * responsible for this particular transaction.
     * @param sipEvent
     * @return The matching {@link com.mobeon.masp.callmanager.notification.OutboundNotification} for this
     * transaction. Null if none is found or if the sipEvent is null.
     */
    public OutboundNotification getNotification(SipEventImpl sipEvent) {

        OutboundNotification notification = null;

        if (sipEvent == null) {
            log.error("Trying to locate an ongoing notification procedure " +
                    "but the sipEvent is null.");
        } else {

            // Construct a transaction id and lookup a corresponding notification
            String transactionId = getTransactionId(sipEvent.getSipMessage());
            if (log.isDebugEnabled())
                log.debug("Looking for ongoing notification procedure with id=" +
                        transactionId);
            notification = ongoingNotifications.get(transactionId);
        }

        return notification;

    }


    /**
     * Calculates the client transaction id for this SIP message
     * The transaction ID looks like this:
     * <callid>:<from tag>
     *
     * @param request
     *
     * @return A transaction id created from the sip request callId and
     * From header tag.
     */
    private String getTransactionId(SipMessage request) {

        String transactionId;
        transactionId = request.getCallId() + ":" +
                    request.getFromHeaderTag();

        return transactionId.toLowerCase();
    }

}
