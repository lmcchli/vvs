/*
 * Info.java
 *
 */

package com.mobeon.sdp;

/**
 * An Info represents the i= fields contained within either a MediaDescription
 * or a SessionDescription. 
 *
 *  Please refer to IETF RFC 2327 for a description of SDP. 
 *
 * @author  Malin Flodin
 */
public interface Info extends Field {

    /** Returns the value.
     */
    public String getValue() throws SdpParseException;
    
    /** Set the value.
     */
    public void setValue(String value) throws SdpException;

}


