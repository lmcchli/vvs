package com.mobeon.masp.callmanager.loadregulation;

import junit.framework.TestCase;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.loadregulation.states.HighLoadState;
import com.mobeon.masp.callmanager.loadregulation.states.MaxLoadState;
import com.mobeon.masp.callmanager.loadregulation.states.NormalLoadState;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.CallManagerLicensingMock;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.common.configuration.ConfigurationManagerImpl;

/**
 * LoadRegulator Tester.
 *
 * @author Malin Flodin
 */
public class LoadRegulatorTest extends TestCase
{
    // NOTE: There must be a span of at least two between MAX and HWM and
    // between HWM and LWM for the tests below to work.
    private final int MAX = 10;
    private final int HWM = 8;
    private final int LWM = 6;

    private LoadRegulator loadRegulator;

    public LoadRegulatorTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

        // Create a configuration manager and read the configuration file
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile(CallManagerTestContants.CALLMANAGER_XML);
        CMUtils.getInstance().setCallManagerLicensing(new CallManagerLicensingMock());
        ConfigurationReader.getInstance().setInitialConfiguration(cm.getConfiguration());
        ConfigurationReader.getInstance().update();
        // Make sure that ramping is disabled.
        assertConfig(0, 0);
//        loadRegulator = new LoadRegulator();
    }

    private void assertConfig(int highWaterMark, double rampFactor) {
        ConfigurationReader.getInstance().getConfig().setInitialRampHWM(highWaterMark);
        ConfigurationReader.getInstance().getConfig().setRampFactor(rampFactor);
        loadRegulator = new LoadRegulator();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that when adding a call below HWM in state Normal Load, the
     * state remains unchanged and the returned action indicates unchanged
     * traffic.
     * @throws Exception if test case fails.
     */
    public void testAddCallBelowHwmInNormalLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(HWM-2);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);

        // Verify
        action = loadRegulator.addCall(null);
        assertEquals(LoadRegulationAction.UNCHANGED_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
    }

    /**
     * Verifies that when adding a call at HWM in state Normal Load, the
     * state is changed to HighLoad and the returned action indicates
     * stop traffic.
     * @throws Exception if test case fails.
     */
    public void testAddCallAtHwmInNormalLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(HWM-1);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);

        // Verify
        action = loadRegulator.addCall(null);
        assertEquals(LoadRegulationAction.STOP_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);
    }

    //TODO, this will not happen.
//    /**
//     * Verifies that when adding a call at Max in state Normal Load, the
//     * state is changed to MaxLoad and the returned action indicates
//     * stop traffic.
//     * @throws Exception if test case fails.
//     */
//    public void testAddCallAtMaxInNormalLoad() throws Exception {
//        LoadRegulationAction action;
//
//        // Initialize
//        loadRegulator.updateThreshold(1, 0, 1);
//        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
//
//        // Verify
//        action = loadRegulator.addCall();
//        assertEquals(LoadRegulationAction.STOP_TRAFFIC, action);
//        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);
//    }

    /**
     * Verifies that when adding a call below Max in state HighLoad, the
     * state remains unchanged and the returned action indicates unchanged
     * traffic.
     * @throws Exception if test case fails.
     */
    public void testAddCallBelowMaxInHighLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(HWM);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);

        // Verify
        action = loadRegulator.addCall(null);
        assertEquals(LoadRegulationAction.UNCHANGED_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);
    }

    /**
     * Verifies that when adding a call at Max in state HighLoad, the
     * state is changed to MaxLoad and the returned action indicates
     * unchanged traffic.
     * @throws Exception if test case fails.
     */
    public void testAddCallAtMaxInHighLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(MAX - 1);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);

        // Verify
        action = loadRegulator.addCall(null);
        assertEquals(LoadRegulationAction.UNCHANGED_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);
    }

    /**
     * Verifies that when adding a call in state MaxLoad, the
     * state remains unchanged and the returned action indicates that
     * traffic should be redirected.
     * @throws Exception if test case fails.
     */
    public void testAddCallInMaxLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(MAX);
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);

        // Verify
        action = loadRegulator.addCall(null);
        assertEquals(LoadRegulationAction.REDIRECT_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);
    }

    /**
     * Test the ramping of threshold during startup phase.
     * <pre>
     * Each time LowWaterMark is reached, the threshold values are increased,
     * until the configured values are met.
     * <p/>
     * 1. Reach high water mark.
     *  Condition:
     *      MAX, HWM and LWM are configured to 10, 8 and 6.
     *      The ramp factor is 1. The dynamic dHWM starts at 1, but will
     *      be increased to 2 as soon as updateThreshold is called (since
     *      current calls is below dLWM).
     *  Action:
     *      Add two calls.
     *  Result:
     *      HighWaterMark is reached. The state is set to HighLoadState
     *      and call manager should unregister in SSP.
     * <p/>
     * 2. Reach low water mark.
     *  Condition:
     *      As in 1. Two calls are added.
     *  Action:
     *      Remove two calls.
     *  Result:
     *      LowWaterMark is reached. State is set to NormalLoadState and
     *      call manager should register in SSP again. The threshold
     *      values are increased.
     * <p/>
     * 3. Reach max.
     *  Condition:
     *      As in 1, but the dHWM is now 3, dMax = 5, dLWM = 1.
     *  Action:
     *      Add 6 calls.
     *  Result:
     *      Max is reached. State is set to MaxLoadState and call manager
     *      should start redirecting traffic.
     * <p/>
     * 4. Reach low water mark.
     *  Condition:
     *      As in 3. 5 calls are added.
     *  Action:
     *      Remove 4 calls.
     *  Result:
     *      LowWaterMark is reached. Call manager should register in
     *      SSP again. The dynamic threshold values are increased: dHWM = 4,
     *      dLWM = 2 , dMAX = 6.
     * <p/>
     * 5. Test when the configures values are reached.
     *  Condition:
     *      As in 1. dHWM = 4, dLWM = 2, dMAX = 6. No calls exist.
     *  Action:
     *      Add and remove 1 call until dHWM has been increased to the
     *      configured value 8 (HWM). Add and remove one more call.
     *  Result:
     *      When dHWM has reached HWM, it is no longer increased. The
     *      configured threshold values are used.
     * </pre>
     *
     * @throws Exception if test case fails.
     */
    public void testStartupPhase() throws Exception {
        LoadRegulationAction action;
        int initialHWM = 1;
        double rampFactor = 1;
        int expectedHWM = initialHWM;
        int expectedMax = expectedHWM + (MAX - HWM);
        int expectedLWM = expectedHWM - (HWM - LWM);
        if(expectedLWM < 0)
                expectedLWM = 0;

        // Initiate
        assertConfig(initialHWM, rampFactor);
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        assertEquals(expectedHWM, loadRegulator.getCurrentHWM());
        assertEquals(expectedLWM, loadRegulator.getCurrentLWM());
        assertEquals(expectedMax, loadRegulator.getCurrentMax());

        // 1.
        // Reach HWM.
        action = loadRegulator.addCall(null);
        assertEquals(LoadRegulationAction.STOP_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);

        // 2.
        // Reach LWM, start ramping.
        action = loadRegulator.removeCall(null);
        assertEquals(LoadRegulationAction.START_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
        expectedHWM = expectedHWM + (int)rampFactor;
        expectedLWM = expectedHWM - (HWM - LWM);
        expectedMax = expectedHWM + (MAX - HWM);
        assertEquals(expectedHWM, loadRegulator.getCurrentHWM());
        assertEquals(expectedLWM, loadRegulator.getCurrentLWM());
        assertEquals(expectedMax, loadRegulator.getCurrentMax());

        // 3.
        // Add calls until max.
        addCalls(expectedMax);
        // Add another call. Should be redirected.
        action = loadRegulator.addCall(null);
        assertEquals(LoadRegulationAction.REDIRECT_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);

        // 4.
        // Remove the calls. Should reach LWM, ramp values increased.
        removeCalls(expectedMax - 1);
        action = loadRegulator.removeCall(null);
        assertEquals(LoadRegulationAction.START_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
        expectedHWM = expectedHWM + (int)rampFactor;
        expectedLWM = expectedHWM - (HWM - LWM);
        expectedMax = expectedHWM + (MAX - HWM);
        assertEquals(expectedHWM, loadRegulator.getCurrentHWM());
        assertEquals(expectedLWM, loadRegulator.getCurrentLWM());
        assertEquals(expectedMax, loadRegulator.getCurrentMax());
        removeCalls(1);

        // 5.
        // Test until configured values are met. (HWM, LWM, MAX)
        while(loadRegulator.getCurrentHWM() < HWM) {
            while(loadRegulator.getCurrentCalls() < loadRegulator.getCurrentHWM())
                addCalls(1);
            while(loadRegulator.getCurrentCalls() > loadRegulator.getCurrentLWM())
                removeCalls(1);
        }
        addCalls(1);
        removeCalls(1);
        assertEquals(HWM, loadRegulator.getCurrentHWM());
        assertEquals(LWM, loadRegulator.getCurrentLWM());
        assertEquals(MAX, loadRegulator.getCurrentMax());
        addCalls(1);
        removeCalls(1);
        assertEquals(HWM, loadRegulator.getCurrentHWM());
        assertEquals(LWM, loadRegulator.getCurrentLWM());
        assertEquals(MAX, loadRegulator.getCurrentMax());
    }

    /**
     * Verifies that when removing a call in state Normal Load, the
     * state remains unchanged and the returned action indicates unchanged
     * traffic.
     * @throws Exception if test case fails.
     */
    public void testRemoveCallInNormalLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(LWM);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);

        // Verify
        action = loadRegulator.removeCall(null);
        assertEquals(LoadRegulationAction.UNCHANGED_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
    }

    /**
     * Verifies that when removing a call to LWM in state HighLoad, the
     * state is changed to NormalLoad and the returned action indicates start
     * traffic.
     * @throws Exception if test case fails.
     */
    public void testRemoveCallToLwmInHighLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(HWM);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);
        removeCalls(HWM-LWM-1);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);

        // Verify
        action = loadRegulator.removeCall(null);
        assertEquals(LoadRegulationAction.START_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
    }

    /**
     * Verifies that when removing a call above LWM in state HighLoad, the
     * state remains unchanged and the returned action indicates unchanged
     * traffic.
     * @throws Exception if test case fails.
     */
    public void testRemoveCallAboveLwmInHighLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(HWM);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);

        // Verify
        action = loadRegulator.removeCall(null);
        assertEquals(LoadRegulationAction.UNCHANGED_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);
    }

// TODO: Will not happen.
//    /**
//     * Verifies that when removing a call above Max in state MaxLoad, the
//     * state remains unchanged and the returned action indicates that
//     * traffic should be redirected.
//     * @throws Exception if test case fails.
//     */
//    public void testRemoveCallAboveMaxInMaxLoad() throws Exception {
//        LoadRegulationAction action;
//
//        // Initialize
//        loadRegulator.updateThreshold(HWM, LWM, MAX);
//        addCalls(MAX+1);
//        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);
//
//        // Verify
//        action = loadRegulator.removeCall();
//        assertEquals(action, LoadRegulationAction.UNCHANGED_TRAFFIC);
//        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);
//    }

    /**
     * Verifies that when removing a call below Max but above LWM in state
     * MaxLoad, the state is set to HighLoad and the returned action indicates
     * unchanged traffic.
     * @throws Exception if test case fails.
     */
    public void testRemoveCallBelowMaxInMaxLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(MAX);
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);

        // Verify
        action = loadRegulator.removeCall(null);
        assertEquals(LoadRegulationAction.UNCHANGED_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);
    }

    /**
     * Verifies that when removing a call to LWM in state MaxLoad, the state
     * is set to NormalLoad and the returned action indicates start traffic.
     * @throws Exception if test case fails.
     */
    public void testRemoveCallToLwmInMaxLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(MAX);
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);
        removeCalls(MAX-LWM-1);

        // Verify
        action = loadRegulator.removeCall(null);
        assertEquals(LoadRegulationAction.START_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
    }

    /**
     * Verifies that when setThreshold results in calls below HWM in state
     * Normal Load, the state remains unchanged and the returned action
     * indicates unchanged traffic.
     * @throws Exception if test case fails.
     */
    public void testSetThresholdBelowHwmInNormalLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(HWM-2);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);

        // Verify
        action = loadRegulator.updateThreshold(HWM-1, LWM, MAX);
        assertEquals(LoadRegulationAction.UNCHANGED_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
    }

    /**
     * Verifies that when setThreshold results in calls at HWM in state
     * Normal Load, the state is changed to HighLoad and the returned action
     * indicates stop traffic.
     * @throws Exception if test case fails.
     */
    public void testSetThresholdAtHwmInNormalLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(HWM-1);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);

        // Verify
        action = loadRegulator.updateThreshold(HWM-1, LWM, MAX);
        assertEquals(LoadRegulationAction.STOP_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);
    }

    /**
     * Verifies that when setThreshold results in calls at Max in state
     * NormalLoad, the state is changed to MaxLoad and the returned action
     * indicates stop traffic.
     * @throws Exception if test case fails.
     */
    public void testSetThresholdAtMaxInNormalLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(HWM-1);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);

        // Verify
        action = loadRegulator.updateThreshold(HWM-2, LWM, HWM-1);
        assertEquals(LoadRegulationAction.STOP_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);
    }

    /**
     * Verifies that when setThreshold results in calls below Max in state
     * HighLoad, the state remains unchanged and the returned action indicates
     * unchanged traffic.
     * @throws Exception if test case fails.
     */
    public void testSetThresholdBelowMaxInHighLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(HWM);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);

        // Verify
        action = loadRegulator.updateThreshold(HWM, LWM, MAX);
        assertEquals(LoadRegulationAction.UNCHANGED_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);
    }

    /**
     * Verifies that when setThreshold results in calls at Max in state
     * HighLoad, the state is changed to MaxLoad and the returned action
     * indicates unchanged traffic.
     * @throws Exception if test case fails.
     */
    public void testSetThresholdAtMaxInHighLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(MAX - 1);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);

        // Verify
        action = loadRegulator.updateThreshold(HWM, LWM, MAX-1);
        assertEquals(LoadRegulationAction.UNCHANGED_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);
    }

    /**
     * Verifies that when setThreshold results in calls at LWM in state
     * HighLoad, the state is changed to NormalLoad and the returned action
     * indicates start traffic.
     * @throws Exception if test case fails.
     */
    public void testSetThresholdAtLwmInHighLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(HWM);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);

        // Verify
        action = loadRegulator.updateThreshold(MAX-1, HWM, MAX);
        assertEquals(LoadRegulationAction.START_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
    }

    /**
     * Verifies that when setThreshold results in calls at max in state MaxLoad,
     * the state remains unchanged and the returned action indicates that
     * traffic should be redirected.
     * @throws Exception if test case fails.
     */
    public void testSetThresholdAtMaxInMaxLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(MAX+1);
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);

        // Verify
        action = loadRegulator.updateThreshold(HWM, LWM, MAX);
        assertEquals(LoadRegulationAction.REDIRECT_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);
    }

    /**
     * Verifies that when setThreshold results in calls between LWM and max
     * in state MaxLoad, the state is set to HighLoad and the returned action
     * indicates unchanged traffic.
     * @throws Exception if test case fails.
     */
    public void testSetThresholdBelowMaxInMaxLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(MAX+1);
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);

        // Verify
        action = loadRegulator.updateThreshold(HWM, LWM, MAX+2);
        assertEquals(LoadRegulationAction.UNCHANGED_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);
    }

    /**
     * Verifies that when setThreshold results in calls at LWM in state
     * MaxLoad, the state is set to NormalLoad and the returned action
     * indicates start traffic.
     * @throws Exception if test case fails.
     */
    public void testSetThresholdAtLwmInMaxLoad() throws Exception {
        LoadRegulationAction action;

        // Initialize
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        addCalls(MAX+1);
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);

        // Verify
        action = loadRegulator.updateThreshold(MAX+MAX, MAX+1, MAX+MAX);
        assertEquals(LoadRegulationAction.START_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
    }

    /**
     * Verifies that setThreshold checks the input's validity.
     * @throws Exception if test case fails.
     */
    public void testSetThresholdValidateInput() throws Exception {
        // Too low Max
        loadRegulator.updateThreshold(0,0,0);
        assertEquals(2, loadRegulator.getConfiguredMax());
        assertEquals(1, loadRegulator.getCurrentHWM());
        assertEquals(0, loadRegulator.getCurrentLWM());

        // HWM above Max
        loadRegulator.updateThreshold(5, 0, 3);
        assertEquals(6, loadRegulator.getConfiguredMax());
        assertEquals(5, loadRegulator.getCurrentHWM());
        assertEquals(0, loadRegulator.getCurrentLWM());

        // LWM above HWM
        loadRegulator.updateThreshold(2, 4, 3);
        assertEquals(3, loadRegulator.getConfiguredMax());
        assertEquals(2, loadRegulator.getCurrentHWM());
        assertEquals(1, loadRegulator.getCurrentLWM());

        // Too low HWM
        loadRegulator.updateThreshold(0, 7, 8);
        assertEquals(8, loadRegulator.getConfiguredMax());
        assertEquals(1, loadRegulator.getCurrentHWM());
        assertEquals(0, loadRegulator.getCurrentLWM());

        // Negative LWM
        loadRegulator.updateThreshold(1, -1, 2);
        assertEquals(2, loadRegulator.getConfiguredMax());
        assertEquals(1, loadRegulator.getCurrentHWM());
        assertEquals(0, loadRegulator.getCurrentLWM());

        // Test OK input
        loadRegulator.updateThreshold(50, 40, 55);
        assertEquals(55, loadRegulator.getConfiguredMax());
        assertEquals(50, loadRegulator.getCurrentHWM());
        assertEquals(40, loadRegulator.getCurrentLWM());
    }

    /**
     * Test the ramping of threshold.
     * The number of calls will variously go down to low water mark, and variously
     * below low watermark.
     *
     * @throws Exception if test case fails.
     */
    public void testLongRamp() throws Exception {

        final int MAX = 60;
        final int HWM = 50;
        final int LWM = 40;

        LoadRegulationAction action;
        int initialHWM = 1;
        double rampFactor = 10;
        int expectedHWM = initialHWM;
        int expectedMax = expectedHWM + (MAX - HWM);
        int expectedLWM = expectedHWM - (HWM - LWM);
        if(expectedLWM < 0)
                expectedLWM = 0;
        int currentCalls = 0;

        // Initiate
        assertConfig(initialHWM, rampFactor);
        loadRegulator.updateThreshold(HWM, LWM, MAX);
        assertThresholds(initialHWM, expectedLWM, expectedMax);

        // Reach HWM.
        addCalls(expectedHWM -1);
        action = loadRegulator.addCall(null);
        currentCalls = expectedHWM;
        assertEquals(LoadRegulationAction.STOP_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof HighLoadState);

        // Reach LWM, start ramping.
        removeCalls(expectedHWM - expectedLWM -1);
        action = loadRegulator.removeCall(null);
        currentCalls = expectedLWM;
        assertEquals(LoadRegulationAction.START_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
        expectedHWM = calculateHWM(expectedHWM, rampFactor);
        expectedLWM = calculateLWM(expectedHWM, HWM, LWM);
        expectedMax = calculateMax(expectedHWM, MAX, HWM);
        assertThresholds(expectedHWM, expectedLWM, expectedMax);

        // Add calls until max.
        int numberOfCalls = expectedMax - currentCalls;
        addCalls(numberOfCalls);
        currentCalls += numberOfCalls;
        // Add another call. Should be redirected.
        action = loadRegulator.addCall(null);
        assertEquals(LoadRegulationAction.REDIRECT_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof MaxLoadState);

        // Remove all calls down to LWM.
        removeCalls(currentCalls - expectedLWM -1);
        action = loadRegulator.removeCall(null);
        currentCalls = expectedLWM;
        assertEquals(LoadRegulationAction.START_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
        expectedHWM = calculateHWM(expectedHWM, rampFactor);
        expectedLWM = calculateLWM(expectedHWM, HWM, LWM);
        expectedMax = calculateMax(expectedHWM, MAX, HWM);
        assertThresholds(expectedHWM, expectedLWM, expectedMax);

        // Remove all calls down to 0. This should not affect HWM.
        removeCalls(currentCalls-1);
        action = loadRegulator.removeCall(null);
        currentCalls = 0;
        assertEquals(LoadRegulationAction.UNCHANGED_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
        assertThresholds(expectedHWM, expectedLWM, expectedMax);

        // Add a call. This should not affect load situation.
        action = loadRegulator.addCall(null);
        currentCalls++;
        assertEquals(LoadRegulationAction.UNCHANGED_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
        assertThresholds(expectedHWM, expectedLWM, expectedMax);

        // Add calls to between currentHWM and currentMax, then go down to LWM.
        // Should result in HWM getting updated.
        addCalls(expectedHWM +2);
        currentCalls += expectedHWM +2;
        removeCalls(currentCalls-expectedLWM-1);
        action = loadRegulator.removeCall(null);
        expectedHWM = calculateHWM(expectedHWM, rampFactor);
        expectedLWM = calculateLWM(expectedHWM, HWM, LWM);
        expectedMax = calculateMax(expectedHWM, MAX, HWM);
        assertEquals(LoadRegulationAction.START_TRAFFIC, action);
        assertTrue(loadRegulator.getCurrentState() instanceof NormalLoadState);
        assertThresholds(expectedHWM, expectedLWM, expectedMax);
    }

    private void assertThresholds(int expectedHWM, int expectedLWM, int expectedMax) {
        assertEquals(expectedHWM, loadRegulator.getCurrentHWM());
        assertEquals(expectedLWM, loadRegulator.getCurrentLWM());
        assertEquals(expectedMax, loadRegulator.getCurrentMax());
    }

    private int calculateMax(int currentHWM, int MAX, int HWM) {
        return currentHWM + (MAX - HWM);
    }

    private int calculateHWM(int currentHWM, double rampFactor) {
        return currentHWM + (int)rampFactor;
    }

    private int calculateLWM(int currentHWM, int HWM, int LWM) {
        return currentHWM - (HWM - LWM);
    }

    private void addCalls(int numberOfCalls) {
        for (int i = 0; i < numberOfCalls; i++) {
            loadRegulator.addCall(null);
        }
    }

    private void removeCalls(int numberOfCalls) {
        for (int i = 0; i < numberOfCalls; i++) {
            loadRegulator.removeCall(null);
        }
    }

}
