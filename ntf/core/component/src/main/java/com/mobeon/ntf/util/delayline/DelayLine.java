/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.delayline;

import java.util.*;
import java.io.*;

import com.mobeon.ntf.text.Template;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.time.NtfTime;


/**
 * DelayLine is the basic delay line that can just delay an object through a
 * sequence of delays.
 */
public class DelayLine implements DelayEventListener {
    
    protected final static Logger log = Logger.getLogger(DelayLine.class); 
    /**This is the hashtable that contain the actual data.
     */
    protected Hashtable _objectTable;
    /** Delayables that are waiting for a trigger to start. */
    protected Hashtable _held;

    protected Delayer _delayers[];
    protected int _numDelayers;

    protected DelayEventListener _listener;
    protected DelayEventListener _timeoutListener;



    /**
     * Constructor.
     *@param grp - thread group of the delayers, for log name. If null, a
     * default name will be used.
     *@param delayers - list of delay times in seconds.
     *@param timeoutDelayer - delay time in seconds from the last regular delay
     * until the timeout handler is notified.
     */
    public DelayLine(ThreadGroup grp, int delayers[], int timeoutDelayer) {
        _listener = null;

        // Do not forget the last "timeout" delayer
        _delayers = new Delayer[delayers.length + 1];
        for (int i = 0; i < delayers.length; i++) {
            if (grp == null) {
                _delayers[i] = new Delayer(null, "Delayer_" + i + "_" + delayers[i], delayers[i], this);
            } else {
                _delayers[i] =
                    new Delayer(grp, "Delayer_" + i + "_" + delayers[i], delayers[i], this);
            }
        }
        if (grp == null) {
            _delayers[delayers.length] = new Delayer(null, "Delayer_timeout_" + timeoutDelayer, timeoutDelayer, this);
        } else {
            _delayers[delayers.length] =
                new Delayer(grp, "Delayer_timeout_" + timeoutDelayer, timeoutDelayer, this);
        }

        // Set up the hashtable
        _objectTable = new Hashtable(1000);
    }

    /**
     * Constructor.
     *@param delayers - list of delay times in seconds.
     *@param timeoutDelayer - delay time in seconds from the last regular delay
     * until the timeout handler is notified.
     */
    public DelayLine(int delayers[], int timeoutDelayer) {
        this(null, delayers, timeoutDelayer);
    }

    /**
     * Retrieve an object from the delay line.
     *@param key is the key of the item that is retrieved
     *@return the item, or null if it does not exist.
     */
    public synchronized Delayable get(Object key) {
        DelayItem theItem = (DelayItem) _objectTable.get(key);
        if (theItem == null) {
            return null;
        }
        return theItem.getItem();
    }

    /**
     * Retrieve the transaction id from the object within the delayline.
     *@param key - the key of the item that is retrieved
     *@return the transaction id of the itme, or -1 if it does not exist,
     */
    public synchronized int getTransactionId(Object key) {
        DelayItem theItem = (DelayItem) _objectTable.get(key);
        if (theItem == null) {
            return -1;
        }
        return theItem.getItemTransactionId();
    }

    /**
     * Get all keys for the objectTable.
     *@return enumeration of all keys in the delay line.
     */
    public synchronized Enumeration getQueueKeys() {
        return _objectTable.keys();
    }

    /**
     * Get the number of items in the delay line. Does only retrieve the number
     * of active (not cancelled) items.
     *@return the number of items in the complete delay line.
     */
    public synchronized int size() {
        return _objectTable.size();
    }

    /**
     * Cancel an item in the delay line. The delay item is returned.
     *@param key - the key to the object to cancel.
     *@return the item removed from the delayline, or null if the item did not
     * exist.
     */
    public synchronized Delayable cancel(Object key) throws java.io.IOException {
        if (key == null) { return null; }

        Object theKey = key;
        if (key instanceof Delayable) {
            theKey = ((Delayable) key).getKey();
        }

        if (theKey == null) { return null; }

        DelayItem it = (DelayItem) _objectTable.remove(theKey);
        if (it != null) {
            Delayable ref = it.getItem();
            it.cancel();
            return ref;
        }
        return null;
    }

    /**
     * Check if a value exists and is not cancelled in the delay line.
     *@param key - is the key to find.
     *@return true if the object was found and is not cancelled, false otherwise
     */
    public synchronized boolean exists(Object key) {
        if (key == null) { return false; }

        Object theKey = key;
        if (key instanceof Delayable) {
            theKey = ((Delayable) key).getKey();
        }

        if (theKey == null) { return false; }

        DelayItem it = (DelayItem) _objectTable.get(theKey);

        return it != null && !it.isCancelled();
    }

    /**
     * Cancel current instances of the key and add the key with object to the delay line.
     *@param key - the key.
     *@param item - a delay item that is either new or reinserted.
     *@return the previously stored item with the same key.
     */
    protected synchronized Delayable in(Object key, DelayItem item) throws java.io.IOException {
        if (key == null || item == null) { return null; }

        Delayable prevValue = cancel(key);

        _objectTable.put(key, item);
        if (!item.isOnHold()) {
            _delayers[0].add(item);
        }
        return prevValue;
    }

    /**
     * Add a object to the delay line. Previous Delayables with the same key are
     * replaced.
     *@param delayable - the object to delay.
     *@return previous item with the same key.
     */
    public synchronized Delayable in(Delayable delayable) throws java.io.IOException {
        if (delayable == null || delayable.getKey() == null) { return null; }

        DelayItem newItem = new DelayItem(delayable);
        newItem.setQueueNumber((byte) 0);
        newItem.setArrivalTime(NtfTime.now);
        return in(delayable.getKey(), newItem);
    }

    /**
     * Adds an object to the delay line, but does not start the delays. Previous
     * Delaybles with the same key are replaced. The delays are started by a
     * later call to start().
     *@param delayable - the object to delay.
     *@return previous item with the same key.
     */
    public synchronized Delayable hold(Delayable delayable) throws java.io.IOException {
        if (delayable == null || delayable.getKey() == null) { return null; }

        DelayItem newItem = new DelayItem(delayable);
        newItem.setQueueNumber((byte) 0);
        newItem.setArrivalTime(NtfTime.now);
        newItem.setOnHold(true);
        return in(delayable.getKey(), newItem);
    }

    /**
     * Starts the delays for a Delayable on hold.
     *@param key - key of the object to start.
     */
    public synchronized void start(Object key) {
        if (key == null || !exists(key)) { return; }

        DelayItem item = (DelayItem) _objectTable.get(key);
        if (item != null) {
            if (item.isOnHold()) {
                item.setArrivalTime(NtfTime.now);
                item.setOnHold(false);
                _delayers[0].add(item);
            } else {

            }
        }
    }

    /**
     * Set the listener for the Delay Line. The method delayCompleted is called
     * with the delayed item when delay ends.
     *@param listener - the listener for the Delay Line.
     */
    public void setListener(DelayEventListener listener) {
        _listener = listener;
    }

    /**
     * Set the listener for the Delay Line timeout. The method delayCompleted is called
     * with the delayed item when the object has timed out.
     * @param listener - the timeoutlistener for the Delay Line.
     */
    public void setTimeoutListener(DelayEventListener listener) {
        _timeoutListener = listener;
    }

    /**
     * Called by a delayer when an object expires, so the delay line can move it
     * to the next delayer and notify its own listener.
     *@param o - the object that completed the delay.
     */
    public void delayCompleted(Object o) {
        log.logMessage("Delay line delay completed for " + o, log.L_DEBUG);
        if (o instanceof DelayItem) {
            // Check if the item comes from the timeout queue.
            DelayItem popped = (DelayItem) o;
            if (popped == null || popped.isCancelled()) {
                log.logMessage("DelayLine: not forwarding " + popped + " since it is deleted", log.L_DEBUG);
                return;
            }
            byte queueNum = popped.getQueueNumber();
            Delayable item = popped.getItem();
            if (queueNum == _delayers.length - 1) {
                try {
                    cancel(item);
                } catch (java.io.IOException ioE) {
                    log.logMessage("Delay line could not cancel item " + item + ". Message: " + ioE.getMessage(), log.L_ERROR);
                }
                if (_timeoutListener != null) {
                    _timeoutListener.delayCompleted(item);
                }
            } else if (_listener != null) {
                log.logMessage("Delay line forward " + o, log.L_DEBUG);
                _listener.delayCompleted(item);
                popped.setQueueNumber((byte) (queueNum + 1));
                _delayers[queueNum + 1].add(popped);
            }
        }
    }
}


