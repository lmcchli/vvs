package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.GroupCardinalityException;
import com.mobeon.common.configuration.UnknownGroupException;
import com.mobeon.masp.execution_engine.runtime.RuntimeConstants;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 12, 2006
 * Time: 8:13:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurationMock implements IConfiguration {

    ConfigurationGroupMock groupMock = new ConfigurationGroupMock();
    AppConfigurationGroupMock appGroupMock = new AppConfigurationGroupMock();


    public IGroup getGroup(String name) throws GroupCardinalityException, UnknownGroupException {
        if(name.equals( RuntimeConstants.CONFIG.GROUP_NAME)){
            return groupMock;
        } else {
            return null;
        }
    }

    public List<IGroup> getGroups(String name) throws UnknownGroupException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasGroup(String name) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IConfiguration getConfiguration() {
        return new ConfigurationMock();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean getBackupUsed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
