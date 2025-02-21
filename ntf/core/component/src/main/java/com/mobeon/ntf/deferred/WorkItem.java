/*
 * WorkItem.java
 *
 * Created on den 14 september 2004, 10:30
 */

package com.mobeon.ntf.deferred;

import com.mobeon.common.storedelay.DelayHandler;
import com.mobeon.common.storedelay.DelayInfo;
import com.mobeon.common.storedelay.DelayEvent;

/**
 * Package information about a delayed event into one object.
 * This is so we can put the information in a queue and
 * take care of it in a separate thread.
 */
public class WorkItem {

    private DelayHandler delayer;
    private DelayInfo    info;
    private int          status;
    private DelayEvent   event;


    /**
     * Create with initial data
     **/
    public WorkItem(DelayHandler delayer, DelayInfo info,
                    int status, DelayEvent event)
    {
        this.delayer = delayer;
        this.info    = info;
        this.status  = status;
        this.event   = event;
    }

    public DelayHandler getDelayer()
    {
        return delayer;
    }

    public DelayInfo getInfo()
    {
        return info;
    }

    public int getStatus()
    {
        return status;
    }

    public DelayEvent getEvent()
    {
        return event;
    }


}
