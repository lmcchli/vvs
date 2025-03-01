/*
 * URI.java
 *
 */

package com.mobeon.sdp;

import java.net.*;

/**
 * An URI represents the u= field within a SessionDescription.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * @author Malin Flodin
 */
public interface URI extends Field {

    /** Returns the value.
     * @throws SdpParseException
     * @return the value
     */    
    public URL get()
        throws SdpParseException;
    
    /** Sets the value.
     * @param value the new information
     * @throws SdpException if the parameter is null
     */    
    public void set(URL value)
         throws SdpException;
    
}

