/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.configuration;

import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.configuration.IGroup;

public abstract class AbstractConfiguration {
    private static ILogger logger = ILoggerFactory.getILogger(AbstractConfiguration.class);
    protected abstract IServiceInstance getService();

    /**
     * Parameter access wrapper for retreiving String parameters.
     * @param parameterName
     * @param defaultValue
     * @return the retreived value (or the default value)
     */
    public String getParameter(String parameterName, String defaultValue) {
        IServiceInstance service = getService();
        if (service != null) {
        	String parameter = service.getProperty(parameterName);
        	if (parameter != null) { 
        		return parameter;
        	}
        	else {
        		logger.warn("property [" + parameterName +
        				"] is null, using default [" + defaultValue + "]");
        	}
        }
        return defaultValue;
    }

    /**
     * Parameter access wrapper for retreiving int parameters.
     * @param parameterName
     * @param defaultValue
     * @return the retreived value (or the default value)
     */
    protected int getParameter(String parameterName, int defaultValue) {
        String value = getParameter(parameterName, "" + defaultValue);
        return Integer.parseInt(value);
    }
}
