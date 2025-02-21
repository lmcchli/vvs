/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util;

import java.util.ArrayList;
import java.util.Map;

/**
 * An interface to provide access to the configuration manager instantiated by the INotifierConfigManagerFactory
 */
public interface INotifierConfigManager {

    /**
     * This method returns the String value of the component parameter Name
     * @param parameterName the name of the configuration parameter
     * @return String value of the looked up parameter
     */
    public String getParameter(String parameterName);

    /**
     * This method return the String value of the TableItem from Table Map of Component. 
     * <br><br>
     * <pre>
     * &lt;tableName&gt;
     *  &lt;item&gt;tableItemKey
     *      &lt;parameterName&gt;return value&lt;/parameterName&gt;
     *  &lt;/item&gt;
     * &lt;tableName&gt;
     * </pre>
     * @param tableName the name of the table in the configuration file
     * @param tableItemKey the name of the element key in the table
     * @param parameterName the name of the configuration parameter to get
     * @return String value of the looked up parameter
     */
    public String getTableParameter(String tableName, String tableItemKey, String parameterName);

    /**
     * This method returns the Map of the component or parameter Name
     * @param tableName the name of the table in the configuration file
     * @return Map of the looked up parameter
     */
    public Map<String, Map<String, String>> getTable(String tableName);

    /**
     * This method returns the ArrayList of the component parameter Name
     * @param listName the name of the table in the configuration file
     * @return ArrayList of the looked up parameter
     */
    public ArrayList<String> getList(String listName);

}
