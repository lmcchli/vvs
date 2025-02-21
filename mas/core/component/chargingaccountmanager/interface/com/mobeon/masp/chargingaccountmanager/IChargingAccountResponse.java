package com.mobeon.masp.chargingaccountmanager;

/**
 * Interface for a ChargingAccountResponse
 *
 * @author emahagl
 */
public interface IChargingAccountResponse {

    /**
     * Returns the responsecode
     *
     * @return the responsecode
     */
    public int getResponseCode();

    /**
     * Retrieves a parameter from the response
     *
     * @param name
     * @return the value of the parameter, null if not found
     */
    public String getParameter(String name);
}
