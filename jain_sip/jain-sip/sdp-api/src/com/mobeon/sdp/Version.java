/*
 * Version.java
 *
 */

package com.mobeon.sdp;

/** A Version field represents the v= fields contained within the SessionDescription.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * @author Malin Flodin
 */
public interface Version extends Field{

    
    /** Returns the version number.
     * @throws SdpParseException
     * @return int
     */    
    public int getVersion()
               throws SdpParseException;
    
    /** Sets the version.
     * @param value the - new version value.
     * @throws SdpException if the value is <=0
     */    
    public void setVersion(int value)
                throws SdpException;
}

