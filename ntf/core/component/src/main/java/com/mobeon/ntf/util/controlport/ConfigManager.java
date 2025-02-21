/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.controlport;


import com.mobeon.ntf.Config;

/**********************************************************************************
 * This class knows about NTF configuration file(s).
 */

class ConfigManager extends ActionManager {
    
    

    public ConfigManager() {
	super();
    }
    
    public boolean reReadConfig() {
	Config.updateCfg();
	return true;
    }
    
    
    /**********************************************************************************
     * This is a very ugly implementation but practical very fast and
     simple. Since none of the config files share parameters, we can set them to
     whatever we want!
     @return true if the parameter existed, false otherwise.
    */
    public boolean setConfigParameter(String parameter, String value) {
	boolean existed = false;


	Object o = Config.getConfigValue(parameter);
	if(o != null) {
	    Config.setCfgVar(parameter, value);
	    existed = true;
	}
	
	if(!existed) {
	    Config.setCfgVar(parameter, value);
	}
	
	return existed;
    }

    /**********************************************************************************
     * This method get a parameter from the notification config class.
     @return the value for the parameter or null if the parameter is not set in
     any of the config files supported by this manager.
    */
    public String getParameter(String parameter) {
	String r = null;
	if(parameter == null) {
	    r = "All config values. Not implemented.";
	}
	else {
	    r = Config.getConfigValue(parameter);
	}
	
	return r;
    }
}





