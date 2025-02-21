/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.masp.execution_engine.ccxml.runtime.IdGenerator;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.ISessionFactory;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerOperate;

public interface ServiceEnabler {
    public ServiceEnablerOperate initService(String service, String host, int port)
            throws ServiceEnablerException;

    public void setSessionFactory(ISessionFactory sessionFactory);
}
