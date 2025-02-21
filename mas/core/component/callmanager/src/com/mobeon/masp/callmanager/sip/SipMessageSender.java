/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip;

import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.sip.message.SipRequest;

import javax.sip.SipException;
import javax.sip.Dialog;
import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;

/**
 * Interface towards a SIP message sender.
 *
 * @author Malin Flodin
 */
public interface SipMessageSender {

    // Send requests

    /**
     * Sends the given out-of-dialog <param>request</param> by creating a new
     * {@link ClientTransaction} over which the <param>request</param> is sent.
     * @param   sipRequest
     * @return  The client transaction that was created to send the request.
     * @throws  SipException    if the <param>request</param> could not be sent.
     */
    public ClientTransaction sendRequest(SipRequest sipRequest)
            throws SipException;

    /**
     * Sends the given <param>request</param> within a <param>dialog</param>.
     * @param   sipRequest
     * @throws  SipException    if the <param>request</param> could not be sent.
     */
    public void sendRequestWithinDialog(Dialog dialog, SipRequest sipRequest)
            throws SipException;

    /**
     * Sends the <param>request</param> without creating {@link ClientTransaction}.
     * @param   sipRequest
     * @return  none
     * @throws  SipException If the <param>request</param> could not be sent.
     */
    public void sendRequestStatelessly(SipRequest sipRequest) throws SipException;
    
    // Send responses

    /**
     * Sends the given SIP response. If the server transaction in the response
     * is null, the response is sent statelessly by sending the response
     * directly to the SIP stack provider.
     * @param   sipResponse
     * @throws  SipException                if the response could not be sent.
     * @throws  InvalidArgumentException    if the response could not be sent
     *                                      due to invalid argument.
     */
    public void sendResponse(SipResponse sipResponse)
            throws SipException, InvalidArgumentException;

    /**
     * Sends the given provisional SIP response reliably.
     * 
     * @param   dialog          The dialog within which the response is sent.
     * @param   sipResponse     The SIP response must be created reliably.
     * @throws  SipException    SipException is thrown if the response could
     *                          not be sent.
     */
    public void sendReliableProvisionalResponse(
            Dialog dialog, SipResponse sipResponse)
            throws SipException;
}
