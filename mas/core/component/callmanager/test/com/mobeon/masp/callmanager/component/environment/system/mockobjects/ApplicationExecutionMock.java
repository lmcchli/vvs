/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.system.mockobjects;

import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.operateandmaintainmanager.Supervision;

/**
 * A mock of IApplicationExecution used for testing.
 * This class is immutable.
 */
public class ApplicationExecutionMock implements IApplicationExecution {
    private ISession session;
    private final IEventDispatcher eventDispatcher;

    public ApplicationExecutionMock(ISession session,
                                    IEventDispatcher eventDispatcher) {
        this.session = session;
        this.eventDispatcher = eventDispatcher;
    }

    public void start() {

    }

    public void terminate() {
    }

    public ISession getSession() {
        return session;
    }

    public void setSession(ISession iSession) {
        session = iSession;
    }

    public IEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public void setSupervision(Supervision supervision) {
    }
}
