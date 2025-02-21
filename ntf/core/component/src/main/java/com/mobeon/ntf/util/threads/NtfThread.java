/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.threads;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;;


/**
 * NtfThread implements an NTF thread state aware thread, that is able to shutdown and lock/unlock.
 * 
 * The idea is you create a thread based on NTFThread instead of java.lang.Thread and implement the method ntfRun.
 * The run method of NtfThread, check's the administrative status of NTF and acts accordingly. For example if the
 * state is set to LOCEKD, it will wait for NTF to unlock before continuing or just perform lock state tasks.
 * <p>
 * In order to use it you must implement your ntfRun class in such a way as to drop back to the main loop after each
 * transaction, in order to check the state.  If that action is processing a queued item or something else does not 
 * matter. However it should not perform long tasks, or block for extended periods (less than 5 seconds recommended), before
 * returning to the the run method of NTFThread to check state.
 * <p>
 * It is necessary to implement shutdown() and NtfRun() as a minimum. Shutdown is used to complete the shutdown of your
 * thread, you should put any cleanup code here before shutdown.  return true, when you wish to exit, false to be
 * called back again for another iteration, again it is not recommended to block for longer than 5 seconds in a shutdown
 * call.
 * <p>
 * It is suggested to also to over-ride ntfThreadInit(), for initialising your thread, and if you require to do any maintenance
 * activities while locked, the ntflockAction()- this should be minimal and not traffic related..
 * <p>
 * IMPORTANT INFORMATION: the NtfThread registers itself with the  for shutdown supervision.  This means that
 * when NTF is told to shutdown, it will wait for this Thread to exit for the notification.conf parameter shutdownTime (default 60).
 * Before forcing a shutdown. when run() exits, it will inform the ManagementInfo that it has shutdown. If NTF is still
 * running 5 seconds before the shutdown limit, your thread will receive a interrupt exception, you should then immediately
 * clean up and exit.
 * <p>
 * Basically you have a shutDownTime-5 seconds period to clean out any queues before exiting, if this is what you require, or
 * any other processing by your thread, before you are forced to exit.
 * 
 */
public abstract class NtfThread extends Thread {
    private static LogAgent log =  NtfCmnLogger.getLogAgent(NtfThread.class);

    /**
     * Internal state of this thread <OL>
     * <LI>Unlocked
     * <LI>Locked
     * <LI>Shutdown
     * </OL
     */
    public enum InternalState {UNLOCKED, LOCKED, SHUTDOWN, EXIT };
    protected InternalState internalState=InternalState.UNLOCKED;
    
    private boolean specialActionOnLock=false; //do an action periodically when locked;
    private long lockActionTime=5000; // how often to do action.
    

    /**
     *Constructor.
     *@param name - the name of the thread.
     */
    public NtfThread(String name) {
        setName(name);
    }

    /**
     * This method does the normal work for an NtfThread. It is called
     * repeatedly as long as the administrative state is unlocked.
     * When implementing this function, try to use the loop in the run-method of
     * NtfThread instead of making a loop in ntfRun, so the administrative state
     * is checked frequently.
     *@return false until the thread wants to stop, then true.
     */
    public abstract boolean ntfRun();

    /**
     * This method shall complete the work currently going on in the thread. It
     * shall return false until the work is done and then return true. The run
     * method of NtfThread calls it over and over again until it returns true.
     * When implementing this function, try to use the loop in the run-method of
     * NtfThread instead of making a loop in ntfRun, so the administrative state
     * is checked frequently.
     *@return false until the thread is done with shutdown activities, then
     * true.
     */
    public abstract boolean shutdown();
    /**
     * This method is called once, when the thread is started, before the loop
     * that calls ntfRun begins.
     */
    public void ntfThreadInit() {
        return;
    }
    
    protected void setLockAction(boolean specialActionOnLock)
    {
        this.specialActionOnLock=specialActionOnLock;
    }
    
    protected void setLockAction(boolean specialActionOnLock,long lockActionTime)
    {
        this.lockActionTime=lockActionTime;
        this.specialActionOnLock=specialActionOnLock;
    }
    
    /**
     * This method is called while locked. Usually just waits,
     * but if special action is set then executes ntfLockAction()
     * @throws InterruptedException 
     */
    private void doLockAction() throws InterruptedException {
        if (specialActionOnLock) {
            ManagementInfo.get().waitOnLock(lockActionTime);
            ntfLockAction();
        } else
        {
            ManagementInfo.get().waitOnLock();
        }
    }
    
    /**
     * This method can be overridden to create an action
     * when locked.  
     * specialActionOnLock must be true...
     * lockActionTime can be set to indicate how often to check.
     * @throws InterruptedException 
     */
    
    protected void ntfLockAction()
    {
        //do nothing...
    }

    /**
     * This method return the internal state of the NTFThread class.
     * @return the internal state unlocked(1), locked(2) or shutdown(3).
     */
     public  InternalState getInternalState() { return internalState; }

    /**
     * Run is a loop that continues until the ManagementInfo class decides that
     * NTF shall stop.  Run has three modes; normally, it calls ntfRun() to do
     * some useful work, but when the ManagementInfo decides that NTF shall shut
     * down, it calls shutdown() instead of ntfRun(). When ManagementInfo
     * decides that NTF shall be locked, or the thread has completed the
     * shutdown operation, run() does nothing but wait to be unlocked.
     */
     public void run() {
         try {
             if (ManagementInfo.get().isAdministrativeStateShutdown())
             {
                 log.debug("Thread: " + getName() + " not starting, due to NTF shutting down.");
                 return;
             } //don't start new threads if shutting down.

             log.info("Thread " + getName() + " Starting");
             ManagementInfo.get().registerThreadforShutdown();
             ntfThreadInit();
         } catch (OutOfMemoryError e) {
             try {
                 ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                 log.error("NTF out of memory, shutting down... ",e);
             } catch (Throwable e2) {;} //ignore second exception
             ManagementInfo.get().threadDone();
             return;
         } catch (Throwable e) {
             if (e instanceof Error) {
                 ManagementInfo.get().threadDone();
                 try {
                     log.error("Unrecovarable error when starting thread " + getName() + " " + e.getMessage());
                 } catch (Throwable e2) {;} //ignore second exception
                 return;
             } else
             {
                 log.error("Unexpected exception when starting thread: " + getName() + " " , e);
             }
         }
         while (internalState != InternalState.EXIT) { //Loop until stopped, even if there are exceptions
             try {
                 while ( internalState != InternalState.EXIT ) { //Loop forever as long as nothing unexpected happens
                     if (ManagementInfo.get().isAdministrativeStateUnlocked()) {
                         if (ntfRun()) {
                             ManagementInfo.get().threadDone();
                             log.info("Thread exiting normally");
                             internalState = InternalState.EXIT;
                             return;
                         }
                     } else if (ManagementInfo.get().isAdministrativeStateShutdown()) {
                         switch (internalState) {
                             case UNLOCKED: //Unlocked
                                 internalState = InternalState.SHUTDOWN;
                                 break;
                             case LOCKED: //Locked
                                 internalState = InternalState.SHUTDOWN;
                                 break;
                             case SHUTDOWN:
                                 if (shutdown()) {
                                     internalState = InternalState.EXIT;
                                     ManagementInfo.get().threadDone();
                                     log.info("Thread exiting on request from ManagementInfo: " + getName());
                                     return;
                                 } else {
                                     sleep(1); // give 1 ms time before trying to shutdown again, let other threads run.
                                 }
                                 break;
                         }
                     } else if (ManagementInfo.get().isAdministrativeStateLocked()){
                         internalState = InternalState.LOCKED;
                         synchronized(ManagementInfo.get()) {
                             log.info("Thread locked, waiting for unlock: " +  Thread.currentThread().getName());
                             while (ManagementInfo.get().isAdministrativeStateLocked())
                             {
                                 doLockAction();                                 
                             }
                             switch (ManagementInfo.get().getNtfAdministrativeState()) {
                                 case UNLOCKED:
                                     log.info("Thread unlocked: " +  Thread.currentThread().getName());
                                     internalState = InternalState.UNLOCKED;
                                     break;
                                 case LOCKED:
                                     internalState = InternalState.LOCKED;
                                     break;
                                 case SHUTDOWN:
                                     internalState = InternalState.SHUTDOWN;
                                     break;
                                 case EXIT:
                                     //should never happen while we are registered but...
                                     internalState = InternalState.EXIT;
                                     break;
                                 default:
                                     internalState = InternalState.UNLOCKED;
                             }
                         }

                     }
                 }
             } catch (InterruptedException ie)
             {
                 if (ManagementInfo.get().isAdministrativeStateShutdown())  {
                     log.info("Thread interrupted, exiting on request from ManagementInfo: " + getName());
                     internalState = InternalState.SHUTDOWN;
                 } else
                 {
                     log.error("Thread interrupted, but management state not shutting down!, exiting thread: " + getName());
                     internalState = InternalState.SHUTDOWN;
                 }

                 break;

             } catch (OutOfMemoryError e) {
                 try {
                     ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                     log.error("NTF out of memory, shutting down... ",e);
                     ManagementInfo.get().threadDone();
                     return;
                 } catch (Throwable e2) {;} //ignore
                 ManagementInfo.get().threadDone();
                 return;
             } catch (Throwable e) {
                 log.error("Unexpected: ", e);
                 if (e instanceof Error) {
                     ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                     ManagementInfo.get().threadDone();
                     try {
                         log.error("Unrecovarable error in thread " + getName() + " exiting: ",e);
                     } catch (Throwable i) {;} //ignore second exception;
                     return;
                 }
                 try {
                     log.error("Unxpected exception, e");
                 } catch (Throwable i) {
                     ManagementInfo.get().threadDone();
                     return;
                 }
             }
         }

         ManagementInfo.get().threadDone();

         log.debug("Thread: " + getName() + " exiting normally");
     }
}
