/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.compiler.Executable;

import java.net.URI;
import java.util.Arrays;

public abstract class RuntimeFacade {
    private Engine engine;
    private ExecutionContext executionContext;
    private Module entryModule;
    private ModuleCollection moduleCollection;

    public RuntimeFacade(Engine engine, ExecutionContext executionContext) {
        this.engine = engine;
        this.executionContext = executionContext;
    }


    public final Engine getEngine() {
        return engine;
    }

    public final void setEngine(Engine engine) {
        this.engine = engine;
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }


    public abstract void wireEventHandling(ExecutionContext createdExecutionContext, String mimeType, URI uri);

    public void start(Executable ... initialExecutables) {
        getExecutionContext().setExecutingModule(getEntryModule());
        getExecutionContext().setModuleCollection(moduleCollection);
        getEngine().push(Arrays.asList(initialExecutables));
    }

    public final void setEntryModule(Module entryModule) {
        this.entryModule = entryModule;
    }

    public final void setModuleCollection(ModuleCollection moduleCollection) {
        this.moduleCollection = moduleCollection;
    }

    public final Module getEntryModule() {
        return entryModule;
    }
}
