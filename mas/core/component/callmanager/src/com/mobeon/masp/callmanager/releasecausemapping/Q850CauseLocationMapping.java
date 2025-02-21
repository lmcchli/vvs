/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.releasecausemapping;

import com.mobeon.masp.callmanager.configuration.IntegerInterval;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Collection;

/**
 * Contains a mapping from Q.850 cause and location values to network status
 * codes.
 * <p>
 * In SIP, the Q.850 cause value can retrieved from the SIP Reason header field
 * as specified in RFC 3326. Retrieval of the Q.850 location value has however
 * not been specified in the SIP community.
 * The SIP gateway ViG has however implemented a proprietary support for the
 * Q.850 location value that is also supported by the Call Manager.
 * Therefore, Call Manager also has to be able to handle situations where only
 * the cause value but not the location value is available.
 * <br>
 * This class thus contains one mapping to use when a location value is
 * available and another mapping to use when the location value is unknown.
 * A mapping that is created with no location specification will
 * be inserted into both mapping variants.
 * A mapping that is created with a location specification will only be
 * inserted into the first mapping variant.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Nyfeldt
 */
public class Q850CauseLocationMapping {

    private static final ILogger LOG =
            ILoggerFactory.getILogger(Q850CauseLocationMapping.class);

    private final MappedNetworkStatusCode[][] q850CauseLocationMap =
        new MappedNetworkStatusCode
                [ReleaseCauseConstants.Q850_CAUSE_MAX + 1]
                [ReleaseCauseConstants.Q850_LOCATION_MAX + 1];

    private final MappedNetworkStatusCode[] q850CauseMap =
        new MappedNetworkStatusCode
                [ReleaseCauseConstants.Q850_CAUSE_MAX + 1];


    /**
     * Adds a mapping from Q.850 cause intervals and Q.850 location intervals
     * to a network status code.
     * <p>
     * If a cause interval is invalid or out-of-range, the interval is ignored
     * during the mapping and a warning log is printed.
     * <br>
     * If a location interval is invalid or out-of-range, the interval is
     * ignored during the mapping and a warning log is printed.
     *
     * @param mappedNSC             The mapped network status code.
     * @param q850CauseIntervals    Q.850 cause intervals.
     * @param q850LocationIntervals Q.850 location intervals. May be null in
     *                              which case it implies all location values.
     * @throws IllegalArgumentException
     *                              If mappedNSC or q850CauseIntervals is null.
     */
    public void addMapping(MappedNetworkStatusCode mappedNSC,
                           Collection<IntegerInterval> q850CauseIntervals,
                           Collection<IntegerInterval> q850LocationIntervals) {

        // Verify input parameters
        if (mappedNSC == null) {
            throw new IllegalArgumentException(
                    "Mapped network status code must not be null.");
        } else if (q850CauseIntervals == null) {
            throw new IllegalArgumentException(
                    "Q.850 cause intervals must not be null.");
        }

        // Insert each Q.850 cause and location value combination
        for (IntegerInterval causeInterval : q850CauseIntervals) {
            int causeStart = causeInterval.getStart();
            int causeEnd = causeInterval.getEnd();

            // Ignore intervals where end value is smaller than start value
            if (causeEnd < causeStart) {
                LOG.warn("The following configured Q.850 cause interval <" +
                        causeInterval + "> is invalid and is ignored in " +
                        "the release cause mapping <" +
                        mappedNSC.getMappingName() + ">.");

            // Ignore intervals that are not within range
            } else if ((causeStart < ReleaseCauseConstants.Q850_CAUSE_MIN) ||
                    (causeEnd > ReleaseCauseConstants.Q850_CAUSE_MAX)) {
                LOG.warn("The following configured Q.850 cause interval <" +
                        causeInterval + "> is not within range and is " +
                        "ignored in the release cause mapping <" +
                        mappedNSC.getMappingName() + ">.");

            // Add mapping for all cause values in the interval
            } else {
                for (int i = causeStart; i <= causeEnd; i++)
                    addMapping(mappedNSC, i, q850LocationIntervals);
            }
        }
    }

    /**
     * Retrieves the mapped network status code for the given Q.850 cause and
     * location values.
     * <br>
     * If no mapping exists or if <param>q850Cause</param> or
     * <param>q850Location</param> is out-of-range, null is returned.
     * <br>
     * If <param>q850Location</param> is null, the mapped network status code
     * for the cause value only is selected.
     *
     * @param q850Cause     Value range 0-127.
     * @param q850Location  Value range 0-15.
     *
     * @return  Returns the mapped network status code.
     *          Null is returned if no mapped network status code can be found
     *          or if input parameters are out-of-range.
     */
    public MappedNetworkStatusCode getMappedNetworkStatusCode(
            int q850Cause, Integer q850Location) {
        MappedNetworkStatusCode nsc = null;

        if ((q850Cause >= ReleaseCauseConstants.Q850_CAUSE_MIN) &&
                (q850Cause <= ReleaseCauseConstants.Q850_CAUSE_MAX)) {
            if (q850Location == null) {
                nsc = q850CauseMap[q850Cause];
            } else if ((q850Location >= ReleaseCauseConstants.Q850_LOCATION_MIN) &&
                (q850Location <= ReleaseCauseConstants.Q850_LOCATION_MAX)) {
                nsc = q850CauseLocationMap[q850Cause][q850Location];
            }
        }

        return nsc;
    }

    //=========================== Static methods =========================

    /**
     * Returns a {@link Q850CauseLocationMapping} containing the default mapping.
     * The default mapping is:
     * <br>
     * Busy:
     *  cause = 17
     *  location = 1-15
     *  NSC = {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_BUSY}
     * <br>
     * No Reply:
     *  cause = 18-19
     *  location = 0-15
     *  NSC = {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_NO_REPLY}
     * <br>
     * Not Reachable:
     *  cause = 1-9, 20-23, 25-31
     *  location = 0-15
     *  NSC = {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_NOT_REACHABLE}
     * <br>
     * Suppressed:
     *  cause = 17
     *  location = 0
     *  NSC = {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_SUPPRESSED}
     * <br>
     * Congestion:
     *  cause = 39-44, 46
     *  location = 0-15
     *  NSC = {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_CONGESTION}
     *
     * @return The default {@link Q850CauseLocationMapping}.
     */
    public static Q850CauseLocationMapping getDefaultReleaseCauseMappings() {

        Q850CauseLocationMapping q850Mapping = new Q850CauseLocationMapping();

        q850Mapping.addBusyMapping();
        q850Mapping.addNoReplyMapping();
        q850Mapping.addNotReachableMapping();
        q850Mapping.addSuppressedMapping();
        q850Mapping.addCongestionMapping();

        return q850Mapping;
    }


    //=========================== Private methods =========================


    /**
     * Adds a mapping from a Q.850 cause value and Q.850 location value
     * (for each location value in the intervals) to a network status code.
     *
     * @param mappedNSC             The mapped network status code.
     *                              MUST NOT be null.
     * @param q850Cause             Q.850 cause value.
     * @param q850LocationIntervals Q.850 location intervals. May be null in
     *                              which case it implies all location values.
     */
    private void addMapping(MappedNetworkStatusCode mappedNSC,
                           int q850Cause,
                           Collection<IntegerInterval> q850LocationIntervals) {


        // If location intervals is null, the mapping shall be made for all
        // location values
        if (q850LocationIntervals == null) {
            q850LocationIntervals = IntegerInterval.parseIntegerIntervals(
                    ReleaseCauseConstants.Q850_LOCATION_MIN + "-" +
                            ReleaseCauseConstants.Q850_LOCATION_MAX);
            addMapping(mappedNSC, q850Cause);
        }

        // Insert each Q.850 cause and location value combination
        for (IntegerInterval locationInterval : q850LocationIntervals) {
            int locationStart = locationInterval.getStart();
            int locationEnd = locationInterval.getEnd();

            // Ignore intervals where end value is smaller than start value
            if (locationEnd < locationStart) {
                LOG.warn("The following configured Q.850 location interval <" +
                        locationInterval + "> is invalid and is ignored in " +
                        "the release cause mapping <" +
                        mappedNSC.getMappingName() + ">.");

            // Ignore intervals that are not within range
            } else if ((locationStart < ReleaseCauseConstants.Q850_LOCATION_MIN) ||
                    (locationEnd > ReleaseCauseConstants.Q850_LOCATION_MAX)) {
                LOG.warn("The following configured Q.850 location interval <" +
                        locationInterval + "> is not within range and is " +
                        "ignored in the release cause mapping <" +
                        mappedNSC.getMappingName() + ">.");

            // Add mapping for all location values in the interval
            } else {
                for (int i = locationStart; i <= locationEnd; i++) {
                    addMapping(mappedNSC, q850Cause, i);
                }
            }
        }
    }

    /**
     * Adds a mapping from a Q.850 cause and location value to a network status
     * code.
     *
     * @param mappedNSC     The mapped network status code. MUST NOT be null.
     * @param q850Cause     Value range 0-127. MUST NOT be out of range.
     * @param q850Location  Value range 0-15. MUST NOT be out of range.
     */
    private void addMapping(MappedNetworkStatusCode mappedNSC,
                            int q850Cause, int q850Location) {
    	
    	MappedNetworkStatusCode previousMappedNSC = q850CauseLocationMap[q850Cause][q850Location];
    	if ( (previousMappedNSC == null) ||
    		 (previousMappedNSC.getQ850Priority() > mappedNSC.getQ850Priority()) ) {
    		// if there are multiple networkStatusCode for the same cause and location, 
    		// the one with the smallest prioritary number is kept 
    		q850CauseLocationMap[q850Cause][q850Location] = mappedNSC;
    	}
    }

    /**
     * Adds a mapping from a Q.850 cause value to a network status code.
     * This mapping will be used if the location value is unknown.
     *
     * @param mappedNSC     The mapped network status code. MUST NOT be null.
     * @param q850Cause     Value range 0-127. MUST NOT be out of range.
     */
    private void addMapping(MappedNetworkStatusCode mappedNSC, int q850Cause) {
    	MappedNetworkStatusCode previousMappedNSC = q850CauseMap[q850Cause];
    	if ( (previousMappedNSC == null) ||
    		 (previousMappedNSC.getQ850Priority() > mappedNSC.getQ850Priority()) ) {
    		// if there are multiple networkStatusCode for the same cause and location, 
    		// the one with the smallest prioritary number is kept 

    		q850CauseMap[q850Cause] = mappedNSC;
    	}
    }

    /**
     * Adds the default mapping for the busy scenario.
     * <p>
     * Q.850 cause value 17 in combination with Q.850 location values 1-15
     * is mapped to network status code
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_BUSY}.
     */
    private void addBusyMapping() {
        addMapping(ReleaseCauseConstants.BUSY_MAPPED_NSC, 17,
                IntegerInterval.parseIntegerIntervals("1-15"));
        addMapping(ReleaseCauseConstants.BUSY_MAPPED_NSC, 17);
    }

    /**
     * Adds the default mapping for the no reply scenario.
     * <p>
     * Q.850 cause values 18-19 in combination with all Q.850 location values
     * are mapped to network status code
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_NO_REPLY}.
     */
    private void addNoReplyMapping() {
        addMapping(ReleaseCauseConstants.NO_REPLY_MAPPED_NSC,
                IntegerInterval.parseIntegerIntervals("18-19"), null);
    }

    /**
     * Adds the default mapping for the not reachable scenario.
     * <p>
     * Q.850 cause values 1-9, 20-23, 25-31 in combination with all Q.850
     * location values are mapped to network status code
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_NOT_REACHABLE}.
     */
    private void addNotReachableMapping() {
        addMapping(ReleaseCauseConstants.NOT_REACHABLE_MAPPED_NSC,
                IntegerInterval.parseIntegerIntervals("1-9,20-23,25-31"), null);
    }

    /**
     * Adds the default mapping for the user suppressed scenario.
     * <p>
     * Q.850 cause value 17 in combination with Q.850 location value 0 is
     * mapped to network status code
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_SUPPRESSED}.
     */
    private void addSuppressedMapping() {
        addMapping(ReleaseCauseConstants.SUPPRESSED_MAPPED_NSC, 17, 0);
    }

    /**
     * Adds the default mapping for the congestion scenario.
     * <p>
     * Q.850 cause values 39-44, 46 in combination with all Q.850
     * location values are mapped to network status code
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_CONGESTION}.
     */
    private void addCongestionMapping() {
        addMapping(ReleaseCauseConstants.CONGESTION_MAPPED_NSC,
                IntegerInterval.parseIntegerIntervals("39-44,46"), null);
    }

}
