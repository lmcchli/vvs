package com.abcxyz.services.moip.userinfo;

import com.abcxyz.services.moip.userinfo.NotificationFilter;

public interface UserInfo {

    /** @return the users notification filter. */
    public NotificationFilter getFilter();
    
    /** @return the users notification filter. */
    public NotificationFilter getRoamingFilter();

    /** @return the users telephone number for notifications. */
    public String getNotifNumber();
    
    /** @return the users cos maximum number of Slamdown Information files that can be accumulated. */
    public int getMaxSlamdownInfoFiles();

}
