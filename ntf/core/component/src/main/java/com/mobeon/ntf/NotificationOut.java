/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf;

import com.mobeon.ntf.userinfo.OdlFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.Logger;

/**********************************************************************************
 * A general notification out interface. They is recognized through that they
 can be started, they belong to a certain subsystem and all thread is connected
 to a certain thread group.
 */
abstract public class NotificationOut {

    protected final static Logger log = Logger.getLogger(NotificationOut.class); 

    protected boolean isStarted;

    /** The name of the subsystem */
    protected String subSystem;

    /** The thread group for the subsystem */
    protected ThreadGroup subSystemThGrp;

    protected NotificationOut(String subSystem) {
	isStarted = false;
	subSystemThGrp = new ThreadGroup(subSystem);
    }


    /**********************************************************************************
     * Cancel a notification. Shall check that the notification subsystem is started.
     */
    abstract public boolean cancel(String recipient);

    /**********************************************************************************
       Replace current notifications in the "system". Shall check that the
       notification sub system is started.
       @param user user info.
       @param info OdlFilterInfo
       @param recipientId String
       @return true if the notification was added successfully, false otherwise.
     */
    abstract public int notify(UserInfo user, OdlFilterInfo info, String recipientId);


    public boolean isStarted() {
	return isStarted;
    }

    /**********************************************************************************
     * Check if a notification exists or not. Shall check if the notification
     subsystem is started.
       @return true if the notification existed, false otherwise.
     */
    abstract public boolean exists(String recipient);


    abstract public boolean start();

}
