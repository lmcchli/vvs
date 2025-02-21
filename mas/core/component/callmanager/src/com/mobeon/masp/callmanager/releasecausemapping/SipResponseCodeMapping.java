/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.releasecausemapping;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.configuration.IntegerInterval;

import javax.sip.message.Response;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains a mapping from SIP response codes to network status codes.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Nyfeldt
 */
public class SipResponseCodeMapping {

    private static final ILogger LOG =
            ILoggerFactory.getILogger(SipResponseCodeMapping.class);

    private final ConcurrentHashMap<Integer, MappedNetworkStatusCode>
            responseCodeMap =
            new ConcurrentHashMap<Integer, MappedNetworkStatusCode>();

    /**
     * Adds a mapping from SIP response code intervals to a network status code.
     * <p>
     * If a SIP response code interval is invalid or out-of-range,
     * the interval is ignored during the mapping and a warning log is printed.
     *
     * @param mappedNSC             The mapped network status code.
     * @param responseCodeIntervals The response code intervals.
     * @throws IllegalArgumentException
     *                              If mappedNSC or responseCodeIntervals is null.
     */
    public void addMapping(MappedNetworkStatusCode mappedNSC,
                           Collection<IntegerInterval> responseCodeIntervals) {

        if (mappedNSC == null) {
            throw new IllegalArgumentException(
                    "Mapped network status code must not be null.");
        } else if (responseCodeIntervals == null) {
            throw new IllegalArgumentException(
                    "SIP response code intervals must not be null.");
        }

        for (IntegerInterval responseInterval : responseCodeIntervals) {
            int start = responseInterval.getStart();
            int end = responseInterval.getEnd();

            // Ignore intervals where end value is smaller than start value
            if (end < start) {
                LOG.warn("The following configured SIP response code interval <" +
                        responseInterval + "> is invalid and is ignored in " +
                        "the release cause mapping <" +
                        mappedNSC.getMappingName() + ">.");

            // Ignore intervals that are not within range
            } else if ((start < ReleaseCauseConstants.SIP_RESPONSE_CODE_MIN) ||
                    (end > ReleaseCauseConstants.SIP_RESPONSE_CODE_MAX)) {
                LOG.warn("The following configured SIP response code interval <" +
                        responseInterval + "> is not within range and is " +
                        "ignored in the release cause mapping <" +
                        mappedNSC.getMappingName() + ">.");

            // Add mapping for all SIP response code values in the interval
            } else {
                for (int i = start; i <= end; i++) {
                    addMapping(mappedNSC, i);
                }
            }
        }
    }

    /**
     * Retrieves the mapped network status code for the given SIP response code.
     * <br>
     * If no mapping exists or if <param>sipResponseCode</param> is
     * out-of-range, null is returned.
     *
     * @param sipResponseCode Value range 300-699.
     *
     * @return  Returns the mapped network status code.
     *          Null is returned if no mapped network status code can be found
     *          or if <param>sipResponseCode</param> is out-of-range.
     */
    public MappedNetworkStatusCode getMappedNetworkStatusCode(
            int sipResponseCode) {
        MappedNetworkStatusCode nsc = null;

        if ((sipResponseCode >= ReleaseCauseConstants.SIP_RESPONSE_CODE_MIN) &&
                (sipResponseCode <= ReleaseCauseConstants.SIP_RESPONSE_CODE_MAX))
            nsc = responseCodeMap.get(sipResponseCode);

        return nsc;
    }

    //=========================== Static methods =========================

    /**
     * Returns a {@link SipResponseCodeMapping} containing the default mapping.
     * The default mapping is:
     * <br>
     * Busy:
     *  SIP response code = {@link Response.BUSY_HERE}
     *  NSC = {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_BUSY}
     * <br>
     * No Reply:
     *  SIP response code = {@link Response.REQUEST_TIMEOUT},
     *      {@link Response.TEMPORARILY_UNAVAILABLE}
     *  NSC = {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_NO_REPLY}
     * <br>
     * Not Reachable:
     *  SIP response code = {@link Response.MOVED_PERMANENTLY},
     *      {@link Response.FORBIDDEN}, {@link Response.NOT_FOUND},
     *      {@link Response.GONE}, {@link Response.ADDRESS_INCOMPLETE},
     *      {@link Response.NOT_IMPLEMENTED}, {@link Response.BAD_GATEWAY},
     *      {@link Response.DECLINE}
     *  NSC = {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_NOT_REACHABLE}
     * <br>
     * Suppressed:
     *  SIP response code = {@link Response.BUSY_EVERYWHERE}
     *  NSC = {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_SUPPRESSED}
     * <br>
     * Congestion:
     *  SIP response code = {@link Response.SERVICE_UNAVAILABLE}
     *  NSC = {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_CONGESTION}
     *
     * @return The default {@link SipResponseCodeMapping}.
     */
    public static SipResponseCodeMapping getDefaultReleaseCauseMappings() {

        SipResponseCodeMapping mapping = new SipResponseCodeMapping();

        mapping.addBusyMapping();
        mapping.addNoReplyMapping();
        mapping.addNotReachableMapping();
        mapping.addSuppressedMapping();
        mapping.addCongestionMapping();

        return mapping;
    }


    //=========================== Private methods =========================

    /**
     * Adds a mapping from a SIP response code to a network status code.
     *
     * @param mappedNSC     The mapped network status code. MUST NOT be null.
     * @param responseCode  Value range 300-699. MUST NOT be out of range.
     */
    private void addMapping(MappedNetworkStatusCode mappedNSC, int responseCode) {
        responseCodeMap.put(responseCode, mappedNSC);
    }

    /**
     * Adds the default mapping for the busy scenario.
     * <p>
     * SIP response code 486 (Busy Here) is mapped to network status code
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_BUSY}.
     */
    private void addBusyMapping() {
        addMapping(ReleaseCauseConstants.BUSY_MAPPED_NSC, Response.BUSY_HERE);
    }

    /**
     * Adds the default mapping for the no reply scenario.
     * <p>
     * SIP response codes 408 (Request Timeout) and 480
     * (Temporarily Unavailable) are mapped to network status code
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_NO_REPLY}.
     */
    private void addNoReplyMapping() {
        addMapping(ReleaseCauseConstants.NO_REPLY_MAPPED_NSC,
                Response.REQUEST_TIMEOUT);
        addMapping(ReleaseCauseConstants.NO_REPLY_MAPPED_NSC,
                Response.TEMPORARILY_UNAVAILABLE);
    }

    /**
     * Adds the default mapping for the not reachable scenario.
     * <p>
     * SIP response codes 301 (Moved Permanently), 403 (Forbidden),
     * 404 (Not Found), 410 (Gone), 484 (Address Incomplete),
     * 501 (Not Implemented), 502 (Bad Gateway) and 603 (Decline)
     * are mapped to network status code
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_NOT_REACHABLE}.
     */
    private void addNotReachableMapping() {
        addMapping(ReleaseCauseConstants.NOT_REACHABLE_MAPPED_NSC,
                Response.MOVED_PERMANENTLY);
        addMapping(ReleaseCauseConstants.NOT_REACHABLE_MAPPED_NSC,
                Response.FORBIDDEN);
        addMapping(ReleaseCauseConstants.NOT_REACHABLE_MAPPED_NSC,
                Response.NOT_FOUND);
        addMapping(ReleaseCauseConstants.NOT_REACHABLE_MAPPED_NSC,
                Response.GONE);
        addMapping(ReleaseCauseConstants.NOT_REACHABLE_MAPPED_NSC,
                Response.ADDRESS_INCOMPLETE);
        addMapping(ReleaseCauseConstants.NOT_REACHABLE_MAPPED_NSC,
                Response.NOT_IMPLEMENTED);
        addMapping(ReleaseCauseConstants.NOT_REACHABLE_MAPPED_NSC,
                Response.BAD_GATEWAY);
        addMapping(ReleaseCauseConstants.NOT_REACHABLE_MAPPED_NSC,
                Response.DECLINE);
    }

    /**
     * Adds the default mapping for the suppressed scenario.
     * <p>
     * SIP response code 600 (Busy Everywhere) is mapped to network status code
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_SUPPRESSED}.
     */
    private void addSuppressedMapping() {
        addMapping(ReleaseCauseConstants.SUPPRESSED_MAPPED_NSC,
                Response.BUSY_EVERYWHERE);
    }

    /**
     * Adds the default mapping for the congestion scenario.
     * <p>
     * SIP error response 503 (Service Unavailable) is mapped to
     * network status code
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_CONGESTION}.
     */
    private void addCongestionMapping() {
        addMapping(ReleaseCauseConstants.CONGESTION_MAPPED_NSC,
                Response.SERVICE_UNAVAILABLE);
    }

}
