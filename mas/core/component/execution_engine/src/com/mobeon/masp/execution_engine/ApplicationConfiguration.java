/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Abcxyz 2009
 *
 * The copyright to the computer program(s) herein is the property of
 * Abcxyz Inc. The programs may be used and/or copied only with written
 * permission from Abcxyz Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.mobeon.masp.execution_engine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.GroupCardinalityException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.UnknownGroupException;

/**
 * The purpose of this class is to read the configuration parameters for the
 * applications.
 */
public class ApplicationConfiguration {

    // default values for the configuration
    private static final String DEFAULT_WORKING_DIR = "/opt/moip/mas/";

    /**
     * Current configuration
     */
    private final AtomicReference<Map<String, Object>> mConfiguration =
            new AtomicReference<Map<String, Object>>();

    // configuration parameter keys

    /**
     * group name in configuration file.
     */
    private static final String CONFIGURATION_GROUP_NAME = CommonOamManager.MAS_SPECIFIC_CONF;

    /**
     * working directory of the vva
     */
    public static final String WORKING_DIR = "applicationWorkingDir";

    /**
     * singleton instance
     */
    private static final ApplicationConfiguration INSTANCE =
            new ApplicationConfiguration();

    /**
     * Initial configuration instance. Is used to read an updated configuration
     * when the "configuration has changed"-event is received.
     */
    private IConfiguration mInitialConfiguration;

    /**
     * Private constructor of ToolsConfiguration. Creates a new instance of
     * <code>ToolsConfiguration</code>.
     */
    private ApplicationConfiguration() {

    }

    /**
     * @return The singleton instance of ToolsConfiguration.
     */
    public static ApplicationConfiguration getInstance() {
        return INSTANCE;
    }

    /**
     * Sets the initial configuration instance. This method should only be
     * called once when the stream component is initiated.
     *
     * @param config The initial configuration isntance.
     * @throws IllegalArgumentException If <code>config</code> is
     *         <code>null</code>.
     */
    public void setInitialConfiguration(final IConfiguration config) {
        if(config == null) {
            throw new IllegalArgumentException(
                    "Parameter config may not be null");
        }
        mInitialConfiguration = config;
    }

    /**
     * Updates by reading the latest configuration information.
     *
     * @throws GroupCardinalityException
     * @throws UnknownGroupException
     */
    public void update() throws GroupCardinalityException,
            UnknownGroupException {

        final IConfiguration configuration =
                mInitialConfiguration.getConfiguration();
        final IGroup configGroup =
                configuration.getGroup(CONFIGURATION_GROUP_NAME);
        final Map<String, Object> newConfig = new HashMap<String, Object>();

        // add the configuration for the vva working directory
        newConfig.put(WORKING_DIR, configGroup.getString(WORKING_DIR,
                DEFAULT_WORKING_DIR));

        final Map<String, Object> oldConfig = mConfiguration.get();
        mConfiguration.set(newConfig);
        if(oldConfig != null) {
            oldConfig.clear();
        }

    }

    /**
     * @return The working directory for vva.
     */
    public String getWorkingDir() {
        if(mConfiguration.get() == null) {
            throw new IllegalStateException("Methods setInitialConfiguration"
                    + " and update must be called first.");
        }

        final String workingDir =
                (String) mConfiguration.get().get(WORKING_DIR);
        return (workingDir != null) ? workingDir : DEFAULT_WORKING_DIR;
    }

}
