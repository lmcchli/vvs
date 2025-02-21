package com.mobeon.masp.chargingaccountmanager;

/**
 * This interface is used to send requests for charging account information.
 *
 * @author emahagl
 */
public interface IChargingAccountManager {

    /**
     * Sends a charging account request
     *
     * @param request
     * @param clientId
     * @return
     * @throws ChargingAccountException
     */
    public IChargingAccountResponse sendRequest(IChargingAccountRequest request,
                                                int clientId) throws ChargingAccountException;

    /**
     * Retrieves a client id for a specific assigned AIR node.
     *
     * @return next id
     */
    public int getNextClientId();

    /**
     * Creates a new IChargingAccountRequest
     *
     * @return a new IChargingAccountRequest
     */
    public IChargingAccountRequest createChargingAccountRequest();
}
