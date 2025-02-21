/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.masp.execution_engine.compiler.IApplication;

public interface IApplicationExecutionComponent extends IApplicationExecution {
    public void setApplication(IApplication app);


}
