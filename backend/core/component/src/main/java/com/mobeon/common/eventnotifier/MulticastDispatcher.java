/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.common.eventnotifier;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
//import com.mobeon.masp.util.executor.ExecutorServiceManager;


/**
 * A dispatcher class distributing events to registered listeners. The receivers must fullfill the
 * IEventReceiver interface. The dispacher submit a "dispatchtask" to a thread from a threadpool.
 * The listeners/receivers of events are organized in a tree-like structure utilizing the class Multicaster,
 * in order to minimize the need to create/copy objects.
 * <p/>
 * A client wanting to send an event uses the fireEvent(Event) method. The event is the distributed to all
 * registered IEventReceivers, through calling the doEvent(Event) method of the IEventReceiver. The doEvent callback should
 * be implemented in an asynchronous fashion, if it is not extremely simple in nature.
 * <p/>
 * The EventDispatcher is Threadsafe
 *
 * @author David Looberger
 * @see IEventReceiver
 * @see Multicaster
 */
public class MulticastDispatcher implements IEventDispatcher {
    IEventReceiver subscription_list;
    int numReceivers = 0;
    private final static ArrayList<IEventDispatcher> dispList = new ArrayList<IEventDispatcher>();
    private final static ILogger logger = ILoggerFactory.getILogger(MulticastDispatcher.class);
    private final static ReadWriteLock dispListLock = new ReentrantReadWriteLock();
    private final static Object multicasterLock = new Object();

    public void addEventReceiver(IEventReceiver IEventReceiver) {
        synchronized (multicasterLock) {
            subscription_list = Multicaster.add(subscription_list, IEventReceiver);
            dispListLock.writeLock().lock();
            try{
                numReceivers++;
                if (numReceivers == 1) {
                    dispList.add(this);
                }
            }
            finally{
                dispListLock.writeLock().unlock();

            }
        }
    }

    public void removeEventReceiver(IEventReceiver IEventReceiver) {
        synchronized (multicasterLock) {
            subscription_list = Multicaster.remove(subscription_list, IEventReceiver);
            dispListLock.writeLock().lock();
            try{
                numReceivers--;
                if (numReceivers == 0) {
                    dispList.remove(this);
                }
            }
            finally {
                dispListLock.writeLock().unlock();

            }
        }
    }

    public void removeAllEventReceivers() {
        synchronized (multicasterLock) {
            // TODO: Find a better solution
            ArrayList<IEventReceiver> list = getEventReceivers();
            for (IEventReceiver iEventReceiver : list) {
                removeEventReceiver(iEventReceiver);
            }
        }
    }

    public ArrayList<IEventReceiver> getEventReceivers() {
        if (subscription_list instanceof Multicaster) {
            Multicaster multicaster = (Multicaster) subscription_list;
            return multicaster.getReceiverArray();
        } else {
            ArrayList<IEventReceiver> ret = new ArrayList<IEventReceiver>();
            ret.add(subscription_list);
            return ret;
        }
    }

    public int getNumReceivers() {
        return numReceivers;
    }

    public void fireEvent(final Event e) {
        IEventReceiver list = subscription_list;
        if (list != null)
            list.doEvent(e);
    }

    /**
     * TODO: Implement
     *
     * @param e
     */
    public void fireGlobalEvent(Event e) {
        logger.debug("fire event from dispatcher");
        dispListLock.readLock().lock();
        try{
            for (IEventDispatcher iEventDispatcher : dispList) {
                if (iEventDispatcher instanceof MulticastDispatcher) {
                    MulticastDispatcher multicastDispatcher = (MulticastDispatcher) iEventDispatcher;
                    multicastDispatcher.fireGlobalEventImpl(e);
                }
            }
        }
        finally{
            dispListLock.readLock().unlock();
        }
    }

    public void fireGlobalEventImpl(final Event e) {
        if (subscription_list != null)
            subscription_list.doGlobalEvent(e);
    }
}
