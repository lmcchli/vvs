/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.registration;

import com.mobeon.masp.callmanager.registration.states.UnregisteredState;
import com.mobeon.masp.callmanager.registration.states.RegisteringState;
import com.mobeon.masp.callmanager.registration.states.UnregisteringState;
import com.mobeon.masp.callmanager.registration.states.RegisteredState;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;

/**
 * RegisteringState Tester.
 *
 * @author Malin Flodin
 */
public class RegisteringStateTest extends SspInstanceTest
{
    public RegisteringStateTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        assertCurrentState(UnregisteredState.class);

        // GOTO Registering state
        expectRegisterSent(cSeq++, SUCCEED);
        sspInstance.doRegister();
        assertCurrentState(RegisteringState.class);
        assertPendingUnregister(false);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that a doRegister request in state Registering is ignored.
     * @throws Exception if the test case fails.
     */
    public void testDoRegister() throws Exception {
        sspInstance.doRegister();
        assertCurrentState(RegisteringState.class);
    }

    /**
     * Verifies that a doRegister request in state Registering while there is a
     * pending unregister results in the pending unregister parameter cleared.
     * @throws Exception if the test case fails.
     */
    public void testDoRegisterWhilePendingUnregister() throws Exception {
        // Set pending unregister
        sspInstance.doUnregister();
        assertPendingUnregister(true);

        // Do register
        sspInstance.doRegister();
        assertPendingUnregister(false);
        assertCurrentState(RegisteringState.class);
    }

    /**
     * Verifies that a doUnregister in state Registering results in the
     * pending unregister parameter set.
     * @throws Exception if the test case fails.
     */
    public void testDoUnregister() throws Exception {
        sspInstance.doUnregister();
        assertPendingUnregister(true);
        assertCurrentState(RegisteringState.class);
    }

    /**
     * Verifies that a SIP OK response received in state Registering with an
     * expire time that is zero or missing results in the backoff timer being
     * scheduled and the SIP REGISTER is retried when that timer expires. Also,
     * the SSP is marked as NOT registered.
     * @throws Exception if the test case fails.
     */
    public void testProcessSipOkResponseWhenExpireIsZero() throws Exception {
        // Add the SSP to the list of registered SSPs only to verify that this
        // test case removes the SSP from that list.
        sspInstance.markAsRegistered();
        int sspAmountBefore = SspStatus.getInstance().getNumberOfRegisteredSsp();

        sspInstance.processSipOkResponse(EXPIRE_TIME_MALFORMED);
        assertCurrentState(RegisteringState.class);

        expectRegisterSent(cSeq++, SUCCEED);
        // Wait until backoff timer has expired in order to retrieve the
        // second SIP REGISTER. Multiplying with 2 to make
        // sure that the backoff timer really has expired.
        Thread.sleep(ConfigurationReader.getInstance().getConfig().
                getRegisterBackoffTimer()*2 );
        assertCurrentState(RegisteringState.class);

        int sspAmountAfter = SspStatus.getInstance().getNumberOfRegisteredSsp();
        assertEquals(sspAmountBefore - 1, sspAmountAfter);
    }

    /**
     * Verifies that a SIP OK response in state Registering while there is a
     * pending unregister results in a SIP UNREGISTER sent, the state is set
     * to Unregistering and the SSP is marked as NOT registered.
     * @throws Exception if test case fails.
     */
    public void testProcessSipOkResponseWhenPendingUnregister() throws Exception {
        // Add the SSP to the list of registered SSPs only to verify that this
        // test case removes the SSP from that list.
        sspInstance.markAsRegistered();
        int sspAmountBefore = SspStatus.getInstance().getNumberOfRegisteredSsp();

        // Set pending unregister
        sspInstance.doUnregister();
        assertPendingUnregister(true);

        // Handle SIP OK response
        expectUnregisterSent(cSeq++, SUCCEED);
        sspInstance.processSipOkResponse(EXPIRE_TIME_OK);
        assertPendingUnregister(false);
        assertCurrentState(UnregisteringState.class);

        int sspAmountAfter = SspStatus.getInstance().getNumberOfRegisteredSsp();
        assertEquals(sspAmountBefore - 1, sspAmountAfter);
    }

    /**
     * Verifies that a SIP OK response in state Registering while there is a
     * pending unregister results in a SIP UNREGISTER sent and the SSP is marked
     * as NOT registered. If a failure occurs when sending the SIP UNREGISTER,
     * the state is set to Unregistered.
     * @throws Exception if test case fails.
     */
    public void testProcessSipOkResponseWhenPendingUnregisterAndUnregisterFails()
            throws Exception {
        // Add the SSP to the list of registered SSPs only to verify that this
        // test case removes the SSP from that list.
        sspInstance.markAsRegistered();
        int sspAmountBefore = SspStatus.getInstance().getNumberOfRegisteredSsp();

        // Set pending unregister
        sspInstance.doUnregister();
        assertPendingUnregister(true);

        // Handle SIP OK response
        expectUnregisterSent(cSeq++, FAIL);
        sspInstance.processSipOkResponse(EXPIRE_TIME_OK);
        assertPendingUnregister(false);
        assertCurrentState(UnregisteredState.class);

        int sspAmountAfter = SspStatus.getInstance().getNumberOfRegisteredSsp();
        assertEquals(sspAmountBefore - 1, sspAmountAfter);
    }

    /**
     * Verifies that a SIP OK response is state Registering (while there is no
     * pending unregister) results in scheduling the re-register timer,
     * setting the state to Registered and marking the SSP as registered.
     * When the re-register timer expires, a new SIP REGISTER request is sent
     * and the state is set to Registering.
     * @throws Exception if test case fails.
     */
    public void testProcessSipOkResponse() throws Exception {
        int sspAmountBefore = SspStatus.getInstance().getNumberOfRegisteredSsp();

        sspInstance.processSipOkResponse(EXPIRE_TIME_OK);
        assertCurrentState(RegisteredState.class);

        // Wait until retry timer has expired in order to retrieve a
        // second SIP REGISTER. Multiplying with 1100 instead of 1000 to make
        // sure that the retry timer really has expired.
        expectRegisterSent(cSeq++, SUCCEED);
        Thread.sleep(EXPIRE_TIME_OK * 1100);
        assertCurrentState(RegisteringState.class);

        int sspAmountAfter = SspStatus.getInstance().getNumberOfRegisteredSsp();
        assertEquals(sspAmountBefore + 1, sspAmountAfter);
    }


    /**
     * Verifies that a SIP Error response in state Registering while there is a
     * pending unregister results in setting the state to Unregistered and the
     * SSP is marked as NOT registered.
     * @throws Exception if test case fails.
     */
    public void testProcessSipErrorResponseWhenPendingUnregister()
            throws Exception {
        // Add the SSP to the list of registered SSPs only to verify that this
        // test case removes the SSP from that list.
        sspInstance.markAsRegistered();
        int sspAmountBefore = SspStatus.getInstance().getNumberOfRegisteredSsp();

        // Set pending unregister
        sspInstance.doUnregister();
        assertPendingUnregister(true);

        // Handle SIP Error response
        sspInstance.processSipErrorResponse(SIP_ERROR_CODE);
        assertPendingUnregister(false);
        assertCurrentState(UnregisteredState.class);

        int sspAmountAfter = SspStatus.getInstance().getNumberOfRegisteredSsp();
        assertEquals(sspAmountBefore - 1, sspAmountAfter);
    }

    /**
     * Verifies that a SIP Error response in state Registering (when there is no
     * pending unregister) results in scheduling the backoff timer and the SSP
     * is marked as NOT registered. When the backoff timer expires, a new SIP
     * REGISTER request is sent.
     * @throws Exception if test case fails.
     */
    public void testProcessSipErrorResponse() throws Exception {
        // Add the SSP to the list of registered SSPs only to verify that this
        // test case removes the SSP from that list.
        sspInstance.markAsRegistered();
        int sspAmountBefore = SspStatus.getInstance().getNumberOfRegisteredSsp();

        // Handle SIP Error response
        sspInstance.processSipErrorResponse(SIP_ERROR_CODE);
        assertCurrentState(RegisteringState.class);

        expectRegisterSent(cSeq++, SUCCEED);
        // Wait until backoff timer has expired in order to retrieve the
        // second SIP REGISTER. Multiplying with 2 to make
        // sure that the backoff timer really has expired.
        Thread.sleep(ConfigurationReader.getInstance().getConfig().
                getRegisterBackoffTimer()*2 );
        assertCurrentState(RegisteringState.class);

        int sspAmountAfter = SspStatus.getInstance().getNumberOfRegisteredSsp();
        assertEquals(sspAmountBefore - 1, sspAmountAfter);
    }

    /**
     * Verifies that a SIP Timeout in state Registering while there is a
     * pending unregister results in a SIP UNREGISTER sent, the state is set
     * to Unregistering and the SSP is marked as NOT registered.
     * @throws Exception if test case fails.
     */
    public void testProcessSipTimeoutWhenPendingUnregister() throws Exception {
        // Add the SSP to the list of registered SSPs only to verify that this
        // test case removes the SSP from that list.
        sspInstance.markAsRegistered();
        int sspAmountBefore = SspStatus.getInstance().getNumberOfRegisteredSsp();

        // Set pending unregister
        sspInstance.doUnregister();
        assertPendingUnregister(true);

        // Handle SIP OK response
        expectUnregisterSent(cSeq++, SUCCEED);
        sspInstance.processSipTimeout();
        assertPendingUnregister(false);
        assertCurrentState(UnregisteringState.class);

        int sspAmountAfter = SspStatus.getInstance().getNumberOfRegisteredSsp();
        assertEquals(sspAmountBefore - 1, sspAmountAfter);
    }

    /**
     * Verifies that a SIP Timeout in state Registering while there is a
     * pending unregister results in a SIP UNREGISTER sent and the SSP is marked
     * as NOT registered. If a failure occurs when sending the SIP UNREGISTER,
     * the state is set to Unregistered.
     * @throws Exception if test case fails.
     */
    public void testProcessSipTimeoutWhenPendingUnregisterAndUnregisterFails()
            throws Exception {
        // Add the SSP to the list of registered SSPs only to verify that this
        // test case removes the SSP from that list.
        sspInstance.markAsRegistered();
        int sspAmountBefore = SspStatus.getInstance().getNumberOfRegisteredSsp();

        // Set pending unregister
        sspInstance.doUnregister();
        assertPendingUnregister(true);

        // Handle SIP OK response
        expectUnregisterSent(cSeq++, FAIL);
        sspInstance.processSipTimeout();
        assertPendingUnregister(false);
        assertCurrentState(UnregisteredState.class);

        int sspAmountAfter = SspStatus.getInstance().getNumberOfRegisteredSsp();
        assertEquals(sspAmountBefore - 1, sspAmountAfter);
    }

    /**
     * Verifies that a SIP Timeout in state Registering (when there is no
     * pending unregister) results in scheduling the backoff timer and the SSP
     * is marked as NOT registered. When the backoff timer expires, a new SIP
     * REGISTER request is sent.
     * @throws Exception if test case fails.
     */
    public void testProcessSipTimeout() throws Exception {
        // Add the SSP to the list of registered SSPs only to verify that this
        // test case removes the SSP from that list.
        sspInstance.markAsRegistered();
        int sspAmountBefore = SspStatus.getInstance().getNumberOfRegisteredSsp();

        // Handle SIP Error response
        sspInstance.processSipTimeout();
        assertCurrentState(RegisteringState.class);

        expectRegisterSent(cSeq++, SUCCEED);
        // Wait until backoff timer has expired in order to retrieve the
        // second SIP REGISTER. Multiplying with 2 to make
        // sure that the backoff timer really has expired.
        Thread.sleep(ConfigurationReader.getInstance().getConfig().
                getRegisterBackoffTimer()*2 );
        assertCurrentState(RegisteringState.class);

        int sspAmountAfter = SspStatus.getInstance().getNumberOfRegisteredSsp();
        assertEquals(sspAmountBefore - 1, sspAmountAfter);
    }

    /**
     * Verifies that a re-Register timeout in state Registering is ignored.
     * @throws Exception if the test case fails.
     */
    public void testHandleReRegisterTimeout() throws Exception {
        sspInstance.handleTimeout(RegistrationTimerTask.Type.REGISTER_RETRY);
        assertCurrentState(RegisteringState.class);
    }

    /**
     * Verifies that a backoff timeout in state Registering results in a
     * SIP REGISTER being sent.
     * @throws Exception if the test case fails.
     */
    public void testHandleBackoffTimeout() throws Exception {
        expectRegisterSent(cSeq++, SUCCEED);
        sspInstance.handleTimeout(RegistrationTimerTask.Type.REGISTER_BACKOFF);
        assertCurrentState(RegisteringState.class);
        // This sleep is done to make sure that the SIP REGISTER request has
        // been sent before the test case finished. Otherwise the test case fails.
        Thread.sleep(30);
    }

    /**
     * Verifies that a backoff timeout in state Registering results in a SIP
     * REGISTER sent. If a failure occurs when sending the SIP REGISTER, the
     * backoff timer is scheduled again.
     * @throws Exception if the test case fails.
     */
    public void testHandleBackoffTimeoutWhenRegisterFails() throws Exception {
        expectRegisterSent(cSeq++, FAIL);
        sspInstance.handleTimeout(RegistrationTimerTask.Type.REGISTER_BACKOFF);
        assertCurrentState(RegisteringState.class);

        expectRegisterSent(cSeq++, SUCCEED);
        // Wait until backoff timer has expired in order to retrieve the
        // second SIP REGISTER. Multiplying with 2 to make
        // sure that the backoff timer really has expired.
        Thread.sleep(ConfigurationReader.getInstance().getConfig().
                getRegisterBackoffTimer()*2 );
        assertCurrentState(RegisteringState.class);
    }

}
