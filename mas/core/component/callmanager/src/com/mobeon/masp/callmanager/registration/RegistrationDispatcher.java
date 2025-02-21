/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.registration;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.sip.message.SipMessage;
import com.mobeon.masp.callmanager.sip.events.SipEvent;
import com.mobeon.masp.callmanager.sip.events.SipEventImpl;

import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO: Phase 2! Document!
 *
 * @author Malin Flodin
 */
public class RegistrationDispatcher {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final ConcurrentHashMap<String, SspInstance> ongoingRegistrations =
            new ConcurrentHashMap<String, SspInstance>();

    public RegistrationDispatcher() {
    }

    /**
     * @return amount of ongoing registrations.
     */
    public int amountOfOngoingRegistrations() {
        return ongoingRegistrations.size();
    }

    /**
     * Clear all ongoing registrations.
     */
    public void clearOngoingRegistrations() {
        ongoingRegistrations.clear();
    }

    /**
     * The given {@link SipEvent} is mapped to the {@link SspInstance}
     * responsible for this particular REGISTER transaction.
     * @param sipEvent
     * @return The matching {@link SspInstance} responsible for the REGISTRATION
     * transaction. Null if none is found or if the sipEvent is null.
     */
    public synchronized SspInstance getSspInstance(SipEventImpl sipEvent) {

        SspInstance sspInstance = null;

        if (sipEvent == null) {
            log.error("Trying to locate an ongoing registration procedure " +
                    "but the sipRequest is null.");
        } else {

            // Construct a transaction id and lookup a corresponding sspInstance.
            SipMessage sipMessage = sipEvent.getSipMessage();
            String transactionId = getTransactionId(sipMessage);
            if (log.isDebugEnabled())
                log.debug("Looking for ongoing registration procedure with id=" +
                        transactionId);
            sspInstance = ongoingRegistrations.get(transactionId);
        }

        return sspInstance;
    }

    /**
     * Inserts the given {@link SspInstance} in the map of ongoing registration
     * procedured. If the sspInstance or sipEvent is null, no insertion is made.
     * @param sspInstance The {@link SspInstance} to insert. MUST NOT be null.
     * If null, an error log is generated and no insertion is made.
     * @param sipRequest is used to retrieve the transaction id for the ongoing
     * REGISTER transaction. MUST NOT be null.
     * If null, an error log is generated and no insertion is made.
     */
    public synchronized void addOngoingRegistration(SspInstance sspInstance,
                                                    SipRequest sipRequest) {

        if ((sspInstance == null) || (sipRequest == null)) {
            log.error("Trying to insert an SSP instance but parameter is null. " +
                    "SSP instance: " + sspInstance +
                    ", SipRequest: " + sipRequest);
        } else {
            String transactionId = getTransactionId(sipRequest);
            if (log.isDebugEnabled())
                log.debug("Inserting an ongoing registration procedure with id=" +
                        transactionId);
            ongoingRegistrations.put(transactionId, sspInstance);
        }
    }

    /**
     * Removes the given {@link SspInstance} from the map of ongoing
     * registrations. If the sspInstance or sipEvent is null, no removal is done.
     * @param sspInstance The {@link SspInstance} to remove. MUST NOT be null.
     * If null, an error log is generated and no removal is done.
     * @param sipRequest is used to retrieve the transaction id for the ongoing
     * REGISTER transaction. MUST NOT be null.
     * If null, an error log is generated and no removal is done.
     */
    public synchronized void removeOngoingRegistration(SspInstance sspInstance,
                                                       SipRequest sipRequest) {

        if ((sspInstance == null) || (sipRequest == null)) {
            log.error("Trying to remove an SSP instance but parameter is null. " +
                    "SSP istance: " + sspInstance +
                    ", SipRequest: " + sipRequest);

        } else {
            String transactionId = getTransactionId(sipRequest);
            if (log.isDebugEnabled())
                log.debug("Removing the ongoing registration procedure with id=" +
                        transactionId);
            ongoingRegistrations.remove(transactionId, sspInstance);
        }
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
