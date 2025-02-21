package com.mobeon.masp.callmanager.releasecausemapping;

import junit.framework.*;

import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.configuration.IntegerInterval;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Collection;

/**
 * Q850CauseLocationMapping Tester.
 *
 * @author Malin Nyfeldt
 */
public class Q850CauseLocationMappingTest extends TestCase
{
    Q850CauseLocationMapping mapping;

    private final MappedNetworkStatusCode aMappedNsc =
            new MappedNetworkStatusCode("name", 600);

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

        mapping = new Q850CauseLocationMapping();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }


    //=========================== Positive tests =========================

    /**
     * Verifies that there are no mappings in a newly created
     * {@link Q850CauseLocationMapping}.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testEmptyMapping() throws Exception {
        assertNoMapping(new Q850CauseLocationMapping(), "0-127", "0-15");
        assertNoMapping(new Q850CauseLocationMapping(), "0-127");
    }


    /**
     * This method creates a new non-default mapping.
     * It is verified that a mapping can be retrieved for the
     * cause/location values that have been mapped and that no mapping can be
     * retrieved for cause/location values that have not been mapped.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testNonDefaultMapping() throws Exception {
        MappedNetworkStatusCode     mappedNSC;
        Collection<IntegerInterval> causeInterval;
        Collection<IntegerInterval> locationInterval;

        // Add first mapping: cause=0-50, location=0-10, NSC=600
        mappedNSC = new MappedNetworkStatusCode("first mapping", 600);
        causeInterval = IntegerInterval.parseIntegerIntervals("0-100");
        locationInterval = IntegerInterval.parseIntegerIntervals("0-10");
        mapping.addMapping(mappedNSC, causeInterval, locationInterval);

        // Add second mapping: cause=100-127, location=11-15, NSC=699
        mappedNSC = new MappedNetworkStatusCode("second mapping", 699);
        causeInterval = IntegerInterval.parseIntegerIntervals("100-126");
        locationInterval = IntegerInterval.parseIntegerIntervals("11-15");
        mapping.addMapping(mappedNSC, causeInterval, locationInterval);

        // Add third mapping: cause=127, location=null, NSC=650
        mappedNSC = new MappedNetworkStatusCode("third mapping", 650);
        causeInterval = IntegerInterval.parseIntegerIntervals("127");
        mapping.addMapping(mappedNSC, causeInterval, null);

        // Verify the mappings
        assertMapping(mapping, "first mapping", 600, "0-100", "0-10");
        assertMapping(mapping, "second mapping", 699, "100-126", "11-15");
        assertMapping(mapping, "third mapping", 650, "127", "0-15");
        assertMapping(mapping, "third mapping", 650, "127");
        assertNoMapping(mapping, "0-99", "11-15");
        assertNoMapping(mapping, "101-126", "0-10");
        assertNoMapping(mapping, "0-126");
    }

    /**
     * Verifies the default Q.850 cause/location mapping.
     * The following mapping is verified:
     * <br>
     * Busy: cause=17 location=1-15 + unknown NSC=603
     * <br>
     * No Reply: cause=18-19 location=any NSC=610
     * <br>
     * Not Reachable: cause=1-9,20-23,25-31 location=any NSC=613
     * <br>
     * Suppressed: cause=17 location=0 NSC=614
     * <br>
     * Congestion: cause=39-44,46 location=any NSC=620
     * <p>
     * For all other cause/location combinations it is verified that the
     * mapping is null.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testDefaultMapping() throws Exception {
        Q850CauseLocationMapping defaultMapping =
                Q850CauseLocationMapping.getDefaultReleaseCauseMappings();

        assertMapping(defaultMapping, "busy", 603, "17", "1-15");
        assertMapping(defaultMapping, "noreply", 610, "18-19", "0-15");
        assertMapping(defaultMapping, "notreachable", 613, "1-9,20-23,25-31", "0-15");
        assertMapping(defaultMapping, "suppressed", 614, "17", "0");
        assertMapping(defaultMapping, "congestion", 620, "39-44,46", "0-15");
        assertNoMapping(defaultMapping, "0,10-16,24,32-38,45,47-127", "0-15");

        assertMapping(defaultMapping, "busy", 603, "17");
        assertMapping(defaultMapping, "noreply", 610, "18-19");
        assertMapping(defaultMapping, "notreachable", 613, "1-9,20-23,25-31");
        assertMapping(defaultMapping, "congestion", 620, "39-44,46");
        assertNoMapping(defaultMapping, "0,10-16,24,32-38,45,47-127");
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
            mapping.addMapping(null,
                    IntegerInterval.parseIntegerIntervals("1"),
                    IntegerInterval.parseIntegerIntervals("0"));
            fail("Exception not thrown when expected");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Verifies that an {@link IllegalArgumentException} is thrown if the
     * cause interval is null when adding a mapping.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testAddMappingWhenCauseIntervalIsNull() throws Exception {
        try {
            mapping.addMapping(aMappedNsc,
                    null,
                    IntegerInterval.parseIntegerIntervals("0"));
            fail("Exception not thrown when expected");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Verifies that an invalid cause interval is ignored when adding a mapping.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testAddMappingWhenCauseIntervalIsInvalid() throws Exception {
        mapping.addMapping(aMappedNsc,
                IntegerInterval.parseIntegerIntervals("10-9"),
                IntegerInterval.parseIntegerIntervals("0"));
        assertNoMapping(mapping, "0-127", "0-15");
    }

    /**
     * Verifies that a cause interval out-of-range is ignored when adding a
     * mapping.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testAddMappingWhenCauseIntervalIsOutOfRange() throws Exception {
        mapping.addMapping(aMappedNsc,
                IntegerInterval.parseIntegerIntervals("0-128"),
                IntegerInterval.parseIntegerIntervals("0"));
        mapping.addMapping(aMappedNsc,
                IntegerInterval.parseIntegerIntervals("130-139"),
                IntegerInterval.parseIntegerIntervals("0"));
        mapping.addMapping(aMappedNsc,
                IntegerInterval.parseIntegerIntervals("140"),
                IntegerInterval.parseIntegerIntervals("0"));

        assertNoMapping(mapping, "0-127", "0-15");
    }

    /**
     * Verifies that an invalid location interval is ignored when adding a mapping.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testAddMappingWhenLocationIntervalIsInvalid() throws Exception {
        mapping.addMapping(aMappedNsc,
                IntegerInterval.parseIntegerIntervals("1"),
                IntegerInterval.parseIntegerIntervals("10-9"));
        assertNoMapping(mapping, "0-127", "0-15");
    }

    /**
     * Verifies that a location interval out-of-range is ignored when adding a
     * mapping.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testAddMappingWhenLocationIntervalIsOutOfRange() throws Exception {
        mapping.addMapping(aMappedNsc,
                IntegerInterval.parseIntegerIntervals("0"),
                IntegerInterval.parseIntegerIntervals("14-16"));
        mapping.addMapping(aMappedNsc,
                IntegerInterval.parseIntegerIntervals("1"),
                IntegerInterval.parseIntegerIntervals("19-23"));
        mapping.addMapping(aMappedNsc,
                IntegerInterval.parseIntegerIntervals("2"),
                IntegerInterval.parseIntegerIntervals("100"));

        assertNoMapping(mapping, "0-127", "0-15");
    }

    /**
     * Verifies that null is returned when retrieving the network status code
     * for a cause value out-of-range.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testGetMappingWhenCauseIsOutOfRange() throws Exception {

        Q850CauseLocationMapping defaultMapping =
                Q850CauseLocationMapping.getDefaultReleaseCauseMappings();
        assertNull(defaultMapping.getMappedNetworkStatusCode(128, 0));
        assertNull(defaultMapping.getMappedNetworkStatusCode(130, 0));
        assertNull(defaultMapping.getMappedNetworkStatusCode(-1, 0));
    }

    /**
     * Verifies that null is returned when retrieving the network status code
     * for a location value out-of-range.
     *
     * @throws Exception Exception is thrown if test case fails.
     */
    public void testGetMappingWhenLocationIsOutOfRange() throws Exception {

        Q850CauseLocationMapping defaultMapping =
                Q850CauseLocationMapping.getDefaultReleaseCauseMappings();
        assertNull(defaultMapping.getMappedNetworkStatusCode(1, 16));
        assertNull(defaultMapping.getMappedNetworkStatusCode(2, 100));
        assertNull(defaultMapping.getMappedNetworkStatusCode(3, -1));
    }


    //=========================== Private methods =========================

    /**
     * For each cause and location combination listed in
     * <param>causeIntervals</param> and <param>locationIntervals</param>, this
     * method verifies that the q850 cause/location <param>mapping</param>
     * contains a mapping from the combination to the given network status code
     * <param>nsc</param> with the given mapping name <param>nscName</param>.
     *
     * @param mapping           The Q.850 cause/location mapping
     * @param nscName           The mapping name.
     * @param nsc               The network status code.
     * @param causeIntervals    The Q.850 cause intervals.
     * @param locationIntervals The Q.850 location intervals.
     *
     * @throws Exception An exception is thrown if expected mapping does not exist.
     */
    private void assertMapping(Q850CauseLocationMapping mapping,
                               String nscName, int nsc,
                               String causeIntervals, String locationIntervals)
            throws Exception {
        Collection<IntegerInterval> cIntervals =
                IntegerInterval.parseIntegerIntervals(causeIntervals);
        Collection<IntegerInterval> lIntervals =
                IntegerInterval.parseIntegerIntervals(locationIntervals);

        for (IntegerInterval causeInterval : cIntervals) {
            for (int i = causeInterval.getStart();
                 i <= causeInterval.getEnd(); i++) {
                for (IntegerInterval locationInterval : lIntervals) {
                    for (int j = locationInterval.getStart();
                         j <= locationInterval.getEnd(); j++) {
                        MappedNetworkStatusCode mappedNSC =
                                mapping.getMappedNetworkStatusCode(i, j);
                        // Verify mapping name and NSC
                        assertEquals(nscName, mappedNSC.getMappingName());
                        assertEquals(nsc, mappedNSC.getNetworkStatusCode());
                    }
                }
            }

        }
    }

    /**
     * For each cause listed in <param>causeIntervals</param>, this
     * method verifies that the <param>mapping</param> contains a mapping from
     * the cause value to the given network status code
     * <param>nsc</param> with the given mapping name <param>nscName</param>.
     *
     * @param mapping           The Q.850 cause/location mapping
     * @param nscName           The mapping name.
     * @param nsc               The network status code.
     * @param causeIntervals    The Q.850 cause intervals.
     *
     * @throws Exception An exception is thrown if expected mapping does not exist.
     */
    private void assertMapping(Q850CauseLocationMapping mapping,
                               String nscName, int nsc, String causeIntervals)
            throws Exception {
        Collection<IntegerInterval> cIntervals =
                IntegerInterval.parseIntegerIntervals(causeIntervals);

        for (IntegerInterval causeInterval : cIntervals) {
            for (int i = causeInterval.getStart();
                 i <= causeInterval.getEnd(); i++) {
                MappedNetworkStatusCode mappedNSC =
                        mapping.getMappedNetworkStatusCode(i, null);
                // Verify mapping name and NSC
                assertEquals(nscName, mappedNSC.getMappingName());
                assertEquals(nsc, mappedNSC.getNetworkStatusCode());
            }

        }
    }

    /**
     * For each cause and location combination listed in
     * <param>causeIntervals</param> and <param>locationIntervals</param>, this
     * method verifies that the q850 cause/location <param>mapping</param>
     * is null.
     *
     * @param mapping           The Q.850 cause/location mapping
     * @param causeIntervals    The Q.850 cause intervals.
     * @param locationIntervals The Q.850 location intervals.
     *
     * @throws Exception An exception is thrown if a mapping exists.
     */
    private void assertNoMapping(Q850CauseLocationMapping mapping,
                                 String causeIntervals, String locationIntervals)
            throws Exception {
        Collection<IntegerInterval> cIntervals =
                IntegerInterval.parseIntegerIntervals(causeIntervals);
        Collection<IntegerInterval> lIntervals =
                IntegerInterval.parseIntegerIntervals(locationIntervals);

        for (IntegerInterval causeInterval : cIntervals) {
            for (int i = causeInterval.getStart();
                 i <= causeInterval.getEnd(); i++) {
                for (IntegerInterval locationInterval : lIntervals) {
                    for (int j = locationInterval.getStart();
                         j <= locationInterval.getEnd(); j++) {
                        assertNull(mapping.getMappedNetworkStatusCode(i, j));
                    }
                }
            }

        }
    }

    /**
     * For each cause value listed in <param>causeIntervals</param>, this
     * method verifies that the q850 cause <param>mapping</param> is null.
     *
     * @param mapping           The Q.850 cause/location mapping
     * @param causeIntervals    The Q.850 cause intervals.
     *
     * @throws Exception An exception is thrown if a mapping exists.
     */
    private void assertNoMapping(Q850CauseLocationMapping mapping,
                                 String causeIntervals)
            throws Exception {
        Collection<IntegerInterval> cIntervals =
                IntegerInterval.parseIntegerIntervals(causeIntervals);

        for (IntegerInterval causeInterval : cIntervals) {
            for (int i = causeInterval.getStart();
                 i <= causeInterval.getEnd(); i++) {
                assertNull(mapping.getMappedNetworkStatusCode(i, null));
            }

        }
    }
}
