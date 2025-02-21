/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.database;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;

/**
 * This sample class acts as a "dummy" subscriber profile and does not contain any attributes or attribute values.
 * Its sole purpose is to enable the NTF and the Notifier plug-in code to send notifications without any
 * subscriber preferred values.
 */
public class NotifierSubscriberProfileEmpty extends ANotifierDatabaseSubscriberProfile {

    public static final String[] STRING_ARRAY_ONE_EMPTY_STRING_VALUE = { "" };

        
    public String[] getStringAttributes(String attributeName) {
        return STRING_ARRAY_ONE_EMPTY_STRING_VALUE;
    }
    
}
