/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.controlport;


import java.util.*;
import java.io.*;

abstract class ActionRequest {

    protected String action;
    protected String parameters;
    
    protected int requestType;
    
    protected ActionManager manager;

    protected Object response;


    protected ActionRequest() {
	
    }

    public void setParameters(String parameters) throws BadParametersException {
	this.parameters = parameters;
    }

    abstract public boolean send();
    
    public boolean status() {
	return true;
    }

    public Object getResponse() {
	return response;
    }
    
    abstract public String getUsage();
}




class RequestRegister {
    
    protected static Vector registeredClasses;
    
    
    protected static String defaultClasses[] = {"utilities.ControlPort.StopRequest",
						 "utilities.ControlPort.StateRequest",
						 "utilities.ControlPort.ThreadsRequest",
						 "utilities.ControlPort.ConfigRequest",
						 "utilities.ControlPort.HelpRequest",
						 "utilities.ControlPort.ThreadRequest"};


    protected static Hashtable actionRequestMapping;

    static {
	registeredClasses = new Vector();
	actionRequestMapping = new Hashtable();
	initialize();
    }


    /**********************************************************************************
     * Initialize the Request register. Add automatically on defaults Stop,
     State, Threads, Config and Help.
    It first check the file ctrl_port_request.reg for furthermore registered
    requests in that file and adds them too.
    */
    protected static void initialize() {
	try {
	    BufferedReader f = new BufferedReader(new FileReader("ctrl_port_request.reg"));
	    String ln;
	    
	    while((ln = f.readLine()) != null) {
		//System.out.println("Has read #"+ln+"#");
		if(!registerName(ln)) {
		    //System.out.println("#"+ln+"# was not added");
		}
	    }
	}catch(Exception e) {}
	
	for(int i = 0; i < defaultClasses.length; i++) {
	    if(!isRegistered(defaultClasses[i])) {
		try {
		    if(registerName(defaultClasses[i])) {
			//	System.out.println("Could register "
			// +defaultClasses[i]);
		    }
		    else {
			//System.out.println("Could not register" + defaultClasses[i]);
		    }
		} catch(Exception e) {}
	    }
	}
    }


    /**********************************************************************************
     * Check if a specific class is registered.
     @return true if the class was registerd, false otherwise.
    */
    public static boolean isRegistered(String className) {
	return registeredClasses.contains(className);
    }


    public static Enumeration getRegisteredRequests() {
	return actionRequestMapping.keys();
    }

    /**********************************************************************************
     * Get the class name for a particular request.
     @param request is the name of the request to get the registered class name
     for (e.g. Stop). The search is case insensitive.
    */
    public static String getClassForRequest(String request) {
	if(request == null)
	    return null;
	String o = (String) actionRequestMapping.get(request.toLowerCase());
	return o;
    }


    /**********************************************************************************
     * Get a vector of all classes that is registered
     */
    public static Vector getRegisteredClasses() {
	return (Vector) registeredClasses.clone();
    }

	

    /**********************************************************************************
     * Register a request class that inherits from ActionRequest. Make sure that
     it is an ActionRequest by create an object of the class by
     Beans.instantiate(null, class) and check that the result is an
     ActionRequest. The name of the class MUST end with Request. The name of the
     request is the class name without package information.
     Example: className = a.b.c.StopRequest will be registered as action
     stop. If several classes gives the same request name, the last one will be
     used but the other will exist.
     @param className is the class that is to be registered as a action class.
     @return true if the class got registered or was already registered. False
     otherwise.
     @throws ClassNotFoundException if the class was not found 
    */
    public static boolean registerName(String className) throws ClassNotFoundException, IOException  {
	if(className == null)
	    return false;
	
	boolean endsWithRequest = true;

	if(!className.endsWith("Request")) {
	    //System.out.println("Register.registerName for " + className + " class did not end with Request");
	    endsWithRequest = false;
	}
	String requestName = className;
	
	Object o = java.beans.Beans.instantiate(null, className);
	if(o instanceof ActionRequest) {
	    if(!registeredClasses.contains(className)) {
		// Check if the request belong to a package
		int indexOfDot = className.lastIndexOf(".");
		if(indexOfDot >= 0) {
		    // Do not need to check the length since the className can
		    // not end with a dot due to the endsWith("Request") above.
		    requestName = className.substring(indexOfDot+1);
		    //System.out.println("Has got request name " + requestName);
		}

		if(endsWithRequest) {
		    requestName = requestName.substring(0, requestName.length()-7);
		}
		//System.out.println("Has got new request name "+requestName);
		// Only register the name of the class. Not the object it self.
		registeredClasses.addElement(className);
		actionRequestMapping.put(requestName.toLowerCase(), className);
		//System.out.println("Has registered " + requestName +" with class " + className);
	    }
	    return true;
	}
	return false;
    }
}

