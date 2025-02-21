package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.eventnotifier.IEventDispatcher;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 12, 2006
 * Time: 8:12:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurationManagerMock implements IConfigurationManager {

    private ConfigurationMock configurationMock = new ConfigurationMock();

    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean reload() throws ConfigurationException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setConfigFile(String... configFile) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setConfigFile(List<String> configFile) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addConfigFile(String configFile) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public IConfiguration getConfiguration() {
        return configurationMock;
    }

    public void clearConfiguration() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean getBackupUsed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
