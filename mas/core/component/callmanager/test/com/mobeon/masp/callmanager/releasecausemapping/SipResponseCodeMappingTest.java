/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.releasecausemapping;

import junit.framework.*;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.configuration.IntegerInterval;

import java.util.Collection;

/**
 * SipResponseCodeMapping Tester.
 *
 * @author Malin Nyfeldt
 */
public class SipResponseCodeMappingTest extends TestCase
{
    SipResponseCodeMapping mapping;

    private final MappedNetworkStatusCode aMappedNsc =
            new MappedNetworkStatusCode("name", 600);

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

        mapping = new SipResponseCodeMapping();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    //=========================== Positive tests =========================

    /**
     * Verifies that there are no mappings in a newly created
     * {@link SipResponseCodeMapping}.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testEmptyMapping() throws Exception {
        assertNoMapping(new SipResponseCodeMapping(), "300-699");
    }

    /**
     * This method creates a new non-default mapping.
     * It is verified that a mapping can be retrieved for the
     * SIP response codes that have been mapped and that no mapping can be
     * retrieved for SIP response codes that have not been mapped.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testNonDefaultMapping() throws Exception {
        MappedNetworkStatusCode     mappedNSC;
        Collection<IntegerInterval> responseCodeInterval;

        // Add first mapping: SIP response code = 300-500, NSC=600
        mappedNSC = new MappedNetworkStatusCode("first mapping", 600);
        responseCodeInterval = IntegerInterval.parseIntegerIntervals("300-500");
        mapping.addMapping(mappedNSC, responseCodeInterval);

        // Add second mapping: SIP response code = 699, NSC=699
        mappedNSC = new MappedNetworkStatusCode("second mapping", 699);
        responseCodeInterval = IntegerInterval.parseIntegerIntervals("699");
        mapping.addMapping(mappedNSC, responseCodeInterval);

        // Verify the mappings
        assertMapping(mapping, "first mapping", 600, "300-500");
        assertMapping(mapping, "second mapping", 699, "699");
        assertNoMapping(mapping, "501-698");
    }


    /**
     * Verifies the default SIP response code mapping.
     * The following mapping is verified:
     * <br>
     * Busy: response code = 486   NSC=603
     * <br>
     * No Reply: response code = 408,480   NSC=610
     * <br>
     * Not Reachable: response code = 301,403-404,410,484,501-502,603   NSC=613
     * <br>
     * Suppressed: response code = 600   NSC=614
     * <br>
     * Congestion: response code = 503   NSC=620
     * <p>
     * For all other SIP response codes it is verified that the mapping is null.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testDefaultMapping() throws Exception {
        SipResponseCodeMapping defaultMapping =
                SipResponseCodeMapping.getDefaultReleaseCauseMappings();

        assertMapping(defaultMapping, "busy", 603, "486");
        assertMapping(defaultMapping, "noreply", 610, "408,480");
        assertMapping(defaultMapping, "notreachable", 613,
                "301,403-404,410,484,501-502,603");
        assertMapping(defaultMapping, "suppressed", 614, "600");
        assertMapping(defaultMapping, "congestion", 620, "503");

        assertNoMapping(defaultMapping,
                "300,302-402,405-407,409,411-479,481-483,485,487-500," +
                        "504-599,601-602,604-699");
    }


    //=========================== Negative tests =========================

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown if the
     * mapped network status code is null when adding a mapping.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testAddMappingWhenMappedNscIsNull() throws Exception {
        try {
            mapping.addMapping(null, IntegerInterval.parseIntegerIntervals("300"));
            fail("Exception not thrown when expected");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown if the
     * SIP response code interval is null when adding a mapping.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testAddMappingWhenResponseCodeIntervalIsNull() throws Exception {
        try {
            mapping.addMapping(aMappedNsc, null);
            fail("Exception not thrown when expected");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Verifies that an invalid SIP response code interval is ignored when
     * adding a mapping.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testAddMappingWhenResponseCodeIntervalIsInvalid() throws Exception {
        mapping.addMapping(aMappedNsc,
                IntegerInterval.parseIntegerIntervals("390-380"));
        assertNoMapping(mapping, "300-699");
    }

    /**
     * Verifies that a SIP response code interval out-of-range is ignored when
     * adding a mapping.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testAddMappingWhenResponseCodeIntervalIsOutOfRange() throws Exception {
        mapping.addMapping(aMappedNsc,
                IntegerInterval.parseIntegerIntervals("350-700"));
        mapping.addMapping(aMappedNsc,
                IntegerInterval.parseIntegerIntervals("299-301"));
        mapping.addMapping(aMappedNsc,
                IntegerInterval.parseIntegerIntervals("100"));
        mapping.addMapping(aMappedNsc,
                IntegerInterval.parseIntegerIntervals("800"));

        assertNoMapping(mapping, "300-699");
    }

    /**
     * Verifies that null is returned when retrieving the network status code
     * for a SIP response code out-of-range.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testGetMappingWhenResponseCodeIsOutOfRange() throws Exception {

        SipResponseCodeMapping defaultMapping =
                SipResponseCodeMapping.getDefaultReleaseCauseMappings();
        assertNull(defaultMapping.getMappedNetworkStatusCode(299));
        assertNull(defaultMapping.getMappedNetworkStatusCode(700));
        assertNull(defaultMapping.getMappedNetworkStatusCode(-1));
        assertNull(defaultMapping.getMappedNetworkStatusCode(800));
    }

    //=========================== Private methods =========================

    /**
     * For each SIP response code listed in <param>responseCodeIntervals</param>
     * this method verifies that <param>mapping</param> contains a mapping from
     * the SIP response code to the given network status code
     * <param>nsc</param> with the given mapping name <param>nscName</param>.
     *
     * @param mapping               The SIP response code mapping.
     * @param nscName               The mapping name.
     * @param nsc                   The network status code.
     * @param responseCodeIntervals The SIP response code intervals.
     *
     * @throws Exception An exception is thrown if expected mapping does not exist.
     */
    private void assertMapping(SipResponseCodeMapping mapping,
                               String nscName, int nsc,
                               String responseCodeIntervals)
            throws Exception {
        Collection<IntegerInterval> intervals =
                IntegerInterval.parseIntegerIntervals(responseCodeIntervals);

        for (IntegerInterval interval : intervals) {
            for (int i = interval.getStart(); i <= interval.getEnd(); i++) {
                MappedNetworkStatusCode mappedNSC =
                        mapping.getMappedNetworkStatusCode(i);
                // Verify mapping name and NSC
                assertEquals(nscName, mappedNSC.getMappingName());
                assertEquals(nsc, mappedNSC.getNetworkStatusCode());
            }

        }
    }

    /**
     * For each SIP response code listed in <param>responseCodeIntervals</param>
     * this method verifies that the SIP response code <param>mapping</param>
     * is null.
     *
     * @param mapping               The SIP response code mapping.
     * @param responseCodeIntervals The SIP response code intervals.
     *
     * @throws Exception An exception is thrown if a mapping exists.
     */
    private void assertNoMapping(SipResponseCodeMapping mapping,
                                 String responseCodeIntervals)
            throws Exception {
        Collection<IntegerInterval> intervals =
                IntegerInterval.parseIntegerIntervals(responseCodeIntervals);

        for (IntegerInterval interval : intervals)
            for (int i = interval.getStart(); i <= interval.getEnd(); i++)
                assertNull(mapping.getMappedNetworkStatusCode(i));
    }
}
