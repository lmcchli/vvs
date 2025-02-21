 /**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.threads;

/****************************************************************
 * StateThread extends Thread with functions to handle a simple state variable
 * that can be retrieved from the outside.
 */
public class StateThread extends Thread {
    /** The state of this thread. */
    protected int threadState;
    /** Set to true when thread state is retrieved, and to false whenever the
	thread state is updated. The net result is that IF a thread is stuck in
	a state, this variable will be true.*/
    protected boolean stuck= false;


    /****************************************************************/
    public StateThread() {
    }


    /****************************************************************
     * Forwards a thread group to the parent Thread.
     * @param tg thread group for this thread.
     */
    public StateThread(ThreadGroup tg, String name) {
	super(tg, name);
    }

    /****************************************************************
     * Sets a new state, and maintains the stuck variable
     * @param state the new state
     */
    protected void setThreadState(int state) {
	stuck= false;
	threadState= state;
    }
	
    /****************************************************************
     * @return true if this thread has not updated its state since the state was
     * last retrieved. This should of course not be called immediately after
     * getThreadState or stateAsString.
     */
    public boolean isStuck() {
	return stuck;
    }


    /****************************************************************
     * @return the current state of this StateThread
     * Interpretation of the value is up to the
     * subclass of StateThread. There is a convention for common states:
     * <UL>
     * <LI>0 - Not running
     * <LI>1 - Run called
     * <LI>2 - Sleeping. If run() has several calls to sleep, this state should
     * be used for the "main" sleep.
     * <LI>3 - Waiting. If run() has several calls to wait, this state should
     * be used for the "main" wait.
     * <LI>4 - Joining. If run() has several calls to join, this state should
     * be used for the "main" join.
     * <LI>5 - Run exited
     * </UL>
     */
    public int getThreadState() {
	stuck= true;
	return threadState;
    }

    /****************************************************************
     * @return a String representation of the state of this StateThread. This
     * method can be overridden by subclasses, to provide more user-friendly
     * state information. The default implementation returns the state number,
     * preceeded by a ">" if the thread is stuck in the same state, and by a blank
     * otherwise. 
     */
    public String stateAsString() {
	boolean s= stuck;
	stuck= true;
	if (s) {
	    return ">" + threadState;
	} else {
	    return " " + threadState;
	}
    }
}
