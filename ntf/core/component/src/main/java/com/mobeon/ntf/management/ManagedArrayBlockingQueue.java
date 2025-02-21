/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2012.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.mobeon.ntf.management;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


/**
 *
 * An implementation of LinkedBlockingqueue that only blocks for short period if
 * Management status is unlocked.
 *
 * Used to make sure the system can shutdown and lock properly, without blocking...
 * @author lmcdasi
 * @param <E> Event
 *
 */
public class ManagedArrayBlockingQueue<E> extends ArrayBlockingQueue<E> {
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(ManagedArrayBlockingQueue.class);
    
   /*
    * Make ManagedArrayBlockingQueue use this kind of blocking queue.
    */

    //Maximum time to wait before checking if NTF is UNLOCKED.
    private static final long MAXTIMETOCHECKADMINSTATE = 3; //Seconds
    private static long checkTime = TimeUnit.SECONDS.toNanos(MAXTIMETOCHECKADMINSTATE);

    protected long lastAccess;
    protected Object notEmpty = new Object();
    
    /* We use a synchronised hash set here not a concurrent has map.
     * The reason being is that a concurrent hash map does not guarantee that an item removed and added are one to one,
     * it is more a best effort, however if we never remove a just added item in the hash map this will cause
     * the queue to become stuck and never allow another phone on for that number (race condition), this is especially true with multi-core.
     * Even though a concurrent HashMap causes less contention than a synchronised hash map, It is more important to have the 1-1 guarantee.
     * As we cannot afford to lose phone on as this can cause semi-permanent blocking in slam-down and out-dial (Until timeout, usually 24 hrs).
     * 
     * Always ensure to add the object to the hash before placing on the queue, because it is possible another thread may remove from the queue
     * and remove the object from the hash before your thread adds it to the hash(as the thread waiting on the queue will wake up) which would make it
     * appear there is one on the queue when actually not.  Remember the hash-map to prevent duplicates is separate to the actual queue, so order is important.
     * 
     * By adding it first to the hash, then adding to the queue, you ensure the item will be removed from the hash always.
     * If you fail to add the item to the queue, ensure to remove it from the hash, as we must always ensure the hash at worst case is cleared event if this
     * ends up with a duplicate on the queue, in the rare cases.  
     * 
     * The rule should be to ensure the hash object is always removed upon the object being removed from the queue, if we we end up with
     * more than one on the queue that is not fatal, if we miss one being added, may be acceptable, but if the queue becomes permanently blocked
     * for one item due to never being removed from the hash (i.e. phone on for a number), we are in big trouble.
     */
    Set<Integer> avoidDuplicatesSet = null;
    private boolean avoidDuplicates = false;

    public ManagedArrayBlockingQueue(int i) {
        super(i);
        
        lastAccess = System.currentTimeMillis();
    }
    
    public ManagedArrayBlockingQueue(int i, boolean avoidDuplicates) {
        super(i);
        lastAccess = System.currentTimeMillis();
        if (avoidDuplicates) {
            this.avoidDuplicates =true;
            avoidDuplicatesSet = Collections.synchronizedSet(new HashSet<Integer>());
            
        }
    }
    
    @Override
    /* try to put until not locked. checking management status periodically
     * NOTE, will wait for MAXTIMETOCHECKADMINSTATE if not unlocked before
     * throwing exception. */
    public void put(E e) throws InterruptedException {

        while (!ManagementInfo.get().isAdministrativeStateExit()) {
            if (avoidDuplicates) {
                int hashcode = e.hashCode(); //get before syncronizing to minimise contention time.
                if (avoidDuplicatesSet.contains(hashcode)) {
                    //If already on the queue just return
                    return; 
                } 
            }
            if (offer(e, MAXTIMETOCHECKADMINSTATE, TimeUnit.SECONDS)) {
                return; //Successfully added..
            } else if (!ManagementInfo.get().isAdministrativeStateUnlocked()) {
                throw new InterruptedException("Management state, not unlocked.");
            }
        }
        //Throw interruptedException if shutting down
        throw new InterruptedException("Managment state, Exiting.");
    }

    @Override
    /* try to take while unlocked - checking for not locked periodically, return null if not unlocked.*/
    public E take() {
        E object = null;

        try {
            while (ManagementInfo.get().isAdministrativeStateUnlocked()) {
                object = super.poll(MAXTIMETOCHECKADMINSTATE, TimeUnit.SECONDS);
                if (object != null) {
                    lastAccess = System.currentTimeMillis();
                    
                    if (avoidDuplicates) {
                        int hashcode = object.hashCode(); //get before syncronizing to minimise contention time.
                        avoidDuplicatesSet.remove(hashcode);
                    }
                    break;
                }
            }
        } catch (Throwable t) {
            // catch all and let it return null
        }
        
        return object;
    }

    @Override
    /* try to offer while unlocked - checking for not unlocked periodically, return false if no unlocked or
     * time out.*/
    public boolean offer(E e, long timeout, TimeUnit unit) {
        boolean status = false;
        
        if (avoidDuplicates) {
            int hashcode = e.hashCode(); //get before syncronizing to minimise contention time.
            if (avoidDuplicatesSet.add(hashcode) == false) {
                //we do this in one operation to ensure we do not miss a removed object while checking already on the queue
                //otherwise we would have to synchronise the map while we check until the point in which we add the
                //one to the hash, this way we do not miss any new entries if one has just been removed.
                return true; //already an instance.
            }
        }
                
        if (!Thread.interrupted()) {
            long nanosRem = unit.toNanos(timeout);
            long waitTime = 0;

            while (ManagementInfo.get().isAdministrativeStateUnlocked()) {
                if (nanosRem <= checkTime) {
                    waitTime = nanosRem;
                } else {
                    waitTime = checkTime;
                }

                try {
                    if (super.offer(e, waitTime, TimeUnit.NANOSECONDS)) {
                        lastAccess = System.currentTimeMillis();
                        status = true;
                        break;
                    } else {
                        nanosRem -= waitTime;
                        if (nanosRem <= 0) {
                            break;
                        }
                    }
                } catch (Throwable ie) {
                    break;
                }
            }
        }
        

         if (avoidDuplicates && status == false) {
                int hashcode = e.hashCode(); //get before syncronizing to minimise contention time.
                avoidDuplicatesSet.remove(hashcode); //if we failed to add it to the queue remove from the hash.
         }

        return status;
    }

    public boolean offer(E e) {
        boolean status = false;

        try {
           
            if (avoidDuplicates) {
                int hashcode = e.hashCode(); // we want to lock on the synced object for as short as time as possible so fetch the hash-code before putting on map.
                if (avoidDuplicatesSet.add(hashcode) == false) {
                    return true; //already an instance queud.
                }
            }
            if (super.offer(e)) {
                lastAccess = System.currentTimeMillis();
                signalNotEmpty();
                status = true;
            }
        } catch (NullPointerException npe) {
            // do nothing.
        }
        
        if (avoidDuplicates && status == false) {
            int hashcode = e.hashCode(); //get before syncronizing to minimise contention time.
            avoidDuplicatesSet.remove(hashcode); //if we failed to add it to the queue then remove from the hash.
        }

        return status;
    }

    
    
    /* offer, with default timeout in MILLISECONDS */
    public boolean offer(E e, long timeout)
    {
        return offer(e, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    /* try to take off queue until timeout or not unlocked.*/
    public E poll(long timeout, TimeUnit unit) {
        E e = null;

        long nanosRem = unit.toNanos(timeout);
        long waitTime = 0;

        while (ManagementInfo.get().isAdministrativeStateUnlocked()) {
            if (nanosRem <= checkTime) {
                waitTime = nanosRem;
            } else {
                waitTime = checkTime;
            }

            try {
                e = super.poll(waitTime, TimeUnit.NANOSECONDS);
                if (e != null) {
                    lastAccess = System.currentTimeMillis();                   
                    if (avoidDuplicates == true) {
                        int hashcode = e.hashCode(); //get before syncronizing to minimize contention time.
                        avoidDuplicatesSet.remove(hashcode);
                    }
                    break;
                } else {
                    nanosRem-=waitTime;
                    if (nanosRem <= 0) break;
                }
            } catch (InterruptedException ie) {
                break;
            }
        }
        
        return e;
    }

    /**
     * Wait for an item to be added to queue for specified period.
     * @param timeout - time to wait
     * @param unit - unit of time
     * @return true if queue size not 0 during the wait period, false otherwise or if interrupted.
     */
    public boolean waitNotEmpty(long timeout, TimeUnit unit) {
        boolean status = false;

        //don't check management status if queued items.
        synchronized (this) {
            if (size() > 0)
            {
                return true;
            }
        }

        long nanosRem = unit.toNanos(timeout);
        long waitTime = 0;
        long stopTime;
        long startTime;

        try {
            while (!ManagementInfo.get().isAdministrativeStateExit()) {
                if (nanosRem <= checkTime) {
                    waitTime = nanosRem;
                } else {
                    waitTime = checkTime;
                }

                startTime = System.currentTimeMillis();
                synchronized (notEmpty) {
                    notEmpty.wait(TimeUnit.NANOSECONDS.toMillis(waitTime));
                }
                stopTime = System.currentTimeMillis();
                if (size() != 0) {
                    status = true;
                    break;
                } else {
                    long execTime  = TimeUnit.MILLISECONDS.toNanos(stopTime - startTime);
                    nanosRem-=execTime;
                    if (nanosRem <= 0) break;
                }
            }
        } catch (Throwable t) {
            // catch all and let it return false
        }
        return status;
    }

    /* poll, with default timeout in MILLISECONDS */
    public E poll(long timeout) {
        return poll(timeout, TimeUnit.MILLISECONDS);
    }

    public E poll() {
        E obj = null;
        obj = super.poll();
        if (obj != null) {
            lastAccess = System.currentTimeMillis();
            if (avoidDuplicates) {
                int hashcode = obj.hashCode(); //get before syncronizing to minimise contention time.
                avoidDuplicatesSet.remove(hashcode);
            }
        }
        return obj;
    }


    /**
     * @param time - idle time
     * @param unit - unit of idle time
     * @return true if idle for more than idle time.
     */
    public boolean isIdle(long time, TimeUnit unit) {
        long idleTime = unit.toMillis(time);
        if ( System.currentTimeMillis() > (lastAccess+idleTime) ) {
            return true;
        } else {
            return false;
        }
    }

    private void signalNotEmpty() {
        synchronized (notEmpty) {
            notEmpty.notifyAll();
        }
    }
}



