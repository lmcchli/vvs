/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.SessionImpl;
import com.mobeon.masp.execution_engine.session.ISessionFactory;

public class SessionFactory implements ISessionFactory {
    public ISession create() {
        return new SessionImpl();
    }
}
