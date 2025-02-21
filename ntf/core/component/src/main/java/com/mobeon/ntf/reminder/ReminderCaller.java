package com.mobeon.ntf.reminder;

import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.mail.UserMailbox;

/**
 * Created by IntelliJ IDEA.
 * User: mnify
 * Date: 2007-apr-12
 * Time: 09:58:58
 * To change this template use File | Settings | File Templates.
 */
public interface ReminderCaller {
     public void send(ReminderInfo info, UserInfo user, UserMailbox inbox );
}

