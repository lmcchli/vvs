 /**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.controlport;

public class ThreadsRequest extends ActionRequest {
    protected static final String usage = "threads [all | outdial | in]";

    public ThreadsRequest() {
	super();
	manager = new StateManager();
	action = "threads";
    }

    public void setParameters(String subSystem) throws BadParametersException {
	super.setParameters(subSystem);
	if(subSystem == null) {
	    // All sub systems
	    requestType = StateManager.ALL_SUBSYSTEMS;
	    return;
	}

	String lSubSystem = subSystem.toLowerCase();
	if(lSubSystem.startsWith("all")) {
	    requestType = StateManager.ALL_SUBSYSTEMS;
	}
	else if(lSubSystem.startsWith("outdial")) {
	    requestType = StateManager.OUTDIAL_SUBSYSTEM;
	}
	else if(lSubSystem.startsWith("in")) {
	    requestType = StateManager.IN_SUBSYSTEM;
	}
	else
	    throw new BadParametersException("Not supported subsystem " + subSystem +
				". Usage: " + usage);
    }

    public boolean send() {
	    response = ((StateManager) manager).threads(requestType);
	return true;
    }
    
    
    public String getUsage() {
	return "Get thread activity for the component or parts of the component";
    }
}
