package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.dummies.DefaultExpectTarget;
import com.mobeon.masp.execution_engine.dummies.ExecutionContextDummy;
import com.mobeon.masp.execution_engine.dummies.VoiceXMLExecutionContextDummy;
import com.mobeon.masp.execution_engine.dummies.VoiceXMLRuntimeData;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.RuntimeCase;
import com.mobeon.masp.execution_engine.runtime.EventProcessor;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import org.jmock.Mock;

/**
 * @author Mikael Andersson
 */
public abstract class VoiceXMLRuntimeCase extends RuntimeCase implements VoiceXMLRuntimeData {
    private Connection connection;

    public VoiceXMLRuntimeCase(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        createMediaObjectFactory();
        connection = createConnection();
    }

    public Connection getConnection() {
        return connection;
    }

    protected IMediaObjectFactory createMediaObjectFactory() {
        mockMediaObjectFactory = mock(IMediaObjectFactory.class);
        return (IMediaObjectFactory) mockMediaObjectFactory.proxy();
    }

    public IMediaObjectFactory getMediaObjectFactory() {
        return (IMediaObjectFactory)mockMediaObjectFactory.proxy();
    }

    protected ExecutionContext createExecutionContext() {
        mockExecutionContext = new Mock(VXMLExecutionContext.class);
        mockExecutionContext.stubs().method("getEventProcessor").will(returnValue(mockEventProcessor.proxy()));
        mockExecutionContext.stubs().method("getEventHub").will(returnValue(mockEventHub.proxy()));
        mockExecutionContext.stubs().method("getValueStack").will(returnValue(mockValueStack.proxy()));

        return (VXMLExecutionContext) mockExecutionContext .proxy();
    }

    protected EventProcessor createEventProcessor() {
        mockEventProcessor = mock(VoiceXMLEventProcessor.class);
        return (EventProcessor) mockEventProcessor.proxy();
    }

}
