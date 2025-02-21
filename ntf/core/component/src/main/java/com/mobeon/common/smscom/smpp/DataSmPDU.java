/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

import static com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPPduEnum.DATA_SM;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPParamHandler;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPParamValues;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPSMSInfo;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPSMSInfoFacade;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;

/**
 * DataSmPDU implements an SMPP data sm PDU for receiving from
 * the SMS-C.
 */
public class DataSmPDU extends SendShortMessagePDU {
    
    /**
     * Constructor.
     * @param conn the connection that uses this PDU instance.
     */
    DataSmPDU(SMPPCom conn) {
        super(conn, "data_sm_pdu");
        commandId = SMPPCMD_DATA_SM;
        commandStatus = 0;
    }

    
    /****************************************************************
     * Sending data_sm to SMSC
     ****************************************************************/

    @Override
    public int getExpectedPduResponseCommandId() {
        return SMPPCMD_DATA_SM_RESP;
    }
    
    @Override
    public byte[] getBuffer(SMSAddress destAddress, SMSAddress origAddress, SMSMessage msg) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        SMPPParamHandler smppParamAppender = SMPPParamHandler.get();
        SMPPParamValues smppParamValues = smppParamAppender.getSMPPParamValuesInstance();
        SMPPSMSInfo smsInfo = new SMPPSMSInfoFacade(origAddress, destAddress, msg);

        try {
            commandId = SMPPCMD_DATA_SM;
            commandStatus = 0;
            sequenceNumber = conn.nextSequenceNumber();

            // Write the header.
            writeHeader(dos);

            // Write the mandatory parameters.
            writeNTS(dos, smppParamValues.getServiceType(DATA_SM, msg.getServiceType(), smsInfo));
            if (origAddress != null) {
                dos.writeByte(smppParamValues.getSourceAddrTon(DATA_SM, origAddress.getTON(), smsInfo));
                dos.writeByte(smppParamValues.getSourceAddrNpi(DATA_SM, origAddress.getNPI(), smsInfo));
                writeNTS(dos, smppParamValues.getSourceAddr(DATA_SM, origAddress.getNumber(), smsInfo));
            } else {
                dos.writeByte(smppParamValues.getSourceAddrTon(DATA_SM, 0, smsInfo));
                dos.writeByte(smppParamValues.getSourceAddrNpi(DATA_SM, 0, smsInfo));
                writeNTS(dos, smppParamValues.getSourceAddr(DATA_SM, "", smsInfo));
            }
            dos.writeByte(smppParamValues.getDestAddrTon(DATA_SM, destAddress.getTON(), smsInfo));
            dos.writeByte(smppParamValues.getDestAddrNpi(DATA_SM, destAddress.getNPI(), smsInfo));
            writeNTS(dos, smppParamValues.getDestinationAddrOutbound(DATA_SM, destAddress.getNumber(), smsInfo));
            dos.writeByte(smppParamValues.getEsmClass(DATA_SM, msg.getEsmClass(), smsInfo));
            dos.writeByte(smppParamValues.getRegisteredDelivery(DATA_SM, msg.getRegisteredDelivery(), smsInfo));
            dos.writeByte(smppParamValues.getDataCoding(DATA_SM, msg.getDCS(), smsInfo));

            // Write the optional/additional parameters.
            smppParamAppender.appendOptionalAndVendorSpecificParamToPdu(DATA_SM, dos, smsInfo);
        } catch (IOException e) {

        }

        byte[] buf = bos.toByteArray();
        putInt(buf, 0, 4, buf.length);

        return buf;
    }
    
    
    /****************************************************************
     * Receiving data_sm from SMSC
     ****************************************************************/
    
    protected String serviceType;
    protected SMSAddress sourceAddress;
    protected SMSAddress destAddress;
    protected int esmClass;
    protected int registeredDelivery;
    protected int dataCoding;

    /**
     * Read reads a submit sm response PDU from the supplied input stream
     * and parses all parameters.
     */
    public void parseBody() {
        pos = 4 * 4;
        serviceType = getNTS();
        sourceAddress = new SMSAddress(getInt(1), getInt(1), getNTS());
        destAddress = new SMSAddress(getInt(1), getInt(1), getNTS());
        esmClass = getInt(1);
        registeredDelivery = getInt(1);;
        dataCoding = getInt(1);
        getOptionalParameters();
    }


}

