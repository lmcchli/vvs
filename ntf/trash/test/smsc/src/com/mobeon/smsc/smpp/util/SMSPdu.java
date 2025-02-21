/*
 * SMSPdu.java
 *
 * Created on January 22, 2004, 11:53 AM
 */

package com.mobeon.smsc.smpp.util;

import com.mobeon.ntf.util.delayline.Delayable;
import java.util.*;
import java.text.SimpleDateFormat;
import com.mobeon.smsc.smpp.util.CharsetConverter;
import com.mobeon.smsc.interfaces.SmppConstants;

/**
 * SMSPdu is used to save a received SMS delivery request PDU parameters.
 */

public class SMSPdu implements Delayable, SmppConstants {
    public static int count = 0;
    public int id = count++;

    /* SMPP PDU parameters used to cache the SMS request
     * See <I>Short Message Peer to Peer Protocol Specification</I> v3.4 for
     * details.
     **/
    private int commandLength = -1;
    private int commandId = -1;
    private int commandStatus = -1;
    private int sequenceNumber = -1;
    private String serviceType = "";
    private int sourceAddrTon = 0;
    private int sourceAddrNpi = 0;
    private String sourceAddr = "";
    private int destAddrTon = 0;
    private int destAddrNpi = 0;
    private String destinationAddr = "";
    private int esmClass = 0;
    private int protocolId = 0;
    private int priorityFlag = 0;
    private String sheduleDeliveryTime = "";
    private String validityPeriod = "";
    private int registeredDelivery = 0;
    private int replaceIfPresentFlag = 0;
    private int dataCoding = 0;
    private int smDefaultMsgId = 0;
    private int smLength = 0;
    private String shortMessage = "";
    private Hashtable optionalParameters = null;
    private CharsetConverter conv = new CharsetConverter();

    /* The name of ESME account that sent this SMS delivery request**/
    private String account = null;
    /* Buffer to save the request data in plain text formated format**/
    private StringBuffer plainSms = null;
    /* Buffer to save the request data in a HTML formated format**/
    private StringBuffer httpSms = null;
    /* The date the SMS request received to the SMS-C**/
    private Date date = null;
    static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    static {
        df.setTimeZone(Calendar.getInstance().getTimeZone());
    }

    public SMSPdu() {
        date = new Date();
    }

    public Object getKey() {
        return new Integer(id);
    }


    public boolean isMwiOn(){
        if( dataCoding == 200 && protocolId == 95 ) return true;
        else return false;
    }

    public boolean isMwiOff(){
        if( dataCoding == 192 && protocolId == 95 ) return true;
        else return false;
    }

    public boolean isSms(){
       if( dataCoding != 192 && protocolId != 95 && protocolId != 64) return true;
        else return false;
    }

    public boolean isSmsNull(){
       if( protocolId == 64 ) return true;
        else return false;
    }

    public boolean isCancel() {
	return (commandId == SMPPCMD_CANCEL_SM);
    }

    public int getTransactionId() {
        return id;
    }

    /* The following functions is used to set SMPP PDU data**/
    /**
     * Get the commandLength .
     *@return the commandLength
     */
    public int getCommandLength() { return this.commandLength; }
    /**
     * Set the commandLength.
     *@param commandLength - the new commandLength
     */
    public void setCommandLength(int commandLength) { this.commandLength = commandLength; }

    /**
     * Get the commandId .
     *@return the commandId
     */
    public int getCommandId() { return this.commandId; }
    /**
     * Set the commandId.
     *@param commandId - the new commandId
     */
    public void setCommandId(int commandId) { this.commandId = commandId; }

    /**
     * Get the commandStatus .
     *@return the commandStatus
     */
    public int getCommandStatus() { return this.commandStatus; }
    /**
     * Set the commandStatus.
     *@param commandStatus - the new commandStatus
     */
    public void setCommandStatus(int commandStatus) { this.commandStatus = commandStatus; }

    /**
     * Get the sequenceNumber .
     *@return the sequenceNumber
     */
    public int getSequenceNumber() { return this.sequenceNumber; }
    /**
     * Set the sequenceNumber.
     *@param sequenceNumber - the new sequenceNumber
     */
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    /**
     * Get the serviceType .
     *@return the serviceType
     */
    public String getServiceType() { return this.serviceType; }
    /**
     * Set the serviceType.
     *@param serviceType - the new serviceType
     */
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    /**
     * Get the sourceAddrTon .
     *@return the sourceAddrTon
     */
    public int getSourceAddrTon() { return this.sourceAddrTon; }
    /**
     * Set the sourceAddrTon.
     *@param sourceAddrTon - the new sourceAddrTon
     */
    public void setSourceAddrTon(int sourceAddrTon) { this.sourceAddrTon = sourceAddrTon; }

    /**
     * Get the sourceAddrNpi .
     *@return the sourceAddrNpi
     */
    public int getSourceAddrNpi() { return this.sourceAddrNpi; }
    /**
     * Set the sourceAddrNpi.
     *@param sourceAddrNpi - the new sourceAddrNpi
     */
    public void setSourceAddrNpi(int sourceAddrNpi) { this.sourceAddrNpi = sourceAddrNpi; }

    /**
     * Get the sourceAddr .
     *@return the sourceAddr
     */
    public String getSourceAddr() { return this.sourceAddr; }
    /**
     * Set the sourceAddr.
     *@param sourceAddr - the new sourceAddr
     */
    public void setSourceAddr(String sourceAddr) { this.sourceAddr = sourceAddr; }

    /**
     * Get the destAddrTon .
     *@return the destAddrTon
     */
    public int getDestAddrTon() { return this.destAddrTon; }
    /**
     * Set the destAddrTon.
     *@param destAddrTon - the new destAddrTon
     */
    public void setDestAddrTon(int destAddrTon) { this.destAddrTon = destAddrTon; }

    /**
     * Get the destAddrNpi .
     *@return the destAddrNpi
     */
    public int getDestAddrNpi() { return this.destAddrNpi; }
    /**
     * Set the destAddrNpi.
     *@param destAddrNpi - the new destAddrNpi
     */
    public void setDestAddrNpi(int destAddrNpi) { this.destAddrNpi = destAddrNpi; }

    /**
     * Get the destinationAddr .
     *@return the destinationAddr
     */
    public String getDestinationAddr() { return this.destinationAddr; }
    /**
     * Set the destinationAddr.
     *@param destinationAddr - the new destinationAddr
     */
    public void setDestinationAddr(String destinationAddr) { this.destinationAddr = destinationAddr; }

    /**
     * Get the esmClass .
     *@return the esmClass
     */
    public int getEsmClass() { return this.esmClass; }
    /**
     * Set the esmClass.
     *@param esmClass - the new esmClass
     */
    public void setEsmClass(int esmClass) { this.esmClass = esmClass; }

    /**
     * Get the protocolId .
     *@return the protocolId
     */
    public int getProtocolId() { return this.protocolId; }
    /**
     * Set the protocolId.
     *@param protocolId - the new protocolId
     */
    public void setProtocolId(int protocolId) { this.protocolId = protocolId; }

    /**
     * Get the priorityFlag .
     *@return the priorityFlag
     */
    public int getPriorityFlag() { return this.priorityFlag; }
    /**
     * Set the priorityFlag.
     *@param priorityFlag - the new priorityFlag
     */
    public void setPriorityFlag(int priorityFlag) { this.priorityFlag = priorityFlag; }

    /**
     * Get the sheduleDeliveryTime .
     *@return the sheduleDeliveryTime
     */
    public String getSheduleDeliveryTime() { return this.sheduleDeliveryTime; }
    /**
     * Set the sheduleDeliveryTime.
     *@param sheduleDeliveryTime - the new sheduleDeliveryTime
     */
    public void setSheduleDeliveryTime(String sheduleDeliveryTime) { this.sheduleDeliveryTime = sheduleDeliveryTime; }

    /**
     * Get the validityPeriod .
     *@return the validityPeriod
     */
    public String getValidityPeriod() { return this.validityPeriod; }
    /**
     * Get the validityPeriod in seconds (relative time)
     *@return the validityPeriod in seconds
     */
    public int getValidityPeriodSec() {

	int seconds = 0;

	if(this.validityPeriod!="")
	{
		try
		{
			// Seconds
			seconds=(Integer.decode((this.validityPeriod).substring(8,10))).intValue();

			// Minutes in seconds
			seconds=seconds+60*(Integer.decode((this.validityPeriod).substring(6,8))).intValue();

			// Hours in seconds
			seconds=seconds+3600*(Integer.decode((this.validityPeriod).substring(4,6))).intValue();

			// Days in seconds
			seconds=seconds+3600*24*(Integer.decode((this.validityPeriod).substring(2,4))).intValue();

			// Validity period > 1 month will be ignored
		}
		catch(NumberFormatException e)
		{
			System.out.println("NumberFormatException getting validity period: " +e.getMessage());
			seconds = 0;
		}
	}

	return seconds;
    }

    /**
     * Set the validityPeriod.
     *@param validityPeriod - the new validityPeriod
     */
    public void setValidityPeriod(String validityPeriod) { this.validityPeriod = validityPeriod; }

    /**
     * Get the registeredDelivery .
     *@return the registeredDelivery
     */
    public int getRegisteredDelivery() { return this.registeredDelivery; }
    /**
     * Set the registeredDelivery.
     *@param registeredDelivery - the new registeredDelivery
     */
    public void setRegisteredDelivery(int registeredDelivery) { this.registeredDelivery = registeredDelivery; }

    /**
     * Get the replaceIfPresentFlag .
     *@return the replaceIfPresentFlag
     */
    public int getReplaceIfPresentFlag() { return this.replaceIfPresentFlag; }
    /**
     * Set the replaceIfPresentFlag.
     *@param replaceIfPresentFlag - the new replaceIfPresentFlag
     */
    public void setReplaceIfPresentFlag(int replaceIfPresentFlag) { this.replaceIfPresentFlag = replaceIfPresentFlag; }

    /**
     * Get the dataCoding .
     *@return the dataCoding
     */
    public int getDataCoding() { return this.dataCoding; }
    /**
     * Set the dataCoding.
     *@param dataCoding - the new dataCoding
     */
    public void setDataCoding(int dataCoding) { this.dataCoding = dataCoding; }

    /**
     * Get the smDefaultMsgId .
     *@return the smDefaultMsgId
     */
    public int getSmDefaultMsgId() { return this.smDefaultMsgId; }
    /**
     * Set the smDefaultMsgId.
     *@param smDefaultMsgId - the new smDefaultMsgId
     */
    public void setSmDefaultMsgId(int smDefaultMsgId) { this.smDefaultMsgId = smDefaultMsgId; }

    /**
     * Get the smLength .
     *@return the smLength
     */
    public int getSmLength() { return this.smLength; }
    /**
     * Set the smLength.
     *@param smLength - the new smLength
     */
    public void setSmLength(int smLength) { this.smLength = smLength; }

    /**
     * Get the shortMessage as an ISO (USM-2) charset string.
     *@return the shortMessage
     */
    public String getShortMessage() { return this.shortMessage; }
    /**
     * Get the shortMessage as a GSM charset string.
     *@return the shortMessage
     */
    public String getShortMessageGsm() { return conv.iso2Gsm(this.shortMessage); }

    /**
     * Set the shortMessage from a GSM charset string
     *@param shortMessage - the new shortMessage
     */

    public void setShortMessage(String shortMessage) { this.shortMessage = conv.gsm2Iso(shortMessage); }
    /**
     * Set the shortMessage from an ISO (USM-2) charset string
     *@param shortMessage - the new shortMessage
     */
    public void setShortMessageIso(String shortMessage) { this.shortMessage = shortMessage; }

    /**
     * Get the optionalParameters .
     *@return the optionalParameters
     */
    public Hashtable getOptionalParameters() { return this.optionalParameters; }
    /**
     * Set the optionalParameters.
     *@param optionalParameters - the new optionalParameters
     */
    public void setOptionalParameters(Hashtable optionalParameters) { this.optionalParameters = optionalParameters; }

    public void setAccountName(String account) { this.account = account; }
    public String toString(){ return getString(); }
    public String toHttpString(){ return getHttpString(); }

    /**
     * @return SMSPdu information in plain text
     **/
    private String getString(){
        if ( plainSms != null ) return plainSms.toString();
        plainSms = new StringBuffer(1000);
	if (isCancel()) {
	    plainSms.append("Account: " + nullValue(account) + "\n");
	    plainSms.append("Received to SMS-C : " + date + "\n");
	    plainSms.append("CANCEL HEADER\n");
	    plainSms.append("------\n");
	    plainSms.append("Command length: " + commandLength + "\n");
	    plainSms.append("Command id: 0x" + Integer.toHexString(commandId) + "\n");
	    plainSms.append("Command status: " + nullValue(commandStatus) + "\n");
	    plainSms.append("Sequence number: " + sequenceNumber + "\n");
	    plainSms.append("BODY\n");
	    plainSms.append("----\n");
	    plainSms.append("To: " + destAddrTon + "," + destAddrNpi + "," + destinationAddr + "\n");
	    plainSms.append("From: " + sourceAddrTon + "," + sourceAddrNpi + "," + sourceAddr + "\n");
	    plainSms.append("Service type: " + nullValue(serviceType) + "\n");
	    plainSms.append("Message id: " + nullValue(smDefaultMsgId) + "\n");
	} else {
	    plainSms.append("Account: " + nullValue(account) + "\n");
	    plainSms.append("Received to SMS-C : " + date + "\n");
	    plainSms.append("HEADER\n");
	    plainSms.append("------\n");
	    plainSms.append("Command length: " + commandLength + "\n");
	    plainSms.append("Command id: 0x" + Integer.toHexString(commandId) + "\n");
	    plainSms.append("Command stauts: " + nullValue(commandStatus) + "\n");
	    plainSms.append("Sequence number: " + sequenceNumber + "\n");
	    plainSms.append("BODY\n");
	    plainSms.append("----\n");
	    plainSms.append("To: " + destAddrTon + "," + destAddrNpi + "," + destinationAddr + "\n");
	    plainSms.append("From: " + sourceAddrTon + "," + sourceAddrNpi + "," + sourceAddr + "\n");
	    plainSms.append("Service type: " + nullValue(serviceType) + "\n");
	    plainSms.append("ESM class: " + esmClass + "\n");
	    plainSms.append("Protocol id: " + protocolId + "\n");
	    plainSms.append("Priority flag: " + priorityFlag + pidValue(priorityFlag) + "\n");
	    plainSms.append("Shedule delivery time: " + sheduleDeliveryTime + "\n");
	    plainSms.append("Validity period: " + nullValue(validityPeriod) + "\n");
	    plainSms.append("Registered delivery: " + registeredDelivery + "\n");
	    plainSms.append("Replace: " + replaceIfPresentFlag + " (" + replace(replaceIfPresentFlag) + ")\n");
	    plainSms.append("Data coding: " + getDCS(dataCoding) + "\n");
	    plainSms.append("Message id: " + nullValue(smDefaultMsgId) + "\n");
	    plainSms.append("SMS length: " + smLength + "\n");
	    plainSms.append("Short message : " + shortMessage + "\n");
	    plainSms.append("OPTIONAL PARAMETERS\n");
	    plainSms.append("-------------------\n");
	    if ( optionalParameters != null ){
		Enumeration keys = optionalParameters.keys();
		while ( keys.hasMoreElements() ){
		    Integer key = (Integer)keys.nextElement();
		    Integer i= (Integer)(optionalParameters.get(key));
		    if ( i != null )
			plainSms.append("Id: \"" + key.intValue() + "\" Value: \"" + i.intValue() + "\"\n");
		}
	    }
	}
	return plainSms.toString();
    }

    /**
     * @return SMSPdu information in HTML formatted text
     */
    private String getHttpString(){
        if ( httpSms != null ) return httpSms.toString();
        httpSms = new StringBuffer(1000);
	if (isCancel()) {
	    httpSms.append("<table border=0 width=\"100%\">\n")
		.append("<tr><th colspan=4>Account: " + nullValue(account) + "</th></tr>\n")
		.append("<tr><th colspan=4>Received to SMS-C: " + date + "</th></tr>\n")
		.append("<tr bgcolor=\"#e0e0e0\"><th colspan=4>SMPP-HEADER</th></tr>\n")
		.append("<tr><td width=\"25%\">Command length:</td><td width=\"25%\">" + commandLength + "</td>\n")
		.append("<td width=\"25%\">Command id:</td><td width=\"25%\">0x" + Integer.toHexString(commandId) + "</td></tr>\n")
		.append("<tr><td width=\"25%\">Command status:</td><td width=\"25%\">" + nullValue(commandStatus) + "</td>\n")
		.append("<td width=\"25%\">Sequence number:</td><td width=\"25%\">" + sequenceNumber + "</td></tr>\n")
		.append("<tr bgcolor=\"#e0e0e0\"><th colspan=4>SMPP-BODY</th></tr>\n")
		.append("<tr><td width=\"25%\">To:</td><td width=\"25%\">" + destinationAddr + "<BR>ton:"  + destAddrTon + "(" +
			getTonValue(destAddrTon) + ")<BR>npi:" + destAddrNpi + "("
			+ getNpiValue(destAddrNpi) + ")" + "</td>\n")
		.append("<td width=\"25%\">From:</td><td width=\"25%\">" + sourceAddr + "<BR>ton:"  + sourceAddrTon + "("
			+ getTonValue(sourceAddrTon) + ")<BR>npi:" + sourceAddrNpi
			+ "(" + getNpiValue(sourceAddrNpi) + ")" + "</td></tr>\n")
		.append("<tr><td width=\"25%\">Service type:</td><td width=\"25%\">" + nullValue(serviceType) + "</td>\n")
		.append("<td width=\"25%\">Message id:</td><td width=\"25%\">" + nullValue(smDefaultMsgId) + "</td></tr>\n")
		.append("<tr><td width=\"25%\">&nbsp;</td><td colspan=3>CANCEL SM</td></tr>\n")
		.append("</TABLE>\n");
	} else {
	    httpSms.append("<table border=0 width=\"100%\">\n")
		.append("<tr><th colspan=4>Account: " + nullValue(account) + "</th></tr>\n")
		.append("<tr><th colspan=4>Received to SMS-C: " + date + "</th></tr>\n")
		.append("<tr bgcolor=\"#e0e0e0\"><th colspan=4>SMPP-HEADER</th></tr>\n")
		.append("<tr><td width=\"25%\">Command length:</td><td width=\"25%\">" + commandLength + "</td>\n")
		.append("<td width=\"25%\">Command id:</td><td width=\"25%\">0x" + Integer.toHexString(commandId) + "</td></tr>\n")
		.append("<tr><td width=\"25%\">Command status:</td><td width=\"25%\">" + nullValue(commandStatus) + "</td>\n")
		.append("<td width=\"25%\">Sequence number:</td><td width=\"25%\">" + sequenceNumber + "</td></tr>\n")
		.append("<tr bgcolor=\"#e0e0e0\"><th colspan=4>SMPP-BODY</th></tr>\n")
		.append("<tr><td width=\"25%\">To:</td><td width=\"25%\">" + destinationAddr + "<BR>ton:"  + destAddrTon + "(" +
			getTonValue(destAddrTon) + ")<BR>npi:" + destAddrNpi + "("
			+ getNpiValue(destAddrNpi) + ")" + "</td>\n")
		.append("<td width=\"25%\">From:</td><td width=\"25%\">" + sourceAddr + "<BR>ton:"  + sourceAddrTon + "("
			+ getTonValue(sourceAddrTon) + ")<BR>npi:" + sourceAddrNpi
			+ "(" + getNpiValue(sourceAddrNpi) + ")" + "</td></tr>\n")
		.append("<tr><td width=\"25%\">Service type:</td><td width=\"25%\">" + nullValue(serviceType) + "</td>\n")
            .append("<td width=\"25%\">ESM class:</td><td width=\"25%\">" + esmClass + "</td></tr>\n")
		.append("<tr><td width=\"25%\">Protocol id:</td><td width=\"25%\">" + protocolId + "</td>\n")
		.append("<td width=\"25%\">Priority flag:</td><td width=\"25%\">" + priorityFlag + pidValue(priorityFlag) + "</td></tr>\n")
		.append("<tr><td width=\"25%\">Shedule delivery time:</td><td width=\"25%\">" + sheduleDeliveryTime + "</td>\n")
		.append("<td width=\"25%\">Validity period:</td><td width=\"25%\">" + nullValue(validityPeriod) + "</td></tr>\n")
		.append("<tr><td width=\"25%\">Registered delivery:</td><td width=\"25%\">" + registeredDelivery + "</td>\n")
		.append("<td width=\"25%\">Replace:</td><td width=\"25%\">" + replaceIfPresentFlag + " (" + replace(replaceIfPresentFlag) + ")</td></tr>\n")
		.append("<tr><td width=\"25%\">Data coding:</td><td width=\"25%\">" + getDCS(dataCoding) + "</td>\n")
		.append("<td width=\"25%\">Message id:</td><td width=\"25%\">" + nullValue(smDefaultMsgId) + "</td></tr>\n")
		.append("<tr><td width=\"25%\">SMS length:</td><td width=\"25%\">" + smLength + "</td>\n")
		.append("<td width=\"25%\">&nbsp;</td><td width=\"25%\">&nbsp;</td></tr>\n")
		.append("<tr><td width=\"25%\">Short message:</td><td colspan=3>" + shortMessage + "</td></tr>\n")
		.append("</TABLE>\n");

	    if ( optionalParameters != null ){
		Enumeration keys = optionalParameters.keys();
		while ( keys.hasMoreElements() ){
		    Integer key = (Integer)keys.nextElement();
		    Integer i= (Integer)(optionalParameters.get(key));
		    if ( i != null )
			plainSms.append("Id: \"" + key.intValue() + "\" Value: \"" + i.intValue() + "\"<BR>\n");
		}
	    }
	}
        return httpSms.toString();
    }

    /**
     * @return SMSPdu summary as HTML formatted text
     */
    public String getHttpSummary(String esme){
        return "<tr>"
            + "<td>" + account + "</td>"
            + "<td>" + sequenceNumber + "</td>"
            + "<td>0x" + Integer.toHexString(commandId) + "</td>"
            + "<td>" + df.format(date) + "</td>"
            + "<td>" + destinationAddr + "</td>"
            + "<td>" + sourceAddr + "</td>\n"
            + "<td>" + dataCoding + "</td>"
            + "<td>" + (isCancel() ? "&nbsp;" : (""+esmClass)) + "</td>"
            + "<td>" + (isCancel()
			? "<FONT COLOR=RED>CANCEL SM</FONT>"
			: shortMessage.substring(0, Math.min(shortMessage.length(), 40))) + "</td>\n"
            + "<td><form method=post action=view-sms-list target=\"SMS PDU " + sequenceNumber + "\">\n"
            + "<input type=\"hidden\" name=\"esme\" value=\"" + esme + "\">\n"
            + "<input type=\"hidden\" name=\"account\" value=\"" + account + "\"\n>"
            + "<input type=\"hidden\" name=\"seqno\" value=\"" + sequenceNumber + "\"\n>"
            + "<input type=\"submit\" value=\"View\" onClick=\"MM_openBrWindow('','SMS PDU " + sequenceNumber + "','scrollbars=no,resizable=no,width=600,height=400')\">\n"
            + "</form></td>"
            + "</tr>\n";
    }

    /**
     * This method returns a HTML table header for the table rows generated by
     * getHttpSummary, so that the table layout knowledge is localized in this
     * class.
     */
    public static String getHttpSummaryHeader() {
        return "<tr>"
            + "<th>Account</th>"
            + "<th>Seq</th>"
            + "<th>Command</th>"
            + "<th>Date</th>"
            + "<th>To</th>"
            + "<th>From</th>"
            + "<th>DCS</th>"
            + "<th>ESM class</th>"
            + "<th>Message</th>"
            + "<th>&nbsp;</th>"
            + "</tr>";
    }

    private boolean replace(int i){ return (i==1); }
    private String getDCS( int code ) {
        String returnLine = "" + code;
        switch( code ) {
        case 192:
            returnLine += " (MWI OFF)";
            break;
        case 208:
            returnLine += " (MWI OFF WITH SMS)";
            break;
        case 224:
            returnLine += " (MWI OFF WITH SMS (UCS2))";
            break;
        case 200:
            returnLine += " (MWI ON)";
            break;
        case 216:
            returnLine += " (MWI ON WITH SMS)";
            break;
        case 232:
            returnLine += " (MWI ON WITH SMS(UCS2))";
            break;
        case 16:
            returnLine += " (FLASH)";
            break;
        case 24:
            returnLine += " (FLASH (UCS2))";
            break;
        case 240:
            returnLine += " (FLASH (VODAFONE))";
            break;
        }
        return returnLine;
    }
    private String isMwi(int i){
        if( dataCoding == 192 ) return dataCoding + " (MWI OFF)";
        else if( dataCoding == 200 ) return dataCoding + " (MWI ON)";
        else return dataCoding + "";
    }
    private String pidValue(int protocolId){ return (protocolId == 64)?" (Null SMS)":""; }
    private String nullValue(int i){ return (i==0)?null:""+i; }
    private String nullValue(String i){ return (i.length()==0)?null:""+i; }

    private final int UNKNOWN =           0;
    /** TON values*/
    private final int TONInternational = 1;
    private final int TONNational =      2;
    private final int NETWORKSpecific =  3;
    private final int SUBSCRIBERNumber = 4;
    private final int ALPHANUMERIC =      5;
    private final int ABBREVIATED =       6;
    private String getTonValue(int ton){
        switch(ton){
            case UNKNOWN:
                return "Unknown";
            case TONInternational:
                return "International";
            case TONNational:
                return "National";
            case NETWORKSpecific:
                return "Network specific";
            case SUBSCRIBERNumber:
                return "Subscriber number";
            case ALPHANUMERIC:
                return "Alphanumeric";
            case ABBREVIATED:
                return "Abbreviated";
            default:
                return "Not defined";
        }
    }
        /** NPI values*/
    private final int ISDN =              1;
    private final int DATA =              3;
    private final int TELEX =             4;
    private final int LAND_MOBILE =       6;
    private final int NPI_NATIONAL =      8;
    private final int PRIVATE =           9;
    private final int ERMES =             10;
    private final int INTERNET =          14;
    private final int WAP =               18;
    private String getNpiValue(int npi){
        switch(npi){
            case UNKNOWN:
                return "Unknown";
            case ISDN:
                return "Isdn";
            case DATA:
                return "Data";
            case TELEX:
                return "Telex";
            case LAND_MOBILE:
                return "Land mobile";
            case NPI_NATIONAL:
                return "National";
            case PRIVATE:
                return "Private";
            case ERMES:
                return "ERMES";
            case INTERNET:
                return "Internet";
            case WAP:
                return "Wap";
            default:
                return "Not defined";
        }
    }
}

