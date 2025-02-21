/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.callmanager.events;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.util.NamedValue;

import java.util.Collection;

/**
 * An event generated when a SIP response have been received for a sent SIP request.
 * See {@link com.mobeon.masp.callmanager.CallManager#sendSipMessage}
 *
 * The event contains a collection of parameter name/values. The possible parameters
 * depend on which method used when sending the SipMessage. Currently only one method
 * is supported:
 *
 * For the {@link com.mobeon.masp.callmanager.CallManager.METHOD_MWI}
 * method the following parameters are supported:
 * <ul>
 *   <li>{@link RESPONSE_CODE} : A three digit SIP response code. (Mandatory)
 *   <li>{@link RESPONSE_TEXT} : A text describing the response code. (Optional)
 *   <li>{@link RETRY_AFTER}   : An optional retry-after time (in milliseconds)
 *    as an indication of the minimum time to wait before resending the request. (Optional)
 * </ul>
 * This class is immutable.
 *
 * @author Mats Hägg
 */
public class SipMessageResponseEvent implements Event {

    public static final String NAME = "SipMessageResponseEvent";
    public static final String RESPONSE_CODE = "responsecode";
    public static final String RESPONSE_TEXT = "responsetext";
    public static final String RETRY_AFTER = "retryafter";

    private final Collection<NamedValue<String,String>> params; // Parameter name/value pairs

    public SipMessageResponseEvent(Collection<NamedValue<String,String>> params) {
        this.params = params;
    }


    /**
     * Get the collection of parameter name/value pairs.
     * @return Collection of name value pairs
     */
    public Collection<NamedValue<String,String>> getParams() {
        return params;
    }


    public String toString() {

        StringBuffer result = new StringBuffer(NAME);
        boolean first = true;
        for (NamedValue<String,String> nv : params) {
            if (first) {
                first = false;
            } else {
                result.append(",");
            }
            result.append("<");
            result.append(nv.getName());
            result.append(" = ");
            result.append(nv.getValue());
            result.append(">");
        }

        return result.toString();

    }

}
