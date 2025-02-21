/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.eventnotifier;

import java.util.ArrayList;


/**
    The multicaster (modeled after the AWTEventMulticaster) provides an
    efficient way to manage relatively-short lists of subscribers. Each
    Multicaster object can reference two Subscriber objects, one or both
    of which can be another multicaster. The top-level multicaster is
    passed a publish message, which it broadcasts (recursively) to both
    of the subscribers that it references.


    The multicaster is an immutable object, so you can't modify it. The
    add() method, for example, is passed two multicasters and returns a
    third one that effectively references all the subscribers referenced by
    the original two. Any notifications that are in progress when the add()
    is executed will not be affected by the operation, however. It's
    perfectly acceptable for notifications to be performed on one thread
    while a second thread is adding or removing members from the
    Multicaster. The order in which Subscribers are notified is undefined.
    (It is not necessarily the order of insertion.)

    @see java.awt.AWTEventMulticaster

    @see IEventReceiver

*/



public class Multicaster implements IEventReceiver
{
    protected final IEventReceiver a, b;

    protected Multicaster(IEventReceiver a, IEventReceiver b)
    {   this.a = a;
        this.b = b;
    }

    /**
       Ask the IEventReceivers of this multicaster to receive the
       publication. This is the publish operation, as seen from
       the perspective of a IEventReceiver. Remember, a multicaster is a list of
       IEventReceivers. Note that the order of traversal should generally be
       considered undefined. However, if you really need to notify listeners
       in a known order, and you consistently add nodes as follows:


       subscription_list = Multicaster.add( subscription_list, new_node );


       (Where subscription_list is the head-of-list reference and
       new_node is the node you're adding), IEventReceivers  will be
       notified in the order they were added. Removing nodes does not affect
       the order of notification. If you transpose the two arguments in the
       foregoing code:


       subscription_list = Multicaster.add( new_node, subscription_list );


       IEventReceivers will be notified in reverse order.
    */
    public void doEvent( Event e )
    {   a.doEvent( e );
        b.doEvent( e );
    }




    /**
       Add a new IEventReceiver to the list. The way that this call is used can
       impact the order in which IEventReceivers are notified. (See receive().)

       @param a Typically the head-of-list pointer.

       @param b Typically the IEventReceiver you're adding to the list.
    */
    public static IEventReceiver add(IEventReceiver a, IEventReceiver b)
    {   return  (a == null)  ? b :
                (b == null)  ? a : new Multicaster(a, b);
    }




    /**
       Remove the indicated IEventReceiver from the list
    */
    public static IEventReceiver remove(IEventReceiver list, IEventReceiver remove_me)
    {
        if( list == remove_me || list == null  )
            return null;
        else if( !(list instanceof Multicaster) )
            return list;
        else
            return ((Multicaster)list).remove( remove_me );
    }

    private IEventReceiver remove(IEventReceiver remove_me)
    {
        if (remove_me == a)  return b;
        if (remove_me == b)  return a;

        IEventReceiver a2 = remove( a, remove_me );
        IEventReceiver b2 = remove( b, remove_me );

        return (a2 == a && b2 == b ) // it's not here
                ? this
                : add(a2, b2)
                ;
    }

    public void doGlobalEvent(Event event) {
        a.doGlobalEvent( event );
        b.doGlobalEvent( event );
    }

    public ArrayList<IEventReceiver> getReceiverArray() {
        ArrayList<IEventReceiver> ret = null;
        if (a instanceof Multicaster) {
            Multicaster multicaster = (Multicaster) a;
            ret = multicaster.getReceiverArray();
        }
        if (b instanceof Multicaster) {
            Multicaster multicaster = (Multicaster) b;
            if (ret == null)
                ret = multicaster.getReceiverArray();
            else
                ret.addAll(multicaster.getReceiverArray());
        }

        // Handle the case where a & b NOT are Multicasters
        if (ret == null)
            ret = new ArrayList<IEventReceiver>();
        if (! (a instanceof Multicaster)) {
            ret.add(a);
        }
        if (! (b instanceof Multicaster)) {
            ret.add(b);
        }

        return ret;
    }
}