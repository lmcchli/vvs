 /**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.charset;

import java.util.*;
import com.mobeon.common.smscom.SMSComConfigException;


/****************************************************************
 * ChopConverter converts characters from the unicode character set used
 * internally in java programs to an output character set by chopping pff the
 * most significant byte.
 ****************************************************************/
public class ChopConverter extends Converter {
    
    /****************************************************************
     * unicodeToBytes is the central method in ChopConverter. It takes a java
     * string and creates a byte array with the least significant bytes from the
     * characters in the string.
     * @param s The string to convert.
     * @return a byte array with the converted characters.
     */
    public byte[] unicodeToBytes(String s) {
        char[] chars = s.toCharArray();
        int i;
        byte[] ret = new byte[s.length()]; //The result byte array to return
        int retpos = 0; //Where in ret are we appending?
        
        //Process all characters in the input string
        for (i = 0; i < chars.length; i++) {
                ret[retpos++] = (byte) (chars[i] & 0xFF);
        }
        
        return ret;
    }
    
    
    /****************************************************************
     * toString makes a string with the major data structures, suppressing
     * copies of identical lines.
     * @return a string with the conversion tables in hex format
     */
    public String toString() {
        int ch;
        StringBuffer sb = new StringBuffer("{ChopConverter: DCS=" + dcs + "}\n");

        return sb.toString();
    }


    /****************************************************************
     * Constructor
     */
    protected ChopConverter() {
    }


    /****************************************************************
     * init initialises the converter and should be overridden by subclasses.
     */
    protected void init(Properties conv) throws SMSComConfigException {
    }
}
