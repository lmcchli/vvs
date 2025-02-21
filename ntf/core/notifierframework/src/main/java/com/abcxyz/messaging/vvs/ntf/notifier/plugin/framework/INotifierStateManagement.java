/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */

package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;

/**
 * The INotifierStateManagement interface defines the methods that the Notifier plug-in can invoke to access NTF's state information
 * and shutdown/lock controls.
 * <p>
 * This will allow your plug-in to act on state changes such as LOCK UNLOCK/SHUTDOWN/EXIT see {@link INotifierNtfAdminState}.
 * <p>
 * It is recommended that you use the registerThreadforShutdown in and threads created by your plug-in to inform NTF to wait
 * for your thread to shutdown before exiting.  You can then do your cleanup and call threadDone() to inform NTF it is OK for
 * it to exit.
 * <p>
 * It is also recommended to implement the LOCK functionality.  when NTF is locked, it should not process and traffic.  You can
 * do this by rejecting any informEvents and allowing a retry, and by suspending your threads using the waitonLock() functions.
 * <p>
 * When NTF is LOCKED, the senders such as SMPP/SMS will also be locked, so any new messages sent will just be queued or rejected.
 * So any messages sent by the plug-in will not be send until UNLOCKED anyway.
 */

public interface INotifierStateManagement {

    /**
     * Tells if NTFs administrative state is unlocked.
     *@return true if the administrative state is unlocked.
     */
    boolean isAdministrativeStateUnlocked();

    /**
     * Tells if NTFs administrative state is locked.
     *@return true if the administrative state is locked.
     */
    boolean isAdministrativeStateLocked();

    /**
     * Tells if NTFs administrative state is shutdown.
     *@return true if the administrative state is shutdown.
     */
    boolean isAdministrativeStateShutdown();

    /**
     * Tells if NTFs administrative state is exit, i.e. can exit now.
     *@return true if the administrative state is exit.
     */
    boolean isAdministrativeStateExit();

    /**
     * Gets the administrative state of NTF
     *@return administrative state of the NTF instance, i.e. unlocked(1), locked(2) or shutdown(3)
     */
    AdministrativeState getNtfAdministrativeState();

    /**
     * Tells if NTF should exit as soon as possible, i.e within 5 seconds...
     *@return true if NTF shall exit.
     */
    boolean isExit();
  
    /**
     * Informs NTF to perform a controlled shutdown.
     * This should only be used in case of an 
     * unrecoverable error of some kind.
     * 
     * State changes are usually initiated by the
     * system via SNMP or through the operating system
     * sending a terminate signal.
     */
    void shutdownNtf();
    
    /**
     * Tells NTF wait for current thread to shut down before
     * moving the administrative state from SHUTDOWN to EXIT.
     * It will wait a maximum of shutdownTime (notification.conf/xsd)
     * before forcing an exit.
     */
    void registerThreadforShutdown();

    /**
     * Inform NTF that this thread has shutdown.
     * Use in conjunction with registerThread.
     * if this thread has not shutdown by
     * shutdownTime(notification.conf)-5 seconds then it will be interrupted and force and given 5 seconds before system exits.
     * This allows for a controlled shutdown of the plug-in threads.  They can try to empty there queues etc until 5 seconds
     * before the forced shutdown, and then cleanup.
     * 
     * To inform NTF that your thread has shutdown, please call threadDone() upon exiting.
     * 
     */
    void threadDone();

    /**
     * Wait for management status to unlock..
     * @throws InterruptedException if interrupted
     */
    void waitOnLock() throws InterruptedException;

    /**
     * Wait for management status to unlock for 
     * a maximum of time specifies ms.
     * @throws InterruptedException if interrupted
     */
    void waitOnLock(long millis) throws InterruptedException;

}
