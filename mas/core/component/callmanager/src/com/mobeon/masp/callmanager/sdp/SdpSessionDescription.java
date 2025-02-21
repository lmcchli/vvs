/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import com.mobeon.masp.callmanager.sdp.attributes.SdpAttributes;
import com.mobeon.masp.callmanager.sdp.fields.SdpConnection;
import com.mobeon.masp.callmanager.sdp.fields.SdpMedia;
import com.mobeon.masp.callmanager.sdp.fields.SdpOrigin;
import com.mobeon.masp.callmanager.sdp.fields.SdpBandwidth;
import com.mobeon.sdp.SdpFactory;

import java.util.List;
import java.util.HashMap;

/**
 * Interface towards {@link SdpSessionDescriptionImpl}.
 * Its sole purpose is to simplify basic test.
 *
 * @author Malin Nyfeldt
 */
public interface SdpSessionDescription {

    /**
     * Returns the attributes for the Session Description.
     * <p>
     * If no attributes are set, an empty {@link SdpAttributes} instance is
     * returned. Null is never returned.
     *
     * @return the attributes for the Session Description.
     */
    public SdpAttributes getAttributes();

    /**
     * Returns the bandwidth value for the Session Description for a given
     * bandwidth type.
     *
     * @return  The bandwidth value for a given bandwidth type is returned.
     */
    public SdpBandwidth getBandwidth(String type);

    /**
     * Returns all bandwidth values for the Session Description.
     * <p>
     * If no bandwidth field exists for the Session Description,
     * an empty hash map is returned.
     *
     * @return  The bandwidth values for the Session Description.
     */
    public HashMap<String, SdpBandwidth> getBandwidths();

    /**
     * @return Returns the connection field for the Session Description.
     */
    public SdpConnection getConnection();

    /**
     * @return Returns the origin field for the Session Description.
     */
    public SdpOrigin getOrigin();

    /**
     * @return  whether the session description contains a media description of
     *          type "video" or not. True is returned if a media description of
     *          type "video" exists in the session description. False is
     *          returned otherwise.
     */
    public boolean containsVideo();

    /**
     * Returns the {@link SdpMediaDescription} for a given {@link SdpMedia} 
     * @param sdpMedia {@link SdpMedia}
     * @return SdpMediaDescription, null if not found
     */
    public SdpMediaDescription getMedia(SdpMedia sdpMedia);

    /**
     * Returns the media description at the specified <param>index</param>.
     * IndexOutOfBoundsException is thrown if there is no media description
     * with the given index.
     * @param index
     * @return the media description at the specified <param>index</param>.
     * @throws IndexOutOfBoundsException if index is out-of-bounds.
     */
    public SdpMediaDescription getMediaDescription(int index)
            throws IndexOutOfBoundsException;

    /**
     * @return a list of all media descriptions in this session description.
     */
    public List<SdpMediaDescription> getMediaDescriptions();

    /**
     * @return toString2 will return the SDP fields and attributes except SDP Origin Field.
     */
    public String toString2();

    /**
     * Compare if SdpSessionDescription are the same (for limited criteria)  
     * @param sdpSessionDescription SdpSessionDescription 
     * @param skipTransmissionMode true if transmission mode must be skipped, false otherwise 
     * @return true if both  
     */
    public boolean compareWith(SdpSessionDescription sdpSessionDescription, boolean skipTransmissionMode);

    /**
     * Encodes the session description into an SDP using the
     * <param>sdpFactory</param>.
     * A textual SDP representation of the session description is returned.
     *
     * @param   sdpFactory
     * @return  The textual SDP representation.
     * @throws  SdpInternalErrorException if the SDP could not be created.
     */
    public String encodeToSdp(SdpFactory sdpFactory)
            throws SdpInternalErrorException;

    /**
     * Verifies if all the media descriptions in the session description
     * have their connection-address on hold (zeroed).   
     * @return boolean
     */
    public boolean isSdpMediaDescriptionOnHold();

}
