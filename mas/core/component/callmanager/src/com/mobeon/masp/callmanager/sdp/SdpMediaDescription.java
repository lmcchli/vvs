/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import com.mobeon.masp.callmanager.sdp.fields.SdpConnection;
import com.mobeon.masp.callmanager.sdp.fields.SdpMedia;
import com.mobeon.masp.callmanager.sdp.fields.SdpBandwidth;
import com.mobeon.masp.callmanager.sdp.attributes.SdpAttributes;

import com.mobeon.sdp.MediaDescription;
import com.mobeon.sdp.SdpException;
import com.mobeon.sdp.SdpFactory;

import jakarta.activation.MimeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Interface towards {@link SdpMediaDescriptionImpl}.
 * Only used to simplify basic test.
 *
 * @author Malin Nyfeldt
 */
public interface SdpMediaDescription {

    /**
     * Creates and returns a copy of this media description, with the exception
     * that the copy should be marked unused (i.e. should not be used in any
     * media setup). A media description is marked unused by setting the media
     * port to zero (see RFC 3264).
     * <p>
     * The <param>sessionDescription</param> to which the new, copied media
     * description belongs is also given. This session description is used to
     * retrieve the session level values of the connection field and attributes.
     *
     * @param   sessionDescription
     * @return  A new media description that shall not be used in media setup.
     * @throws  NullPointerException    if any parameter is null.
     */
    public SdpMediaDescription createUnusedMediaDescriptionCopy(
            SdpSessionDescription sessionDescription) throws NullPointerException;

    /**
     * Returns the specific attributes for the Media Description.
     * @return the attributes for the Media Description.
     */
    public SdpAttributes getMediaAttributes();

    /**
     * Returns the attributes for the Media Description.
     * <p>
     * The returned attributes contains a merge of the attributes specified
     * on session level in the Session Description and the overriding attributes
     * specified in the Media Description itself.
     * <p>
     * If no attributes are set, an empty {@link SdpAttributes} instance is
     * returned. Null is never returned.
     *
     * @return the attributes for the Media Description.
     */
    public SdpAttributes getAttributes();

    /**
     * Returns the bandwidth value for the Media Description for a given
     * bandwidth type.
     * <p>
     * If no bandwidth field exists for the Media Description for the given
     * bandwidth type, the session level bandwidth fields from the Session
     * Description are searched through as well.
     *
     * @return  The bandwidth value for a given bandwidth type is returned.
     *          If no bandwith value is found, null is returned.
     */
    public SdpBandwidth getBandwidth(String type);

    /**
     * Returns all bandwidth values for the Media Description.
     * <p>
     * If no bandwidth field exists for the Media Description,
     * an empty hash map is returned.
     *
     * @return  The bandwidth values for the Media Description.
     */
    public HashMap<String, SdpBandwidth> getBandwidths();

    /**
     * Returns the connection field for the Media Description.
     * <p>
     * If no connection field exists for the Media Description, null is returned.
     * Connection field from the Session Description is not considered.
     * 
     * @return the connection field for the Media Description.
     */
    public SdpConnection getMediaConnection();

    /**
     * Returns the connection field for the Media Description.
     * <p>
     * If no connection field exists for the Media Description, the session
     * level connection field from the Session Description is returned instead.
     *
     * @return the connection field for the Media Description.
     */
    public SdpConnection getConnection();

    /**
     * @return the media field for the Media Description. Since a media
     * description must have a media field, null is never returned.
     */
    public SdpMedia getMedia();

    /**
     * Encodes the media description into an SDP stack format using the
     * <param>sdpFactory</param>.
     * A {@link MediaDescription} containing the media field, the connection
     * field and the attributes is returned.
     *
     * @param   sdpFactory
     * @return  A media description in a stack format.
     * @throws  SdpException if the stack format could not be created.
     */
    public MediaDescription encodeToStackFormat(SdpFactory sdpFactory)
            throws SdpException;

    /**
     * @param   mimeTypes       A collection of valid content types,
     *                          e.g. "audio/pcmu".
     * @return  Returns true if all given <param>mimeTypes</param> are supported
     *          by this Media Description. Otherwise, false is returned.
     *          The comparison is based on both type of encoding and on
     *          bandwidth support.
     */
    public boolean areEncodingsSupported(Collection<MimeType> mimeTypes);

    /**
     * @param   mimeType        A valid content type, e.g. "audio/pcmu".
     * @return  Returns the list rtp payload type of the matching encoding.
     */
    public ArrayList<Integer> getSupportedRtpPayload(MimeType mimeType);

    /**
     * Verifies if the media description has a connection-address on hold (zeroed).
     * @return boolean
     */
    public boolean isSdpMediaDescriptionOnHold();

    /**
     * Compare if SdpMediaDescription are the same (for limited criteria)  
     * @param sdpMediaDescription SdpMediaDescription
     * @param excludeTransmissionMode true if transmissionMode must be exclude out of the comparison, false otherwise
     * return true if both objects are equal, false otherwise
     */
    public boolean compareWith(SdpMediaDescription sdpMediaDescription, boolean excludeTransmissionMode);
}
