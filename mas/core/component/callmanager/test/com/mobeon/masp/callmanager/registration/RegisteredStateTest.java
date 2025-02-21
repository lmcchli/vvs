/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.registration;

import com.mobeon.masp.callmanager.registration.states.RegisteringState;
import com.mobeon.masp.callmanager.registration.states.RegisteredState;
import com.mobeon.masp.callmanager.registration.states.UnregisteringState;
import com.mobeon.masp.callmanager.registration.states.UnregisteredState;

/**
 * RegisteredState Tester.
 *
 * @author Malin Flodin
 */
public class RegisteredStateTest extends SspInstanceTest {
    public RegisteredStateTest(String name) {
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
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that a doRegister request in state Registered is ignored.
     * @throws Exception if the test case fails.
     */
    public void testDoRegister() throws Exception {
        sspInstance.doRegister();
        assertCurrentState(RegisteredState.class);
    }

    /**
     * Verifies that a doUnregister in state Registered results in a
     * SIP UNREGISTER sent, the re-register timer is canceled, the state is
     * set to Unregistering and the SSP is marked as NOT registered.
     * @throws Exception if test case fails.
     */
    public void testDoUnregister() throws Exception {
        int sspAmountBefore = SspStatus.getInstance().getNumberOfRegisteredSsp();

        expectUnregisterSent(cSeq++, SUCCEED);
        sspInstance.doUnregister();
        assertCurrentState(UnregisteringState.class);

        // Wait to make sure that retry timer will not expire, i.e. that it has
        // been canceled. Multiplying with 1100 instead of 1000 to make
        // sure that the retry timer really would have expired if it had not
        // been canceled.
        Thread.sleep(EXPIRE_TIME_OK * 1100);
        assertCurrentState(UnregisteringState.class);

        int sspAmountAfter = SspStatus.getInstance().getNumberOfRegisteredSsp();
        assertEquals(sspAmountBefore - 1, sspAmountAfter);
    }

    /**
     * Verifies that a doUnregister in state Registered results in a
     * SIP UNREGISTER sent, the re-register timer is canceled and the SSP is
     * marked as NOT registered. If sending the SIP UNREGISTER fails, the state
     * is set to Unregistered.
     * @throws Exception if test case fails.
     */
    public void testDoUnregisterWhenUnregisterFails() throws Exception {
        int sspAmountBefore = SspStatus.getInstance().getNumberOfRegisteredSsp();

        expectUnregisterSent(cSeq++, FAIL);
        sspInstance.doUnregister();
        assertCurrentState(UnregisteredState.class);

        // Wait to make sure that retry timer will not expire, i.e. that it has
        // been canceled. Multiplying with 1100 instead of 1000 to make
        // sure that the retry timer really would have expired if it had not
        // been canceled.
        Thread.sleep(EXPIRE_TIME_OK * 1100);
        assertCurrentState(UnregisteredState.class);

        int sspAmountAfter = SspStatus.getInstance().getNumberOfRegisteredSsp();
        assertEquals(sspAmountBefore - 1, sspAmountAfter);
    }

    /**
     * Verifies that a SIP OK response in state Registered is ignored.
     * @throws Exception if the test case fails.
     */
    public void testProcessSipOkResponse() throws Exception {
        sspInstance.processSipOkResponse(EXPIRE_TIME_OK);
        assertCurrentState(RegisteredState.class);
    }

    /**
     * Verifies that a SIP Error response in state Registered is ignored.
     * @throws Exception if the test case fails.
     */
    public void testProcessSipErrorResponse() throws Exception {
        sspInstance.processSipErrorResponse(SIP_ERROR_CODE);
        assertCurrentState(RegisteredState.class);
    }

    /**
     * Verifies that a SIP Timeout or transaction error in state Registered
     * is ignored.
     * @throws Exception if the test case fails.
     */
    public void testProcessSipTimeout() throws Exception {
        sspInstance.processSipTimeout();
        assertCurrentState(RegisteredState.class);
    }

    /**
     * Verifies that if the re-register timer expires in state Registered, a
     * new SIP REGISTER request is sent and the state is set to Registering.
     * @throws Exception if the test case fails.
     */
    public void testHandleReRegisterTimeout() throws Exception {
        int sspAmountBefore = SspStatus.getInstance().getNumberOfRegisteredSsp();

        expectRegisterSent(cSeq++, SUCCEED);
        sspInstance.handleTimeout(RegistrationTimerTask.Type.REGISTER_RETRY);
        assertCurrentState(RegisteringState.class);

        int sspAmountAfter = SspStatus.getInstance().getNumberOfRegisteredSsp();
        assertEquals(sspAmountBefore, sspAmountAfter);
    }

    /**
     * Verifies that a backoff timeout in state Registered is ignored.
     * @throws Exception if the test case fails.
     */
    public void testHandleBackoffTimeout() throws Exception {
        sspInstance.handleTimeout(RegistrationTimerTask.Type.REGISTER_BACKOFF);
        assertCurrentState(RegisteredState.class);
    }

}
