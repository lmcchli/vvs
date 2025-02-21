/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo;

import java.util.*;

import com.abcxyz.messaging.vvs.ntf.notifier.NotifierUtil;

public class SmsFilterInfo {
    
    public static final String SMS_PROPERTY_KEY = "SMS";
    public static final String MWI_PROPERTY_KEY = "MWI";
    public static final String FLS_PROPERTY_KEY = "FLS";
    
    public static enum SmsNotificationType {
        SMS("SMS"),
        MWI("MWI"),
        SMS_AND_MWI("SMSMWI");
        
        String internalName = null;
        private SmsNotificationType(String internalName) {
            this.internalName = internalName;
        }
        
        private String getInternalName() {
            return internalName;
        }
    }

    protected boolean mwi             = false;
    protected boolean sms             = false;
    protected boolean flash           = false;
    protected boolean hasMwiCount     = false;
    protected String notifContent     = "s";
    protected String flashContent     = "";
    private boolean roaming           = false; //indicates that one or more notification numbers are roaming.
    
    private HashMap<String, String> numbers = null;
   
    
    public SmsFilterInfo(Properties filterInfo, String[] smsNumbers, String[] mwiNumbers,boolean roaming) {
        this(filterInfo,smsNumbers,mwiNumbers);
        this.setRoaming(roaming);
    }
    
    public SmsFilterInfo(Properties filterInfo, String[] smsNumbers, String[] mwiNumbers) {
        this(filterInfo, smsNumbers, mwiNumbers, null); 
    }
    
    public SmsFilterInfo(Properties filterInfo, String[] smsNumbers, String[] mwiNumbers, String[] flashNumbers,boolean roaming) {
        this(filterInfo, smsNumbers, mwiNumbers, flashNumbers);
        this.roaming = roaming;
    }
    
    public SmsFilterInfo(Properties filterInfo, String[] smsNumbers, String[] mwiNumbers, String[] flashNumbers) {
        String content;
        if (mwiNumbers !=null && mwiNumbers.length > 0) { 
            content = filterInfo.getProperty(MWI_PROPERTY_KEY);
            if (content != null) {
                mwi = true;
                if (content.equals("c")) {
                    hasMwiCount = true;
                }
            }
        }
        if (smsNumbers!=null && smsNumbers.length > 0) {
            content = filterInfo.getProperty(SMS_PROPERTY_KEY);
            if (content != null) {
                sms = true;
                if (!content.equals("")) {
                    notifContent = content;
                }
            }
        }
        content = filterInfo.getProperty(FLS_PROPERTY_KEY);
        if (flashNumbers !=null && flashNumbers.length > 0) { 
            if( content != null ) {
                flash = true;
                if( !content.equals("")) {
                    flashContent = content;
                }
            }
        }
        
        numbers = new HashMap<String, String>();
        if( smsNumbers != null ) {
            for( int i=0;i<smsNumbers.length;i++ ) {
                if (smsNumbers[i] != null) {
                    numbers.put( smsNumbers[i], "SMS");
                }
            }
        }
        if( mwiNumbers != null ) {
            for( int i=0;i<mwiNumbers.length;i++ ) {
                if (mwiNumbers[i] != null) {
                    String value = numbers.get( mwiNumbers[i] );
                    if( value == null ) {
                        numbers.put( mwiNumbers[i], "MWI");
                    } else {
                        numbers.put( mwiNumbers[i], value + "MWI" );
                    }
                }                    
            }
        }
        if( flashNumbers != null ) {
            for( int i=0;i<flashNumbers.length;i++ ) {
                if (flashNumbers[i] != null) {
                    String value = numbers.get( flashNumbers[i] );
                    if( value == null ) {
                        numbers.put( flashNumbers[i], "FLS");
                    } else {
                        numbers.put( flashNumbers[i], value + "FLS" );
                    }
                }                    
            }
        }
        
    }

    public String[] getNumbers() {
        Set<String> set = numbers.keySet();
        ArrayList<String> result = new ArrayList<String>(set.size());
        Iterator<String> it = set.iterator();
        while( it.hasNext() ) {
            String val = it.next();
            if (val != null)    {
                result.add(val);
            }
        }
        if (result.size() == 0) {
            return new String[0];
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Limits the notification numbers in this filter to the given notification number.
     * This setting of the notification number will succeed only if the given notification number 
     * (in its normalised or non-normalised form) is part of the original set of notification numbers in this filter 
     * and its associated SMS notification type is set.
     * 
     * @param notificationNumber the only notification number to be set in this filter.
     * @param isNotificationNumberNormalized indicates if the given notification number is normalised.
     * @param desiredSmsNotifType the type of SMS notification to send (i.e. SMS, MWI or both SMS and MWI); 
     *                            if null, the original type for the notification number in this filter will be used
     * @return true if the given notification number was set as the only notification number in this filter and its SMS notification type was set.
     */
    public boolean limitNotificationNumbersTo(String notificationNumber, boolean isNotificationNumberNormalized, SmsNotificationType desiredSmsNotifType) {
        boolean isNotificationNumberSet = false;
        String desiredSmsNotifTypeString = null;
        if(desiredSmsNotifType != null) {
            desiredSmsNotifTypeString = desiredSmsNotifType.getInternalName();
        }
        Iterator<String> iter = numbers.keySet().iterator();
        while(iter.hasNext()) {
            String filterNumber = iter.next();
            String filterNumberToCompare = filterNumber;
            if(isNotificationNumberNormalized) {
                filterNumberToCompare = NotifierUtil.get().getNormalizedTelephoneNumber(filterNumber);
            }
            if(filterNumberToCompare.equalsIgnoreCase(notificationNumber)) {
                String smsNotifType;
                if(desiredSmsNotifTypeString != null) {
                    smsNotifType = desiredSmsNotifTypeString;
                } else {
                    smsNotifType = numbers.get(filterNumber);
                }
                if (smsNotifType != null) {
                    numbers.clear();
                    numbers.put(notificationNumber, smsNotifType);
                    isNotificationNumberSet = true;
                }
                break;
            }
        }
        return isNotificationNumberSet;
    }

    /**
    **@return @return true if MWI in filter setting and this number is MWI
     */
    public boolean isMwi(String number) {
        if (mwi == false) {
            return false;
        }
        String thisNumber = numbers.get( number );
        if (thisNumber == null) {
            return false;
        }
        
        return ( thisNumber.contains("MWI") );  
    }
    
    public boolean isMwi() {
        return mwi;
    }
    
    /**
    **@return @return true if SMS in filter setting and this number is SMS
     */
    public boolean isSms(String number) {
        if (sms == false) {
            return false;
        }
        String thisNumber = numbers.get( number );
        if (thisNumber == null) {
            return false;
        }
        
        return ( thisNumber.contains("SMS") ); 
    }
    
    public boolean isSms() {
        return sms;
    }
    
    /**
    *@ return true @return true if MWI in filter setting and this number is FLASH
    */
    public boolean isFlash(String number) {
        if (flash == false) {
            return false;
        }
        String thisNumber = numbers.get( number );
        if (thisNumber == null) {
            return false;
        }
        
        return ( thisNumber.contains("FLS") ); 
    }
    
    public boolean isFlash() {
        return flash;
    }
    
    /**
    *@return the notification content selected in the users filter setting for SMS,
    * e.g. "c", "s" or "h".
    */
    public String getNotifContent(){
        return notifContent;
    }
    
    public String getFlashContent() {
        return flashContent;
    }
    
    /**
    *@return true if a MWI count should be sent. Default is false.
    */
    public boolean hasMwiCount(){
        return hasMwiCount;
    }
    
    /**
     *Sets mwi notification flag for this this filter. Defaultvalue: false.
     */
    public void setMwi(boolean mwi){       
        this.mwi = mwi;
    }
    
    /**
    *Sets sms notification flag for this this filter. Defaultvalue: false.
    */
    public void setSms(boolean sms){   
        this.sms = sms;
    }
    
    public void setFlash(boolean flash) {
        this.flash = flash;
    }
    
    public void setNotifContent(String content){
        notifContent = content;
    }

    public void setFlashContent(String content){
        flashContent = content;
    }
    
    public void setHasMwiCount(boolean hasCount){
        hasMwiCount = hasCount;
    }
    
    public boolean isRoaming() {
        return roaming;
    }

    public void setRoaming(boolean roaming) {
        this.roaming = roaming;
    }

    public String toString() {
	return "{SmsFilterInfo}";
    }
}
