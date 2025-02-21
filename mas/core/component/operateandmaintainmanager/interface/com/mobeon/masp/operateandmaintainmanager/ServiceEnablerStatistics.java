/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.operateandmaintainmanager;

/**
 * Handles counter operation for the ServiceEnabler
 */
public interface ServiceEnablerStatistics {
    /**
     * Set the protocol for the registered service enabler
     * @param protocol
     */
    void setProtocol(String protocol);

    /**
     *  Set max connections used by the service enabler.
     * @param connections
     */
    void setMaxConnections(Integer connections);

    /**
     * Sets the counter valure for the counter defined by type and direction.
     * @param type
     * @param direction
     * @param CurrConn
     */
    void setCurrentConnections(String type,String direction,int CurrConn);

    /**
     * Ancrement counter value for the  counter defined by type, resul and direction
     * @param type
     * @param result
     * @param direction
     */
    void incrementNumberOfConnections(String type,String result,String direction);
}
