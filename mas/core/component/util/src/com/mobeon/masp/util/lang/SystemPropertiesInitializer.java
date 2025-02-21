/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.lang;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Properties;
import java.util.Map;

/**
 * This class can be used to initialize Java system properties from a provided {@link Properties} object.
 * @author Håkan Stolt
 */
public class SystemPropertiesInitializer {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(SystemPropertiesInitializer.class);

    private Properties systemProperties;

    public Properties getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Properties systemProperties) {
        this.systemProperties = systemProperties;
    }

    public void init() {
        if(systemProperties != null) {
            for(Map.Entry entry : systemProperties.entrySet()) {
                LOGGER.debug("Setting system property "+entry.getKey()+"="+entry.getValue());
            }
            System.getProperties().putAll(systemProperties);
        }
        if(LOGGER.isDebugEnabled())  {
            for(Map.Entry entry : System.getProperties().entrySet()) {
                LOGGER.debug("[System property "+entry.getKey()+"="+entry.getValue()+"]");
            }
        }
    }

}
