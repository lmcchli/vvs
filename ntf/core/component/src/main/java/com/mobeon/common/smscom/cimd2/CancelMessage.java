package com.mobeon.common.smscom.cimd2;

import com.mobeon.common.smscom.SMSAddress;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/****************************************************************
 * CancelMessage knows the format of a CIMD2 cancel message. This class can only
 * be used for <i>sending</i> to the SMS-C, receiving is not supported.
 ****************************************************************/
public class CancelMessage extends CIMD2Message {

    /****************************************************************
     * Constructor.
     * @param conn The CIMD2Com used for this message.
     */
    CancelMessage(CIMD2Com conn) {
        super(conn);
    }


    /****************************************************************
     * getBuffer creates a buffer with a cancel message.
     * @param to the address to send the message to
     * @return the created buffer
     */
    public byte[] getBuffer(SMSAddress to) {
        operationCode= CIMD2_CANCEL_MESSAGE;
        packetNumber= conn.nextPacketNumber();

        ByteArrayOutputStream bos= new ByteArrayOutputStream(100);
        DataOutputStream dos= new DataOutputStream(bos);
        try {
            writeHeader(dos);
            writeAddress(dos, to, null );

            writeInt(dos, 3, CIMD2_CANCEL_MODE, CIMD2_COLON);
            writeInt(dos, 1, 0, CIMD2_TAB);
            writeTrailer(dos);
        } catch (IOException e) {
            //Since we write to a buffer with known size, this should never happen
            return null;
        }

        return bos.toByteArray();
    }
}
