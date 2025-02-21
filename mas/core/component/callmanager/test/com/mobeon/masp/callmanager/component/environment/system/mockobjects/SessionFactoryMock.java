/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.system.mockobjects;

import com.mobeon.masp.execution_engine.session.ISessionFactory;
import com.mobeon.masp.execution_engine.session.ISession;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * TODO: Document
 * @author Malin Nyfeldt
 */
public class SessionFactoryMock extends MockObjectTestCase {
    private final Mock sessionFactoryMock = new Mock(ISessionFactory.class);
    private final ISession session;

    public SessionFactoryMock(ISession session) {
        super();
        this.session = session;

        stubCreateMethod();
    }

    public ISessionFactory getSessionFactory() {
        return (ISessionFactory)sessionFactoryMock.proxy();
    }

    private void stubCreateMethod() {
        sessionFactoryMock.stubs().method("create").will(returnValue(session));
    }

    // This is only included to make IntelliJ happy. Not used at all.
    public SessionFactoryMock() {
        session = null;
    }

    public void testDoNothing() throws Exception {
    }
}
