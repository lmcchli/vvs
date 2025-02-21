package com.mobeon.common.smscom.smpp.test;

import com.mobeon.common.smscom.smpp.CancelSmPDU;
import com.mobeon.common.smscom.smpp.SMPPCom;
import com.mobeon.common.smscom.SMSCom;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.test.NtfTestCase;


/**
 * Created by IntelliJ IDEA.
 * User: mnify
 * Date: 2007-mar-27
 * Time: 16:57:52
 */
public class PDUTest extends NtfTestCase {
    public PDUTest(String name) {
        super(name);
    }

    public void testCancel() throws Exception {
        SMPPCom smppCom = new SMPPCom();
        CancelSmPDU cancelPDU = new CancelSmPDU(smppCom);

        SMSAddress from = new SMSAddress(1,2,"3456");
        SMSAddress to = new SMSAddress(3,4,"78910");
        byte [] data = cancelPDU.getBuffer(to, from, null);

        assertNotNull(data);
        assertEquals(33, data.length);

        byte serviceByte = data[16];
        assertEquals(0,serviceByte);
        byte messageByte = data[17];
        assertEquals(0,messageByte);

        byte ton = data[18];
        byte npi = data[19];
        String toAddr = getNTS(data, 20);
        assertEquals(1, ton);
        assertEquals(2, npi);
        assertEquals("3456", toAddr);

        ton = data[25];
        npi = data[26];
        String fromAddr = getNTS(data, 27);
        assertEquals(3, ton);
        assertEquals(4, npi);
        assertEquals("78910", fromAddr);


    }


    public String getNTS(byte[] buffer, int pos) {
        int i = pos;

        while (i < buffer.length && buffer[i] != 0) { i++; } //Find null terminator
        String s = new String(buffer, pos, i - pos);

        if (i < buffer.length) { //This is the normal case, otherwise the string
                                 //ended without terminator
            i++; //Skip null terminator
        }
        pos = i;
        return s;
    }
}
