package com.mobeon.masp.servicerequestmanager.diagnoseservice;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.*;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mmawi
 */
public class DiagnoseServiceConfiguration {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private static final DiagnoseServiceConfiguration INSTANCE =
            new DiagnoseServiceConfiguration();

    private IConfiguration configuration;

    private ConcurrentHashMap<String, Object> configurationMap =
            new ConcurrentHashMap<String, Object>();

    private static final String CONFIGURATION_GROUP_NAME = CommonOamManager.MAS_SPECIFIC_CONF;
    private static final String DS_CONFIGURATION_GROUP_NAME = CommonOamManager.MAS_SPECIFIC_CONF;
    private static final String DS_CLIENT_ID = "serviceRequestManagerDiagnoseClientId";
    private static final String DS_REQUST_TIMEOUT = "serviceRequestManagerRequestTimeout";
    private static final String DEFAULT_CLIENT_ID = "diagnoseservice@localhost";
    private static final int DEFAULT_REQUEST_TIMEOUT = 30000;

    /**
     * Hide due to singleton instance.
     */
    private DiagnoseServiceConfiguration() {
    }

    public static DiagnoseServiceConfiguration getInstance() {
        return INSTANCE;
    }

    public void setInitialConfiguration(IConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException(
                    "Parameter configuration may not be null");
        }
        this.configuration = configuration;
    }

    public void updateConfiguration() {
        IConfiguration configuration = this.configuration.getConfiguration();

        try {
            // Get the ServiceRequestManager group
            IGroup configGroup = configuration.getGroup(CONFIGURATION_GROUP_NAME);

            // Retrieve request timeout
            configurationMap.put(DS_REQUST_TIMEOUT,
                    configGroup.getInteger(DS_REQUST_TIMEOUT, DEFAULT_REQUEST_TIMEOUT));

            // Get the DiagnoseService group
            IGroup dsGroup = configGroup.getGroup(DS_CONFIGURATION_GROUP_NAME);

            // Retrieve client id
            configurationMap.put(DS_CLIENT_ID,
                    dsGroup.getString(DS_CLIENT_ID, DEFAULT_CLIENT_ID));

        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Could not retreive the diagnose service configuration, " +
                        "using default values instead. Exception: " + e);
        }
    }

    /**
     * @return the client id for diagnose service.
     */
    public String getClientId() {
        String s = (String)configurationMap.get(DS_CLIENT_ID);
        return (s != null) ? s : DEFAULT_CLIENT_ID;
    }

    /**
     * Sets the client id for diagnose service
     * Used only for testing purposes!
     * @param clientId
     */
    public void setClientId(String clientId) {
        configurationMap.put(DS_CLIENT_ID, clientId);
    }

    /**
     * @return the request timeout
     */
    public int getRequestTimeout() {
        Integer i = (Integer)configurationMap.get(DS_REQUST_TIMEOUT);
        return (i != null) ? i : DEFAULT_REQUEST_TIMEOUT;
    }

    /**
     * Sets the value of request timeout.
     * Used only for testing purposes!
     * @param timeout
     */
    public void setRequestTimeout(int timeout) {
        configurationMap.put(DS_REQUST_TIMEOUT, timeout);
    }
}
