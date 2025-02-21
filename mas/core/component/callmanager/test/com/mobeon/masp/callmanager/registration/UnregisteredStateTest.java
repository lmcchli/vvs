package com.mobeon.masp.callmanager.registration;

import com.mobeon.masp.callmanager.registration.states.UnregisteredState;
import com.mobeon.masp.callmanager.registration.states.RegisteringState;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;

/**
 * UnregisteredState Tester.
 *
 * @author Malin Flodin
 */
public class UnregisteredStateTest extends SspInstanceTest {

    public UnregisteredStateTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();

        assertCurrentState(UnregisteredState.class);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that a doRegister in Unregistered state results in a SIP
     * REGISTER send and the state is set to Registering.
     * @throws Exception if the test case fails.
     */
    public void testDoRegister() throws Exception {
        expectRegisterSent(cSeq++, SUCCEED);
        sspInstance.doRegister();
        assertCurrentState(RegisteringState.class);
    }

    /**
     * Verifies that when sending a SIP REGISTER request (due to a doRegister in
     * Unregistered state) fails, the backoff timer is started and the state
     * is set to Registering.
     * @throws Exception if the test case fails.
     */
    public void testDoRegisterThatThrowsException() throws Exception {
        expectRegisterSent(cSeq++, FAIL);
        sspInstance.doRegister();
        assertCurrentState(RegisteringState.class);

        expectRegisterSent(cSeq++, SUCCEED);
        // Wait until backoff timer has expired in order to retrieve the
        // second SIP REGISTER. Multiplying with 2 to make
        // sure that the backoff timer really has expired.
        Thread.sleep(ConfigurationReader.getInstance().getConfig().
                getRegisterBackoffTimer()*2 );
    }

    /**
     * Verifies that a doUnregister request in state Unregistered is ignored.
     * @throws Exception if the test case fails.
     */
    public void testDoUnregister() throws Exception {
        sspInstance.doUnregister();
        assertCurrentState(UnregisteredState.class);
    }

    /**
     * Verifies that a SIP OK response in state Unregistered is ignored.
     * @throws Exception if the test case fails.
     */
    public void testProcessSipOkResponse() throws Exception {
        sspInstance.processSipOkResponse(EXPIRE_TIME_OK);
        assertCurrentState(UnregisteredState.class);
    }

    /**
     * Verifies that a SIP Error response in state Unregistered is ignored.
     * @throws Exception if the test case fails.
     */
    public void testProcessSipErrorResponse() throws Exception {
        sspInstance.processSipErrorResponse(SIP_ERROR_CODE);
        assertCurrentState(UnregisteredState.class);
    }

    /**
     * Verifies that a SIP Timeout or transaction error in state Unregistered
     * is ignored.
     * @throws Exception if the test case fails.
     */
    public void testProcessSipTimeout() throws Exception {
        sspInstance.processSipTimeout();
        assertCurrentState(UnregisteredState.class);
    }

    /**
     * Verifies that a re-Register timeout in state Unregistered is ignored.
     * @throws Exception if the test case fails.
     */
    public void testHandleReRegisterTimeout() throws Exception {
        sspInstance.handleTimeout(RegistrationTimerTask.Type.REGISTER_RETRY);
        assertCurrentState(UnregisteredState.class);
    }

    /**
     * Verifies that a backoff timeout in state Unregistered is ignored.
     * @throws Exception if the test case fails.
     */
    public void testHandleBackoffTimeout() throws Exception {
        sspInstance.handleTimeout(RegistrationTimerTask.Type.REGISTER_BACKOFF);
        assertCurrentState(UnregisteredState.class);
    }

}
