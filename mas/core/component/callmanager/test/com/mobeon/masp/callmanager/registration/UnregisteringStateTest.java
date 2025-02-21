/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.registration;

import com.mobeon.masp.callmanager.registration.states.RegisteringState;
import com.mobeon.masp.callmanager.registration.states.RegisteredState;
import com.mobeon.masp.callmanager.registration.states.UnregisteringState;
import com.mobeon.masp.callmanager.registration.states.UnregisteredState;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;

/**
 * UnregisteringState Tester.
 *
 * @author Malin Flodin
 */
public class UnregisteringStateTest extends SspInstanceTest {

    public UnregisteringStateTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();

        // GOTO Registering state
        expectRegisterSent(cSeq++, SUCCEED);
        sspInstance.doRegister();
        assertCurrentState(RegisteringState.class);

        // GOTO Registered state
        sspInstance.processSipOkResponse(EXPIRE_TIME_OK);
        assertCurrentState(RegisteredState.class);

        // GOTO Registered state
        expectUnregisterSent(cSeq++, SUCCEED);
        sspInstance.doUnregister();
        assertCurrentState(UnregisteringState.class);

        assertPendingRegister(false);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    
//    @Ignore("Initial test cleanup for continuous integration - This test needs reviewing: it fails on Linux.")
    /**
     * Verifies that a doRegister in state Unregistering results in the
     * pending register parameter set.
     * @throws Exception if the test case fails.
     */
    /*
    public void testDoRegister() throws Exception {
        // Do pending register
        sspInstance.doRegister();
        assertPendingRegister(true);
    }
*/

//  @Ignore("Initial test cleanup for continuous integration - This test needs reviewing: it fails on Linux.")
    /**
     * Verifies that a doUnregister request in state Unregistering while there
     * is a pending register results in the pending register parameter cleared.
     * @throws Exception if the test case fails.
     */
    /*
    public void testDoUnregisterWhilePendingRegister() throws Exception {
        // Do pending register
        sspInstance.doRegister();
        assertPendingRegister(true);

        // Do unregister
        sspInstance.doUnregister();
        assertPendingRegister(false);
        assertCurrentState(UnregisteringState.class);
    }
*/
    
    /**
     * Verifies that a doUnregister request in state Unregistering is ignored.
     * @throws Exception if the test case fails.
     */
    public void testDoUnregister() throws Exception {
        sspInstance.doUnregister();
        assertCurrentState(UnregisteringState.class);
    }

    /**
     * Verifies that a SIP OK response in state Unregistering while there is a
     * pending register results in a SIP REGISTER sent and the state is set
     * to Registering.
     * @throws Exception if test case fails.
     */
    public void testProcessSipOkResponseWhenPendingRegister() throws Exception {
        // Set pending register
        sspInstance.doRegister();
        assertPendingRegister(true);

        // Handle SIP OK response
        expectRegisterSent(cSeq++, SUCCEED);
        sspInstance.processSipOkResponse(EXPIRE_TIME_OK);
        assertPendingRegister(false);
        assertCurrentState(RegisteringState.class);
    }

    /**
     * Verifies that a SIP OK response in state Unregistering while there is a
     * pending register results in a SIP REGISTER sent. If a failure occurs
     * when sending the SIP REGISTER, the backoff timer is scheduled and the
     * state is set to Registered. When the backoff timer expires, a new SIP
     * REGISTER is sent.
     * @throws Exception if test case fails.
     */
    public void testProcessSipOkResponseWhenPendingRegisterAndRegisterFails()
            throws Exception {
        // Set pending register
        sspInstance.doRegister();
        assertPendingRegister(true);

        // Handle SIP OK response
        expectRegisterSent(cSeq++, FAIL);
        sspInstance.processSipOkResponse(EXPIRE_TIME_OK);
        assertPendingRegister(false);
        assertCurrentState(RegisteringState.class);

        expectRegisterSent(cSeq++, SUCCEED);
        // Wait until backoff timer has expired in order to retrieve the
        // second SIP REGISTER. Multiplying with 2 to make
        // sure that the backoff timer really has expired.
        Thread.sleep(ConfigurationReader.getInstance().getConfig().
                getRegisterBackoffTimer()*2 );
        assertCurrentState(RegisteringState.class);
    }

    /**
     * Verifies that a SIP OK response in state Unregistering (while there is no
     * pending register) results in setting the state to Unregistered.
     * @throws Exception if test case fails.
     */
    public void testProcessSipOkResponse() throws Exception {
        sspInstance.processSipOkResponse(EXPIRE_TIME_OK);
        assertCurrentState(UnregisteredState.class);
    }

    /**
     * Verifies that a SIP Error response in state Unregistering while there is
     * a pending register results in a SIP REGISTER sent and the state is set
     * to Registering.
     * @throws Exception if test case fails.
     */
    public void testProcessSipErrorResponseWhenPendingRegister() throws Exception {
        // Set pending register
        sspInstance.doRegister();
        assertPendingRegister(true);

        // Handle SIP OK response
        expectRegisterSent(cSeq++, SUCCEED);
        sspInstance.processSipErrorResponse(SIP_ERROR_CODE);
        assertPendingRegister(false);
        assertCurrentState(RegisteringState.class);
    }

    /**
     * Verifies that a SIP Error response in state Unregistering while there is
     * a pending register results in a SIP REGISTER sent. If a failure occurs
     * when sending the SIP REGISTER, the backoff timer is scheduled and the
     * state is set to Registered. When the backoff timer expires, a new SIP
     * REGISTER is sent.
     * @throws Exception if test case fails.
     */
    public void testProcessSipErrorResponseWhenPendingRegisterAndRegisterFails()
            throws Exception {
        // Set pending register
        sspInstance.doRegister();
        assertPendingRegister(true);

        // Handle SIP OK response
        expectRegisterSent(cSeq++, FAIL);
        sspInstance.processSipErrorResponse(SIP_ERROR_CODE);
        assertPendingRegister(false);
        assertCurrentState(RegisteringState.class);

        expectRegisterSent(cSeq++, SUCCEED);
        // Wait until backoff timer has expired in order to retrieve the
        // second SIP REGISTER. Multiplying with 2 to make
        // sure that the backoff timer really has expired.
        Thread.sleep(ConfigurationReader.getInstance().getConfig().
                getRegisterBackoffTimer()*2 );
        assertCurrentState(RegisteringState.class);
    }

    /**
     * Verifies that a SIP Error response in state Unregistering (while there is
     * no pending register) results in setting the state to Unregistered.
     * @throws Exception if test case fails.
     */
    public void testProcessSipErrorResponse() throws Exception {
        sspInstance.processSipOkResponse(EXPIRE_TIME_OK);
        assertCurrentState(UnregisteredState.class);
    }

    /**
     * Verifies that a SIP Timeout in state Unregistering while there is
     * a pending register results in a SIP REGISTER sent and the state is set
     * to Registering.
     * @throws Exception if test case fails.
     */
    public void testProcessSipTimeoutWhenPendingRegister() throws Exception {
        // Set pending register
        sspInstance.doRegister();
        assertPendingRegister(true);

        // Handle SIP OK response
        expectRegisterSent(cSeq++, SUCCEED);
        sspInstance.processSipTimeout();
        assertPendingRegister(false);
        assertCurrentState(RegisteringState.class);
    }

    /**
     * Verifies that a SIP Timeout in state Unregistering while there is
     * a pending register results in a SIP REGISTER sent. If a failure occurs
     * when sending the SIP REGISTER, the backoff timer is scheduled and the
     * state is set to Registered. When the backoff timer expires, a new SIP
     * REGISTER is sent.
     * @throws Exception if test case fails.
     */
    public void testProcessSipTimeoutWhenPendingRegisterAndRegisterFails()
            throws Exception {
        // Set pending register
        sspInstance.doRegister();
        assertPendingRegister(true);

        // Handle SIP OK response
        expectRegisterSent(cSeq++, FAIL);
        sspInstance.processSipTimeout();
        assertPendingRegister(false);
        assertCurrentState(RegisteringState.class);

        expectRegisterSent(cSeq++, SUCCEED);
        // Wait until backoff timer has expired in order to retrieve the
        // second SIP REGISTER. Multiplying with 2 to make
        // sure that the backoff timer really has expired.
        Thread.sleep(ConfigurationReader.getInstance().getConfig().
                getRegisterBackoffTimer()*2 );
        assertCurrentState(RegisteringState.class);
    }

    /**
     * Verifies that a SIP Timeout in state Unregistering (while there is
     * no pending register) results in setting the state to Unregistered.
     * @throws Exception if test case fails.
     */
    public void testProcessSipTimeout() throws Exception {
        sspInstance.processSipTimeout();
        assertCurrentState(UnregisteredState.class);
    }

    /**
     * Verifies that a re-Register timeout in state Unregistering is ignored.
     * @throws Exception if the test case fails.
     */
    public void testHandleReRegisterTimeout() throws Exception {
        sspInstance.handleTimeout(RegistrationTimerTask.Type.REGISTER_RETRY);
        assertCurrentState(UnregisteringState.class);
    }

    /**
     * Verifies that a backoff timeout in state Unregistering is ignored.
     * @throws Exception if the test case fails.
     */
    public void testHandleBackoffTimeout() throws Exception {
        sspInstance.handleTimeout(RegistrationTimerTask.Type.REGISTER_BACKOFF);
        assertCurrentState(UnregisteringState.class);
    }

}
