/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.factory.DialogEventFactory;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.runtime.Engine;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.RuntimeFacade;

import java.net.URI;

public class VoiceXMLRuntime extends RuntimeFacade {

    public VoiceXMLRuntime(
            Engine engine, ExecutionContext executionContext, Module entryModule, ModuleCollection moduleCollection) {
        super(engine, executionContext);
        this.setEntryModule(entryModule);
        this.setModuleCollection(moduleCollection);
    }

    public void start(Executable ... initialExecutables) {
        final VXMLExecutionContext executionContext = getExecutionContext();
        if (getEntryModule() == null) {
            DialogEventFactory factory = new DialogEventFactory();
            CCXMLEvent event = factory.create(
                    executionContext,
                    Constants.Event.ERROR_DIALOG_NOT_STARTED,
                    executionContext.getDialog().getSrc()+" was not found",
                    executionContext.getCurrentConnection(),
                    executionContext.getDialog(),
                    DebugInfo.getInstance());
            executionContext.getEventHub().fireEvent(event);
        } else {
            super.start(initialExecutables);
            onStart();
            executionContext.getEventProcessor().runAsNeeded();
        }
    }


    public void wireEventHandling(ExecutionContext createdExecutionContext, String mimeType, URI uri) {
    }

    public VXMLExecutionContext getExecutionContext() {
        return (VXMLExecutionContext) super.getExecutionContext();
    }

    protected void onStart() {
        DialogEventFactory fac = new DialogEventFactory();
        VXMLExecutionContext ex = getExecutionContext();
        if (ex.getParent() == null) {
            CCXMLEvent ev = fac.create(ex, Constants.Event.DIALOG_STARTED, "VXML dialog started", ex.getCurrentConnection(), ex.getDialog(), null);
            ex.getCurrentConnection().join(ex.getDialog(), true, true);
            ex.getEventHub().fireEvent(ev);
        }
        ;
    }

}
