/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.trafficeventsender;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Contains configuration parameters used for the Radius functionality. Reads the trafficeventsender configuration and
 * loads the relevant parameters.
 *
 * @author ermmaha
 */
public class RadiusConfiguration {

    private static final String NUMBER_ATTR = "number";
    private static final String TYPE_ATTR = "type";
    private static final String DATATYPE_ATTR = "datatype";

    private Map<String, RadiusConfigurationAttribute> radiusAttributeMap =
            new HashMap<String, RadiusConfigurationAttribute>();

    /**
     * @return map with RadiusConfigurationAttribute
     */
    Map<String, RadiusConfigurationAttribute> getRadiusAttributeMap() {
        return radiusAttributeMap;
    }

    protected void readConfiguration(IConfiguration configuration) throws ConfigurationException {
        // param names are case-sensitive
        IGroup radiusGroup = configuration.getGroup(CommonOamManager.BACK_END_CONF);

        loadRadiusMethodMap(radiusGroup);

    }

    private void loadRadiusMethodMap(IGroup radiusGroup) throws ConfigurationException {
        Map<String, RadiusConfigurationAttribute> newRadiusAttributeMap =
                new HashMap<String, RadiusConfigurationAttribute>();

        Map<String, Map<String, String>> radiusTable = radiusGroup.getTable(ConfigParam.RADIUS_CONFIG_TABLE);
        Map<String, String> radius = null;
        Iterator<String> iter = radiusTable.keySet().iterator();
        while (iter.hasNext()) {
        	String name = iter.next();
        	radius = radiusTable.get(name);
            int number = Integer.parseInt(radius.get(NUMBER_ATTR));
            int type = Integer.parseInt(radius.get(TYPE_ATTR));
            String datatype = radius.get(DATATYPE_ATTR);
            RadiusConfigurationAttribute attr = new RadiusConfigurationAttribute(number, type, datatype);

            newRadiusAttributeMap.put(name, attr);
        }
        radiusAttributeMap = newRadiusAttributeMap;
    }

}
