/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip;

import java.util.Set;

import javax.sip.ClientTransaction;
import javax.sip.SipProvider;
import javax.sip.TransactionUnavailableException;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;

/**
 * Interface towards the SIP stack wrapper.
 * <p>
 * Creating a new tagm call ID or client transaction is done using this interface.
 *
 * @author Malin Flodin
 */
public interface SipStackWrapper {
    public String generateTag();    

    public String getHost();
    public String getHostAddress();
    public int getPort();

    public SipProvider getSipProvider();

    public CallIdHeader getNewCallId();
    public ClientTransaction getNewClientTransaction(Request request)
            throws TransactionUnavailableException;

    public String auditStack(Set<String> activeCallIDs, 
    		long leakedDialogTimer, long leakedTransactionTimer);

}
