/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.database;


import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierSendInfoSms;

/**
 * The NotifierDatabaseHelper provides convenience methods for accessing the database profile information.
 */
public class NotifierDatabaseHelper {
	
	//Attributes.
    public static final String IDENTITY_SCHEME_MSID = "msid";
    public static final String ATTR_COS_IDENTITY = "MOIPCosIdentity";
    public static final String ATTR_NOTIF_EXP_TIME = "MOIPNotifExpTime";
    public static final String ATTR_MOIP_CALL_TEMPLATES = "MOIPCallFlowTemplates";
    public static final String ATTR_USER_SD = "MOIPUserSD"; //services disabled.
    
    
    //values for multi types.
	private static final String ALL = "ALL"; //all are enabled/disabled
	private static final String NONE = "NONE"; //none are enabled/disabled.
	
	//value in the  ATTR_USER_SD  indicating if replace is switched on.
	private static final String MOIP_SD_REPLACE = "replace";
    
    
    public static String getSubscriberMsidIdentity(ANotifierDatabaseSubscriberProfile subscriberProfile) {
        String msid = null;
        if (subscriberProfile != null) {
            String[] msidArray = subscriberProfile.getSubscriberIdentities(IDENTITY_SCHEME_MSID);
            if(msidArray != null && msidArray.length > 0) {
                msid = msidArray[0];
            }
        }
        return msid;
    }
    
    public static String getCosName(ANotifierDatabaseSubscriberProfile subscriberProfile) {
        String cosName = null;
        if (subscriberProfile != null) {
            String[] cosNameArray = subscriberProfile.getStringAttributes(ATTR_COS_IDENTITY);
            if(cosNameArray != null && cosNameArray.length > 0) {
                cosName = cosNameArray[0];
            }
        } 
        return cosName;
    }
    
    public static int getNotificationSmscExpirationTime(ANotifierDatabaseSubscriberProfile subscriberProfile) {
        int expirTime = ANotifierSendInfoSms.NOTIFICATION_SMSC_VALIDITY_HOURS_DEFAULT;
        if(subscriberProfile != null && subscriberProfile.getCosProfile() != null) {
            int[] expirationTimes = subscriberProfile.getCosProfile().getIntegerAttributes(ATTR_NOTIF_EXP_TIME);
            if(expirationTimes != null && expirationTimes.length > 0) {
                expirTime = expirationTimes[0];
            }
        }
        return expirTime;
    }
    
    public static boolean isTempalateEnabledForSub(ANotifierDatabaseSubscriberProfile subscriberProfile,String eventName) {
    	
    	//note this checks first the subscriber profile, and then the COS if not set in sub.
    	String [] values = subscriberProfile.getStringAttributes(ATTR_MOIP_CALL_TEMPLATES);
    	if (values.length == 0) {
    		//if not set we assume NONE
    		return false;
    	} else
    	{
    		if (values.length==1 ) {
    			//check if value is set to ALL or NONE 
    			if (values[0].equalsIgnoreCase(ALL))
    				{return true;}
    			if (values[0].equalsIgnoreCase(NONE))
    				{return false;}
    		}
    		//check if the value is in the list, return true if enabled.
    		for (int i=0; i< values.length;i++) {
    			if (values[i].equalsIgnoreCase(eventName)) {
    				return true;
    			}  					
    		}
    	}    		    	     	
    	return false;	
    }
    
public static boolean isSubscriberReplaceDisabled(ANotifierDatabaseSubscriberProfile subscriberProfile) {
    	String [] values = subscriberProfile.getStringAttributes(ATTR_USER_SD);
    	if (values.length == 0) {
    		//if not set we assume enabled
    		return false;
    	} else
    	{
    		//check if the value is in the list, return true if enabled.
    		for (int i=0; i< values.length;i++) {
    			String[] split = values[i].split(",");
    			for (int j=0; j< split.length;j++) {
    				if (split[j].equalsIgnoreCase(MOIP_SD_REPLACE)) {
    					return true;
    				}
    			}
    		}
    	}    		    	     	
    	return false;	
    }
}
