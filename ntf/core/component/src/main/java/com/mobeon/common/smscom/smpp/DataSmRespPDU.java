/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

/**
 * DataSmRespPDU implements a rudimentary response to return if the SMSC sends a data sm request.
 */
public class DataSmRespPDU extends SMPP_PDU {

    protected String messageId;
    protected int deliveryFailureReason;
    protected String networkErrorCode;
    protected String additionalStatusInfoText;
    protected int dpfResult;

    /**
     * Constructor.
     * 
     * @param conn
     *        - the connection that uses this PDU instance.
     */
    DataSmRespPDU(SMPPCom conn) {
        super(conn, "data_sm_resp_pdu", true);
        commandId= SMPPCMD_DATA_SM_RESP;
    }

    public void parseBody() {
        pos = 4 * 4;
        messageId = getNTS();
        getOptionalParameters();

        if (optionalParameters.size()>0) {
            Integer deliveryFailureReasonInteger = (Integer) (optionalParameters.get(new Integer(SMPPTAG_DELIVERY_FAILURE_REASON)));
            String networkErrorCodeString = (String) (optionalParameters.get(new Integer(SMPPTAG_NETWORK_ERROR_CODE)));
            String additionalStatusInfoTextString = (String) (optionalParameters.get(new Integer(
                    SMPPTAG_ADDITIONAL_STATUS_INFO_TEXT)));
            Integer dpfResultInteger = (Integer) (optionalParameters.get(new Integer(SMPPTAG_DPF_RESULT)));

            if(deliveryFailureReasonInteger != null){
                deliveryFailureReason = deliveryFailureReasonInteger.intValue();
            }
            
            if(networkErrorCodeString != null){
                 networkErrorCode = networkErrorCodeString;
            }
           
            if(additionalStatusInfoText != null){
                additionalStatusInfoText = additionalStatusInfoTextString;
            }
            
            if(dpfResultInteger != null){
                dpfResult = dpfResultInteger.intValue();
            }

        }
    }

    /**
     * getBuffer creates a buffer with an deliver sm PDU.
     * 
     * @return the created buffer
     */
    public byte[] getBuffer() {
        byte[] buf = new byte[HEADER_SIZE + 1];
        commandLength = HEADER_SIZE + 1;
        commandId = SMPPCMD_DATA_SM_RESP;
        commandStatus = 0x0;
        // sequenceNumber = whatever the caller has already set
        putHeader(buf);
        putInt(buf, HEADER_SIZE, 1, 0); // No message id
        return buf;
    }
}
