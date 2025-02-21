/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.abcxyz.services.moip.userinfo;

import java.util.*;

public class SmsFilterInfo {

    protected boolean mwi             = false;
    protected boolean sms             = false;
    protected boolean flash           = false;
    protected boolean hasMwiCount     = false;
    protected String notifContent    = "s";
    protected String flashContent     = "";
    
    private HashMap numbers = null;
    
    public SmsFilterInfo(Properties filterInfo, String[] smsNumbers, String[] mwiNumbers) {
        this(filterInfo, smsNumbers, mwiNumbers, null); 
    }
    
    public SmsFilterInfo(Properties filterInfo, String[] smsNumbers, String[] mwiNumbers, String[] flashNumbers) {
        String content;
        content = filterInfo.getProperty("MWI");
        if (content != null) {
            mwi = true;
            if (content.equals("c")) {
                hasMwiCount = true;
            }
        }
        content = filterInfo.getProperty("SMS");
        if (content != null) {
            sms = true;
            if (!content.equals("")) {
                notifContent = content;
            }
        }
        content = filterInfo.getProperty("FLS");
        if( content != null ) {
            flash = true;
            if( !content.equals("")) {
                flashContent = content;
            }
        }
        
        numbers = new HashMap();
        if( smsNumbers != null ) {
            for( int i=0;i<smsNumbers.length;i++ ) {
                numbers.put( smsNumbers[i], "SMS");
            }
        }
        if( mwiNumbers != null ) {
            for( int i=0;i<mwiNumbers.length;i++ ) {
                String value = (String)numbers.get( mwiNumbers[i] );
                if( value == null ) {
                    numbers.put( mwiNumbers[i], "MWI");
                } else {
                    numbers.put( mwiNumbers[i], value + "MWI" );
                }
                    
            }
        }
        if( flashNumbers != null ) {
            for( int i=0;i<flashNumbers.length;i++ ) {
                String value = (String)numbers.get( flashNumbers[i] );
                if( value == null ) {
                    numbers.put( flashNumbers[i], "FLS");
                } else {
                    numbers.put( flashNumbers[i], value + "FLS" );
                }
                    
            }
        }
        
    }
            
    
   

    public String[] getNumbers() {
        Set set = numbers.keySet();
        String[] result = new String[set.size()];
        Iterator it = set.iterator();
        int index = 0;
        while( it.hasNext() ) {
            result[index] = (String)it.next();
            index++;
        }
        return result;
    }
    
    
    
    /**
    **@return true if the MWI kind of SMS notification is selected in the users
    * filter settings.
     */
    public boolean isMwi(String number) {
        String value = (String)numbers.get( number );
        if( value != null ) {
            if( value.indexOf("MWI") == -1 )
                return false;
            else 
                return true;
            
        }
        //?? shouldnt happen 
	return false;
    }
    
    public boolean isMwi() {
        return mwi;
    }
    
    /**
    **@return true if the SMS(textual message) kind of SMS notification is selected in the users
    * filter settings.
     */
    public boolean isSms(String number) {
        String value = (String)numbers.get( number );
        
        if( value != null ) {
            if( value.indexOf("SMS") == -1 )
                return false;
            else 
                return true;
            
        }
        // shouldnt happen
	return false;
    }
    
    public boolean isSms() {
        return sms;
    }
    
    /**
    **@return true if the Flash kind of SMS notification is selected in the users
    * filter settings.
     */
    public boolean isFlash(String number) {
        String value = (String)numbers.get( number );
        
        if( value != null ) {
            if( value.indexOf("FLS") == -1 )
                return false;
            else 
                return true;
            
        }
        // shouldnt happen
	return false;
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
    
    public String toString() {
	return "{SmsFilterInfo}";
    }
}
