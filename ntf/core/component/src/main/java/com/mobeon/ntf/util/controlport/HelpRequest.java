/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.controlport;

import java.util.*;


/**********************************************************************************
 * The purpose of this class is to search for all classes that is an instance of
 ActionRequest. For each of these requests it shall build up a message by
 calling the static method getUsage.
 For the time being, the class only support the default ones, config, stop,
 state and threads.
*/
public class HelpRequest extends ActionRequest {

    public HelpRequest() {
	super();
	action = "help";
    }

    public boolean send() {
	Enumeration requests = RequestRegister.getRegisteredRequests();
    String sResponse = "";
	sResponse = ControlPort.getUsage()+"\n";
	if(!requests.hasMoreElements()) {
	    response = sResponse + "help - " + getUsage() + "\nNo further information!\n";
	    return true;
	}

    String tempResponse = "";
	while(requests.hasMoreElements()) {
	  
	    String curReq = (String) requests.nextElement();
	    try {
		ActionRequest a = (ActionRequest) java.beans.Beans.instantiate(null, RequestRegister.getClassForRequest(curReq));
		tempResponse += curReq + " - " + a.getUsage() + "\n";
	    }catch(Exception e) {}
	}
	response = sResponse + tempResponse;
	return true;
    }
    
    public String getUsage() {
	return "This help message";
    }

}
