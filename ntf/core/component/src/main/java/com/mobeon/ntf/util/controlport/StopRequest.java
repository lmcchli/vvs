/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.controlport;

public class StopRequest extends ActionRequest {

    public StopRequest() {
	super();
	manager = new StateManager();
	action = "stop";
    }

    public boolean send() {
	if(((StateManager) manager).stop())
	    response = new String("Not implemented yet.");
	else
	    response = new String("Could not stop whole or parts of the system.");
	return true;
    }

    public String getUsage() {
	return "Stop the component";
    }
    
}
