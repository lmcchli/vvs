 /**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util;

import com.mobeon.ntf.util.delayline.Delayable;

/**********************************************************************************
 * This class represent a default notification like "You have new messages".
 */
public class Notification implements Delayable {
    
    protected String recipient;
    protected String billingNumber;
    protected String language;
    protected int transId = -1;
    
    /**********************************************************************************
     * Create a (default) notification. This contain only the recipient. The
     * notification content is decided by the notification interface.
       @param recipient is the receiver address of the notification
     */
    public Notification(String recipient, String language) {
	this.recipient = recipient;           
        this.billingNumber = recipient;
        this.language = language;
    }
    
    /**********************************************************************************
     * Create a copy (clone ?)
     @return a clone of the notification
     */
    public Notification copy() {
	return new Notification(recipient, language);
    }
    
    /**********************************************************************************
     * Get the recipient of the notification
     @return the recipient of the notification
    */
    public String getRecipient() {
	return recipient;
    }

    /**********************************************************************************
    * Get the recipient of the notification
     @return users prefered language(ISO 639)
    */
    public String getLanguage(){
        return this.language;
    }
                       
    /**********************************************************************************
    * Sets the recipients billingnumber   
    */
    public void setBillingnumber(String b){
        this.billingNumber = b;
    }
    /**********************************************************************************
    * Get the subscribers billingnumber
     @return users billingnumber
    */
    public String getBillingNumber(){
        return this.billingNumber;
    }
    
    /**********************************************************************************
     * Calls getRecipient and return that value.
     */
    public Object getKey() {
	return getRecipient();
    }
    
    /**
     *@return the transaction id from this Notification     
     */
    public int getTransactionId(){
        return transId;
    }
    
    /** Sets the transaction for this object.
     *@param id is the transaction for this Notification.
     */
    public void setTransactionId(int id){
        transId = id;
    }
    
    /**********************************************************************************
     * Return a string representation of the count notification
     */
    public String toString() {
	return "{Notification: to=" + recipient + " language=" + language + " transid="+ transId  +"}";
    }
}
