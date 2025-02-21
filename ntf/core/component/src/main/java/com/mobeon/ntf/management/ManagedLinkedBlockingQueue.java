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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * An implementation of LinkedBlockingqueue that only blocks for short period if
 * Management status is unlocked.
 *
 * Used to make sure the system can shutdown and lock properly, without blocking...
 * @author lmcmajo
 * @param <E>
 *
 */

/**
 * @param <E> type of object to queue.
 *
 * @author lmcmajo
 */
/**
 * @param <E>
 *
 * @author lmcmajo
 */
public class ManagedLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {
    //Maximum time to wait before checking if NTF is UNLOCKED.
    private static final long MAXTIMETOCHECKADMINSTATE = 3; //Seconds
    private static long checkTime = TimeUnit.SECONDS.toNanos(MAXTIMETOCHECKADMINSTATE);

    protected long lastAccess;
    protected Object notEmpty = new Object();

    public ManagedLinkedBlockingQueue(int i) {
        super(i);
        lastAccess = System.currentTimeMillis();
    }

    @Override
    /* try to put until not locked. checking management status periodically
     * NOTE, will wait for MAXTIMETOCHECKADMINSTATE if not unlocked before
     * throwing exception. */
    public void put(E e) throws InterruptedException {

        while (!ManagementInfo.get().isAdministrativeStateExit()) {
            if (offer(e,MAXTIMETOCHECKADMINSTATE,TimeUnit.SECONDS)) {
                return;
            } else if (!ManagementInfo.get().isAdministrativeStateUnlocked()) {
                throw new InterruptedException("Managment state, not locked.");
            }
        }
        //Throw interruptedException if shutting down
        throw new InterruptedException("Managment state, Exiting");
    }

    @Override
    /* try to take while unlocked - checking for not locked periodically, return null if not unlocked.*/
    public E take() throws InterruptedException {
        E object = null;

        while (ManagementInfo.get().isAdministrativeStateUnlocked()) {
            object = super.poll(MAXTIMETOCHECKADMINSTATE,TimeUnit.SECONDS);
            if (object != null) {
                lastAccess = System.currentTimeMillis();
                return object;
                }
        }
        return null;
    }

    @Override
    /* try to offer while unlocked - checking for not unlocked periodically, return false if no unlocked or
     * time out.*/
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {


        if (Thread.interrupted()) { return false; }

        long nanosRem = unit.toNanos(timeout);
        long waitTime = 0;


        while (ManagementInfo.get().isAdministrativeStateUnlocked()) {

            if (nanosRem <= checkTime)
            {
                waitTime = nanosRem;
            }
            else
            {
                waitTime = checkTime;
            }


            if(super.offer(e, waitTime, TimeUnit.NANOSECONDS))
            {
                lastAccess = System.currentTimeMillis();
                return true;
            } else
            {
                nanosRem-=waitTime;
                if (nanosRem <= 0) {return false;}
            }
        }
        return false;
    }

    public boolean offer(E e) {
        if (super.offer(e)) {
            lastAccess = System.currentTimeMillis();
            signalNotEmpty();
            return true;
        } else
        {
            return false;
        }
    }
    /* offer, with default timeout in MILLISECONDS */
    public boolean offer(E e, long timeout) throws InterruptedException
    {
        return offer(e,timeout,TimeUnit.MILLISECONDS);
    }

    @Override
    /* try to take off queue until timeout or not unlocked.*/
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {

        long nanosRem = unit.toNanos(timeout);
        long waitTime = 0;

        while (ManagementInfo.get().isAdministrativeStateUnlocked()) {

            if (nanosRem <= checkTime)
            {
                waitTime = nanosRem;
            }
            else
            {
                waitTime = checkTime;
            }

            E e = super.poll(waitTime,TimeUnit.NANOSECONDS);
            if (e != null) {
                lastAccess = System.currentTimeMillis();
                return e;
            }
            else {
                nanosRem-=waitTime;
                if (nanosRem <= 0) {return null;}
            }
        }
        return null;
    }


    /**
     * Wait for an item to be added to queue for specified period.
     * @param timeout - time to wait
     * @param unit - unit of time
     * @return true if queue size not 0 during the wait period, false otherwise.
     * @throws InterruptedException - if interrupted
     */
    public boolean waitNotEmpty(long timeout, TimeUnit unit) throws InterruptedException {

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

        while (!ManagementInfo.get().isAdministrativeStateExit()) {

            if (nanosRem <= checkTime)
            {
                waitTime = nanosRem;
            }
            else
            {
                waitTime = checkTime;
            }

            startTime = System.currentTimeMillis();
            synchronized (notEmpty) {
                notEmpty.wait(TimeUnit.NANOSECONDS.toMillis(waitTime));
            }
            stopTime = System.currentTimeMillis();
            if (size() != 0)
                return(true);
            else {
                long execTime  = TimeUnit.MILLISECONDS.toNanos( stopTime - startTime);
                nanosRem-=execTime;
                if (nanosRem <= 0) {return false;}
            }
        }
        return(false);
    }

    /* poll, with default timeout in MILLISECONDS */
    public E poll(long timeout) throws InterruptedException {
        return poll(timeout,TimeUnit.MILLISECONDS);
    }

    public E poll() {
        E val = null;
        val = super.poll();
        if (val != null) {
            lastAccess = System.currentTimeMillis();
        }
        return val;
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



