/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2013.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.mobeon.ntf.mail;

import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.ntf.Constants.depositType;


/**
 * Represent information about a message deposit for Notification Filter
 * 
 * @since MiO 3.3
 * @author ejulgri
 */
public interface IMessageDepositInfo {

    /**
     * Tells if this email is a new message that shall be notified about.
     *@return true if this email is a normal new message, and false if it is
     * some special message.
     */
    public boolean isNewMessageNotification();

    /**
     * Gets the type of email or message Deposit as an integer.
     * For example NTF_EMAIL,NTF_VOICE
     * @deprecated use getDepositType() instead.
     * @return the email type as an integer.
     */
    public int getEmailType();
    
    /**
     * Gets the type of email or message Deposit as an integer.
     * For example EMAIL,VOICE
     * @return the email type as an integer.
     */
    public depositType getDepositType();

    /**
    *
    *
    * Extracts the phone number of the sender from the FROM header, without
    * the URI
    * @return The phone number of the sender, or "" if no number could be
    * found.
    */
    public String getSenderPhoneNumber();

    /** Fetches the Subject header from the MFS. The subject is stored in the State file as a Container1 attribute.
     * @return Return the subject header as a String object.
     * The string is decoded using the algorithm specified in RFC 2047, Section 6.1.1.
     * If the charset-conversion fails for any sequence, an UnsupportedEncodingException is thrown.
     * If the String is not an RFC 2047 style encoded header, it is returned as-is
     */
    public String getSubject();

    public String getSender();

    /**
     * Tells if this email is urgent or not.
     * @return true if the email is urgent, false otherwise
     */
    public boolean isUrgent();
    
    public String getReceiverPhoneNumber();

    public String getReceiver();

    public NtfEvent getNtfEvent();

    /**
     * Tells if this email is a forced MWI off message for a deleted or modified subscriber.
     * @return true if this email is a MWI off message.
     */
    public boolean isMwiOffUnsubscribed();


}
