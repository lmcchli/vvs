 /**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util;

/**********************************************************************************
 * The class represent a Message Count notification (either by type or not). It
 carries only some integers and can be used for other things as well that should
 carry integers. Input values is not checked for positive values.
 */
public class CountNotification extends Notification {
    
    protected boolean byType;
    protected int total = -1;
    protected int emails = -1;
    protected int voices = -1;
    protected int faxes = -1;
    protected String language;


    /**********************************************************************************
     * Create a count notification with only total count. The other parameters
     is set to -1.
     @param recipient is the recipient of the notification
     @param total is the total number of messages (or whatever)
     @param language is the users prefered language (ISO 639)
    */
    public CountNotification(String recipient, int total, String language) {
	super(recipient, language);
	byType = false;
	this.total = total;
	emails = -1;
	voices = -1;
	faxes = -1;
        this.language = language;
    }
    
    /**********************************************************************************
     * Create a count notification by type.
     @param recipient is the recipient of the notification
     @param total is the total number of messages (or whatever)
     @param email is the number of email messages
     @param voice is the number of voice messages
     @param fax is the number of fax messages
     @param language is the users prefered language (ISO 639)
    */
    public CountNotification(String recipient, int total, int email, int voice, int fax, String language) {
	super(recipient, language);
	byType = true;
	this.total = total;
	emails = email;
	voices = voice;
	faxes = fax;
        this.language = language;
    }


    /**********************************************************************************
     * Create a copy of the count notification. Should perhaps be called clone
     @return a clone of the count notifictaion
     */
    public Notification copy() {
	return new CountNotification(recipient, total, emails, voices, faxes, language);
    }
    
    
    /**********************************************************************************
     * Get the number of total messages. This is not calculated from other
     values.
     @return the total number of messages or -1 if it is not set
    */
    public int getTotalCount() {
	return total;
    }
    

    /**********************************************************************************
     * Get the number of email messages. This is not calculated from other
     values.
     @return the total number of email messages or -1 if it is not set
    */
    public int getEmailCount() {
	return emails;
    }

    /**********************************************************************************
     * Get the number of fax messages. This is not calculated from other
     values.
     @return the total number of fax messages or -1 if it is not set
    */
    public int getFaxCount() {
	return faxes;
    }

    /**********************************************************************************
     * Get the number of voice messages. This is not calculated from other
     values.
     @return the total number of voice messages or -1 if it is not set
    */
    public int getVoiceCount() {
	return voices;
    }

             
    /**********************************************************************************
     * Get the number of voice messages. This is not calculated from other values.
     @return the users prefered language (ISO 639)  
    */
    public String getLanguage(){
        return this.language;
    }
    
    /**********************************************************************************
     * Check if the count notification has been instansiated as a notification
     by type.
     @return true if the count notification has been instansiated as a
     notification by type. False otherwise.
    */
    public boolean isByType() {
	return byType;
    }

    /**********************************************************************************
     * Return a string representation of the count notification
     */
    public String toString() {
	if(byType) {
	    return "{CountNotification: to=" + recipient + " total=" + total + " emails=" + emails + " voice=" + voices + " fax=" + faxes + " language="+ this.language +" transid=" + transId  + "}";
	}
	else {
	    return "{CountNotification: to=" + recipient + " total=" + total + " language="+ this.language +" transId=" + transId  + "}";
	}
    }
}
