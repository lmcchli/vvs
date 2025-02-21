/*
 * EMail.java
 *
 */

package com.mobeon.sdp;

/**
 * An EMail represents an e= field contained within a SessionDescription.
 *
 *   Please refer to IETF RFC 2327 for a description of SDP. 
 *
 * @author  Malin Flodin
 */
public interface EMail extends Field {

    /** Returns the value.
     */
    public String getValue() throws SdpParseException;
    
    /** Set the value.
     */
    public void setValue(String value) throws SdpException;

}

