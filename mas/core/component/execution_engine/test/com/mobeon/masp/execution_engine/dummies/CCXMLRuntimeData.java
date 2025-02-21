package com.mobeon.masp.execution_engine.dummies;

import com.mobeon.masp.execution_engine.ccxml.EventSourceManager;

/**
 * @author Mikael Andersson
 */
public interface CCXMLRuntimeData extends RuntimeData {
    EventSourceManager getConnectionManager();
}
