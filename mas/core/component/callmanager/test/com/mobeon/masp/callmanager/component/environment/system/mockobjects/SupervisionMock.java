/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.system.mockobjects;

import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.masp.operateandmaintainmanager.ProvidedService;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerOperate;
import com.mobeon.masp.operateandmaintainmanager.ConsumedService;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerInfo;
import com.mobeon.masp.operateandmaintainmanager.SessionInfoFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A mock of Supervision used for testing.
 * This class is immutable.
 * @author Malin Flodin
 */
public class SupervisionMock implements Supervision {

    private final ServiceEnablerInfo serviceEnablerInfo;
    private AtomicReference<ServiceEnablerOperate> serviceEnablerOperate =
            new AtomicReference<ServiceEnablerOperate>();

    public SupervisionMock(ServiceEnablerInfo serviceEnablerInfo) {
        this.serviceEnablerInfo = serviceEnablerInfo;
    }

    public ProvidedService createProvidedServiceEntry(
            String ServiceName, String Host, Integer Port,
            ServiceEnablerOperate serviceEnablerOperate) {
        return null;
    }

    public ConsumedService createConsumedServiceEntry(
            String ServiceName, String Host, int Port) {
        return null;
    }

    public ServiceEnablerInfo getServiceEnablerStatistics(
            ServiceEnablerOperate serviceEnablerRef) throws Exception {
        this.serviceEnablerOperate.set(serviceEnablerRef);
        return serviceEnablerInfo;
    }

    public SessionInfoFactory getSessionInfoFactory() {
        return null;
    }

    // ============= Methods placed here for basic testing ====================

    public void lock() {
        serviceEnablerOperate.get().close(true);
    }

    public void unlock() {
        serviceEnablerOperate.get().open();
    }

    public void shutdown() {
        serviceEnablerOperate.get().close(false);
    }

    public void updateThreshold(int hwm, int lwm, int max) {
        serviceEnablerOperate.get().updateThreshold(hwm, lwm, max);
    }
}
