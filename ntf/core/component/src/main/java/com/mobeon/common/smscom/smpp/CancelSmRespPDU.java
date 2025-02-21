package com.mobeon.common.smscom.smpp;

/****************************************************************
 * SubmitSmRespPDU implements an SMPP submit sm response PDU for receiveing from
 * the SMS-C.
 ****************************************************************/
public class CancelSmRespPDU extends SMPP_PDU {

    /****************************************************************
     * Constructor.
     * @param conn the connection that uses this PDU instance.
     */
    CancelSmRespPDU(SMPPCom conn) {
    super(conn, "cancel_sm_resp_pdu", true);
    commandId= SMPPCMD_CANCEL_SM_RESP;
    }


    /****************************************************************
     * Read reads a submit sm response PDU from the supplied input stream
     * and parses all parameters.
     */
    public void parseBody() {
        pos= 4*4;


    }


}
