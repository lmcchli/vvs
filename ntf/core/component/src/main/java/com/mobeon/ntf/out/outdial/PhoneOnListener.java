/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.outdial;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.OdlEvent;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.event.PhoneOnEvent;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.util.threads.NtfThread;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Handles results from phone on requests.
 */
class PhoneOnListener extends NtfThread implements com.mobeon.ntf.event.PhoneOnEventListener {

    private int PHONEON_QUEUE_SIZE = 1000;
    private ManagedArrayBlockingQueue<Object> phoneOnQueue;
    private ManagedArrayBlockingQueue<Object> WorkerEventQueue;
    private static LogAgent logger = NtfCmnLogger.getLogAgent(PhoneOnListener.class);
    private IEventStore eventStore;
    private MfsEventManager mfsEventManager;

    /**
     * Constructs a phone on listener with synchronized set for pending phone on requests.
     * @param eventStore IEventStore
     */
    public PhoneOnListener(IEventStore eventStore)
    {
        super("ODL:PhoneOnListener");

        if (eventStore == null) {
        	throw new IllegalArgumentException("Parameter eventStore must not be null.");
        }
        this.mfsEventManager = MfsEventFactory.getMfsEvenManager();
        this.eventStore = eventStore;
        
        // Calculate the queue based on no. of outdial workers, fs timeout access and number of
        // phone events by phone.
        PHONEON_QUEUE_SIZE = Config.getOutdialWorkers() * Config.getFsTimeout() * 100; //100 per phone on sender worker..
        phoneOnQueue = new ManagedArrayBlockingQueue<Object>(PHONEON_QUEUE_SIZE,true);
    }

    /**
     * Sets the message queue
     */
    void setQueue(ManagedArrayBlockingQueue<Object> queue) {
    	this.WorkerEventQueue = queue;
    }

    /**
     * The shutdown loop stops after the ntfRun method is finished.
     *
     * @return true always (i.e. this thread has not shutdown activity)
     */
    public boolean shutdown() {
        while (phoneOnQueue.size() > 0 && !Thread.interrupted() ) //drain queue as long as we are not forced to shutdown.
        {
            if (ntfRun() == true ) { return true;} // ntfRun tells us to exit now.
        }
        return true;
    }

    public boolean ntfRun()
    {
        PhoneOnEvent ev = null;

        long lockId = 0;
        try {
            Object o = phoneOnQueue.poll(10, TimeUnit.SECONDS); // timeout to check management status.
            if (o == null) {
                return false; // go back to check management state.
            }
            if (o instanceof PhoneOnEvent) {
                ev = (PhoneOnEvent) o;
            } else {
                logger.error("Invalid object received: " + o.getClass().getName());
                return false;
            }

            logger.debug("Outdial PhoneOn event received: " + ev);

            if (ev.getResult() == PhoneOnEvent.PHONEON_CLIENT_SENT_SUCCESSFULLY) {
                /*
                 * The PHONEON_SENT_SUCCESSFULLY status indicates only that the phone on request has been sent out successfully. Now
                 * we are waiting for the phone response and there is nothing more to do.
                 */
                logger.debug("Received PhoneOn Sent Successfully status event for " + ev.getAddress()
                        + ". Now waiting for the PhoneOn response.");
                return false;
            }

            if (WorkerEventQueue != null) {

                /**
                 * Retrieve all the status files found under the given notification number directory. Multiple files can be found
                 * since the same notification number can be used to notify multiple subscriber numbers (mailboxes). Each file will
                 * generate it's own odlEvent and will be injected independently.
                 */
                List<OdlEvent> events = eventStore.get(ev.getAddress());
                if (events != null) {

                    // Validate if the subscriber's storage is READ-ONLY (using the notification number)
                    if (!CommonMessagingAccess.getInstance().isStorageOperationsAvailable(ev.getAddress())) {
                        logger.warn("Storage currently not available for " + ev.getAddress() + ", PhoneOn will retry");
                        return false;
                    }

                    // Acquire lock file if it's a PHONE_ON response
                    if (ev.isOk() || ev.isBusy()) {
                        Object perf = null;
                        try {
                            boolean internal = mfsEventManager.isInternal(ev.getAddress());
                            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                                perf = CommonOamManager.profilerAgent.enterCheckpoint("NTF.NH.16.OdlPhoneOnLockTime");
                            }
                            lockId = mfsEventManager.acquireLockFile(ev.getAddress(), MfsEventStore.OUTDIAL_PHONE_ON_LOCK_FILE,
                                    Config.getOutdialPhoneOnLockFileValidityInSeconds(), internal);
                            if (lockId == 0) {
                                logger.debug("Outdial PhoneOn lock file not acquired, another NTF instance is processing this PhoneOn event");
                                return false;
                            }
                        } catch (Exception e) {
                            logger.error("Outdial exception while trying to acquire lock file for " + ev.getAddress()
                                    + ", PhoneOn will retry", e);
                            return false;
                        } finally {
                            if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                                CommonOamManager.profilerAgent.exitCheckpoint(perf);
                            }
                        }
                    }

                    // For each status file found under the given notification number directory
                    for (OdlEvent event : events) {
                        event.setOdlTrigger(OdlInfo.EVENT_OUTDIAL_PHONEON);
                        if (ev.isOk()) {
                            event.setOdlCode(OdlInfo.EVENT_CODE_PHONEON);
                        } else if (ev.isBusy()) {
                            event.setOdlCode(OdlInfo.EVENT_CODE_BUSY);
                        } else if (ev.isSs7Error()) {
                            event.setOdlCode(OdlInfo.EVENT_CODE_SS7_ERROR);
                        } else {
                            event.setOdlCode(OdlInfo.EVENT_CODE_DESTINATION_NOT_REACHABLE);
                        }
                        event.setFromNotify(true);

                        logger.debug("Outdial PhoneOn event received " + ev + " for " + event.getRecipentId() + " : "
                                + ev.getAddress());

                        event.notifyObservers();
                        event.setPhoneOnLock(lockId);

                        if (!WorkerEventQueue.offer(event, 500, TimeUnit.MILLISECONDS)) {
                            logger.warn("Phone on Event " + event
                                    + " will not be notified, event queue full - may be a while before retried.. "
                                    + event.getRecipentId() + " : " + ev.getAddress());
                            // release lock
                            try {
                                long lockid = event.getPhoneOnLock();
                                logger.debug("PhoneOn lock: " + lockid);
                                if (lockid != 0) {
                                    boolean internal = mfsEventManager.isInternal(event.getTelNumber());
                                    ;
                                    mfsEventManager.releaseLockFile(event.getTelNumber(), MfsEventStore.OUTDIAL_PHONE_ON_LOCK_FILE,
                                            lockid, internal);
                                    event.setPhoneOnLock(0);
                                }
                            } catch (Exception e) {
                                logger.warn("Exception trying to release lock file " + MfsEventStore.OUTDIAL_PHONE_ON_LOCK_FILE
                                        + " for " + event.getRecipentId() + " : " + event.getTelNumber(), e);
                            }
                        }
                    }
                } else {
                    logger.debug("Event " + ev + " received but not found in storage, discard.");
                }
            } else {
                logger.error("Internal error; discarding PhoneOn response: " + ev);
                throw new IllegalStateException("Message queue not initialized.");
            }
        } catch (Throwable t) {
            logger.error("PhoneOnListener Exception: ", t);
            try {
                if (ev != null && lockId != 0) {
                    boolean internal = mfsEventManager.isInternal(ev.getAddress());
                    ;
                    mfsEventManager.releaseLockFile(ev.getAddress(), MfsEventStore.OUTDIAL_PHONE_ON_LOCK_FILE, lockId, internal);
                }
            } catch (Exception e) {
                logger.warn("Exception trying to release lock file " + MfsEventStore.OUTDIAL_PHONE_ON_LOCK_FILE
                        + (ev != null ? " for " + ev.getAddress() : ""), e);
            }
            if (t instanceof OutOfMemoryError) {
                try {
                    OutOfMemoryError me = (OutOfMemoryError) t;
                    ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                    logger.error("NTF out of memory, shutting down... ", me);
                } catch (OutOfMemoryError me2) {
                    ;
                } // ignore second exception
                return true; // exit.
            }
        }
        return false;
    }

    public void phoneOn(PhoneOnEvent phoneOnEvent) {
        if (!phoneOnQueue.offer(phoneOnEvent, 500, TimeUnit.MILLISECONDS)) {
            logger.info("Event " + phoneOnEvent + " is DROPPED since queue is full: " + phoneOnEvent.getAddress() + " OR message already in queue!");
        }                
    } 

    public String toString()
    {
        return "OutdialNotificationOut.PhoneOnReceiver";
    }
}
