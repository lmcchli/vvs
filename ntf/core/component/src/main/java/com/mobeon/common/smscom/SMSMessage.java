/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom;

import java.util.Date;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierSendInfoSms.NotifierSmppPduType;
import com.mobeon.common.sms.request.Request;
import com.mobeon.common.smscom.charset.Converter;
import com.mobeon.common.smscom.udh.ApplicationPort16Bits;
import com.mobeon.common.smscom.udh.IE;
import com.mobeon.common.smscom.udh.UserDataHeader;
import com.mobeon.ntf.util.NtfUtil;

/**
 * SMSMessage carries all message-related information for sending a short
 * message. The basic parameters are set in the constructor, and set-methods can
 * be used for more special parameters.<P>
 * SMSMessage supports fragmentation of messages if a maximum fragment size is
 * set with setFragmentSize(). The idea is that a "stupid" consumer just calls
 * getText(), getPosition() and getLength() and gets the current fragment text,
 * while a more intelligent producer controls the fragmentation using
 * nextFragment() and hasMoreFragments(). This allows the producer control over
 * the handling of each fragment.
 */
public class SMSMessage {
    /**The text part of the SMS message*/
    protected byte[] text;
    /**The data coding scheme for the message*/
    protected int dcs = Request.GSM_DCS_GENERAL_DATA_CODING_INDICATION_8_BIT_DATA;
    /**Message count to send with the message*/
    protected int count = -1;
    /**Time when the message is obsolete and can be discarded*/
    protected Date expiryTimeAbsolute = null;
    /**Time when the message is obsolete and can be discarded*/
    protected int expiryTimeRelative = 7 * 24;
    /**Protocol id of the message*/
    protected int pid = 0;
    /**Tells if the message should replace a previous message*/
    protected boolean replace = false;
    /**Tells if the mobile should alert the user when the notification arrives*/
    protected int alert = -1;
    /**Service type for CDMA networks*/
    protected String serviceType = "";
    /** The priority for the message */
    protected int priority = 0;
    /**The maximum fragment size in bytes*/
    protected int fragSize;
    /**fragPos always points to the first octet in the next fragment*/
    protected int fragPos = 0;
    /**The number of the current fragment (after nextFragment())*/
    protected int fragNo = -1;
    /* Used for phoneon */
    protected boolean useReplyPath = false;
    /* Used for phoneon */
    protected boolean deliveryReceipt = false;
    /* The time the message is to be scheduled by the SMS-C for delivery */
    protected int scheduledDeliverTime = 0;
    /* contains userdataheaders */
    protected UserDataHeader udh = null;

    protected boolean packed = false;

    private Converter converter = null;

    private NotifierSmppPduType notifierSmppPduType = NotifierSmppPduType.SUBMIT_SM;
    private boolean setDpf = false;

     /**
     * Constructor with the most basic message parameters. Additional parameters
     * can be added with set methods.
     * @param text the bytes of the message text.
     * @param dcs the data coding scheme of the bytes.
     * same service type.
     *
     */
    public SMSMessage(byte[] text, int dcs) {
        if (text != null) {
            this.text = text;
        } else {
            this.text = "".getBytes();
        }
        this.dcs = dcs;
        fragSize= this.text.length;
    }

    /**Check if there are more fragments, i.e. if nextFragment can be called at
       least once more.
       @return true iff there are more fragments*/
    public boolean hasMoreFragments() {
        if (this.text == null) {
            return false;
        }

        return fragNo < 0 ||
            fragNo >= 0 && fragPos + fragSize < this.text.length;
    }

    /**Move on to the next fragment.*/
    public void nextFragment() {
        if (fragNo >= 0) {
            fragPos += fragSize;
        }
        fragNo++;
    }

    /**Get the position in the text of the current fragment
     * @return the position of the current fragment.*/
    public int getPosition() {
        if (this.text == null) {
            return 0;
        }

        if (fragPos >= this.text.length) {
            return 0;
        } else {
            return fragPos;
        }
    }

    /**Get the number of the current fragment, starting from 0.
       @return the number of the current fragment.*/
    public int getFragmentNumber() {
        return fragNo;
    }

    /**@return true if the SMS consists of more that one fragment*/
    public boolean isLargerThanOneFragment() {
        return text.length > fragSize;
    }

    /**Get the length of the current fragment.
     * @return the length of the current fragment.*/
    public int getLength() {
        int len = 0;
        if (this.text != null) {
            len = this.text.length - fragPos;
            if (len > fragSize) { len = fragSize; }
            if (len < 0) { len = 0; }
        }

        return len;
    }

    public int getLength(int fillBits) {
    	int len = getLength();
    	if( len == fragSize ) {
			len = (len * 8 - fillBits) / 7; //make length in 7 bits.
			if(len > (text.length-fragPos)) {
				len = (text.length-fragPos);
			}
		}
    	return len;
    }

    /**Sets the fragment size for the message.
     *@param fragSize - the maximum size of the fragments.
     */
    public void setFragmentSize(int fragSize) {
        if (fragSize > 0) { this.fragSize = fragSize; }
    }

    public void adjustFragziseForUDH() {
    	fragSize -= getUDHLength();
    }

    /**Get the text of the message.
       @return the message text.*/
    public byte[] getText() { return this.text; }

    /**Sets the text of the message.*/
    public void setText(byte[] text) { this.text = text; }

    /**Get the expiry time of the message.
       @return the time when the message expires.*/
    public Date getExpiryTimeAbsolute() { return expiryTimeAbsolute; }
    /**Get the expiry time of the message.
       @return the time when the message expires.*/
    public int getExpiryTimeRelative() { return expiryTimeRelative; }
    /**Set the expiry time of the message.
       @param exp - the new expiry time*/
    public void setExpiryTimeRelative(int exp) { this.expiryTimeRelative = exp; }
    /** Get the scheduled delivery time the message shall first be attempt from
     * the SMS-C.
     * @return the delayed time the message shall first be attempt */
    public int getScheduledDeliveryTime() {
        return scheduledDeliverTime;
    }
    /** Set the scheduled delivery time the message shall first be attempt from
     * the SMS-C.
     * @param delay - the delayed time in seconds. */
    public void setScheduledDeliveryTime(int delay) {
        scheduledDeliverTime = delay;
    }
    /**Get the replace flag of the message.
       @return true iff the message will replace.*/
    public boolean getReplace() { return replace; }
    /**Set the replace flag of the message.
       @param replace the new replace flag.*/
    public void setReplace(boolean replace) { this.replace = replace; }

    /**Get the DCS of the message.
       @return the DCS.*/
    public int getDCS() { return dcs; }
    /**Set the DCS of the message.
       @param dcs - the new DCS.*/
    public void setDCS(int dcs) { this.dcs = dcs; }

    /**Get the message count for the message.
       @return the message count.*/
    public int getCount() { return count; }
    /**Set the message count for the message.
       @param count - the new message count.*/
    public void setCount(int count) { this.count = count; }

    /**Get the PID of the message.
       @return the PID.*/
    public int getPID() { return pid; }
    /**Set the PID of the message.
       @param pid - the new PID.*/
    public void setPID(int pid) { this.pid = pid; }

    /**Get the alert value of the message.
       @return the alert value.*/
    public int getAlert() { return alert; }
    /**Set the alert valueof the message.
       @param alert - the new alert value.*/
    public void setAlert(int alert) { this.alert = alert; }

    /**Get the service type of the message.
       @return the service type.*/
    public String getServiceType() { return serviceType; }
    /**Set the service type of the message.
       @param serviceType - the new service type.*/
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    /** Get the priority for the message.
     *@return the priority. */
    public int getPriority() { return priority; }

    /** Set the priority for the message.
     *@param priority - the priority for the message. */
    public void setPriority(int priority) { this.priority = priority; }

    /**
     * Get the useReplyPath .
     *@return the useReplyPath
     */
    public boolean getUseReplyPath() { return useReplyPath; }
    /**
     * Set the useReplyPath.
     *@param useReplyPath - the new useReplyPath
     */
    public void setUseReplyPath(boolean useReplyPath) { this.useReplyPath = useReplyPath; }

    /**
     * Get the deliveryReceipt .
     *@return the deliveryReceipt
     */
    public boolean getDeliveryReceipt() { return deliveryReceipt; }
    /**
     * Set the deliveryReceipt.
     *@param deliveryReceipt - the new deliveryReceipt
     */
    public void setDeliveryReceipt(boolean deliveryReceipt) { this.deliveryReceipt = deliveryReceipt; }

    public NotifierSmppPduType getNotifierSmppPduType() {
        return this.notifierSmppPduType;
    }

    public void setNotifierSmppPduType(NotifierSmppPduType notifierSmppPduType) {
        this.notifierSmppPduType = notifierSmppPduType;
    }

    public boolean getSetDpf() {
        return this.setDpf;
    }

    public void setSetDpf(boolean setDpf) {
        this.setDpf = setDpf;
    }

    /**
     * Creates the esm class parameter from other, more high-level, parameters
     * set on this class.
     */
    public int getEsmClass() {
    	int res = 0;
    	if( useReplyPath ) {
    		res ^= 0x80;
    	}
    	if( udh != null ) {
    		res ^= 0x40;
    	}
    	return res;
        //return useReplyPath ? 0x80 : 0;
    }

    /**
     * Creates the registered delivery parameter from other, more high-level,
     * parameters set on this class.
     */
    public int getRegisteredDelivery() {
        return deliveryReceipt ? 1 : 0;
    }

    public void setConverter(Converter converter) {
    	this.converter = converter;
    }

    public void setPacked(boolean packed) {
    	this.packed = packed;
    }

    public void setUDHPorts(int sourcePort, int destinationPort) {
    	if( udh == null) {
    		udh = new UserDataHeader();
    	}
    	udh.addIE(new ApplicationPort16Bits(sourcePort, destinationPort));
    }
    
    /**
     * Add an information element to the user data header
     * @param ie The information element to add.
     */
    public void addIEtoUDH(IE ie){
        if( udh == null) {
            udh = new UserDataHeader();
        }
        udh.addIE(ie);
    }
    
    public int getUDHLength() {
    	if( udh == null ) {
    		return 0;
    	} else {
    		return udh.getUDHLength();
    	}
    }

    /**
     * Calculates the total length of the body. This includes udh and convertion to 7 bits.
     * @return the number of septets or octets that the message contains.
     */
    public int getUserDataLength(byte[] udhData) {
    	return udhData.length + getLength();

	// Refer to TR HK70801
	//if(packed) { //7 bits
      	//	int fillBits = getFillBits(udhData);
	//	int messageLen = getLength(fillBits);
	//	int headerBits = udhData.length * 8;
	//	int headerLen = headerBits / 7;
	//	if( headerBits % 7 > 0 ) {
	//	headerLen++;
	//	}
	//	return messageLen + headerLen;
	//} else { //8 bits
  	//return udhData.length + getLength();
   	//}

    }

    public boolean isPacked() {
    	return packed;
    }

    public byte[] getUserData(byte[] udhData) {
	byte[] res = null;

	// Refer to TR HK70801
	//if( packed ) {
	//	int fillBits = getFillBits(udhData);
	//	int len = getLength(fillBits);
	//	byte [] b = new byte[len];
	//	System.arraycopy(text, fragPos, b, 0, len);
	//	res = converter.packCharIn7Bits(b, fillBits);

	//	} else {
		// calculate length
		int len = getLength();
		res = new byte[len];
		System.arraycopy(text, fragPos, res, 0, len);

	//	}
		return res;
	}

    public byte[] getUDHBytes() {
        if(udh != null){
            return udh.getUDHBytes();
        }
        return new byte[0];
    }

	private int getFillBits(byte[] udhData) {
		int fillBits = 0;
		fillBits = ( udhData.length * 8 ) % 7;
		if ( fillBits != 0 ) {
			fillBits = 7 - fillBits;
		}
		return fillBits;
	}

    /**
     * Creates a printable version of the message information
     * @return A string of the form <code>
     * {SMSMessage&nbsp;text=Hello&nbsp;dcs=0&nbsp;count=-1&nbsp;expiryTime=&nbsp;pid=0&nbsp;replace=true&nbsp;alert=0&nbsp;serviceType=""}
     * </code>
     */
    public String toString() {
        if (this.text == null) {
            this.text = "<empty>".getBytes();
        }

        return "{SMSMessage text=" + new String(text)
            + " dcs=" + dcs
            + " count=" + count
            + " expiryTime=" + expiryTimeRelative
            + " pid=" + pid
            + (replace ? " replace" : "")
            + " alert=" + alert
            + " serviceType=" + serviceType
            + " deliveryReceipt="+deliveryReceipt
            + " Esm Class="+getEsmClass()
            + " UDH Length="+getUDHLength()
            + " Length="+getLength()
            + " Use Reply Path="+getUseReplyPath()
            + " Priority="+getPriority()
            + " isLargerThanOneFragment="+isLargerThanOneFragment()
            + " Position="+getPosition()
            + " FragmentNumber="+getFragmentNumber()
            + (getExpiryTimeAbsolute()!=null? "ExpiryTimeAbsolute="+ getExpiryTimeAbsolute(): "")
            + (getExpiryTimeRelative()>0? " ExpiryTimeRelative="+ getExpiryTimeRelative(): "")
            + (getScheduledDeliveryTime()>0? " ScheduledDeliveryTime="+ getScheduledDeliveryTime(): "")
            + (text!=null?" text=\n"+NtfUtil.hexDump(text):"")
            + (getUDHBytes()!=null?" UDH=\n"+NtfUtil.hexDump(getUDHBytes()):"")
            + "}";
    }
}
