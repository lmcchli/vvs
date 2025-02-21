/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess;

import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util.IPlatformAccessProfiler;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;

public class PlatformAccessProfiler implements IPlatformAccessProfiler {

    private static PlatformAccessProfiler instance = null;

    private PlatformAccessProfiler() {
    }

    public static PlatformAccessProfiler get() {
        if (instance == null) {
            instance = new PlatformAccessProfiler();
        }
        return instance;
    }

    @Override
    public Object enterProfilerPoint(String pointName) {
        Object result = null;
        if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
            result = CommonOamManager.profilerAgent.enterCheckpoint(pointName);
        }
        return result;
    }

    @Override
    public void exitProfilerPoint(Object objectPoint) {
        if (objectPoint != null) {
            CommonOamManager.profilerAgent.exitCheckpoint(objectPoint);            
        }
    }

}
