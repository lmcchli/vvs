/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess;

import java.util.List;
import java.util.Map;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util.IPlatformAccessConfigManager;

public class PlatformAccessConfigManager implements IPlatformAccessConfigManager {

    private ConfigManager configManager = null;

    public PlatformAccessConfigManager(ConfigManager configManager) {
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
    public List<String> getList(String listName) {
        return configManager.getList(listName);
    }

}
