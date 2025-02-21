package com.mobeon.masp.chargingaccountmanager;

import org.apache.xmlrpc.XmlRpcException;

import java.util.List;

/**
 * This is an interface for the AIR communication.
 *
 * @author emahagl
 */
public interface IAir {

    /**
     * Executes an AIR request
     *
     * @param requestName Name on the request
     * @param params      Contains a list with parameters
     * @return the AIR response
     * @throws XmlRpcException
     */
    public Object execute(String requestName, List params) throws XmlRpcException;

    /**
     * This method is used to see if this Air unit is available i.e. that no error has been detected during previous
     * requests
     *
     * @return true if available, false if not.
     */
    public boolean isAvailable();
}
