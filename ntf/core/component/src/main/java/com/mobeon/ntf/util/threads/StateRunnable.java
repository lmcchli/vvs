 /**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.threads;

/****************************************************************
 * StateRunnable extends Runnable with functions to handle a simple state variable
 * that can be retrieved from the outside.
 */
public interface StateRunnable extends Runnable {

    /****************************************************************
     * @return true if this thread has not updated its state since the state was
     * last retrieved. This should of course not be called immediately after
     * getState or stateAsString.
     */
    public boolean isStuck();

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
    public int getState();

    /****************************************************************
     * @return a String representation of the state of this StateRunnable. This
     * method is supposed to be overridden by subclasses, to provide more
     * user-friendly state information
     */
    public String stateAsString();
}
