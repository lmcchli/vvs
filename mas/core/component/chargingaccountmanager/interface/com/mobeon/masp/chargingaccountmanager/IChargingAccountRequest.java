package com.mobeon.masp.chargingaccountmanager;

import java.util.HashMap;

/**
 * Interface for a ChargingAccountRequest
 *
 * @author emahagl
 */
public interface IChargingAccountRequest {

    /**
     * Sets name on the request
     *
     * @param name
     */
    public void setName(String name);

    /**
     * Adds a parameter to the request.
     *
     * @param name
     * @param value
     * @throws ChargingAccountException
     */
    public void addParameter(String name, String value)
            throws ChargingAccountException;

    /**
     * Adds a parameter struct into an array
     *
     * @param arrayName name of the array
     * @param params
     * @param values
     * @throws ChargingAccountException
     */
    public void addArrayParameter(String arrayName, String[] params, String[] values)
            throws ChargingAccountException;

    /**
     * Returns name on the request
     *
     * @return the name
     */
    public String getName();

    public HashMap<String, Object> getParameters();
}
