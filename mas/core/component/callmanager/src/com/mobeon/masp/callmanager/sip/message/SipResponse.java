/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.message;

import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ExtensionHeader;
import javax.sip.message.Response;

/**
 * A container for a SIP response that is created by the Call Manager and
 * shall be sent to peer. This SIP response is created by the
 * SipResponseFactoryImpl.
 *
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class SipResponse extends SipMessageImpl {
    private final Transaction transaction;
    private final SipProvider sipProvider;
    private final Response response;

    public SipResponse(Response response, Transaction transaction,
                       SipProvider sipProvider) {
        super(response);
        // TODO: Throw exception if sipprovider is null!
        this.response = response;
        this.transaction = transaction;
        this.sipProvider = sipProvider;
    }

    public ServerTransaction getServerTransaction() {
        ServerTransaction serverTransaction = null;
        if (transaction instanceof ServerTransaction)
            serverTransaction = (ServerTransaction)transaction;
        return serverTransaction;
    }

    public SipProvider getSipProvider() {
        return sipProvider;
    }

    public Response getResponse() {
        return response;
    }

    public String getMethod() {
        String method = "";
        // Retrieve the method from the CSeq header of the response.
        CSeqHeader cseqheader = (CSeqHeader)response.getHeader(CSeqHeader.NAME);
        if (cseqheader != null)
            method = cseqheader.getMethod();
        return method;
    }

    /**
     * Add an extension header.<br/>
     * 
     * <b>You should use {@link SipMessage} interface methods to add specific headers instead. </b>
     * <br/><br/>
     * 
     * This is added in order to be able to add proprietary headers to SIP 183 Session Progress
     * response for early media from call flow.
     * 
     * @param header {@link ExtensionHeader}
     */
    public void addExtensionHeader(ExtensionHeader header) {
        message.addHeader(header);
    }
    
    public String toString() {
        return "SIP " + getResponse().getStatusCode() + " response";
    }
}
