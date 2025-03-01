    /*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.operateandmaintainmanager;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.operateandmaintainmanager.MulticasterMock;

import java.util.ArrayList;


    /**
     * A dispatcher class distributing events to registered listeners. The receivers must fullfill the
     * IEventReceiver interface. The dispacher submit a "dispatchtask" to a thread from a threadpool.
     * The listeners/receivers of events are organized in a tree-like structure utilizing the class MulticasterMock,
     * in order to minimize the need to create/copy objects.
     * <p>
     * A client wanting to send an event uses the fireEvent(Event) method. The event is the distributed to all
     * registered IEventReceivers, through calling the doEvent(Event) method of the IEventReceiver. The doEvent callback should
     * be implemented in an asynchronous fashion, if it is not extremely simple in nature.
     *
     * The EventDispatcher is Threadsafe
     * @author David Looberger
     *
     * @see com.mobeon.common.eventnotifier.IEventReceiver
     * @see MulticasterMock
     */
    public class MulticastDispatcherMock implements IEventDispatcher
    {
        IEventReceiver subscription_list;
        int numReceivers = 0;
        private static final ArrayList<IEventDispatcher> dispList = new ArrayList<IEventDispatcher>();

        public synchronized void addEventReceiver( IEventReceiver IEventReceiver )
        {   subscription_list =  MulticasterMock.add( subscription_list, IEventReceiver);
            numReceivers++;
            if (numReceivers == 1) {
                dispList.add(this);
            }
        }

        public synchronized void removeEventReceiver( IEventReceiver IEventReceiver )
        {   subscription_list = MulticasterMock.remove( subscription_list, IEventReceiver );
            numReceivers--;
            if (numReceivers == 0) {
                // dispList.remove(this);
            }
        }

        public synchronized void removeAllEventReceivers() {
            // TODO: Find a better solution
            ArrayList<IEventReceiver> list = getEventReceivers();
            for (IEventReceiver iEventReceiver : list) {
                removeEventReceiver(iEventReceiver);
            }
        }

        public ArrayList<IEventReceiver> getEventReceivers() {
            if (subscription_list instanceof MulticasterMock) {
                MulticasterMock multicaster = (MulticasterMock) subscription_list;
                return multicaster.getReceiverArray();
            }
            else {
                ArrayList<IEventReceiver> ret = new ArrayList<IEventReceiver>();
                ret.add(subscription_list);
                return ret;
            }
        }

        public int getNumReceivers() {
            return numReceivers;
        }

        public void fireEvent(final Event e)
        {
            if( subscription_list != null )
                subscription_list.doEvent( e );
        }

        /**
         * TODO: Implement
         * @param e
         */
        public void fireGlobalEvent(Event e) {
            for (IEventDispatcher iEventDispatcher : dispList) {
                if (iEventDispatcher instanceof MulticastDispatcherMock) {
                    MulticastDispatcherMock multicastDispatcher = (MulticastDispatcherMock) iEventDispatcher;
                    multicastDispatcher.fireGlobalEventImpl(e);
                }
            }
        }

        public void fireGlobalEventImpl(final Event e) {
            if( subscription_list != null )
                subscription_list.doGlobalEvent( e );
        }
    }




