/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.ArrayList;
import java.util.Map;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierConfigManager;


public class NotifierConfigManager implements INotifierConfigManager {
    private ConfigManager configManager = null;
    
    public NotifierConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }
    
    @Override
    public String getParameter(String parameterName) {
        return configManager.getParameter(parameterName);
    }

    @Override
    public String getTableParameter(String tableName, String tableItemKey, String parameterName) {
        return configManager.getTableParameter(tableName, tableItemKey, parameterName);
    }

    @Override
    public Map<String, Map<String, String>> getTable(String tableName) {
        return configManager.getTable(tableName);
    }

    @Override
    public ArrayList<String> getList(String listName) {
        return configManager.getList(listName);
    }
}
