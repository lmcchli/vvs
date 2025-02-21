package com.mobeon.masp.execution_engine.dummies;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;

/**
 * @author Mikael Andersson
 */
public interface VoiceXMLRuntimeData extends RuntimeData {

    public IMediaObjectFactory getMediaObjectFactory();

    public Connection getConnection();
}
