package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.common.configuration.IConfigurationManager;

/**
 * User: QMIAN
 * Date: 2006-jun-21
 * Time: 10:35:43
 */
public interface EnginePool {
    void release(Engine resource);

    Engine createEngine(IConfigurationManager configurationManager);
}
