/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.operateandmaintainmanager.Supervision;

/**
 *   This interface provides functionality for execution of apps implemented in CCXML/VoiceXML
 *   Each user session has its own instance of the interface, i.e the control of the execution
 *   of a specific application is related to the specific user session.
 */
public interface IApplicationExecution {
    /**
     * Starts the application
     * <p>
     */
    public void start();

    /**
     * Sets the ISession object related to this session. This function is preferably invoked before start().
     * @param session
     */
    public void setSession(ISession session);

    /**
     * Returns the Session related to the ApplicationExecutionImpl instance
     * <p>
     * @return the Session
     */
    public ISession getSession();

    /**
     * Returns the IEventDispatcher related to the ApplicationExecutionImpl instance.
     * Events occuring on this execution of the application shall be notified on
     * the returned IEventDispatcher.
     * <p>
     * @return the IEventDispatcher
     */
    public IEventDispatcher getEventDispatcher();


    public void setSupervision(Supervision supervision);

    /**
     * Terminate the application
     */
    public void terminate();


}
