/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.sdp.fields.SdpMediaType;
import com.mobeon.masp.callmanager.sdp.fields.SdpMediaTransport;
import com.mobeon.masp.callmanager.sdp.attributes.SdpTransmissionMode;
import com.mobeon.masp.callmanager.CallMediaTypes.CallMediaType;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import jakarta.activation.MimeType;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;

/**
 * This class is responsible for comparing an SDP offer and an SDP answer to
 * find an SDP intersection between them.
 * <p>
 * This is a singleton.
 *
 * @author Malin Flodin
 */
public class SdpMediaComparison {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private static SdpMediaComparison oneInstance = new SdpMediaComparison();

    /**
     * Return the singleton instance of SdpMediaComparison
     * @return the singleton object instance
     */
    public static SdpMediaComparison getInstance() {
        return oneInstance;
    }

    private SdpMediaComparison() {
    }

    /**
     * Tries to find an intersection between a parsed remote SDP
     * (<param>sdpSessionDescription</param>) and the requirements on media from
     * Call Manager itself (<param>mandatoryAudioMediaTypes</param> and
     * <param>mandatoryVideoMediaTypes</param> and call specific requirements
     * <param>callSpecificMediaTypes</param>. If no call specific requirements
     * exists, the <param>callSpecificMediaTypes</param> shall be set to null.
     * <p>
     * The <param>defaultPTime</param> shall be used for the intersection
     * connection if no ptime is given in the remote SDP for an audio
     * media description.
     *
     * @param   sdpSessionDescription       If null, null is returned.
     * @param   mandatoryAudioMediaTypes    If null, null is returned.
     * @param   mandatoryVideoMediaTypes    If null, null is returned.
     * @param   callSpecificMediaTypes      If null, no call specific
     *                                      requirements exists.
     * @return  An SDP intersection. Null is returned if no intersection is
     *          found or could not be retrieved due to a parameter being null.
     */
    public SdpIntersection getSdpIntersection(
            SdpSessionDescription sdpSessionDescription,
            Collection<MimeType> mandatoryAudioMediaTypes,
            Collection<MimeType> mandatoryVideoMediaTypes,
            CallMediaTypes[] callSpecificMediaTypes)  throws SdpInternalErrorException
    {
        if (log.isDebugEnabled())
            log.debug("Trying to find an SDP match. <Session Description=" +
                    sdpSessionDescription + ">, <Mandatory Audio Media Types=" +
                    mandatoryAudioMediaTypes + ">, <Mandatory Video Media Types=" +
                    mandatoryVideoMediaTypes + ">, <Call Specific Media Types=" +
                    Arrays.toString(callSpecificMediaTypes) + ">");

        // Check for null parameters
        if ((sdpSessionDescription == null) ||
                (mandatoryAudioMediaTypes == null) ||
                (mandatoryVideoMediaTypes == null)) {
            if (log.isInfoEnabled()) log.info("No SDP intersection found since input data was not " +
                                              "given correctly. <Remote SDP = " + sdpSessionDescription +
                                              ">, <Mandatory Audio Media Types = " + mandatoryAudioMediaTypes +
                                              ">, <Mandatory Video Media Types = " + mandatoryVideoMediaTypes + ">");
            return null;
        }

        List<SdpMediaDescription> mdList = sdpSessionDescription.getMediaDescriptions();

        // First check mandatory media types
        List<Integer> indexesOfMatchingAudioMedia = new ArrayList<Integer>();
        List<Integer> indexesOfMatchingVideoMedia = new ArrayList<Integer>();
        findMediaDescriptionsMatchingMandatoryMediaTypes(
                mdList,
                mandatoryAudioMediaTypes,
                mandatoryVideoMediaTypes,
                indexesOfMatchingAudioMedia, indexesOfMatchingVideoMedia);

        // Check if support for mandatory types was found
        if (indexesOfMatchingAudioMedia.isEmpty()) {

            if (log.isInfoEnabled()) log.info("The mandatory audio mime types <" +
                                              mandatoryAudioMediaTypes +
                                              "> are not supported by remote SDP media descriptions <" +
                                              mdList + ">.");
            return null;

        } else if (sdpSessionDescription.containsVideo() &&
                (indexesOfMatchingVideoMedia.isEmpty())) {

            if (log.isInfoEnabled()) log.info("Remote SDP indicated video call but the mandatory video " +
                                              "mime types <" + mandatoryVideoMediaTypes +
                                              "> are not supported by remote SDP media descriptions <" +
                                              mdList + ">.");
        }

        SdpIntersection sdpIntersection;

        // Check if support for call specific media types is found
        if (callSpecificMediaTypes == null) {
            // No call specific media types exists so intersection already found

            // Create SDP intersection
            Integer indexOfMatchingAudio = indexesOfMatchingAudioMedia.get(0);
            Integer indexOfMatchingVideo = null;

            if (!indexesOfMatchingVideoMedia.isEmpty()) {
                indexOfMatchingVideo = indexesOfMatchingVideoMedia.get(0);
            }

            sdpIntersection = new SdpIntersection(
                    sdpSessionDescription,
                    indexOfMatchingAudio,
                    indexOfMatchingVideo,
                    null,
                    null,
                    mandatoryAudioMediaTypes,
                    mandatoryVideoMediaTypes);

        } else {
            // Call specific media types exists so we have to find media
            // descriptions that support them as well.
            sdpIntersection = getIntersectionForCallSpecificMediaRequirements(
                    callSpecificMediaTypes,
                    indexesOfMatchingAudioMedia, indexesOfMatchingVideoMedia,
                    sdpSessionDescription,
                    mandatoryAudioMediaTypes, mandatoryVideoMediaTypes);
        }

        return sdpIntersection;
    }

    // ====================== Private methods ==========================

    /**
     * Finds all media descriptions from the <param>mdList</param> that matches
     * the <param>mandatoryAudioMediaTypes</param> and
     * <param>mandatoryVideoMediaTypes</param>.
     * <p>
     * The indexes of the matching media descriptions is returned in the
     * <param>indexesOfMatchingAudioMedia</param> and
     * <param>indexesOfMatchingVideoMedia</param>.
     *
     * @param mdList
     * @param mandatoryAudioMediaTypes
     * @param mandatoryVideoMediaTypes
     * @param indexesOfMatchingAudioMedia
     * @param indexesOfMatchingVideoMedia
     */
    private void findMediaDescriptionsMatchingMandatoryMediaTypes(
            List<SdpMediaDescription> mdList,
            Collection<MimeType> mandatoryAudioMediaTypes,
            Collection<MimeType> mandatoryVideoMediaTypes,
            List<Integer> indexesOfMatchingAudioMedia,
            List<Integer> indexesOfMatchingVideoMedia) {

        boolean unicastEnabled = ConfigurationReader.getInstance().getConfig().isUnicastEnabled();

        for (int i = 0; i < mdList.size(); i++) {
            SdpMediaDescription sdpMediaDescription = mdList.get(i);

            if (log.isDebugEnabled())
                log.debug("Searching for supported mandatory media types " +
                        "in Media Description: " + sdpMediaDescription);

            // A media level or session level connection must exist,
            // otherwise ignore this media description.
            if (sdpMediaDescription.getConnection() == null) {
                if (log.isDebugEnabled())
                    log.debug("No connection found for media description. " +
                            "The media description will not be used.");
                continue;
            }

            /**
             * In a default scenario, only transmission mode 'sendrecv' (which is default if not set) is supported.
             * For other transmission modes, ignore this media description.
             *
             * In the scenario of unicast feature enabled, the transmission modes supported are 'sendrecv' and 'inactive'. 
             */
            SdpTransmissionMode mode = sdpMediaDescription.getAttributes().getTransmissionMode();
            if (mode != null) {
                if (unicastEnabled) {
                    if (SdpTransmissionMode.SENDRECV == mode || SdpTransmissionMode.INACTIVE == mode) {
                        if (log.isDebugEnabled())
                            log.debug("Transmission mode is " + mode + " supported (unicast enabled)");
                    } else {
                        if (log.isDebugEnabled())
                            log.debug("Transmission mode is " + mode + " but only sendrecv and inactive are supported (unicast enabled). The media description will not be used.");
                        continue;
                    }
                } else {
                    if (SdpTransmissionMode.SENDRECV != mode) {
                        if (log.isDebugEnabled())
                            log.debug("Transmission mode is " + mode + " but only sendrecv is supported. The media description will not be used.");
                        continue;
                    }
                }
            }

            SdpMediaType mediaType = sdpMediaDescription.getMedia().getType();
            SdpMediaTransport mediaTransport = sdpMediaDescription.getMedia().getTransport();

            if (mediaType == SdpMediaType.AUDIO) {
                // Audio must have RTP/AVP profile
                if (mediaTransport.equals(SdpMediaTransport.RTP_AVP)) {
                    if (sdpMediaDescription.
                            areEncodingsSupported(mandatoryAudioMediaTypes)) {
                        indexesOfMatchingAudioMedia.add(i);
                        if (log.isDebugEnabled())
                            log.debug("The remote SDP media description <" +
                                    sdpMediaDescription +
                                    "> supports the mandatory audio media types <" +
                                    mandatoryAudioMediaTypes + "> and the audio media transport type <" +
                                    SdpMediaTransport.RTP_AVP + ">.");
                    } else {
                        if (log.isInfoEnabled()) log.info("The remote SDP media description <" +
                                                          sdpMediaDescription +
                                                          "> does not support the mandatory audio media types <" +
                                                          mandatoryAudioMediaTypes + ">.");
                    }
                }
                else {
                    if (log.isInfoEnabled()) log.info("The remote SDP media description <" +
                                                      sdpMediaDescription +
                                                      "> does not specify the mandatory audio media transport type <" +
                                                      SdpMediaTransport.RTP_AVP + ">.");

                }

            } else if (mediaType == SdpMediaType.VIDEO) {

                if (sdpMediaDescription.
                        areEncodingsSupported(mandatoryVideoMediaTypes)) {
                    indexesOfMatchingVideoMedia.add(i);
                    if (log.isDebugEnabled())
                        log.debug("The remote SDP media description <" +
                                sdpMediaDescription +
                                "> supports the mandatory video media types <" +
                                mandatoryVideoMediaTypes + ">.");
                } else {
                    if (log.isInfoEnabled()) log.info("The remote SDP media description <" +
                                                      sdpMediaDescription +
                                                      "> does not support the mandatory video media types <" +
                                                      mandatoryVideoMediaTypes + ">.");
                }
            }
        }
    }


    /**
     * Finds and returns the index of the first media descriptions from the
     * <param>mdList</param> that matches the <param>mimeType</param>.
     * <p>
     * Only the media descriptions whose index is listed in
     * <param>indexesOfMatchingMedia</param> are checked for match.
     * <p>
     * Null is returned if no match was found.
     *
     * @param   mdList
     * @param   indexesOfMatchingMedia
     * @param   mimeType
     * @return  index of the first media description that matched or null is no
     *          match was found.
     */
    private Integer getIndexOfMatchingMediaDescription(
            List<SdpMediaDescription> mdList,
            List<Integer> indexesOfMatchingMedia,
            MimeType mimeType) {

        Integer indexOfMedia = null;

        if (mimeType != null) {
            for (Integer index : indexesOfMatchingMedia) {
                SdpMediaDescription md = mdList.get(index);
                if (log.isDebugEnabled())
                    log.debug("Checking if call specific mime " +
                            "type <" + mimeType +
                            "> is supported by media description: " + md) ;

                if (!md.getSupportedRtpPayload(mimeType).isEmpty()) {
                    indexOfMedia = index;
                    if (log.isDebugEnabled())
                        log.debug("Call specific mime " +
                                "type is supported: " + mimeType) ;
                    break;
                } else {
                    if (log.isInfoEnabled()) log.info("Call specific mime " +
                                                      "type is NOT supported: " + mimeType);
                }
            }
        }

        return indexOfMedia;
    }

    /**
     * Finds and returns the index of the first media descriptions from the
     * <param>mdList</param> that matches the <param>transportType</param>.
     * <p>
     * Only the media descriptions whose index is listed in
     * <param>indexesOfMatchingMedia</param> are checked for match.
     * <p>
     * Null is returned if no match was found.
     *
     * @param   mdList
     * @param   indexesOfMatchingMedia
     * @param   transportType
     * @return  index of the first media description that matched or null is no
     *          match was found.
     */
    private Integer getIndexOfMediaTransportType(
            List<SdpMediaDescription> mdList,
            List<Integer> indexesOfMatchingMedia,
            SdpMediaTransport transportType) {

        Integer indexOfMedia = null;

        if (transportType != null) {
            for (Integer index : indexesOfMatchingMedia) {
                SdpMediaDescription md = mdList.get(index);
                if (log.isDebugEnabled())
                    log.debug("Checking if transport " +
                            "type <" + transportType +
                            "> is supported by media description: " + md) ;

                if (md.getMedia().getTransport().equals(transportType)) {
                    indexOfMedia = index;
                    if (log.isDebugEnabled())
                        log.debug("Transport " +
                                "type is supported: " + transportType) ;
                    break;
                } else {
                    if (log.isInfoEnabled()) log.info("Transport " +
                                                      "type is NOT supported: " + transportType);
                }
            }
        }

        return indexOfMedia;
    }

    /**
     * Tries to find an SDP intersection between a parsed remote SDP
     * (<param>sdpSessionDescription</param>) and call specific requirements
     * on media types (<param>callSpecificMediaTypes</param>).
     * <p>
     * When searching for an intersection, only the media descriptions
     * (from the <param>sdpSessionDescription</param) whose index is listed in
     * <param>indexesOfMatchingAudioMedia</param> and
     * <param>indexesOfMatchingVideoMedia</param> are checked for match.
     * <p>
     * The parameters <param>mandatoryAudioMediaTypes</param>,
     * <param>mandatoryVideoMediaTypes</param> and <param>defaultPTime</param>
     * are not used when finding an intersection, only for creation of an
     * {@link SdpIntersection}.
     * <p>
     * Null is returned if no match was found.
     *
     * @param   callSpecificMediaTypes
     * @param   indexesOfMatchingAudioMedia
     * @param   indexesOfMatchingVideoMedia
     * @param   sdpSessionDescription
     * @param   mandatoryAudioMediaTypes
     * @param   mandatoryVideoMediaTypes
     * @return  An SDP intersection. Null is returned if no intersection is found.
     */
    private SdpIntersection getIntersectionForCallSpecificMediaRequirements  (
            CallMediaTypes[] callSpecificMediaTypes,
            List<Integer> indexesOfMatchingAudioMedia,
            List<Integer> indexesOfMatchingVideoMedia,
            SdpSessionDescription sdpSessionDescription,
            Collection<MimeType> mandatoryAudioMediaTypes,
            Collection<MimeType> mandatoryVideoMediaTypes)  throws SdpInternalErrorException {

        SdpIntersection sdpIntersection = null;
        List<SdpMediaDescription> mdList =
                sdpSessionDescription.getMediaDescriptions();

        for (CallMediaTypes callMediaType : callSpecificMediaTypes) {

            // Select the first index that matches the call specific audio
            // requirements
            Integer indexOfMatchingAudio = getIndexOfMatchingMediaDescription(
                    mdList, indexesOfMatchingAudioMedia,
                    callMediaType.getMimeType(CallMediaType.AUDIO));

            // Select the first index that matches the call specific video
            // requirements
            Integer indexOfMatchingVideo = getIndexOfMatchingMediaDescription(
                    mdList, indexesOfMatchingVideoMedia,
                    callMediaType.getMimeType(CallMediaType.VIDEO));

            // Select the first index that has video RTCP feedback specs
            Integer indexOfMatchingVideoFeedback = getIndexOfMediaTransportType(
                    mdList, indexesOfMatchingVideoMedia,
                    SdpMediaTransport.RTP_AVPF);

            if (indexOfMatchingAudio == null) {
                // If no index is found for audio, no intersection is found
                // Check the next call media type
                if (log.isInfoEnabled()) log.info("No audio intersection is found between remote " +
                                                  "Session Description <" + sdpSessionDescription +
                                                  "> and call specific media types <" +
                                                  callMediaType + ">.");

            } else if (
                    (callMediaType.getMimeType(CallMediaType.VIDEO) != null)
                            && (indexOfMatchingVideo == null)) {
                // If call media types contains a video mime type but no
                // index found for video, no intersection is found.
                // Check the next call media type
                if (log.isInfoEnabled()) log.info("No video intersection is found between remote " +
                                                  "Session Description <" + sdpSessionDescription +
                                                  "> and call specific media types <" +
                                                  callMediaType + ">.");

            } else if (sdpSessionDescription.containsVideo() &&
                    (indexOfMatchingVideo == null)) {
                if (log.isInfoEnabled()){
                    log.info("Remote Session Description <" + sdpSessionDescription +
                                                  "> contains video but no video " +
                                                  "intersection is found for call specific " +
                                                  "media types <" + callMediaType + ">.");
                }
                
                //If we found an audio intersection but the SDP contain a video, we keep the intersection
                //if we doesn't have any yet. This will result as returning at least the audio intersection if
                //we don't have anything else that match in the for loop.
                if(sdpIntersection == null){
                    sdpIntersection = new SdpIntersection(
                        sdpSessionDescription,
                        indexOfMatchingAudio,
                        indexOfMatchingVideo,
                        indexOfMatchingVideoFeedback,
                        callMediaType,
                        mandatoryAudioMediaTypes,
                        mandatoryVideoMediaTypes);
                }
            } else {
                // An intersection is found
                sdpIntersection = new SdpIntersection(
                        sdpSessionDescription,
                        indexOfMatchingAudio,
                        indexOfMatchingVideo,
                        indexOfMatchingVideoFeedback,
                        callMediaType,
                        mandatoryAudioMediaTypes,
                        mandatoryVideoMediaTypes);
                break;
            }
        }
        return sdpIntersection;
    }

}
