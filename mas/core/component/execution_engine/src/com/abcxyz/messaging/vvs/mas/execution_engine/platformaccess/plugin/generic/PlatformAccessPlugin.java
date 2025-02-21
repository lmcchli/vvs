/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.generic;

import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.APlatformAccessPlugin;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.IPlatformAccessServicesManager;
import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.PlatformAccessPluginException;

/**
 * This class is for future use.
 * If a default platformaccess plug-in as to be packaged within the product, this class shall be used. 
 */
public class PlatformAccessPlugin extends APlatformAccessPlugin {

    public void initialize(IPlatformAccessServicesManager platformAccessServicesManager) throws PlatformAccessPluginException {
        return;
    }

    public boolean refreshConfig() {
        return true;
    }

}
