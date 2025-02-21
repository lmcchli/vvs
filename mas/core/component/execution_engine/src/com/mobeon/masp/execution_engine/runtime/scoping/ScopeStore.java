package com.mobeon.masp.execution_engine.runtime.scoping;

import com.mobeon.masp.execution_engine.runtime.scoping.ScopeConnector;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2005-aug-26
 * Time: 17:39:40
 * To change this template use File | Settings | File Templates.
 */
public interface ScopeStore {
    public ScopeConnector getConnector();
}
