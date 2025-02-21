/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.APlatformAccessPlugin;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

public interface PlatformAccessFactory {
    PlatformAccess create(ExecutionContext executionContext);

    PlatformAccessUtil createUtil(ExecutionContext ex);

    APlatformAccessPlugin createPlugin(ExecutionContext ex);
}
