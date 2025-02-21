package com.mobeon.common.smscom.cimd2;

/****************************************************************
 * LoginRespMessage knows the format of a CIMD2 login response message. This
 * class can only be used for <i>receiving</i> from the SMS-C, sending is not
 * supported.<P>
 ****************************************************************/
public class CancelRespMessage extends CIMD2RespMessage {


    /****************************************************************
     * Constructor.
     * @param conn The CIMD2Com used for this message.
     */
    CancelRespMessage(CIMD2Com conn) {
    super(conn);
    operationCode= CIMD2_CANCEL_MESSAGE_RESP;
    }
}
