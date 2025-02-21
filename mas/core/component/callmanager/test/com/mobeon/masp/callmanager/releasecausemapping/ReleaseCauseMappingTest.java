/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.releasecausemapping;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.CallManagerLicensingMock;
import com.mobeon.masp.callmanager.configuration.*;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.UnknownGroupException;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.*;

/**
 * ReleaseCauseMapping Tester.
 *
 * @author Malin Nyfeldt
 */
public class ReleaseCauseMappingTest extends TestCase
{
//    private Mock mockedGroup = mock(IGroup.class);
    private List<IGroup> groups = new ArrayList<IGroup>();

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

       // groups.add((IGroup)mockedGroup.proxy());
        CMUtils.getInstance().setCallManagerLicensing(new CallManagerLicensingMock());

        // Create a configuration manager and read the configuration file
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile("cfg/callManager.conf");
        ConfigurationReader.getInstance().setInitialConfiguration(
                cm.getConfiguration());
        ConfigurationReader.getInstance().update();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    //=========================== Positive tests =========================

    /**
     * Verifies that the SIP response code mappings can be retrieved correctly
     * from the configuration.
     * @throws Exception if test case fails.
     */
    public void testParseReleaseCauseMappings() throws Exception {
        ReleaseCauseMapping mapping =
                ConfigurationReader.getInstance().getConfig().
                        getReleaseCauseMapping();

        // Verify SIP response code mappings

        // Suppressed mapping
        assertResponseCodeMapping(mapping, 614, "600");

        // Busy mapping
        assertResponseCodeMapping(mapping, 603, "486");

        // No reply mapping
        assertResponseCodeMapping(mapping, 610, "408,480");

        // Not reachable mapping
        assertResponseCodeMapping(mapping, 613, "301,403-404,410,484,501-502,603");

        // Test mapping
        assertResponseCodeMapping(mapping, 620, "503");

        // Not mapped, so the default networkStatusCode is used
        assertResponseCodeMapping(mapping, 621,
                "300,302-402,411-479,505-507,518-598,608-698");


        // Verify Q.850 cause/location mappings

        // Suppressed mapping
        assertQ850CauseLocationMapping(mapping, 614, "17", "0");

        // Busy mapping
        assertQ850CauseLocationMapping(mapping, 603, "17", "1-15");
        assertQ850CauseMapping(mapping, 603, "17");

        // No reply mapping
        assertQ850CauseLocationMapping(mapping, 610, "18-19", "0-15");
        assertQ850CauseMapping(mapping, 610, "18-19");

        // Not reachable mapping
        assertQ850CauseLocationMapping(mapping, 613, "1-9,20-23,25-31", "1-2,5-6");
        assertQ850CauseMapping(mapping, 613, "1-9,20-23,25-31");

        // Test mapping
        assertQ850CauseLocationMapping(mapping, 620, "39-44,46", "0-15");
        assertQ850CauseMapping(mapping, 620, "39-44,46");

        // Not mapped
        assertQ850CauseLocationMapping(mapping, 621,
                "10-16,24,32-38,45,47-127", "0-15");
        assertQ850CauseMapping(mapping, 621, "10-16,24,32-38,45,47-127");

    }

    /**
     * Verifies the default mapping of SIP response codes.
     * @throws Exception if test case fails.
     */
    public void testGetDefaultReleaseCauseMappings() throws Exception {
        ReleaseCauseMapping mapping =
                ReleaseCauseMapping.getDefaultReleaseCauseMappings();

        // Verify SIP response code mappings

        // Suppressed mapping
        assertResponseCodeMapping(mapping, 614, "600");

        // Busy mapping
        assertResponseCodeMapping(mapping, 603, "486");

        // No reply mapping
        assertResponseCodeMapping(mapping, 610, "408,480");

        // Not reachable mapping
        assertResponseCodeMapping(mapping, 613,
                "301,403-404,410,484,501-502,603");

        // Congestion mapping
        assertResponseCodeMapping(mapping, 620, "503");

        // Not mapped
        assertResponseCodeMapping(mapping, 621,
                "300,302-402,405-407,411-479,481-483,485,487-500,504-599,601-602,604-699");


        // Verify Q.850 cause/location mappings

        // Suppressed mapping
        assertQ850CauseLocationMapping(mapping, 614, "17", "0");

        // Busy mapping
        assertQ850CauseLocationMapping(mapping, 603, "17", "1-15");
        assertQ850CauseMapping(mapping, 603, "17");

        // No reply mapping
        assertQ850CauseLocationMapping(mapping, 610, "18-19", "0-15");
        assertQ850CauseMapping(mapping, 610, "18-19");

        // Not reachable mapping
        assertQ850CauseLocationMapping(mapping, 613, "1-9,20-23,25-31", "0-15");
        assertQ850CauseMapping(mapping, 613, "1-9,20-23,25-31");

        // Test mapping
        assertQ850CauseLocationMapping(mapping, 620, "39-44,46", "0-15");
        assertQ850CauseMapping(mapping, 620, "39-44,46");

        // Not mapped
        assertQ850CauseLocationMapping(mapping, 621,
                "0,10-16,24,32-38,45,47-127", "0-15");
        assertQ850CauseMapping(mapping, 621, "0,10-16,24,32-38,45,47-127");
    }

    /**
     * Verifies that when retrieving the network status code from both a
     * SIP response code and a Q.850 cause/location pair, the network status
     * code is retrieved in the following order:
     * <ul>
     * <li>If the Q.850 pair is not null, it is mapped to a network status
     * code.</li>
     * <li>Otherwise, if the SIP response code is not null, it is mapped to a
     * network status code.</li>
     * <li>Otherwise, the default network status code is returned.</li>
     * </ul>
     * @throws Exception if test case fails.
     */
    public void testGetNetworkStatusCodeForResponseCodeAndPair() throws Exception {
        ReleaseCauseMapping mapping =
                ReleaseCauseMapping.getDefaultReleaseCauseMappings();

        Integer sipResponseCode = 486;
        Q850CauseLocationPair q850Pair = new Q850CauseLocationPair(17, 0);

        // Q.850 pair is NOT null => map Q.850 pair
        assertEquals(614, mapping.getNetworkStatusCode(sipResponseCode, q850Pair));
        assertEquals(614, mapping.getNetworkStatusCode(null, q850Pair));

        // Q.850 pair is null => map SIP response code
        assertEquals(603, mapping.getNetworkStatusCode(sipResponseCode, null));

        // Both are null null => default Network Status Code is returned
        assertEquals(621, mapping.getNetworkStatusCode(null, null));
    }


    //=========================== Negative tests =========================

    /**
     * Verifies that the default network status code is returned when
     * retrieving network status code for a Q.850 cause/location pair that is
     * null.
     * @throws Exception if test case fails.
     */
    public void testGetReleaseCauseMappingsForNullQ850Pair()
            throws Exception {
        ReleaseCauseMapping mapping = ReleaseCauseMapping.
                getDefaultReleaseCauseMappings();

        assertEquals(621, mapping.getNetworkStatusCode(null));
    }

    /**
     * Verifies that null is returned when an exception is thrown while
     * parsing the release cause mappings.
     * @throws Exception if test case fails.
     */
  /*  public void testParseReleaseCauseMappingsWhenExceptionIsThrown()
            throws Exception {
        mockedGroup.stubs().method("getGroups").will(throwException(
                new UnknownGroupException("Error", null)));

        ReleaseCauseMapping mapping = ReleaseCauseMapping.
                parseReleaseCauseMappings((IGroup)mockedGroup.proxy());

        assertNull(mapping);
    } */

    /**
     * Verifies that null is returned when getting configuration mappings
     * returns null.
     * @throws Exception if test case fails.
     */
  /*  public void testParseReleaseCauseMappingsWhenConfigReturnsNull()
            throws Exception {
        mockedGroup.stubs().method("getGroups").will(returnValue(null));

        ReleaseCauseMapping mapping = ReleaseCauseMapping.
                parseReleaseCauseMappings((IGroup)mockedGroup.proxy());

        assertNull(mapping);
    }
*/
    /**
     * Verifies that null is returned when the mapping contains no configured
     * name.
     * @throws Exception if test case fails.
     */
  /*  public void testParseReleaseCauseMappingsWhenNameIsNull()
            throws Exception {

        mockedGroup.expects(once()).method("getGroups").will(returnValue(groups));
        setupMappingExpectations(null, 601, "501", "1", "0");

        assertNull(ReleaseCauseMapping.
                parseReleaseCauseMappings((IGroup)mockedGroup.proxy()));
    }
*/
    /**
     * Verifies that null is returned when the network status code is less than
     * 601.
     * @throws Exception if test case fails.
     */
  /*  public void testParseReleaseCauseMappingsWhenNSCIsTooSmall()
            throws Exception {

        mockedGroup.expects(once()).method("getGroups").will(returnValue(groups));
        setupMappingExpectations("name", 600, "501", "1", "0");

        assertNull(ReleaseCauseMapping.
                parseReleaseCauseMappings((IGroup)mockedGroup.proxy()));
    }
*/
    /**
     * Verifies that null is returned when the network status code is larger
     * than 634.
     * @throws Exception if test case fails.
     */
 /*   public void testParseReleaseCauseMappingsWhenNSCIsTooLarge()
            throws Exception {

        mockedGroup.expects(once()).method("getGroups").will(returnValue(groups));
        setupMappingExpectations("name", 635, "501", "1", "0");

        assertNull(ReleaseCauseMapping.
                parseReleaseCauseMappings((IGroup)mockedGroup.proxy()));
    }
*/

    //=========================== Private methods =========================

    /**
     * For each SIP response code listed in <param>responseCodeIntervals</param>
     * this method verifies that <param>mapping</param> contains a mapping from
     * the SIP response code to the given network status code <param>nsc</param>.
     *
     * @param mapping               The release cause mapping.
     * @param nsc                   The network status code.
     * @param responseCodeIntervals The SIP response code intervals.
     *
     * @throws Exception An exception is thrown if expected mapping does not exist.
     */
    private void assertResponseCodeMapping(
            ReleaseCauseMapping mapping,
            int nsc,
            String responseCodeIntervals)
            throws Exception {

        Collection<IntegerInterval> intervals =
                IntegerInterval.parseIntegerIntervals(responseCodeIntervals);

        for (IntegerInterval interval : intervals)
            for (int i = interval.getStart(); i <= interval.getEnd(); i++)
                assertEquals(nsc, mapping.getNetworkStatusCode(i));
    }

    /**
     * For each cause and location combination listed in
     * <param>causeIntervals</param> and <param>locationIntervals</param>, this
     * method verifies that the q850 cause/location <param>mapping</param>
     * contains a mapping from the combination to the given network status code
     * <param>nsc</param>.
     *
     * @param mapping           The Q.850 cause/location mapping
     * @param nsc               The network status code.
     * @param causeIntervals    The Q.850 cause intervals.
     * @param locationIntervals The Q.850 location intervals.
     *
     * @throws Exception An exception is thrown if expected mapping does not exist.
     */
    private void assertQ850CauseLocationMapping(
            ReleaseCauseMapping mapping, int nsc,
            String causeIntervals, String locationIntervals)
            throws Exception {

        Collection<IntegerInterval> cIntervals =
                IntegerInterval.parseIntegerIntervals(causeIntervals);
        Collection<IntegerInterval> lIntervals =
                IntegerInterval.parseIntegerIntervals(locationIntervals);

        for (IntegerInterval causeInterval : cIntervals)
            for (int i = causeInterval.getStart();
                 i <= causeInterval.getEnd(); i++)
                for (IntegerInterval locationInterval : lIntervals)
                    for (int j = locationInterval.getStart();
                         j <= locationInterval.getEnd(); j++)
                        assertEquals(nsc, mapping.getNetworkStatusCode(
                                new Q850CauseLocationPair(i,j)));
    }

    /**
     * For each cause value listed in <param>causeIntervals</param>, this
     * method verifies that the q850 cause <param>mapping</param>
     * contains a mapping to the given network status code <param>nsc</param>.
     *
     * @param mapping           The Q.850 cause/location mapping
     * @param nsc               The network status code.
     * @param causeIntervals    The Q.850 cause intervals.
     *
     * @throws Exception An exception is thrown if expected mapping does not exist.
     */
    private void assertQ850CauseMapping(
            ReleaseCauseMapping mapping, int nsc, String causeIntervals)
            throws Exception {

        Collection<IntegerInterval> cIntervals =
                IntegerInterval.parseIntegerIntervals(causeIntervals);

        for (IntegerInterval causeInterval : cIntervals)
            for (int i = causeInterval.getStart();
                 i <= causeInterval.getEnd(); i++)
                assertEquals(nsc, mapping.getNetworkStatusCode(
                        new Q850CauseLocationPair(i,null)));
    }

    /**
     * Sets up mock expectations for a configured mapping.
     *
     * @param name                  The name.
     * @param nsc                   The network status code.
     * @param responseCodeIntervals The SIP response code intervals.
     * @param causeIntervals        The Q.850 cause intervals.
     * @param locationIntervals     The Q.850 location intervals.
     */
   /* private void setupMappingExpectations(
            String name, Integer nsc, String responseCodeIntervals,
            String causeIntervals, String locationIntervals) {

        mockedGroup.expects(once()).method("getInteger").
                with(eq("defaultnetworkstatuscode")).will(returnValue(621));
        mockedGroup.expects(once()).method("getString").with(eq("name")).
                will(returnValue(name));
        mockedGroup.expects(once()).method("getInteger").
                with(eq("networkstatuscode")).will(returnValue(nsc));
        mockedGroup.expects(once()).method("getString").
                with(eq("sipresponsecodeintervals")).will(
                returnValue(responseCodeIntervals));
        mockedGroup.expects(once()).method("getString").
                with(eq("q850causeintervals")).will(
                returnValue(causeIntervals));
        mockedGroup.expects(once()).method("getString").
                with(eq("q850locationintervals")).will(
                returnValue(locationIntervals));
    }*/

}
