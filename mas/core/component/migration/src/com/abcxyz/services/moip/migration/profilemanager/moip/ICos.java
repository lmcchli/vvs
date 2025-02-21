/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

import java.util.Map;
import java.util.Set;

/**
 * Interface used to get class of service attributes
 */
public interface ICos extends SubscriberSettingRead {

    /**
     * Get the aggregated set of attributes interpreted as strings.
     * I.e. the boolean or integer attributes are parsed to "true" "false" 
     * and "12345". It is assumed that this is only used for listing attributes.
     *
     * @return set of requested attributes in string format representing <key, multi/single-value attribute>  
     */
    Set<Map.Entry<String, String[]>> getAttributes();
    
}
