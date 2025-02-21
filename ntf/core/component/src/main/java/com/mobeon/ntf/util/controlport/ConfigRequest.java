/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.controlport;

public class ConfigRequest extends ActionRequest {
    
    protected String parameterId = null;
    protected String parameterValue = null;
    
    protected static final int REQUEST_SET = 1;
    protected static final int REQUEST_REREAD = 2;
    protected static final int REQUEST_GET = 3;

    protected static final String usage = "config [reread | set [parameter value]]";

    public ConfigRequest() {
	super();
	manager = new ConfigManager();
	action = "config";
    }
    
    
    /**********************************************************************************
     * Set parameters for the ConfigRequest. Expected is:
     - config [reread | set [parameter value]]
     @param parameters is the parameter string that is to be parsed.
     @throws Exception if the parameter string isn't what it should be.
    */
    public void setParameters(String parameters) throws BadParametersException {
	
	//System.out.println("Collected parameters #"+parameters+"#");
	
	if(parameters == null || parameters.length() == 0)
	    throw new BadParametersException("Expected parameters. Usage: " + usage);
	
	if(parameters.toLowerCase().startsWith("set")) {
	    // We have a set request
	    try {
		//System.out.println("Found a set request");
		requestType = REQUEST_SET;
		int indexOfSpace = parameters.indexOf(" ");
		//System.out.println("Find out rest");
		if(indexOfSpace < 0 || parameters.length() <= indexOfSpace)
		    throw new BadParametersException("With set the parameter to set and value is mandatory.\nUsage: " + usage);
		//System.out.println("Get next space");
		int divideParameterIdValue = parameters.indexOf(" ", indexOfSpace + 1);
		//System.out.println("Get parameters");
		if(divideParameterIdValue > 0) {
		    //System.out.println("Get parameter");
		    parameterId = parameters.substring(4, divideParameterIdValue);
		    //System.out.println("Get value of parameters");
		    parameterValue = parameters.substring(divideParameterIdValue+1);
		    
		}
		if(parameterId == null ||
		   parameterValue == null ||
		   parameterId.equals("") ||
		   parameterValue.equals("")) {
		    throw new BadParametersException("Could not garble mandatory parameters for set. Usage: " + usage + "\nExpected both values for parameter and value");
		}
	    }catch(Exception e) {
		throw new BadParametersException("Bad request. Usage: " + usage);
	    }
	}
	else if(parameters.toLowerCase().startsWith("get")) {
	    requestType = REQUEST_GET;
	    int indexOfSpace = parameters.indexOf(" ");
		//System.out.println("Find out rest");
	    if(indexOfSpace < 0 || parameters.length() <= indexOfSpace) {
		parameterId = null;
	    }
	    else
		parameterId = parameters.substring(4);
	}
	else if(parameters.toLowerCase().startsWith("reread")) {
	    requestType = REQUEST_REREAD;
	}
	
	//System.out.println("Parameter " + parameterId + " PVal " + parameterValue + "#");
	
	super.setParameters(parameters);
	// Need to parse the parameters to see that it contain what we want!
    }

    public boolean send() {
	
	if(requestType == REQUEST_SET) {
	
	    if(((ConfigManager) manager).setConfigParameter(parameterId, parameterValue))
		response = "Parameter "+ parameterId+" has been set to " + parameterValue+".";
	    else
		response = "Parameter " +parameterId+" is a new parameter and has been set to " +  parameterValue+".";
	}
	else if(requestType == REQUEST_REREAD) {
	    if(((ConfigManager) manager).reReadConfig())
		response = "Configuration has been updated.";
	    else
		response = "Configuration could not be updated.";
	}
	else if(requestType == REQUEST_GET) {
	    String value = ((ConfigManager) manager).getParameter(parameterId);
	    if(value == null)
		response = "The parameter is not set";
	    else {
		if(parameterId != null)
		    response = "The parameter " + parameterId + " is set to " + value;
		else
		    response = value;
	    }
	}


	return true;
    }

    public String getUsage() {
	return "Set or re-read configuration.";
    }
}
