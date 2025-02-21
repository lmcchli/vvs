/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistry;
import com.mobeon.masp.execution_engine.runtime.event.EventHub;
import com.mobeon.masp.execution_engine.runtime.event.HandlerLocator;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.EventSourceManager;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGeneratorImpl;
import com.mobeon.masp.execution_engine.compiler.ApplicationCompilerImpl;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.dummies.CCXMLExecutionContextDummy;
import com.mobeon.masp.execution_engine.dummies.CCXMLRuntimeData;
import com.mobeon.masp.execution_engine.session.SessionImpl;
import com.mobeon.masp.util.url.TestHandlerFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;

import java.net.URI;
import java.util.Arrays;

/**
 * Engine Tester.
 *
 * @author Mikael Andersson
 * @version 1.0
 * @since <pre>09/22/2005</pre>
 */
public class EngineTest extends RuntimeCase {

    public EngineTest(String name) {
        super(name);
    }

    public void testExecuteNext() throws Exception {
        RuntimeFactory factory = RuntimeFactories.getInstance(Constants.MimeType.VOICEXML_MIMETYPE);
        Engine e = factory.createEngine(null);
        e.setTrace(false);
        ExecutionContext exc = factory.createExecutionContext(new SessionImpl(), platformFactoryInstance(), null, null);

        e.setExecutionContext(exc);
        exc.setEngine(e);

        Mock mockNop = mock(Executable.class);
        mockNop.expects(once()).method("execute").with(eq(exc));
        mockNop.expects(once()).method("execute").with(eq(exc));
        Executable nop = (Executable) mockNop.proxy();
        e.push(Arrays.asList(new Executable[]{nop, nop}));
        e.executeNext();
    }


    public void testSimpleCCXML() throws Exception {
        ApplicationCompilerImpl appc = new ApplicationCompilerImpl();
        URI uri = new URI("test:///ccxml/hello_world.ccxml");
        Module module = appc.compileDocument(uri, new ModuleCollection(null,uri, "application/xml+ccxml"));

        RuntimeFactory factory = RuntimeFactories.getInstance(Constants.MimeType.CCXML_MIMETYPE);
        Engine e = factory.createEngine(null);
        e.setTrace(false);
        ExecutionContext exc = factory.createExecutionContext(new SessionImpl(),platformFactoryInstance(), null, null );


        e.setExecutionContext(exc);
        exc.setEngine(e);

        e.push(Arrays.asList(new Executable[]{module.getProduct()}));
        e.executeNext();

        log.info("\n" + module.getProduct().toMnemonic());
    }

    public void testSimpleVoiceXML() throws Exception {
        ApplicationCompilerImpl appc = new ApplicationCompilerImpl();
        URI documentURI = new URI("test:///vxml/simple_test.vxml");
        Module app = appc.compileDocument(documentURI, new ModuleCollection(null, documentURI, "application/xml+vxml"));

        RuntimeFactory factory = RuntimeFactories.getInstance(Constants.MimeType.VOICEXML_MIMETYPE);

        Engine e = factory.createEngine(null);
        e.setTrace(false);
        DialogExecutionContext exc = (DialogExecutionContext)factory.createExecutionContext(new SessionImpl(),
                                                                                            platformFactoryInstance(), null, null);

        e.setExecutionContext(exc);
        exc.setEngine(e);
        RuntimeData runtimeData = new RuntimeData();
        runtimeData.setEventSourceManager(createConnectionManager());
        exc.setDialog(new Dialog(
            IdGeneratorImpl.PARTY_GENERATOR.generateId(),
            documentURI.toString(),
            Constants.MimeType.VOICEXML_MIMETYPE, new CCXMLExecutionContextDummy(null,runtimeData)));

        e.push(Arrays.asList(new Executable[]{app.getProduct()}));
        e.executeNext();
        log.info("\n" + app.getProduct().toMnemonic() );

    }

    public static Test suite() {
        return new TestSuite(EngineTest.class);
    }

    private class RuntimeData implements CCXMLRuntimeData {
        private EventSourceManager eventSourceManager;

        public EventSourceManager getConnectionManager() {
            return eventSourceManager;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Scope getCurrentScope() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public ValueStack getValueStack() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public EventHub getEventHub() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public EventProcessor getEventProcessor() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public HandlerLocator getHandlerLocator() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Engine getEngine() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public ScopeRegistry getScopeRegistry() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public ExecutionContext getExecutionContext() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setEventSourceManager(EventSourceManager eventSourceManager) {
            this.eventSourceManager = eventSourceManager;

        }
    }

}
