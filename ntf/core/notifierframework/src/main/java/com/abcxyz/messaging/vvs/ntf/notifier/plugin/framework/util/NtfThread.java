/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */

package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierStateManagement;

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
    
    private AdministrativeState internalState=AdministrativeState.UNLOCKED;
           
    protected INotifierLoggerFactory loggerFactory = null;
    protected INotifierLogger log = null;
    protected INotifierStateManagement managmentInfo = null;   
    protected boolean specialActionOnLock=false; //do an action periodically when locked;
    protected long lockActionTime=5000; // how often to do lockAction() action in milliseconds when locked.
    
    public NtfThread(INotifierLoggerFactory loggerFactory, INotifierStateManagement managmentState, String ThreadName) {   
        super(ThreadName);
        this.loggerFactory = loggerFactory;
        this.managmentInfo = managmentState;     
    }
    
    public NtfThread(INotifierLoggerFactory loggerFactory, INotifierStateManagement managmentState) {   
        super("NtfThread-Plugin");
        this.loggerFactory = loggerFactory;
        this.managmentInfo = managmentState;     
    }
    
    //disable default constructor
    @SuppressWarnings("unused")
    private NtfThread(){};
    
    /**
     * This method does the normal work for an NtfThread. It is called repeatedly as long as the administrative state is unlocked.
     * <P>
     * When implementing this function, try to use the loop in the run-method of ntfThread instead of making a loop in ntfRun, so 
     * the administrative state is checked frequently. It is also important that the Thread does not block for more than about 5
     * seconds, as this will hold up the shutdown of NTF if for example a queue is empty.
     * <p>
     *@return false until the thread wants to stop, then true.
     */
    protected abstract boolean ntfRun();

    /**
     * This method shall complete the work currently going on in the thread. It shall return false until the work is done and then return true. The run
     * method of NtfThread calls it over and over again until it returns true. When implementing this function, try to use the loop in the run-method of
     * NtfThread instead of making a loop in ntfRun, so the administrative state is checked frequently.
     * <p>
     * This can be implemented to for example call the ntfRun() class until a queue is empty and then write any persistent data before exiting.
     * <p>
     *@return false until the thread is done with shutdown activities, then
     * true.
     */
    protected abstract boolean shutdown();
    /**
     * This method is called once, when the thread is started, before the loop that calls ntfRun begins.
     */
    protected void ntfThreadInit() {
        return;
    }
    
    protected void setLockAction(boolean specialActionOnLock)
    {
        this.specialActionOnLock=specialActionOnLock;
    }
    
    /**
     * Switch on/off and configure the doLockAction, if your thread is required to do administrative tasks while locked.
     * <p>
     * DoLock Action should only be used for administrative tasks, cleanups etc and not for ordinary traffic. When NTF is LOCKED it should not for example send SMS messages or other traffic related functions.
     * <p>
     * @param specialActionOnLock - set to true if you wish the Thread to call doLockAction() when locked.
     * @param lockActionTime - indicate how long often in milliseconds to perform the DoLockAction()
     */
    protected void setLockAction(boolean specialActionOnLock,long lockActionTime)
    {
        this.lockActionTime=lockActionTime;
        this.specialActionOnLock=specialActionOnLock;
    }
    
    /**
     * This method is called while locked. Usually just waits, but ifspecialActionOnLock is set then executes ntfLockAction()
     * @throws InterruptedException 
     */
    @SuppressWarnings({ "javadoc" })
    private void doLockAction() throws InterruptedException {
        if (specialActionOnLock) {
            managmentInfo.waitOnLock(lockActionTime);
            ntfLockAction();
        } else
        {
            managmentInfo.waitOnLock();
        }
    }
    
    /**
     * This method can be overridden to create an action when locked.  
     * NOTE: specialActionOnLock must be true.
     * <p>
     * lockActionTime can be set to indicate how often to check or set directly as they are protected variables.
     * Call function setLockAction() from your over-ridden ntfThreadInit() to set this up if required.
     */
 
    protected void ntfLockAction()
    {
        //do nothing...
    }

    /**
     * This method returns the internal state of the NTFThread class.
     * @return the internal state of the thread, can be used by your thread to determine
     * current thread state.
     */
     public  AdministrativeState getInternalState() { return internalState; }

    /**
     * Run is a loop that continues until NTF decides that it shall stop according to NTF state.
     * <p>
     * Run has two modes, normally:
     * 1) in unlocked state it calls ntfRun() to do some useful work, but when the ManagementInfo decides that NTF shall shutdown
     *    it calls shutdown() instead of ntfRun(). 
     * 2) When ManagementInfo decides that NTF shall be locked, or the thread has completed the run() does nothing but wait to be 
     *    unlocked or shutdown, unless a special lock action has been specified, in which cause the lock action is performed. 
     *    periodically.
     * <p>
     * It is important that nftDun returns to the run function at least every 5 seconds, it does not block indefinitely, so that
     * the run() can check and act on the state changes in a timely fashion.
     */
     public void run() {
        
         if(log == null) {
             log = loggerFactory.getLogger(NtfThread.class);
         }
         try {
             if (managmentInfo.isAdministrativeStateShutdown())
             {
                 log.debug("Thread: " + getName() + " not starting, due to NTF shutting down.");
                 return;
             } //don't start new threads if shutting down.

             log.info("Thread " + getName() + " Starting");
             managmentInfo.registerThreadforShutdown();
             ntfThreadInit();
         } catch (OutOfMemoryError e) {
             try {
                 managmentInfo.shutdownNtf();
                 log.error("NTF out of memory, shutting down... ",e);
             } catch (Throwable e2) {;} //ignore second exception
             managmentInfo.threadDone();
             return;
         } catch (Throwable e) {
             if (e instanceof Error) {
                 managmentInfo.threadDone();
                 try {
                     log.error("Unrecovarable error when starting thread " + getName() + " " + e.getMessage());
                 } catch (Throwable e2) {;} //ignore second exception
                 return;
             } else
             {
                 log.error("Unexpected exception when starting thread: " + getName() + " " , e);
             }
         }
         while (internalState != AdministrativeState.EXIT) { //Loop until stopped, even if there are exceptions
             try {
                 while ( internalState != AdministrativeState.EXIT ) { //Loop forever as long as nothing unexpected happens
                     if (managmentInfo.isAdministrativeStateUnlocked()) {
                         if (ntfRun()) {
                             managmentInfo.threadDone();
                             log.info("Thread exiting normally");
                             internalState = AdministrativeState.EXIT;
                             return;
                         }
                     } else if (managmentInfo.isAdministrativeStateShutdown()) {
                         switch (internalState) {
                             case UNLOCKED: //Unlocked
                                 internalState = AdministrativeState.SHUTDOWN;
                                 break;
                             case LOCKED: //Locked
                                 internalState = AdministrativeState.SHUTDOWN;
                                 break;
                             case SHUTDOWN:
                                 if (shutdown()) {
                                     internalState = AdministrativeState.EXIT;
                                     managmentInfo.threadDone();
                                     log.info("Thread exiting on request from ManagementInfo: " + getName());
                                     return;
                                 } else {
                                     sleep(1); // give 1 ms time before trying to shutdown again, let other threads run.
                                 }
                                 break;
                         }
                     } else if (managmentInfo.isAdministrativeStateLocked()){
                         internalState = AdministrativeState.LOCKED;
                         synchronized(managmentInfo) {
                             log.info("Thread locked, waiting for unlock: " +  Thread.currentThread().getName());
                             while (managmentInfo.isAdministrativeStateLocked())
                             {
                                 doLockAction();                                 
                             }
                             switch (managmentInfo.getNtfAdministrativeState()) {
                                 case UNLOCKED:
                                     log.info("Thread unlocked: " +  Thread.currentThread().getName());
                                     internalState = AdministrativeState.UNLOCKED;
                                     break;
                                 case LOCKED:
                                     internalState = AdministrativeState.LOCKED;
                                     break;
                                 case SHUTDOWN:
                                     internalState = AdministrativeState.SHUTDOWN;
                                     break;
                                 case EXIT:
                                     //should never happen while we are registered but...
                                     internalState = AdministrativeState.EXIT;
                                     break;
                                 default:
                                     internalState = AdministrativeState.UNLOCKED;
                             }
                         }

                     }
                 }
             } catch (InterruptedException ie)
             {
                 if (managmentInfo.isAdministrativeStateShutdown())  {
                     log.info("Thread interrupted, exiting on request from ManagementInfo: " + getName());
                     internalState = AdministrativeState.SHUTDOWN;
                 } else
                 {
                     log.error("Thread interrupted, but management state not shutting down!, exiting thread: " + getName());
                     internalState = AdministrativeState.SHUTDOWN;
                 }

                 break;

             } catch (OutOfMemoryError e) {
                 try {
                     managmentInfo.shutdownNtf();
                     log.error("NTF out of memory, shutting down... ",e);
                     return;
                 } catch (Throwable e2) {;} //ignore
                 return;
             } catch (Throwable e) {
                 log.error("Unexpected: ", e);
                 if (e instanceof Error) {
                     managmentInfo.shutdownNtf();
                     try {
                         log.error("Unrecovarable error in thread " + getName() + " exiting: ",e);
                     } catch (Throwable i) {;} //ignore second exception;
                     return;
                 }
                 try {
                     log.error("Unxpected exception, e");
                 } catch (Throwable i) {
                     return;
                 }
             } finally {
                 //Inform NTF that this thread no longer needs to be supervised for shutdown as it is exiting.
                 managmentInfo.threadDone();
             }
         }
         log.debug("Thread: " + getName() + " exiting normally");
     }
}

