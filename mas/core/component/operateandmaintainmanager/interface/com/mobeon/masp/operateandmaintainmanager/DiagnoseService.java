package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */

/**
 * This interface must be implemented by any service that wants to perform
 * self diagnostics.
 */
public interface DiagnoseService {
    /**
     * Check the status of a service. This call will block until the status has
     * been verified.
     *
     * @param si A ServiceInstance object containing connection details for the
     *           service that is going to be diagnosed.
     * @return The Status of the service. (UP, DOWN, IMPAIRED or UNKNOWN)
     * @throws IllegalArgumentException Thrown if the connection details for the
     *                                  service are illegal, for example the hostname is null.
     */
    public Status serviceRequest(ServiceInstance si) throws IllegalArgumentException;
}
