/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.operateandmaintainmanager;

/**
 * Interface that handles operations on a service enabler.
 *
 */

public interface ServiceEnablerOperate {

    /**
     * open all services on a service enabler
     */
    void open();

    /**
     * close all services on a service enabler
     * If forced is true, all connections is forced to close.
     * If forced is false, all connections is ended in a natural way.
     * @param forced
     */
    void close(boolean forced);


    /**
     * Sets the threshold for the service enabler.
     * @param threshold
     */
    void updateThreshold(int highWaterMark,int lowWaterMark,int threshold);



    /**
     *
     * returns protocol name for the service enabler. ie xmp, sip..
     * @return The protocol name of the service enabler.
    */
    String getProtocol();


    /**
     *
     * Returns a string representation of the object.
     * The string shuld have the format'protocolName+":"+host+":"+port'.
     * Ie "sip:localhost:154".
     * @return The string representation of the object.
    */
    String toString();


}
