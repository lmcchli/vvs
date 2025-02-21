/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.session;

import java.util.Map;

import com.mobeon.masp.execution_engine.ccxml.runtime.Id;

/**
 * The Session interface represents a user session where all relevant
 * user data is stored. It is possible to get and set name/value pairs which
 * may be used from the application
 */
public interface ISession {

    /**
     * Retrieve the GUID of the current session. The ID is unique  across all
     * JVMs
     *
     * @return the id
     */
    public String getId();

    /**
     * If the id has a prefix, this method will return what is after the prefix
     *
     * @return the id
     */
    public String getUnprefixedId();

    /**
     * Set the session id. The existing id will be overridden. The ID shall be unique across all JVMs.
     * This method is used when the session ID is needed, e.g. for logging purposes, before the actual session
     * has been created.
     *
     * @param id
     */
    public void setId(Id<ISession> id);

    /**
     * Set the MDC items. The existing MDC items will be overridden.
     * This method is used when MDC logging information is needed before the
     * actual session has been created.
     *
     * @param sessionMdcItems
     */
    public void setMdcItems(SessionMdcItems sessionMdcItems);

    /**
     * Destroy all data related to the session
     */
    public void dispose();

    /**
     * Set value for the specified name. If the a value associated with name
     * exist it will be overwritten.
     *
     * @param name
     * @param value
     */
    public void setData(String name, Object value);

    /**
     * Get the value associated with name
     *
     * @param name
     * @return The value related to the requested name
     */
    public Object getData(String name);

    /**
     * Get the map
     *
     * @return The Map object
     */
    public Map<String, Object> getMap();

    /**
     * Register session data to be propagated to the Mapped Diagnostic Context. The data set in this call
     * enables traceing of a session based on the value of the data set in the call
     *
     * @param name
     * @param value
     */
    public void setSessionLogData(String name, Object value);

    /**
     * Helper method used to register the session id, and the name/value pairs registered using setSessionLogData,
     * in the Mapped Diagnostic Context
     */
    public void registerSessionInLogger();

    /**
     * key for "session initiator" used in O&M monitoring
     */
    String SESSION_INITIATOR = "sessioninitiator";
    /**
     * key for "service request" used by ServiceRequestManager and Application
     */
    String SERVICE_REQUEST = "Service_Request";
    /**
     * key for the master execution context in EE
     */
    String MASTER_EXECUTION_CONTEXT = "Master_Execution_Context";

    /**
     * Name of the service used in this session
     */
    String SERVICE_NAME = "Service_Name";

    Id<ISession> getIdentity();
}
