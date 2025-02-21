/* Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail;

import com.mobeon.ntf.userinfo.EmailFilterInfo;
import com.mobeon.ntf.util.CommaStringTokenizer;
import java.util.*;


public class EmailSystemNotificationFilterInfo extends EmailFilterInfo {

    /**
     * Constructor
     */
    public EmailSystemNotificationFilterInfo(String sysNotifName, String[] emailAddresses) {
        super(CommaStringTokenizer.getPropertiesFromLists("EML", sysNotifName),emailAddresses);
    }
    
    public String toString() {
	return "{EmailSystemNotificationFilterInfo}";
    }
}
