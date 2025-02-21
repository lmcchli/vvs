package com.mobeon.masp.chargingaccountmanager;

import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.apache.xmlrpc.XmlRpcException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class is used to send requests for charging account information.
 *
 * @author emahagl
 */
public class ChargingAccountManager implements IChargingAccountManager {
    /**
     * Logger
     */
    private static ILogger log = ILoggerFactory.getILogger(ChargingAccountManager.class);

    private static int clientCounter;
    /**
     * Configuration
     */
    private IConfiguration configuration;
    /**
     * List of airclients
     */
    private List<IAir> airClientList;

    /**
     * Setter for ChargingAccountManager's configuration
     *
     * @param configuration The configuration to use
     */
    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * For test purposes, overrides the configured list of air nodes
     *
     * @param airClientList
     */
    public void setAirClientList(List<IAir> airClientList) {
        this.airClientList = airClientList;
    }

    /**
     * Initiates the object. Must be called after construction!
     */
    public void init() {
        updateConfiguration();
        ChargingAccountManagerConfiguration c = ChargingAccountManagerConfiguration.getInstance();

        List<AirNode> list = c.getAirNodeList();
        airClientList = new ArrayList<IAir>(list.size());
        for (Iterator<AirNode> iterator = list.iterator(); iterator.hasNext();) {
            AirNode airNode = iterator.next();
            airClientList.add(new AirClient(airNode.asURL(), airNode.getUid(), airNode.getPwd()));
        }
    }

    public IChargingAccountRequest createChargingAccountRequest() {
        return new ChargingAccountRequest();
    }

    public synchronized int getNextClientId() {
        int clientId = clientCounter;
        if (clientCounter++ >= airClientList.size() - 1) clientCounter = 0;

        IAir airClient = airClientList.get(clientId);
        if (airClient.isAvailable()) {
            return clientId;
        } else {
            Iterator<IAir> it = airClientList.iterator();
            int id = 0;
            while (it.hasNext()) {
                airClient = it.next();
                if (airClient.isAvailable()) {
                    return id;
                }
                id++;
            }
        }
        // ToDo Exception?
        return clientId;
    }

    public IChargingAccountResponse sendRequest(IChargingAccountRequest request, int clientId)
            throws ChargingAccountException {
        if (log.isInfoEnabled()) log.info("sendRequest: clientId=" + clientId);
        if (clientId < 0 || clientId >= airClientList.size()) {
            throw new ChargingAccountException("Invalid clientId " + clientId);
        }

        IAir airClient = airClientList.get(clientId);
        if (airClient.isAvailable()) {
            try {
                return doRequest(request, airClient);
            } catch (XmlRpcException e) {
                throw new ChargingAccountException(e);
            }
        }

        throw new ChargingAccountException("No more AirClients available, could not send the request");
    }

    private ChargingAccountResponse doRequest(IChargingAccountRequest request, IAir airClient)
            throws XmlRpcException {
        ArrayList<HashMap<String, Object>> v = new ArrayList<HashMap<String, Object>>();
        v.add(request.getParameters());

        Object result = airClient.execute(request.getName(), v);
        return makeChargingAccountResponse(result);
    }

    private ChargingAccountResponse makeChargingAccountResponse(Object result) {
        if (! (result instanceof HashMap)) return null;

        HashMap<String, Object> responseMap = (HashMap<String, Object>) result;
        int responseCode = (Integer) responseMap.get("responseCode");

        ChargingAccountResponse chargingAccountResponse = new ChargingAccountResponse(responseCode);
        chargingAccountResponse.setParameters(responseMap);

        return chargingAccountResponse;
    }

    private void updateConfiguration() {
        ChargingAccountManagerConfiguration.getInstance().setConfiguration(configuration);
        try {
            ChargingAccountManagerConfiguration.getInstance().update();
        } catch (ConfigurationException e) {
            log.error("Exception in updateConfiguration " + e);
        }
    }
}
