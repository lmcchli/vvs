 /**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.controlport;

public class ThreadRequest extends ActionRequest {
    protected static final String usage = "threads [<name> <operation>]";
    protected String name;
    protected String operation;

    public ThreadRequest() {
	super();
	manager = new StateManager();
	action = "thread";
    }

    public void setParameters(String parameters) throws BadParametersException {
	super.setParameters(parameters);
	if(parameters == null) {
	    name = null;
	    operation = null;
	    throw new BadParametersException("Expected thread name and operation.");
	}

	int indexOfName = parameters.indexOf(" ");
	if(indexOfName > 0) {
	    if(parameters.length() > indexOfName+2) {
		name = parameters.substring(0, indexOfName);
		name.trim();
		operation = parameters.substring(indexOfName + 1);
		operation.trim();
	    }
	    else
		throw new BadParametersException("Expected thread name and operation. Could not get operation.");
	}
	else {
	    throw new BadParametersException("Expected thread name and operation. Make sure that whitespace is on correct places.");
	}
    }

    public boolean send() {
	response = ((StateManager) manager).thread(name, operation);
	return true;
    }
    
    public String getUsage() {
	return "Make thread operation";
    }
}
