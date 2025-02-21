/**
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.common.xmp.client;

import java.util.Vector;
import java.util.Date;
import com.mobeon.common.xmp.XmpAttachment;

public class XmpRequestInfo {

    public int id;
    public Date expiryTime;
    public XmpResultHandler resultHandler;
    public XmpUnit unit;
    public String request;
    public String service;
    public XmpAttachment[] attachments;

    private Vector triedXmpUnits;

    private static XmpClient client;

    static {
        client = XmpClient.get();
    }
    /**
     * Constructor.
     *@param id the transaction id.
     *@param resultHandler where to send the result of the transaction.
     *@param unit the XMP unit where the request shall be sent.
     *@param request the XML request string.
     *@param service the XMP service requested.
     *@param attachments any attachment "files" to the XMP request.
     */
    public XmpRequestInfo(int id, XmpResultHandler resultHandler, XmpUnit unit, String request, String service, XmpAttachment[] attachments ) {
        this.id = id;
        this.resultHandler = resultHandler;
        this.unit = unit;
        this.request = request;
        this.service = service;
        this.attachments = attachments;
        this.triedXmpUnits = new Vector();
        expiryTime = new Date(new Date().getTime() + ((client.getValidity()+5) * 1000));
    }

    public void renewExpiryTime() {
        expiryTime = new Date(new Date().getTime() + ((client.getValidity()+5) * 1000));
    }

    /**
     *@return a printable representation of this transaction.
     */
    public String toString() {
        return "{XmpRequestInfo id=" + id + " handler=" + resultHandler + " triedUnits"+ "}";
    }

    /**
     * Set this unit as tried. The list should not contain duplicate units.
     * Used to make sure the "handleNext unit" does not try the same twice.
     *
     * @param xmpUnit that was tried, but gave a result for "try the next"
     */
    public void setXmpUnitTried(String xmpUnit) {
        if (!triedXmpUnits.contains(xmpUnit)){
            triedXmpUnits.add(xmpUnit);
        }
    }

    /**
     * Has this request tried this xmpUnit 
     * @param xmpUnit have this unit been tried before?
     * @return true if already in the list of tried units.
     */
    public boolean isXmpUnitTried(String xmpUnit) {
        return triedXmpUnits.contains(xmpUnit);
    }
    
    /**
     * Get the count of tries for this request.
     * @return number of units this request has tried.
     */
    public int getTriedUnitsCount() {
        return triedXmpUnits.size();
    }

}
