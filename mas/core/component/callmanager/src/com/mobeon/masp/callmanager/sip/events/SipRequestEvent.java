/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.events;

import com.mobeon.masp.callmanager.sip.message.SipMessage;

import javax.sip.RequestEvent;
import javax.sip.Transaction;
import javax.sip.ServerTransaction;
import javax.sip.Dialog;
import javax.sip.SipProvider;
import javax.sip.message.Request;
import java.util.Date;

/**
 * The SipRequestEvent is used to carry a received SIP request.
 * <p>
 * This is an interface implemented by {@link SipRequestEventImpl}.
 *
 * @author Malin Nyfeldt
 */
public interface SipRequestEvent extends SipEvent {

    public void generateToTag();

    public Date getCreationDate();
    public Dialog getDialog();
    public String getMethod();
    public Request getRequest();
    public RequestEvent getRequestEvent();
    public void exitCheckPoint();
    public void enterCheckPoint(String checkPointId);

    /**
     * Returns the SIP message contained in the request event.
     * Null is NEVER returned.
     * @return The SIP message that initiated the request event.
     */
    public SipMessage getSipMessage();

    public SipProvider getSipProvider();
    public String getToTag();

    // TODO: Are both necessary?
    public Transaction getTransaction();
    public ServerTransaction getServerTransaction();

    // TODO: Can this handling be improved?
    public boolean isRequestInitialInvite();
    public void setInitialInvite();

    /**
     * @return true if the request was validated ok and false otherwise.
     */
    public boolean validateGeneralPartOfRequest();


    // TODO: Needed in interface?
//    public String toString();

    /**
     * Constructs an early dialog ID for the request event.
     * <p>
     * The dialog ID depends upon the events initial transaction.
     * The dialog ID is based on the CallID, To and From tags.
     * <p>
     * If the initial transaction is a server transaction
     * (i.e. the request event is received for an inbound call), the dialogID
     * will be <callid>:<from tag>
     * <br>
     * If the initial transaction is a client transaction
     * (i.e. the request event is received for an outbound call), the dialogID
     * will be <callid>:<to tag>
     *
     * @return A dialog id
     */
    public String getEarlyDialogId();

    /**
     * Constructs an established dialog ID for the request event.
     * <p>
     * The dialog ID is based on the CallID, To and From tags.
     * The dialogID will be:
     * <callid>:<to tag>:<from tag>
     * <p>
     * If the to tag does not exist in the request, the to tag is retrieved
     * from the sip event itself (if set).
     *
     * @return A dialog id
     */
    public String getEstablishedDialogId();

}
