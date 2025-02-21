/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.common.xmp.client;

import java.util.*;

/**
 * Small class that holds a parsed XMP result.
 */
public class XmpResult {
    private int transactionId;
    private int statusCode;
    private String statusText;
    private Properties properties;
    /** List of XmpAttachments */
    private ArrayList attachments;
    // Not used by current XMP services private String[] parameters;

     public XmpResult(int transactionId, int statusCode, String statusText, Properties props) {
         this(transactionId, statusCode, statusText, props, null);
     }
    
    /**
     * Constructor.
     * @param transactionId - the identity of the transaction.
     * @param statusCode - the code for the result status.
     * @param statusText - the text describing the result status.
     * @param props parameter name and values from the xml-result.
     * @param attachments - XmpAttachmenst.
     */
    public XmpResult(int transactionId, int statusCode, String statusText, Properties props, ArrayList attachments) {
        this.transactionId = transactionId;
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.properties = props;
        this.attachments = attachments;
    }
    
   

    /**
     *@return the id of the transaction.
     */
    public int getTransactionId() {
        return transactionId;
    }

    /**
     *@return the status code of the result.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     *@return the message explaining the result.
     */
    public String getStatusText() {
        return statusText;
    }

    /**
     *@return properties with parameter name and values.
     */
    public Properties getProperties() {
        return properties;
    }
    
    /**
     *@return ArrayList with xmpAttachments.
     */
    public ArrayList getAttachments() {
        return attachments;
    }
    
    /**
     *@return a printable representation of the result.
     */
    public String toString() {
        return "{XmpResult id=" + transactionId + " code="
            + statusCode + " text=" + statusText + "}";
    }
}
