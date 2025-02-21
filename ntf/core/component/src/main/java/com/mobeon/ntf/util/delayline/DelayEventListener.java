 /**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.delayline;

/****************************************************************
 * The listener interface for receiving delay events. The class that is
 * responsible for handling objects popping out of a delaying object (such as
 * Delayer or DelayLine) implements this interface, and is registered with the
 * delaying object when it is created
 */

public interface DelayEventListener {
    /****************************************************************
     * Invoked when the delay time of an Object has expired.
     */
    public void delayCompleted(Object o);
}
