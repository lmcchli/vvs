/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.controlport;

import java.util.*;
import com.mobeon.ntf.util.threads.*;

/**********************************************************************************
 * This class knows how to get different states from the NTF component.
 About threads:
 ALL map to the thread group system
 OUTDIAL map to the thread group outdial
 IN map to the supgroup imap
 */

class StateManager extends ActionManager {


    /** Define subsystems that this state manager shall support */
    protected static final int ALL_SUBSYSTEMS = 1;
    protected static final int OUTDIAL_SUBSYSTEM = 2;
    protected static final int IN_SUBSYSTEM = 3;


    protected static final int THREAD_OP_INTERRUPT = 1;
    protected static final int THREAD_OP_PRIORITY = 2;
    protected static final Hashtable supportedThreadOperations;


    /** Supported sub systems for threads request */
    protected static Hashtable supportedSubSystems;
    
    
    
    ThreadGroup main = null;

    static {
	supportedSubSystems = new Hashtable();
	supportedSubSystems.put(new Integer(ALL_SUBSYSTEMS), "system");
	supportedSubSystems.put(new Integer(OUTDIAL_SUBSYSTEM), "outdial");
	supportedSubSystems.put(new Integer(IN_SUBSYSTEM), "imap");
    
	supportedThreadOperations = new Hashtable();
	supportedThreadOperations.put("interrupt", new Integer(THREAD_OP_INTERRUPT) );
	supportedThreadOperations.put("priority", new Integer(THREAD_OP_PRIORITY));
	
    }

    public StateManager() {
	super();
	setMainThreadGrp();
    }

    protected void setMainThreadGrp() {
	ThreadGroup parentGroup = Thread.currentThread().getThreadGroup();
	
	
	while(parentGroup != null) {
	    main = parentGroup;
	    parentGroup = parentGroup.getParent();
	}
    }
    

    public String state(int subSystem) {
	Object tst = new Integer(subSystem);
	String subSystemName = (String) supportedSubSystems.get(tst);
	//String subSystemName = "outdial";
	if(subSystemName == null) {
	    return null;
	}

	return "Get the state for " + subSystemName;
    }
    

    public boolean stop() {
	return true;
    }




    public String threads(int subSystem) {
	String subSystemName = (String) supportedSubSystems.get(new Integer(subSystem));
	if(subSystemName == null) {
	    return null;
	}
	
	//System.out.println("Get thread state " + subSystemName);

	// First check for a thread group with that name
	
	if(subSystemName.equals("system")) {
	    return getThreadStates(main);
	}
	
	int mainSubGrps = main.activeGroupCount();
	if(mainSubGrps > 0) {
	    ThreadGroup k[] = new ThreadGroup[mainSubGrps];
	    int ct = main.enumerate(k, true);
	    ThreadGroup selected = null;
	    for(int i = 0; i < ct; i++) {
		//System.out.println("Testing subsystem " + k[i].getName());
		if(k[i].getName().equals(subSystemName)) {
		    selected = k[i];
		    //System.out.println("Has found the sub system");
		    break;
		}
	    }
	    if(selected == null) {
		return "No subsystem " + subSystemName + " exists or is not yet supported by the ControlPort.";
	    }
	    else
		return  getThreadStates(selected);
	}
	else
	    return "No subsystem " + subSystemName + " exists or is not yet supported by the ControlPort.";
	
    }

    protected String getThreadStates(ThreadGroup grp) {
	StringBuffer ret = new StringBuffer();
	StringBuffer subInfo = new StringBuffer();

	//System.out.println("Get thread states for " + grp.getName());
	// Get a list of number of thread groups in this group
	int numSubGroups = grp.activeGroupCount();
	ret.append("Subsystem " + grp.getName() + "\n");
	if(numSubGroups > 0) {
	    
	    // Get a list of supgroups
	    ThreadGroup subGrp[] = new ThreadGroup[numSubGroups];
	    int ct = grp.enumerate(subGrp, false);
	    ret.append(ct + " subsystem" + "\n");
	    for(int i = 0; i < ct; i++) {
		ret.append(subGrp[i].getName()+"\n");
		String r = getThreadStates(subGrp[i]);
		if(r != null)
		    subInfo.append(r);
	    }
	}
	else {
	    ret.append("No subsystems" + "\n");
	}
	
	// Get thread states for this subbroup
	int numThreads = grp.activeCount();
	if(numThreads > 0) {
	    
	    Thread thArray[] = new Thread[numThreads];
	    int ct = grp.enumerate(thArray, false);
	    ret.append(ct + " threads in this subsystem" + "\n");
	    for(int i = 0; i < ct; i++) {
		 ret.append("Thread " + thArray[i].getName());
		 if(thArray[i] == null) {
		      ret.append(";!alive");
		      continue;
		 }
		 
		 if(thArray[i].isAlive())
		     ret.append(";alive");
		 else
		     ret.append(";!alive");
		 if(thArray[i].isInterrupted())
		     ret.append(";interrupted");
		 else
		     ret.append(";!interrupted");
		 ret.append(";Pri:"+thArray[i].getPriority());
		 

		 String threadState = null;
		 boolean stuck = false;
		 boolean hasState = false;
		 if(thArray[i] instanceof StateThread) {
		     stuck =((StateThread) thArray[i]).isStuck();
		     // Get the string state of the thread
		     threadState = ((StateThread) thArray[i]).stateAsString();
		     
		     hasState = true;
		 }
		 else {
		     Class interfaces[] = thArray[i].getClass().getInterfaces();
		     if(interfaces != null) {
			 for(int k = 0; k < interfaces.length; k++) {
			     if(interfaces[k].getName().equals("StateRunnable")) {
		     //if(thArray[i] instanceof StateRunnable) {
				 stuck = ((StateRunnable) thArray[i]).isStuck();
				 threadState = ((StateRunnable) thArray[i]).stateAsString();
				 hasState = true;
			     }
			 }
		     }
		 }
		 if(!hasState) {
		     ret.append(";State:undef");
		 }
		 else {
		     ret.append(";State:").append(threadState);
		     if(stuck)
			 ret.append(";stuck");
		     else
			 ret.append(";!stuck");
		 }
		 ret.append("\n");
	    }
	}
	else
	    ret.append("No threads added to this subsystem" + "\n");
	
	ret.append("----------------------------------------------------\n");
	
	ret.append(subInfo.toString());
	return ret.toString();
    }

    
    /**********************************************************************************
     * Make an operation onto a thread
     @param name the thread name
     */
    public String thread(String name, String operation) {
	// Find the specific thread
	
	Integer iOp = (Integer) supportedThreadOperations.get(operation);
	if(iOp == null)
	    return "Does not support the operation " + operation;
	int opId = iOp.intValue();

	int ct = main.activeCount();
	Thread th[] = new Thread[ct];
	
	int ap = main.enumerate(th, true);
	Thread selected = null;
	for(int i = 0; i < ap; i++) {
	    if(th[i].getName().equals(name)) {
		selected = th[i];
		break;
	    }
	}
	
	if(selected == null)
	    return "No thread " + name + " found.";
	
	switch(opId) {
	case THREAD_OP_INTERRUPT:
	    try {
		selected.interrupt();
	    }catch(Exception e) {}
	    return "The thread has been interrupted";
	    
	case THREAD_OP_PRIORITY:
	    try {
		selected.setPriority(selected.getPriority()+1);
	    }catch(Exception e) {
		return "New priority on thread is " + selected.getPriority() + ".";
	    }
	    return "The thread has been put to sleep in 10 seconds";
	    
	default:
	    return "Operation " + operation + " has not been properly implemented ...";
	}
    }

}


