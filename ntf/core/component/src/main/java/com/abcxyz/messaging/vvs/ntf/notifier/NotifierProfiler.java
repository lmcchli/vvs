/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierProfiler;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;

public class NotifierProfiler implements INotifierProfiler {

    private static NotifierProfiler instance = null;

    private NotifierProfiler() {
    }

    public static NotifierProfiler get() {
        if (instance == null) {
            instance = new NotifierProfiler();
        }
        return instance;
    }

    @Override
    public boolean isProfilerEnabled() {
        return CommonOamManager.profilerAgent.isProfilerEnabled();
    }

    @Override
    public Object enterProfilerPoint(String pointName) {
        return CommonOamManager.profilerAgent.enterCheckpoint(pointName);
    }

    @Override
    public void exitProfilerPoint(Object objectPoint) {
        if (objectPoint != null) {
            CommonOamManager.profilerAgent.exitCheckpoint(objectPoint);            
        }
    }

}
