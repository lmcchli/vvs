/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.runtime.Engine;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.RuntimeFacade;
import com.mobeon.masp.execution_engine.runtime.event.LifecycleEvent;

import java.net.URI;

public class CCXMLRuntime extends RuntimeFacade {

    public CCXMLRuntime(Engine engine, ExecutionContext executionContext, Module entry, ModuleCollection collection) {
        super(engine, executionContext);
        this.setEntryModule(entry);
        setModuleCollection(collection);
    }

    public void start(Executable ... initialExecutables) {
        super.start(super.getEntryModule().getProduct());
        getExecutionContext().getEventHub().fireEvent(LifecycleEvent.START);
        getExecutionContext().getEventProcessor().runAsNeeded();
    }

    public void wireEventHandling(ExecutionContext newEc, String mimeType, URI uri) {

        if (Constants.MimeType.VOICEXML_MIMETYPE.equals(mimeType)) {

            //Connect eventStreams
            getExecutionContext().getEventStream().swapUpstream(newEc.getEventStream());

        }
    }
}
