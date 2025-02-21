/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.operateandmaintainmanager;

/**
 * Handles counter operation for the ServiceEnabler
 */
public interface ServiceEnablerInfo {
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
     * This function is removed
     * Sets the counter valure for the counter defined by type and direction.
     * @param type
     * @param direction
     */
    //void setCurrentConnections(CallType type,CallDirection direction,int CurrConn);

    /**
     * Increments the counter valure for the counter defined by type and direction.
     * @param type
     * @param direction
     */
    //void setCurrentConnections(CallType type,CallDirection direction,int CurrConn);
    void incrementCurrentConnections(CallType type,CallDirection direction);

    /**
     * Decrements the counter valure for the counter defined by type and direction.
     * @param type
     * @param direction
     */
    void decrementCurrentConnections(CallType type,CallDirection direction);

    /**
     * Increment counter value for the  counter defined by type, resul and direction
     * @param type
     * @param result
     * @param direction
     */
    void incrementNumberOfConnections(CallType type,CallResult result,CallDirection direction);

    /**
     * Increment counter value specified by 'incrementBy' for the counter defined by type, resul and direction
     * @param type
     * @param result
     * @param direction
     * @param incrementBy
     */
    void incrementNumberOfConnections(CallType type,CallResult result,CallDirection direction,Integer incrementBy);



    /**
     * After a shutdown is complete. Call this method to set service enabler to close.
     */
    //void shutdownComplete();

    /**
     * After a open is complete. Call this method to set service enabler to opened.
     */
    void opened();

    /**
     * After a close is complete. Call this method to set service enabler to closed.
     */
    void closed();

}
