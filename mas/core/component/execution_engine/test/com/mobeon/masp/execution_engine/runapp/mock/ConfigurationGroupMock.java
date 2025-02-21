package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.common.configuration.*;
import com.mobeon.masp.execution_engine.ApplicationConfiguration;
import com.mobeon.masp.execution_engine.runtime.RuntimeConstants;
import com.mobeon.masp.util.test.MASTestSwitches;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 12, 2006
 * Time: 8:14:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurationGroupMock implements IGroup {
	
	

    public static int engineStackSize = 100;
    public static String traceEnabled = "true";
    public static String alwaysCompile = "false";
    public static String generateOps = "true";
    public static String opsPath = ".";
    public static String hostName = "onehostname";
    public static int callManagerWaitTime = 10000;
    public static int accepttimeout = 5000;
    public static int createcalladditionaltimeout = 5000;
    public static int watchdogtimeout = 60000;
    public static int engineccxmlpoolsize = 35;
    public static int enginevxmlpoolsize = 65;
    public static String workingDir = MASTestSwitches.currentMasDir().toURI().getPath();
    
    

    public static void defaultValues() {
        engineStackSize = 100;
        traceEnabled = "true";
        alwaysCompile = "false";
        generateOps = "true";
        opsPath = ".";
        hostName = "onehostname";
        callManagerWaitTime = 10000;
        accepttimeout = 5000;
        createcalladditionaltimeout = 5000;
        engineccxmlpoolsize = 100;
        enginevxmlpoolsize = 100;
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
        if (name.equals(RuntimeConstants.CONFIG.ENGINE_STACK_SIZE)) {
            return engineStackSize;
        }
        if (name.equals(RuntimeConstants.CONFIG.CALL_MANAGER_WAIT_TIME)) {
            Long time = Long.getLong("com.mobeon.junit.runapp.callmanagerwaittime");
            if (time != null) {
                return time.intValue();
            }
            return callManagerWaitTime;
        }
        if (name.equals(RuntimeConstants.CONFIG.ACCEPT_TIMEOUT)) {
            return accepttimeout;
        }
        if (name.equals(RuntimeConstants.CONFIG.CREATECALL_ADDITIONAL_TIMEOUT)) {
            return createcalladditionaltimeout;
        }
        if (name.equals(RuntimeConstants.CONFIG.WATCHDOG_TIMEOUT))
            return watchdogtimeout;
        if (name.equals(RuntimeConstants.CONFIG.ENGINE_CCXML_POOL_SIZE)) {
            return engineccxmlpoolsize;
        }
        if (name.equals(RuntimeConstants.CONFIG.ENGINE_VXML_POOL_SIZE)) {
            return enginevxmlpoolsize;
        }
        throw new UnknownParameterException(name, null);
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

    public String getString(String name) throws UnknownParameterException {
        if (name.equals(RuntimeConstants.CONFIG.TRACE_ENABLED)) {
            return traceEnabled;
        }
        if (name.equals(RuntimeConstants.CONFIG.ALWAYS_COMPILE)) {
            return alwaysCompile;
        }
        if (name.equals(RuntimeConstants.CONFIG.GENERATE_OPS)) {
            return generateOps;
        }
        if (name.equals(RuntimeConstants.CONFIG.OPSPATH)) {
            return opsPath;
        }
        if (name.equals(RuntimeConstants.CONFIG.HOSTNAME)) {
            return hostName;
        } else if (name.equals(ApplicationConfiguration.WORKING_DIR)){
        	return workingDir;
        }

        return null;
    }

    public String getString(String name, String defaultValue) {
    	try {
			String value = getString(name);
			return value!=null?value:defaultValue;
		} catch (UnknownParameterException e) {
			// TODO Auto-generated catch block
			return defaultValue;
		}

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
