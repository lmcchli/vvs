/**
 * Copyright (c) 2003, 2004, 2005 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo;

import com.mobeon.ntf.userinfo.mcd.McdUserInfo;
import com.mobeon.ntf.userinfo.mcd.McdUserFinder;

import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.userinfo.mail.MailUserInfo;


/**
 * UserFactory creates UserInfo objects with information from a MUR user
 * directory.
 * <p/>
 * The factory does not know much about the user, only the attributes that can
 * be used to find a user. The rest is up to UserInfo subclasses.
 */
public class UserFactory {
/* Following the YAGNI (you aren't going to need it) principle, the current
 * version can only create users from a Mur directory. If other schema versions,
 * or even non-Mur data sources, are needed, refactoring is necessary.
 */

    /**
     * Constructor
     */
    public UserFactory() {
    }

    /**
     * This method does not really "find" a user, it creates a user from
     * information in an email.
     *
     * @param mail - the mail to create the user from.
     * @return the new user information.
     */
    static public UserInfo findUser(NotificationEmail mail) {
        return new MailUserInfo(mail);
    }

    /**
     * findUserByMail looks for a user based on a mail address.
     *
     * @param mail the email address identifying the user.
     * @return a new UserInfo object for the found user, or null if no user was
     *         found.
     */
    static public UserInfo findUserByMail(String mail) {
        McdUserFinder mcdf = new McdUserInfo();
        if (mcdf.findByMail(mail)) {
            return (UserInfo) (mcdf);
        } else {
            return null;
        }
    }

    /**
     * findUserByTelephoneNumber looks for a user based on a mail address.
     *
     * @param phone - the telephone number identifying the user.
     * @return a new UserInfo object for the found user, or null if no user was
     *         found.
     */
    static public UserInfo findUserByTelephoneNumber(String phone) {
        McdUserFinder mcdf = new McdUserInfo();
        if (mcdf.findByTelephoneNumber(phone)) {
            return (UserInfo) (mcdf);
        } else {
            return null;
        }
    }

    /**
     * readByDn looks for a user based on a dn.
     *
     * @param dn - the distingish name identifying the user.
     * @return a new UserInfo object for the found user, or null if no user was
     *         found.
     */
    static public UserInfo readByDN(String dn) {
        McdUserFinder mcdf = new McdUserInfo();
        if (mcdf.readByIdentity(dn)) {
            return (UserInfo) (mcdf);
        } else {
            return null;
        }
    }
    
    /**
     * Creates user information based on the data contained in the passed email.
     * <p>
     * The email object must contained information from a user. It will
     * be used to achieve the last required services.
     * </p>
     * <p>
     * This is used, for now, to turn MWI off for deleted or changed users
     * </p>
     * 
     * @param email Email object containing user information .
     * @return UserInfo object containing the user's information.
     */
    public static UserInfo getUnsubscribedUser(NotificationEmail email) {
        return new MwiUnsubscribedUserInfo(email);
    }

    /**
     * Get basic UserFactory info in printable form.
     *
     * @return description of this UserFactory
     */
    public String toString() {
        return "{UserFactory}";
    }
}
