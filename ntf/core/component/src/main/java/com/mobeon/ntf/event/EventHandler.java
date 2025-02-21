package com.mobeon.ntf.event;

import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.UserLogins;

/**
 * Created by IntelliJ IDEA.
 * User: mnify
 * Date: 2007-jun-04
 * Time: 15:44:41
 */
public class EventHandler {
    private final static Logger log = Logger.getLogger(EventHandler.class);

    public EventHandler() {
    }

    public void handleMailLogin(NotificationEmail email) {
        UserInfo user = UserFactory.findUserByMail(email.getReceiver());
        handleLogin(user.getTelephoneNumber());
    }

     public void handleMailLogout(NotificationEmail email) {
        UserInfo user = UserFactory.findUserByMail(email.getReceiver());
        handleLogout(user.getTelephoneNumber());
    }

    public void handleLogin(String telephoneNumber) {
       log.logMessage("HandleLogin for " + telephoneNumber, Logger.L_DEBUG);
       UserLogins.loginUser(telephoneNumber);
    }

    public void handleLogout(String telephoneNumber) {
       log.logMessage("HandleLogout for " + telephoneNumber, Logger.L_DEBUG);
       UserLogins.logoutUser(telephoneNumber);
    }

}
