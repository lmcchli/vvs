/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.diagnoseservice;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IGroup;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.net.InetAddress;

/**
 * Configuration variables for Call Managers Diagnose Service.
 * This class is a singleton.
 * <p>
 * The configuration variables are kept in a Map instead of being read directly
 * from a configuration group. There are two main reason for this
 * implementation:
 * <ul>
 * <li>When reading configuration values, there is no need to handle exceptions
 *     issued from the configuration component.</li>
 * <li>When writing testcases, it is useful to be able to modify some
 *     configuration values at runtime. This is not possible if values
 *     are always read from configuration component classes</li>
 * </ul>
 * <p>
 * The drawback is that for each additional configuration parameter, a line
 * that reads the parameter value and inserts it into the Map has to be added.
 *
 * @author Malin Flodin
 */
public class DiagnoseServiceConfiguration {

    private static final ILogger log =
            ILoggerFactory.getILogger(DiagnoseServiceConfiguration.class);

    private static final DiagnoseServiceConfiguration INSTANCE =
        new DiagnoseServiceConfiguration();

    /** Group name in configuration file. */
    private static final String CONFIGURATION_GROUP_NAME = "callmanager";

    private static final String DS = "diagnoseservice";
    private static final String DS_HOST = "host";
    private static final String DS_PORT = "port";

    /** Default port. */
    public static final int DEFAULT_PORT = 5090;

    /** Default address of host. */
    private static String defaultHostAddress = "0.0.0.0";

    /** Current configuration. */
    private AtomicReference<Map<String, Object>> mConfiguration =
        new AtomicReference<Map<String, Object>>();

    /**
     * Initial configuration instance. Is used to read an updated configuration
     * when the "configuration has changed"-event is received.
     */
    private IConfiguration initialConfiguration;

    /**
     * Creates the single DiagnoseServiceConfiguration instance.
     */
    private DiagnoseServiceConfiguration() {
    }

    /**
     * @return The single DiagnoseServiceConfiguration instance.
     */
    public static DiagnoseServiceConfiguration getInstance() {
        return INSTANCE;
    }

    /**
     * Sets the initial configuration instance. This method should only
     * be called once when the Call Manager Diagnose Service is initiated.
     *
     * @param config The initial configuration isntance.
     *
     * @throws IllegalArgumentException  If <code>config</code> is
     *         <code>null</code>.
     */
    public void setInitialConfiguration(IConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException(
                    "Parameter config may not be null");
        }
        initialConfiguration = config;
    }

    /**
     * Reads configuration parameters.
     * @throws ConfigurationException if the configuration could not be parsed.
     */
    public void update() throws ConfigurationException {

        IConfiguration configuration = initialConfiguration.getConfiguration();
        Map<String, Object> newConfig = new HashMap<String, Object>();
        IGroup configGroup = configuration.getGroup(CONFIGURATION_GROUP_NAME);

        try {
            IGroup dsGroup = configGroup.getGroup(DS);
            defaultHostAddress = InetAddress.getLocalHost().getHostAddress();

            // Retrieve host
            newConfig.put(
                    DS_HOST, dsGroup.getString(DS_HOST,defaultHostAddress));

            // Retrieve port
            newConfig.put(
                    DS_PORT, dsGroup.getInteger(DS_PORT, DEFAULT_PORT));

        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Could not retreive the diagnose service configuration, " +
                        "using default values instead. Exception: " + e);
        }

        Map<String, Object> oldConfig = mConfiguration.get();
        mConfiguration.set(newConfig);
        if (oldConfig != null) {
            oldConfig.clear();
        }
    }

    /**
     * @return The port number for SIP communication
     */
    public int getPort() {
        if (mConfiguration.get() == null) {
            throw new IllegalStateException("Methods setInitialConfiguration" +
                    " and update must be called first.");
        }
        Integer i = (Integer)mConfiguration.get().get(DS_PORT);
        return (i != null) ? i : DEFAULT_PORT;
    }

    /**
     * @return Name of host, for example "0.0.0.0" or "127.0.0.1".
     */
    public String getHostName() {
        if (mConfiguration.get() == null) {
            throw new IllegalStateException("Methods setInitialConfiguration" +
                    " and update must be called first.");
        }
        String s = (String)mConfiguration.get().get(DS_HOST);
        return (s != null) ? s : defaultHostAddress;
    }
}
