/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.controlport;


import java.util.*;



/**********************************************************************************
 * The purpose of this class is to collect all managers and to provide a basis
 for the send amd read stuff.
*/
abstract class ActionManager {

    
    
    /*A list of available managers*/
    protected static Vector managers;
    
    /* This is the manager that actually does something*/
    protected ActionManager theManager;

    
    protected ActionManager() {
	theManager = getManager(getClass().getName());
	if(theManager == null) {
	    managers.addElement(this);
	    theManager = this;
	}
    }
    


    private ActionManager getManager(String managerName) {
	if(managers == null) {
	    managers = new Vector();
	}
	
	for(int i = 0; i < managers.size(); i++) {
	    ActionManager ca = (ActionManager) managers.elementAt(i);
	    //System.out.println("Comparing " + ca.getManagerRegisterId() + " with " + managerType);
	    if(managerName.equals(ca.getClass().getName())) {
		//System.out.println("Found manager in list");
		// We found a manager in the array that has already registered
		return ca;
	    }
	}
	
	//System.out.println("Could not find manager in list. Manager size: " + managers.size());
	return null;
    }



    /**********************************************************************************
     * A method in the manager to send the request. Not yet implemented. The
     managers only contain methods. This return always true.
     @param o the objetc that can transmit (e.g. RMI)
     @return true if the method coudl send, false otherwise
    */
    public final boolean send(Object o) {
	return true;
    }
    

    
    /**********************************************************************************
     * A method in the manager to read the response. Not yet implemented. Return
     always null. You MUST use the manager in the Vector for this. Therefore you
     shoudl store a reference to that in this object.
     @param o the object that can read
     @return the response object
    */
    public final Object read(Object o) {
	return null;
    }

 
    
    /**********************************************************************************
     * Check if the manager is connected to the part in NTF in another process
     or machine. This assume the NTF is in the same process. You MUST use the manager in the Vector for this. Therefore you
     shoudl store a reference to that in this object. Otherwise you will get
     very strange results since you will need to refresh the vector.
     @return true if the manager is connected or not, false otherwise. This
     always return true.
    */
    public boolean isConnected() {
	return true;
    }



    /**********************************************************************************
     * Connect if the manager is not connected to the part in NTF in another process
     or machine. This assume the NTF is in the same process.
     @param host the host to connect to
     @param port the port to connect to
     @return true if the manager could connect, false otherwise. This
     always return true.
    */
    public boolean connect(String host, int port) {
	return true;
    }
   
}


