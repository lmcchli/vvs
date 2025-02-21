/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.releasecausemapping;

import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.UnknownParameterException;
import com.mobeon.masp.callmanager.configuration.IntegerInterval;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Contains the mapping from release causes to network status codes.
 * A release cause can be either a Q.850 cause and location pair retrieved from
 * the SIP Reason header field (RFC 3326) or a SIP response code.
 * <p>
 * A mapping can be created either by parsing a configuration or by retrieving
 * the default mapping.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Nyfeldt
 */
public class ReleaseCauseMapping {

    private static final ILogger LOG =
            ILoggerFactory.getILogger(ReleaseCauseMapping.class);

    private int defaultNetworkStatusCode =
            ReleaseCauseConstants.NETWORK_STATUS_CODE_DEFAULT;
    private int noLicenseNetworkStatusCode  = ReleaseCauseConstants.NETWORK_STATUS_CODE_NO_LICENSE;

    private SipResponseCodeMapping sipResponseCodeMapping =
            new SipResponseCodeMapping();

    private Q850CauseLocationMapping q850CauseLocationMapping =
            new Q850CauseLocationMapping();

    public int getDefaultNetworkStatusCode() {
        return defaultNetworkStatusCode;
    }

    public int getNoLicenseNetworkStatusCode() {
        return noLicenseNetworkStatusCode;
    }

    /**
     * Retrieves the network status code for a given SIP response code and
     * Q.850 cause/location pair.
     * If the Q.850 cause/location pair is not null, the network status code
     * for that pair is returned.
     * Otherwise, if the SIP response code is not null, the network status code
     * for that response code is returned.
     * Otherwise, the default network status code is returned.
     *
     * @param sipResponseCode   Value range: 300-699.
     * @param q850Pair          Pair of Q.850 cause/location.
     * @return The mapped network status code, i.e. a value between
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_MIN} and
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_MAX}.
     */
    public int getNetworkStatusCode(
            Integer sipResponseCode, Q850CauseLocationPair q850Pair) {
        int networkStatusCode = getDefaultNetworkStatusCode();

        if (q850Pair != null) {
            networkStatusCode = getNetworkStatusCode(q850Pair);
        } else if (sipResponseCode != null) {
            networkStatusCode = getNetworkStatusCode(sipResponseCode);
        }

        return networkStatusCode;
    }

    /**
     * Retrieves the network status code for a given SIP response code.
     * If the SIP response code is out-of-range or if no mapping was
     * found, the default network status code is returned.
     *
     * @param sipResponseCode   Value range: 300-699.
     * @return The mapped network status code, i.e. a value between
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_MIN} and
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_MAX}.
     */
    public int getNetworkStatusCode(int sipResponseCode) {
        int networkStatusCode = getDefaultNetworkStatusCode();

        if (LOG.isDebugEnabled())
            LOG.debug("Looking up network status code for SIP " +
                    sipResponseCode + ".");

        MappedNetworkStatusCode mapping =
                sipResponseCodeMapping.getMappedNetworkStatusCode(sipResponseCode);

        if (mapping != null) {
            networkStatusCode = mapping.getNetworkStatusCode();
            if (LOG.isDebugEnabled())
                LOG.debug("SIP " + sipResponseCode +
                        " is mapped to: <network status code = " + networkStatusCode +
                        ">, <mappingName = " + mapping.getMappingName() + ">");
        } else {
            if (LOG.isDebugEnabled())
                LOG.debug("SIP " + sipResponseCode +
                        ": No network status code configured. " +
                        "Default network status code " +
                        networkStatusCode + " is used.");
        }


        return networkStatusCode;
    }

    /**
     * Retrieves the network status code for a given Q.850 cause/location
     * pair.
     * If the Q.850 cause or location are out-of-range or if no mapping was
     * found, the default network status code is returned.
     * If the Q.850 location value is null, a mapping is looked up for the
     * Q.850 cause value only.
     *
     * @param pair          Pair of Q.850 cause/location.
     * @return The mapped network status code, i.e. a value between
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_MIN} and
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_MAX}.
     */
    public int getNetworkStatusCode(Q850CauseLocationPair pair) {
        int networkStatusCode = getDefaultNetworkStatusCode();

        if (pair != null) {
            if (LOG.isDebugEnabled())
                LOG.debug("Looking up network status code for Q.850 cause = " +
                        pair.getCause() + ", Q.850 location = " +
                        pair.getLocation() + ".");

            MappedNetworkStatusCode mapping = q850CauseLocationMapping.
                    getMappedNetworkStatusCode(pair.getCause(), pair.getLocation());

            if (mapping != null) {
                networkStatusCode = mapping.getNetworkStatusCode();
                if (LOG.isDebugEnabled())
                    LOG.debug("Q.850 cause = " + pair.getCause() +
                            " and Q.850 location = " + pair.getLocation() +
                            " are mapped to: <network status code = " +
                            networkStatusCode +
                            ">, <mappingName = " + mapping.getMappingName() + ">");
            } else {
                if (LOG.isDebugEnabled())
                    LOG.debug("Q.850 cause = " + pair.getCause() +
                            " and Q.850 location = " + pair.getLocation() +
                            ": No network status code configured. " +
                            "Default network status code " +
                            networkStatusCode + " is used.");
            }

        }
        return networkStatusCode;
    }

    //=========================== Static methods =========================

    /**
     * Returns a {@link ReleaseCauseMapping} containing the default mapping.
     * @return The default mapping.
     */
    public static ReleaseCauseMapping getDefaultReleaseCauseMappings() {

        ReleaseCauseMapping mapping = new ReleaseCauseMapping();
        mapping.setSipResponseCodeMapping(
                SipResponseCodeMapping.getDefaultReleaseCauseMappings());
        mapping.setQ850CauseLocationMapping(
                Q850CauseLocationMapping.getDefaultReleaseCauseMappings());
        return mapping;
    }

    /**
     * Parses the configuration for release cause mappings and returns a
     * {@link ReleaseCauseMapping}.
     * @param releaseCauseMappingGroup
     * @return Returns the parsed release cause mappings.
     * If the configuration could not be parsed, the default release
     * cause mappings are returned instead.
     */
    public static ReleaseCauseMapping parseReleaseCauseMappings(
            IGroup configGroup) {

        ReleaseCauseMapping releaseCauseMapping = new ReleaseCauseMapping();

        try {
            // Retrieve the list of mappings
            Map<String, Map<String, String>> mappings = configGroup.getTable(ReleaseCauseConstants.CONF_RELEASE_CAUSE_MAPPINGS_TABLE);

            if (mappings != null) {
                // Make sure the list of mappings are reversed before parsing it.
                // The reason for reversing it is that the first mapping in the
                // configuration has the highest priority. Thus it is handled
                // last so that if there are conflicts, the highest prioritized
                // value gets stored last.
                //  Collections.reverse(mappings);

            	// MIO 2.0 - DP19
            	// No reverse will be done because the notion of priority was introduced.
            	// The networkStatusCode corresponding to the lowest priority number is considered.
            	// A null value is possible for q850LocationIntervals and it should not be considered as being the interval 0-15.

                // Retrieve the default Network Status Code
                Integer defaultNetworkStatusCode = configGroup.getInteger(ReleaseCauseConstants.CONF_DEFAULT_NSC);
                releaseCauseMapping.setDefaultNetworkStatusCode(defaultNetworkStatusCode);

                // Retrieve the default Network Status Code
                Integer noLicensetNetworkStatusCode = configGroup.getInteger(ReleaseCauseConstants.CONF_NO_LICENSE_NSC);
                releaseCauseMapping.setNoLicenseNetworkStatusCode(noLicensetNetworkStatusCode);

                Iterator<String> it = mappings.keySet().iterator();
                // For each mapping, retrieve the mapping parameters
                while (it.hasNext()) {

                    // Retrieve mapping name
                    String name = it.next();

                    // Retrieve Network Status Code
                    Integer networkStatusCode = Integer.parseInt(mappings.get(name).get(
                            ReleaseCauseConstants.CONF_NSC));

                    // Retrieve SIP response code intervals
                    Collection<IntegerInterval> responseCodeIntervals =
                            IntegerInterval.parseIntegerIntervals(mappings.get(name).get(
                                    ReleaseCauseConstants.CONF_RESPONSE_CODE));

                    // Retrieve Q.850 cause intervals
                    Collection<IntegerInterval> q850CauseIntervals =
                            IntegerInterval.parseIntegerIntervals(mappings.get(name).get(
                                    ReleaseCauseConstants.CONF_Q850_CAUSE));

                    // Retrieve Q.850 location intervals
                    Collection<IntegerInterval> q850LocationIntervals = null;
                    //this is an optional value, so we'll check if it is null
                    String q850Location = mappings.get(name).get(ReleaseCauseConstants.CONF_Q850_LOCATION);
                    if (q850Location != null) {
                        q850LocationIntervals = IntegerInterval.parseIntegerIntervals(q850Location);
                    }

                    int q850Priority = Integer.parseInt(mappings.get(name).get(ReleaseCauseConstants.CONF_Q850_PRIORITY));

                    releaseCauseMapping.addMapping(
                            name, networkStatusCode, responseCodeIntervals,
                            q850CauseIntervals, q850LocationIntervals, q850Priority);
                }
            } else {
                LOG.warn("Error when retrieving release cause mappings from " +
                        "configuration. Default release cause mapping is used " +
                        "instead.");
                releaseCauseMapping = null;

            }
        } catch (Exception e) {
            LOG.warn("Error when retrieving release cause mappings from " +
                    "configuration. Default release cause mapping is used " +
                    "instead. Error: " + e.getMessage());
            releaseCauseMapping = null;
        }

        return releaseCauseMapping;
    }


    //=========================== Private methods =========================

    /**
     * Private constructor. The only way to create a
     * {@link ReleaseCauseMapping} is to parse a configuration using
     * {@link ReleaseCauseMapping#parseReleaseCauseMappings(
     * com.mobeon.common.configuration.IGroup)} or to create the default mapping
     * using {@link ReleaseCauseMapping#getDefaultReleaseCauseMappings()}
     */
    private ReleaseCauseMapping() {
    }

    /**
     * Sets the SIP response code mapping.
     * <br>
     * Private setter used when creating a {@link ReleaseCauseMapping} from
     * configuration.
     * @param mapping The response code mapping parsed from the configuration.
     */
    private void setSipResponseCodeMapping(SipResponseCodeMapping mapping) {
        this.sipResponseCodeMapping = mapping;
    }

    /**
     * Sets the Q.850 cause/location mapping.
     * <br>
     * Private setter used when creating a {@link ReleaseCauseMapping} from
     * configuration.
     * @param mapping The q.850 cause/location mapping parsed from the
     * configuration.
     */
    private void setQ850CauseLocationMapping(Q850CauseLocationMapping mapping) {
        this.q850CauseLocationMapping = mapping;
    }

    /**
     * Sets the default network status code.
     * <br>
     * Private setter used when creating a {@link ReleaseCauseMapping} from
     * configuration.
     * @param defaultNetworkStatusCode
     * The default network status code parsed from the configuration.
     */
    private void setDefaultNetworkStatusCode(int defaultNetworkStatusCode) {
        this.defaultNetworkStatusCode = defaultNetworkStatusCode;
    }

    /**
     * Sets the default network status code.
     * <br>
     * Private setter used when creating a {@link ReleaseCauseMapping} from
     * configuration.
     * @param defaultNetworkStatusCode
     * The default network status code parsed from the configuration.
     */
    private void setNoLicenseNetworkStatusCode(int noLicenseNetworkStatusCode) {
        this.noLicenseNetworkStatusCode = noLicenseNetworkStatusCode;
    }

    /**
     * Adds a release cause mappingfrom the <param>responseCodeIntervals</param>,
     * <param>q850CauseIntervals</param> and <param>q850LocationIntervals</param>
     * to a <param>networkStatusCode</param>. Each mapping has a specific
     * <param>name</param> that identifies the mapping.
     * <p>
     * {@link IllegalArgumentException} is thrown if <param>name</param> is
     * null or if <param>networkStatusCode</param> is out-of-range.
     * <br>
     * If <param>responseCodeIntervals</param>, <param>q850CauseIntervals</param>
     * or <param>q850LocationIntervals</param> contains an interval out-of-range,
     * that interval is ignored and a warning log is printed out.
     * <br>
     * If <param>q850LocationIntervals</param> is null, the release cause
     * mapping shall be made for all location values.
     *
     * @param name                  The name of the mapping scenario.
     *
     * @param networkStatusCode     Value range:
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_MIN}-
     * {@link ReleaseCauseConstants.NETWORK_STATUS_CODE_MAX}.
     *
     * @param responseCodeIntervals One or more intervals within the value range:
     * {@link ReleaseCauseConstants.SIP_RESPONSE_CODE_MIN}-
     * {@link ReleaseCauseConstants.SIP_RESPONSE_CODE_MAX}.
     *
     * @param q850CauseIntervals    One or more intervals within the value range:
     * {@link ReleaseCauseConstants.Q850_CAUSE_MIN}-
     * {@link ReleaseCauseConstants.Q850_CAUSE_MAX}.
     *
     * @param q850LocationIntervals Zero or more intervals within the value range:
     * {@link ReleaseCauseConstants.Q850_LOCATION_MIN}-
     * {@link ReleaseCauseConstants.Q850_LOCATION_MAX}.
     * If the location interval is null, the release cause mapping shall be
     * made for all location values.
     *
     * @throws IllegalArgumentException
     * {@link IllegalArgumentException} is thrown if <param>name</param> is
     * null or if <param>networkStatusCode</param> is out-of-range.
     */
    private void addMapping(String name, Integer networkStatusCode,
                            Collection<IntegerInterval> responseCodeIntervals,
                            Collection<IntegerInterval> q850CauseIntervals,
                            Collection<IntegerInterval> q850LocationIntervals,
                            int q850Priority) {

        if ((name == null) ||
                (networkStatusCode < ReleaseCauseConstants.NETWORK_STATUS_CODE_MIN) ||
                (networkStatusCode > ReleaseCauseConstants.NETWORK_STATUS_CODE_MAX)) {
            throw new IllegalArgumentException("Illegal mapping parameter. " +
                    "Name=" + name + ", NetworkStatusCode=" + networkStatusCode);
        }

        MappedNetworkStatusCode mappedNSC =
                new MappedNetworkStatusCode(name, networkStatusCode, q850Priority);
        sipResponseCodeMapping.addMapping(mappedNSC, responseCodeIntervals);
        q850CauseLocationMapping.addMapping(
                mappedNSC, q850CauseIntervals, q850LocationIntervals);
    }
}
