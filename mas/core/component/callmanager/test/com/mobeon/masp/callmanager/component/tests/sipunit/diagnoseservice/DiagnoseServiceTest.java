/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.diagnoseservice;

import com.mobeon.masp.operateandmaintainmanager.Status;
import com.mobeon.masp.operateandmaintainmanager.ServiceInstance;

/**
 * Call Manager component test case to verify the Diagnose Service part of Call
 * Manager.
 * @author Malin Flodin
 */
public class DiagnoseServiceTest extends DiagnoseServiceCase {

    /**
     * Verifies that IllegalArgumentException is thrown if the ServiceInstance
     * in the service request is null or does not contain host or port.
     * @throws Exception if test case fails.
     */
    public void testServiceRequestWithInvalidServiceInstance() throws Exception {

        // Verify for null Service Instance
        try {
            diagnoseService.serviceRequest(null);
            fail("Expected exception was not thrown!");
        } catch (IllegalArgumentException e) {
        }

        // Verify for no host in Service Instance
        ServiceInstance siWithoutHost = new ServiceInstance();
        siWithoutHost.setPort(5060);
        try {
            diagnoseService.serviceRequest(siWithoutHost);
            fail("Expected exception was not thrown!");
        } catch (IllegalArgumentException e) {
        }

        // Verify for no port in Service Instance
        ServiceInstance siWithoutPort = new ServiceInstance();
        siWithoutHost.setHostName("0.0.0.0");
        try {
            diagnoseService.serviceRequest(siWithoutPort);
            fail("Expected exception was not thrown!");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Verifies that a service request returns status {@link Status.UNKNOWN}
     * if the experienced status is not set by the Call Manager.
     * @throws Exception if test case fails.
     */
    public void testServiceRequestWhereResponseLacksStatus() throws Exception {
        waitForOptions(SEND_RESPONSE, Status.UNKNOWN);
        Status status = diagnoseService.serviceRequest(serviceInstance);
        assertEquals(Status.UNKNOWN, status);
        assertNoError();
    }

    /**
     * Verifies that a service request returns status {@link Status.UP}
     * if the experienced status is set to up by the Call Manager.
     * @throws Exception if test case fails.
     */
    public void testServiceRequestWhereResponseContainsStatusUp()
            throws Exception {
        waitForOptions(SEND_RESPONSE, Status.UP);
        Status status = diagnoseService.serviceRequest(serviceInstance);
        assertEquals(Status.UP, status);
        assertNoError();
    }

    /**
     * Verifies that a service request returns status {@link Status.DOWN}
     * if the experienced status is set to down by the Call Manager.
     * @throws Exception if test case fails.
     */
    public void testServiceRequestWhereResponseContainsStatusDown()
            throws Exception {
        waitForOptions(SEND_RESPONSE, Status.DOWN);
        Status status = diagnoseService.serviceRequest(serviceInstance);
        assertEquals(Status.DOWN, status);
        assertNoError();
    }

    /**
     * Verifies that a service request returns status {@link Status.IMPAIRED}
     * if the experienced status is set to impaired by the Call Manager.
     * @throws Exception if test case fails.
     */
    public void testServiceRequestWhereResponseContainsStatusImpaired()
            throws Exception {
        waitForOptions(SEND_RESPONSE, Status.IMPAIRED);
        Status status = diagnoseService.serviceRequest(serviceInstance);
        assertEquals(Status.IMPAIRED, status);
        assertNoError();
    }

    /**
     * Verifies that a service request returns status {@link Status.DOWN}
     * if the service request times out.
     * @throws Exception if test case fails.
     */
    public void testServiceRequestThatTimesOut() throws Exception {
        waitForOptions(DO_NOT_SEND_RESPONSE, null);
        Status status = diagnoseService.serviceRequest(serviceInstance);
        assertEquals(Status.DOWN, status);
        assertNoError();

    }

    /**
     * Verifies that a SIP request can be received and is ignored by the Call
     * Manager's diagnose service.
     * @throws Exception
     */
    public void testSipRequestReceivedByDiagnoseService() throws Exception {
        simulatedPhone.sendRegister(false);
        waitForOptions(SEND_RESPONSE, Status.UP);
        Status status = diagnoseService.serviceRequest(serviceInstance);
        assertEquals(Status.UP, status);
        assertNoError();
    }
}
