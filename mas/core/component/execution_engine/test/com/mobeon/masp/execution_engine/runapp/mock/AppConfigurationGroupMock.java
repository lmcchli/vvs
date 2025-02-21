package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.common.configuration.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
/**
 * Mock to be used for misc Application configuration.
 *
 * @author Torsten Eriksson
 */

public class AppConfigurationGroupMock implements IGroup {

    static private Map<String, String> stringConfigurationMap =
            new HashMap<String, String>();


    // Sets a configuration parameter to a particular value. If not
    // found the parameter is added to the configuration
    static public void setParameter(String parameterName, String val){
        if (stringConfigurationMap.containsKey(parameterName)) {
	        stringConfigurationMap.remove(parameterName);
        }
    	stringConfigurationMap.put(parameterName, val);
    }

    // Clear the configuration
    static public void clear(){
        stringConfigurationMap.clear();
    }

    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getFullName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public IGroup getGroup(String name) throws GroupCardinalityException, UnknownGroupException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<IGroup> getGroups(String name) throws UnknownGroupException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> listGroups() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> listParameters() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getInteger(String name) throws UnknownParameterException, ParameterTypeException {
        throw new UnknownParameterException(name, this);
    }

    public int getInteger(String name, int defaultValue) throws ParameterTypeException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getFloat(String name) throws UnknownParameterException, ParameterTypeException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getFloat(String name, double defaultValue) throws ParameterTypeException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getString(String parameterName) throws UnknownParameterException {
        if (stringConfigurationMap.containsKey(parameterName)) {
	    return stringConfigurationMap.get(parameterName);
	}
	throw new UnknownParameterException("Configuration parameter not found:"
                        + parameterName, this);
    }

    public String getString(String name, String defaultValue) {
    	String value;
		try {
			value = getString(name);
		} catch (UnknownParameterException e) {
			value = defaultValue;
			
		}
    	
        return value;
    }

    public boolean getBoolean(String name) throws UnknownParameterException, ParameterTypeException {
        return false;  //TODO: Not implemented
    }

    public boolean getBoolean(String name, boolean defaultValue) throws ParameterTypeException {
        return false;  //TODO: Not implemented
    }

    public String getText() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

	@Override
	public ArrayList<String> getList(String listName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Map<String, String>> getTable(String tableName) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getTableParameter(String tableName, String tableItemKey, String paramName) {
		return null;
	}
}
