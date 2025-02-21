/* Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail;

import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.util.CommaStringTokenizer;
import java.util.*;


public class SystemNotificationFilterInfo extends SmsFilterInfo {

    /**
     * Constructor
     */
    /*
    public SystemNotificationFilterInfo(String sysNotifName, Vector devices) {
        super(CommaStringTokenizer.getPropertiesFromLists("SMS", sysNotifName), devices);
    }
    */
    public SystemNotificationFilterInfo(String sysNotifName, String[] smsNumbers) {
        super(CommaStringTokenizer.getPropertiesFromLists("SMS", sysNotifName),smsNumbers, null );
    }
    
    public String toString() {
	return "{SystemNotificationFilterInfo}";
    }
}
