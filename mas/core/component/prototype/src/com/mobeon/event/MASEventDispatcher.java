/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */
package com.mobeon.event;

import org.apache.log4j.Logger;

import javax.swing.event.EventListenerList;

import com.mobeon.event.types.MASEvent;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2004-nov-25
 * Time: 18:17:58
 * To change this template use File | Settings | File Templates.
 */
public class MASEventDispatcher {
    static Logger logger = Logger.getLogger("com.mobeon");
    private EventListenerList listenerList = new EventListenerList();
   //  private MASEvent MASEvent = null;

    public MASEventDispatcher() {
         EventListenerList listenerList = new EventListenerList();
         MASEvent MASEvent = null;
    }

    public void addMASEventListener(MASEventListener l) {
        listenerList.add(MASEventListener.class, l);
    }

    public void removeMASEventListener(MASEventListener l) {
        listenerList.remove(MASEventListener.class, l);
    }

    public void fireNotify() {
       fire(new MASEvent(this));
    }

    public void fire(MASEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==MASEventListener.class) {
                logger.debug("Fire NOTIFY event (" + e.getClass() + ")");
                ((MASEventListener)listeners[i+1]).notify(e);
            }
        }
    }

}
