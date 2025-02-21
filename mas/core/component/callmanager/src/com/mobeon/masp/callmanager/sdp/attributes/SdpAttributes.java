/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp.attributes;

import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sdp.SdpConstants;
import com.mobeon.masp.callmanager.sdp.SdpSessionDescription;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.sdp.Attribute;
import com.mobeon.sdp.SdpParseException;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SdpException;

import java.util.Collection;
import java.util.Vector;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is a container of attributes present in a Session Description or
 * Media Description of an SDP.d
 * <p>
 * The SDP attributes currently supported are:
 * <ul>
 * <li>rtpmap</li>
 * <li>ptime</li>
 * <li>maxptime</li>
 * <li>fmtp</li>
 * <li>charset</li>
 * <li>sendrecv</li>
 * <li>sendonly</li>
 * <li>recvonly</li>
 * <li>inactive</li>
 * </ul>
 * <p>
 * This class is thread-safe through the use of synchronized on setters, getters
 * and toString.
 *
 * @author Malin Flodin
 */
public class SdpAttributes {

    private static final ILogger LOG =
            ILoggerFactory.getILogger(SdpAttributes.class);

    private final HashMap<Integer, SdpFmtp> fmtps =
            new HashMap<Integer, SdpFmtp>();

    private SdpPTime pTime = null;
    private SdpMaxPTime maxPTime = null;

    private String charset = null;

    private final HashMap<Integer, SdpRtpMap> rtpMaps =
            new HashMap<Integer, SdpRtpMap>();

    private SdpTransmissionMode transmissionMode = null;

    private final AtomicReference<String> stringRepresentation =
            new AtomicReference<String>();

    private final Vector<SdpRTCPFeedback> rtcpFeedback = new Vector<SdpRTCPFeedback>();
    
    private final Vector<SdpPreconditionCurr> currentPreconditions = new Vector<SdpPreconditionCurr>();
    private final Vector<SdpPreconditionDes> desiredPreconditions = new Vector<SdpPreconditionDes>();
    private final Vector<SdpPreconditionConf> confirmPreconditions = new Vector<SdpPreconditionConf>();

    public SdpAttributes() {
    }

    // Getters
    public synchronized HashMap<Integer, SdpFmtp> getFmtps() {
        return fmtps;
    }

    public synchronized SdpPTime getPTime() {
        return pTime;
    }

    public synchronized SdpMaxPTime getMaxPTime() {
        return maxPTime;
    }

    public synchronized String getCharset() {
        return charset;
    }

    public synchronized HashMap<Integer, SdpRtpMap> getRtpMaps() {
        return rtpMaps;
    }

    public synchronized SdpTransmissionMode getTransmissionMode() {
        return transmissionMode;
    }


    // Setters
    public synchronized void addFmtp(SdpFmtp fmtp) {
        if (fmtp != null)
            fmtps.put(fmtp.getFormat(), fmtp);
    }

    public synchronized void addRtpMap(SdpRtpMap rtpMap) {
        if (rtpMap != null)
            rtpMaps.put(rtpMap.getPayloadType(), rtpMap);
    }

    public synchronized void setPTime(SdpPTime pTime) {
        if (pTime != null)
            this.pTime = pTime;
    }

    public synchronized void setMaxPTime(SdpMaxPTime maxPTime) {
        if (maxPTime != null)
            this.maxPTime = maxPTime;
    }

    public synchronized void setCharset(String charset) {
        this.charset = charset;
    }

    public synchronized void setTransmissionMode(SdpTransmissionMode mode) {
        if (mode != null)
            this.transmissionMode = mode;
    }

    public synchronized Vector<SdpRTCPFeedback> getRTCPFeedback() {
        return this.rtcpFeedback;
    }

    public synchronized void addRTCPFeedback(SdpRTCPFeedback value) {
        this.rtcpFeedback.add(value);
    }
    
    public synchronized Vector<SdpPreconditionCurr> getCurrentPreconditions() {
        return this.currentPreconditions;
    }

    public synchronized void addCurrentPrecondition(SdpPreconditionCurr value) {
            this.currentPreconditions.add(value);
    }
    
    public synchronized Vector<SdpPreconditionDes> getDesiredPreconditions() {
        return this.desiredPreconditions;
    }

    public synchronized void addDesiredPrecondition(SdpPreconditionDes value) {
            this.desiredPreconditions.add(value);
    }
    
    public synchronized Vector<SdpPreconditionConf> getConfirmPreconditions() {
        return this.confirmPreconditions;
    }

    public synchronized void addConfirmPrecondition(SdpPreconditionConf value) {
            this.confirmPreconditions.add(value);
    }

    /**
     * Encodes the attributes mode into an SDP stack format using the
     * <param>sdpFactory</param>.
     * A vector of {@link Attribute} is returned.
     * <p>
     * PTime is encoded using {@link SdpPTime#encodeToStackFormat(SdpFactory)}.
     * MaxPTime is encoded using {@link SdpMaxPTime#encodeToStackFormat(SdpFactory)}.
     * Transmission mode is encoded using
     * {@link SdpTransmissionMode#encodeToStackFormat(SdpFactory)}.
     * Rtp maps are encoded using {@link SdpRtpMap#encodeToStackFormat(SdpFactory)}.
     * Fmtp's are encoded using {@link SdpFmtp#encodeToStackFormat(SdpFactory)}.
     * Charset is encoded.
     *
     * @param   sdpFactory
     * @return  A vector of all encoded attributes.
     * @throws  SdpException if the stack format could not be created.
     */
    public Vector<Attribute> encodeToStackFormat(SdpFactory sdpFactory)
            throws SdpException {

        Vector<Attribute> attributes = new Vector<Attribute>();

        // Add pTime attribute
        SdpPTime pTime = getPTime();
        if (pTime != null)
            attributes.add(pTime.encodeToStackFormat(sdpFactory));

        // Add maxPTime attribute
        SdpMaxPTime maxPTime = getMaxPTime();
        if (maxPTime != null)
            attributes.add(maxPTime.encodeToStackFormat(sdpFactory));

        // Add charset attribute
        if (charset != null)
            attributes.add(sdpFactory.createAttribute(
                    SdpConstants.ATTRIBUTE_CHARSET, charset));

        // Add transmission mode attribute
        SdpTransmissionMode mode = getTransmissionMode();
        if (mode != null)
            attributes.add(mode.encodeToStackFormat(sdpFactory));

        // Add rtpmap attributes
        for (SdpRtpMap rtpmap : getRtpMaps().values())
            attributes.add(rtpmap.encodeToStackFormat(sdpFactory));

        // Add fmtp attributes
        for (SdpFmtp fmtp : getFmtps().values())
            attributes.add(fmtp.encodeToStackFormat(sdpFactory));

        // Add RTCP-feedback attributes
        for (SdpRTCPFeedback fb : getRTCPFeedback())
            attributes.add(fb.encodeToStackFormat(sdpFactory));
        
        // Add curr Precondition attributes
        for (SdpPreconditionCurr curr : getCurrentPreconditions())
            attributes.add(curr.encodeToStackFormat(sdpFactory));
        
        // Add des Precondition attributes
        for (SdpPreconditionDes des : getDesiredPreconditions())
            attributes.add(des.encodeToStackFormat(sdpFactory));
        
        // Add conf Precondition attributes
        for (SdpPreconditionConf conf : getConfirmPreconditions())
            attributes.add(conf.encodeToStackFormat(sdpFactory));

        return attributes;
    }

    public synchronized String toString() {
        String representation = stringRepresentation.get();

        if (representation == null) {
            representation = "<PTime = " + pTime +
                    ">, <MaxPTime = " + maxPTime +
                    ">, <RtpMaps = " + rtpMaps.values() +
                    ">, <Fmtps = " + fmtps.values() +
                    ">, <Charset = " + charset +
                    ">, <TransmissionMode = " + transmissionMode + ">";
            stringRepresentation.set(representation);
        }

        return representation;
    }

    /**
     * This method parses a vector of <param>attributes</param> from an SDP
     * and returns a parsed representation of the attributes as an
     * <@link SdpAttributes>.
     * <p>
     * The following attributes are retrieved:
     * <ul>
     * <li>rtpmap (using {@link SdpRtpMap#parseRtpMapAttribute(String)})</li>
     * <li>ptime (using {@link SdpPTime#parsePTimeAttribute(String)})</li>
     * <li>maxptime (using {@link SdpMaxPTime#parseMaxPTimeAttribute(String)})</li>
     * <li>fmtp (using {@link SdpFmtp#parseFmtpAttribute(String)})</li>
     * <li>sendrecv</li>
     * <li>sendonly</li>
     * <li>recvonly</li>
     * <li>inactive</li>
     * </ul>
     * If not supported, an
     * {@link SdpNotSupportedException} is thrown as described for respective
     * method.
     * <p>
     * If the <param>connection</param> field could not be parsed an
     * {@link SdpNotSupportedException} is thrown with
     * {@link SipWarning.SD_PARAMETER_NOT_UNDERSTOOD}.
     *
     * @param   attributes      A vector of attribute fields retrieved from SDP.
     * @return                  A parsed SdpAttributes.
     * @throws SdpNotSupportedException
     *                          If the one of the above listed attributes was
     *                          not parsed ok.
     */
    public static SdpAttributes parseAttributes(Vector<Attribute> attributes)
            throws SdpNotSupportedException {

        SdpAttributes sdpAttributes = new SdpAttributes();

        if (attributes != null) {

            for (Attribute attribute : attributes) {

                if (LOG.isDebugEnabled())
                    LOG.debug("Parsing attribute: " + attribute);

                try {
                    String name = attribute.getName();
                    String value = attribute.getValue();

                    if (name != null) {

                        // RtpMap
                        if (name.equals(SdpConstants.ATTRIBUTE_RTPMAP))
                            sdpAttributes.addRtpMap(
                                    SdpRtpMap.parseRtpMapAttribute(value));

                        // PTime
                        else if (name.equals(SdpConstants.ATTRIBUTE_PTIME))
                            sdpAttributes.setPTime(
                                    SdpPTime.parsePTimeAttribute(value));

                        // MaxPTime
                        else if (name.equals(SdpConstants.ATTRIBUTE_MAXPTIME))
                            sdpAttributes.setMaxPTime(
                                    SdpMaxPTime.parseMaxPTimeAttribute(value));

                        // Fmtp
                        else if (name.equals(SdpConstants.ATTRIBUTE_FMTP))
                            sdpAttributes.addFmtp(
                                    SdpFmtp.parseFmtpAttribute(value));

                        // SendRecv
                        else if (name.equals(SdpConstants.ATTRIBUTE_SENDRECV))
                            sdpAttributes.setTransmissionMode(
                                    SdpTransmissionMode.SENDRECV);

                        // SendOnly
                        else if (name.equals(SdpConstants.ATTRIBUTE_SENDONLY))
                            sdpAttributes.setTransmissionMode(
                                    SdpTransmissionMode.SENDONLY);

                        // RecvOnly
                        else if (name.equals(SdpConstants.ATTRIBUTE_RECVONLY))
                        sdpAttributes.setTransmissionMode(
                                SdpTransmissionMode.RECVONLY);

                        // Inactive
                        else if (name.equals(SdpConstants.ATTRIBUTE_INACTIVE))
                            sdpAttributes.setTransmissionMode(
                                    SdpTransmissionMode.INACTIVE);

                        // Charset
                        else if (name.equals(SdpConstants.ATTRIBUTE_CHARSET))
                            sdpAttributes.setCharset(value);

                        else if (name.equals(SdpConstants.ATTRIBUTE_RTCP_FB))
                            sdpAttributes.addRTCPFeedback(
                                    SdpRTCPFeedback.parseAttribute(value));
                        
                        else {
                            if( ConfigurationReader.getInstance().getConfig().isPreconditionEnabled() ) {
                                // Curr (RFC3312 precondition) 
                                if (name.equals(SdpConstants.ATTRIBUTE_PRECONDITION_CURRENT))
                                    sdpAttributes.addCurrentPrecondition(
                                            SdpPrecondition.parseCurrentStatusAttribute(value));
                                
                                // Des (RFC3312 precondition) 
                                else if (name.equals(SdpConstants.ATTRIBUTE_PRECONDITION_DESIRED))
                                    sdpAttributes.addDesiredPrecondition(
                                            SdpPrecondition.parseDesiredStatusAttribute(value));
                                
                                // Conf (RFC3312 precondition) 
                                else if (name.equals(SdpConstants.ATTRIBUTE_PRECONDITION_CONFIRM))
                                    sdpAttributes.addConfirmPrecondition(
                                            SdpPrecondition.parseConfirmStatusAttribute(value));
                            }
                        }
                    }

                } catch (SdpParseException e) {
                    if (LOG.isDebugEnabled())
                        LOG.debug("Exception occurred when parsing attribute: " +
                                attribute + ". It is ignored.");
                }
            }
        }

        return sdpAttributes;
    }

    /**
     * This method is used to merge <param>defaultAttributes</param> with
     * attributes that override the default values,
     * i.e. the <param>overrideAttributes</param>.
     * <p>
     * First, non-null attributes are selected from the
     * <param>defaultAttributes</param> and stored in a new {@link SdpAttributes}.
     * Then, non-null attributes are selected from the
     * <param>overrideAttributes</param> and stored in a new {@link SdpAttributes}.
     *
     * @param   defaultAttributes
     * @param   overrideAttributes
     * @return  A new merged {@link SdpAttributes}
     */
    public static SdpAttributes mergeAttributes(
            SdpAttributes defaultAttributes, SdpAttributes overrideAttributes) {

        SdpAttributes mergedAttributes = new SdpAttributes();

        // First copy the default attributes to the merged result
        copyAttributes(defaultAttributes, mergedAttributes);

        // Then copy the overridden values in the merged result
        copyAttributes(overrideAttributes, mergedAttributes);

        if (LOG.isDebugEnabled())
            LOG.debug("Merged attributes: " + mergedAttributes);

        return mergedAttributes;
    }

    /**
     * Compare if SdpAttributes are the same (for limited criteria)  
     * @param sdpAttributes {@link SdpAttributes}
     * @param excludeTransmissionMode true if transmissionMode must be exclude out of the comparison, false otherwise
     * return true if both objects are equal, false otherwise
     */
    public boolean compareWith(SdpAttributes sdpAttributes, boolean excludeTransmissionMode) {
        boolean pTimeResult = true;
        boolean maxPTimeResult = true;
        boolean rtpMapsResult = true;
        boolean fmtpsResult = true;
        boolean charsetResult = true;
        boolean transmissionModeResult = true;

        if (pTime != null && sdpAttributes.getPTime() != null) {
            pTimeResult = pTime.toString().equals(sdpAttributes.getPTime().toString());
        } else if (pTime != null || sdpAttributes.getPTime() != null) {
            return false;
        }

        if (maxPTime != null && sdpAttributes.getMaxPTime() != null) {
            maxPTimeResult = maxPTime.toString().equals(sdpAttributes.getMaxPTime().toString());
        } else if (maxPTime != null || sdpAttributes.getMaxPTime() != null) {
            return false;
        }

        if (!rtpMaps.values().isEmpty() && !sdpAttributes.getRtpMaps().values().isEmpty()) {
            rtpMapsResult = rtpMaps.values().toString().equals(sdpAttributes.getRtpMaps().values().toString()); 
        } else if (!rtpMaps.values().isEmpty() || !sdpAttributes.getRtpMaps().values().isEmpty()) {
            return false;
        }

        if (!fmtps.values().isEmpty() && !sdpAttributes.getFmtps().values().isEmpty()) {
            fmtpsResult = fmtps.values().toString().equals(sdpAttributes.getFmtps().values().toString()); 
        } else if (!fmtps.values().isEmpty() || !sdpAttributes.getFmtps().values().isEmpty()) {
            return false;
        }

        if (charset != null && sdpAttributes.getCharset() != null) {
            charsetResult = charset.equals(sdpAttributes.getCharset());
        } else if (charset != null || sdpAttributes.getCharset() != null) {
            return false;
        }

        if (!excludeTransmissionMode) {
            if (transmissionMode != null && sdpAttributes.getTransmissionMode() != null) {
                transmissionModeResult = transmissionMode.toString().equals(sdpAttributes.getTransmissionMode().toString());
            } else if (transmissionMode != null || sdpAttributes.getTransmissionMode() != null) {
                return false;
            }
        }
        return pTimeResult && maxPTimeResult && rtpMapsResult && fmtpsResult && charsetResult && transmissionModeResult;
    }

    /**
     * Copies all attributes that are not null from the <param>from</param>
     * attributes to the <param>to</param> attributes.
     * @param from
     * @param to
     */
    private static void copyAttributes(SdpAttributes from, SdpAttributes to) {

        if (from.getPTime() != null)
            to.setPTime(from.getPTime());

        if (from.getMaxPTime() != null)
            to.setMaxPTime(from.getMaxPTime());

        if (from.getTransmissionMode() != null)
            to.setTransmissionMode(from.getTransmissionMode());

        for (SdpFmtp fmtp : from.getFmtps().values())
            to.addFmtp(fmtp);

        for (SdpRtpMap rtpmap : from.getRtpMaps().values())
            to.addRtpMap(rtpmap);

        for (SdpRTCPFeedback val : from.getRTCPFeedback()) {
            to.addRTCPFeedback(val);
        }
        
        for (SdpPreconditionCurr curr : from.getCurrentPreconditions()) {
            to.addCurrentPrecondition(curr);
        }
        
        for (SdpPreconditionDes des : from.getDesiredPreconditions()) {
            to.addDesiredPrecondition(des);
        }
        
        for (SdpPreconditionConf conf : from.getConfirmPreconditions()) {
            to.addConfirmPrecondition(conf);
        }
    }
}
