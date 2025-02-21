/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.xmp.server;

import java.util.logging.*;
import java.util.*;

/**
 * Remembers a few details about a pending XMP transaction, to let the
 * XmpHandler take care of the validity checks.
 */
public class XmpTransaction {
    /** When this transactions validity period ends. */
    private Date expiry;
    /** The XMP identity of this transaction. Unique per client, i.e. per
        connection */
    private Integer transactionId;
    /** The ServiceHandler that originated this transaction */
    private ServiceHandler sh;

    /**
     * Constructor.
     *@param validity - the number of seconds this transaction has to live.
     *@param transactionId - transaction id.
     *@param sh - the service handler this request was sent to.
     */
    public XmpTransaction(int validity, Integer transactionId, ServiceHandler sh) {
        expiry = new Date(System.currentTimeMillis() + 1000 * validity);
        this.transactionId = transactionId;
        this.sh = sh;
    }

    /**
     * Checks if this transaction has expired.
     *@param now - the current time.
     *@return true iff this transaction has expired.
     */
    public boolean isExpired(Date now) {
        return now.after(expiry);
    }

    /**
     * Gets this transactions identity.
     *@return the transaction id.
     */
    public Integer getTransactionId() {
        return transactionId;
    }

    /**
     * gets this transactions service handler.
     *@return the service handler.
     */
    public ServiceHandler getServiceHandler() {
        return sh;
    }
}
