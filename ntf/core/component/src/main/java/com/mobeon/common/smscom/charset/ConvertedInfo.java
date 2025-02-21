/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.charset;

import com.mobeon.common.smscom.SMSMessage;
import java.text.ParseException;
import java.util.*;


/**
 * ConvertedInfo is a simple container class which holds an array of SMS
 * messages created from an array of Strings. It also contains a mapping table
 * so it is possible to find which string that went to which SMS message.
 */
public class ConvertedInfo {
    private SMSMessage[] _msgs;
    private int[] _crossRef;
    
    public ConvertedInfo(SMSMessage[] msgs, int[] crossRef) {
        _msgs = msgs;
        _crossRef = crossRef;
    }

    public SMSMessage[] getMessages() { return _msgs; }

    public int[] getCrossReference() { return _crossRef; }
}
