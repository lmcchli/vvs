package com.abcxyz.services.moip.userinfo;

import com.abcxyz.services.moip.userinfo.mcd.McdUserInfo;
import com.abcxyz.services.moip.userinfo.mcd.McdUserFinder;

/**
 * UserFactory creates UserInfo objects with information from a MUR user
 * directory.
 * <p/>
 * The factory does not know much about the user, only the attributes that can
 * be used to find a user. The rest is up to UserInfo subclasses.
 */
public class UserFactory {

    /**
     * Constructor
     */
    public UserFactory() {
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
     * Get basic UserFactory info in printable form.
     *
     * @return description of this UserFactory
     */
    public String toString() {
        return "{UserFactory}";
    }
}
