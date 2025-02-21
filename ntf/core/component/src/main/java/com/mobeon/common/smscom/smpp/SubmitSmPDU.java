/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import static com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPPduEnum.SUBMIT_SM;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPParamHandler;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPParamValues;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPSMSInfo;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPSMSInfoFacade;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;

/**
 * SubmitSmPDU implements an SMPP submit sm PDU for sending to the SMS-C.
 */
public class SubmitSmPDU extends SendShortMessagePDU {

    private Calendar cal= null;
    private int utcDiff; //Absolute value of difference to UTC in quarter hours
    private char utcSign; //Sign of above difference ('+' or '-')

    /**
     * Constructor.
     * @param conn the connection that uses this PDU instance.
     */
    SubmitSmPDU(SMPPCom conn) {
        super(conn, "submit_sm_pdu");
        commandId= SMPPCMD_SUBMIT_SM;
        commandStatus= 0;
        cal= Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC")); //SMPP requires UTC for expiry time
        utcDiff= 0;
        utcSign= '+';
    }

    /**
     * smppDate creates a string with the peculiar absolute SMPP time format.
     * @param cal The time to encode.
     * @return The time encoded in SMPP format.
     */
    private String smppDate(Calendar cal) {
        StringBuffer sb= new StringBuffer();
        int i;

        i= cal.get(Calendar.YEAR)%100;
        if (i < 10) sb.append(0);
        sb.append(i);

        i= cal.get(Calendar.MONTH) + 1; //Calendar month range 0-11
        if (i < 10) sb.append(0);
        sb.append(i);

        i= cal.get(Calendar.DAY_OF_MONTH);
        if (i < 10) sb.append(0);
        sb.append(i);

        i= cal.get(Calendar.HOUR_OF_DAY);
        if (i < 10) sb.append(0);
        sb.append(i);

        i= cal.get(Calendar.MINUTE);
        if (i < 10) sb.append(0);
        sb.append(i);

        i= cal.get(Calendar.SECOND);
        if (i < 10) sb.append(0);
        sb.append(i);

        sb.append(0); //Never mind the milliseconds

        if (utcDiff < 10) sb.append(0);
        sb.append(utcDiff);
        sb.append(utcSign);

        return sb.toString();
    }

    /**
     * smppDate creates a string with the peculiar relative SMPP time format.
     * @param seconds The time in seconds to encode.
     * @return The time encoded in SMPP format.
     */
    private String smppDate(int seconds) {
        StringBuffer sb = new StringBuffer();
        
        //Years YY
        int years = seconds / 31104000;
        seconds = seconds % 31104000;
        if (years < 10) { sb.append("0"); }
        sb.append(years);
        
        //Months MM
        int months = seconds / 2592000;
        seconds = seconds % 2592000;
        if (months < 10) { sb.append("0"); }
        sb.append(months);
        
        //Days DD
        int days = seconds / 86400;
        seconds = seconds % 86400;
        if (days < 10) { sb.append("0"); }
        sb.append(days);
        
        //Hours hh
        int hours = seconds / 3600;
        seconds = seconds % 3600;
        if (hours < 10) { sb.append("0"); }
        sb.append(hours);
        
        //Minutes mm
        int minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes < 10) { sb.append("0"); }
        sb.append(minutes);
        
        //Seconds ss
        if (seconds < 10) { sb.append("0"); }
        sb.append(seconds);
        
        sb.append("000R");
        return sb.toString();
    }

    private byte[] getUserDataHeader(SMSMessage msg) throws IOException {
    	if( msg.getUDHLength() == 0 ) {
    		return new byte[0];
    	}
    	return msg.getUDHBytes();

    }

    /**
     * getBuffer returns a byte array with a raw SMPP submit sm message in the form used for communicating with the SMSC.
     * 
     * @param adr
     *        The destination address.
     * @param msg
     *        The message.
     * @return the read or created buffer, or null (if there is not enough information in the PDU yet to create a buffer).
     */
    public byte[] getBuffer(SMSAddress adr, SMSAddress org, SMSMessage msg) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        SMPPParamHandler smppParamAppender = SMPPParamHandler.get();
        SMPPParamValues smppParamValues = smppParamAppender.getSMPPParamValuesInstance();
        SMPPSMSInfo smsInfo = new SMPPSMSInfoFacade(org, adr, msg);

        try {
            commandId = SMPPCMD_SUBMIT_SM;
            commandStatus = 0;
            sequenceNumber = conn.nextSequenceNumber();
            writeHeader(dos);
            writeNTS(dos, smppParamValues.getServiceType(SUBMIT_SM, msg.getServiceType(), smsInfo));
            if (org != null) {
                dos.writeByte(smppParamValues.getSourceAddrTon(SUBMIT_SM, org.getTON(), smsInfo));
                dos.writeByte(smppParamValues.getSourceAddrNpi(SUBMIT_SM, org.getNPI(), smsInfo));
                writeNTS(dos, smppParamValues.getSourceAddr(SUBMIT_SM, org.getNumber(), smsInfo));
            } else {
                dos.writeByte(smppParamValues.getSourceAddrTon(SUBMIT_SM, 0, smsInfo));
                dos.writeByte(smppParamValues.getSourceAddrNpi(SUBMIT_SM, 0, smsInfo));
                writeNTS(dos, smppParamValues.getSourceAddr(SUBMIT_SM, "", smsInfo));
            }
            dos.writeByte(smppParamValues.getDestAddrTon(SUBMIT_SM, adr.getTON(), smsInfo));
            dos.writeByte(smppParamValues.getDestAddrNpi(SUBMIT_SM, adr.getNPI(), smsInfo));
            writeNTS(dos, smppParamValues.getDestinationAddrOutbound(SUBMIT_SM, adr.getNumber(), smsInfo));
            dos.writeByte(smppParamValues.getEsmClass(SUBMIT_SM, msg.getEsmClass(), smsInfo));
            dos.writeByte(smppParamValues.getProtocolId(SUBMIT_SM, msg.getPID(), smsInfo));

            if (msg.getPriority() >= 0 && msg.getPriority() <= 3) {
                dos.writeByte(smppParamValues.getPriorityFlag(SUBMIT_SM, msg.getPriority(), smsInfo));
            } else {
                dos.writeByte(smppParamValues.getPriorityFlag(SUBMIT_SM, 0, smsInfo));
            }
            /* scheduled delivery time in seconds */
            if (msg.getScheduledDeliveryTime() > 0) {
                String defaultValue = smppDate(msg.getScheduledDeliveryTime());
                writeNTS(dos, smppParamValues.getScheduleDeliveryTime(SUBMIT_SM, defaultValue, smsInfo));
            } else {
                // Immediate delivery
                writeNTS(dos, smppParamValues.getScheduleDeliveryTime(SUBMIT_SM, "", smsInfo));
            }
            /* validity_period in hours */
            if (msg.getExpiryTimeRelative() > 0) {
                String defaultValue = smppDate(msg.getExpiryTimeRelative() * 3600);
                writeNTS(dos, smppParamValues.getValidityPeriod(SUBMIT_SM, defaultValue, smsInfo));
            } else {
                dos.writeBytes(smppParamValues.getValidityPeriod(SUBMIT_SM, "\0", smsInfo));// leave it to smsc center to determine
                                                                                            // validity_period
            }

            dos.writeByte(smppParamValues.getRegisteredDelivery(SUBMIT_SM, msg.getRegisteredDelivery(), smsInfo));
            if (msg.getReplace()) {
                dos.writeByte(smppParamValues.getReplaceIfPresentFlag(SUBMIT_SM, 1, smsInfo));
            } else {
                dos.writeByte(smppParamValues.getReplaceIfPresentFlag(SUBMIT_SM, 0, smsInfo));
            }
            dos.writeByte(smppParamValues.getDataCoding(SUBMIT_SM, msg.getDCS(), smsInfo));
            dos.writeByte(smppParamValues.getSmDefaultMsgId(SUBMIT_SM, 0, smsInfo)); // No canned default message

            byte[] udhData = getUserDataHeader(msg);
            byte[] udData = msg.getUserData(udhData);
            byte[] shortMessage = new byte[udhData.length + udData.length];
            System.arraycopy(udhData, 0, shortMessage, 0, udhData.length);
            System.arraycopy(udData, 0, shortMessage, udhData.length, udData.length);
            dos.writeByte(smppParamValues.getSmLength(SUBMIT_SM, msg.getUserDataLength(udhData), smsInfo));
            dos.write(smppParamValues.getShortMessage(SUBMIT_SM, shortMessage, smsInfo));

            /* ==== Optional & additional parameters ================================ */
            smppParamAppender.appendOptionalAndVendorSpecificParamToPdu(SUBMIT_SM, dos, smsInfo);
        } catch (IOException e) {
            ; // Should not happen on a ByteArrayOutputStream
        }

        byte[] buf = bos.toByteArray();
        // Must insert the correct length
        putInt(buf, 0, 4, buf.length);
        return buf;
    }
    
    @Override
    public int getExpectedPduResponseCommandId() {
        return SMPPCMD_SUBMIT_SM_RESP;
    }
    
}

