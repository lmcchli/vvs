/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom;

import java.util.*;

/****************************************************************
 * CommSpy specifies an interface for clients that want to trace the
 * communication with the SMSC. If the SMSCom is given a CommSpy, it
 * will notify it of all communication with the SMSC.
 ****************************************************************/
public interface CommSpy {
    
    /****************************************************************
     *Just before a message is sent to the SMSC, the SMSCom calls this
     *method with the data it is about to send.
     *@param msg raw data that is about to be sent.
     */
    void toSMSC(byte[] msg);
    
    /****************************************************************
     *Immediately after a message has been received from the SMSC, the
     *SMSCom calls this method with the data received.
     *@param msg raw data recevied
     */
    void fromSMSC(byte[] msg);
}
