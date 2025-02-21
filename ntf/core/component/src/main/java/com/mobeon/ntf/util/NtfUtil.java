/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

/**
 * NtfUtil contains various static support functions.
 */
public class NtfUtil {
    private static final String startLine =
            "0000   |  |  |  |  |  |  |  |  |  |  |  |  |  |  |      ................\n";

    /**
     * Constructor, which is never used.
     */
    private NtfUtil() {
    };

    /**
     * Changes a Properties so all the property names are lowercase. This makes
     * the names in the property file case insensitive. Values from a Properties
     * with defaults, are frozen, i.e. subsequent changes to the defaults
     * Properties will not affect props.
     * 
     * @param props - the Properties to modify.
     */
    public static void lowerCasePropertyNames(final Properties props) {
        String name;
        final Properties tmp = new Properties();
        for(final Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
            name = (String) (e.nextElement());
            // dont owerwrite with lowercase. Otherwise default props might
            // overwrite real props.
            if(!name.equals(name.toLowerCase())
                    || tmp.getProperty(name.toLowerCase()) == null) {
                tmp.setProperty(name.toLowerCase(), props.getProperty(name));
            }

        }
        props.clear();
        for(final Enumeration<?> e = tmp.propertyNames(); e.hasMoreElements();) {
            name = (String) (e.nextElement());
            props.setProperty(name, tmp.getProperty(name));
        }
    }

    /**
     * stackTrace puts the stack trace of a Throwable into a StringBuffer
     * suitable for logging to file.
     * 
     * @param e the Throwable.
     *@return a StringBuffer with the stack trace in.
     */
    public static StringBuffer stackTrace(final Throwable e) {
        final StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.getBuffer();
    }

    /**
     * This array is not yet used, it was copied from another program just to
     * save the code. It was boring to type. private static String [] hexByte= {
     * "00|", "01|", "02|", "03|", "04|", "05|", "06|", "07|", "08|", "09|",
     * "0A|", "0B|", "0C|", "0D|", "0E|", "0F|", "10|", "11|", "12|", "13|",
     * "14|", "15|", "16|", "17|", "18|", "19|", "1A|", "1B|", "1C|", "1D|",
     * "1E|", "1F|", "20|", "21|", "22|", "23|", "24|", "25|", "26|", "27|",
     * "28|", "29|", "2A|", "2B|", "2C|", "2D|", "2E|", "2F|", "30|", "31|",
     * "32|", "33|", "34|", "35|", "36|", "37|", "38|", "39|", "3A|", "3B|",
     * "3C|", "3D|", "3E|", "3F|", "40|", "41|", "42|", "43|", "44|", "45|",
     * "46|", "47|", "48|", "49|", "4A|", "4B|", "4C|", "4D|", "4E|", "4F|",
     * "50|", "51|", "52|", "53|", "54|", "55|", "56|", "57|", "58|", "59|",
     * "5A|", "5B|", "5C|", "5D|", "5E|", "5F|", "60|", "61|", "62|", "63|",
     * "64|", "65|", "66|", "67|", "68|", "69|", "6A|", "6B|", "6C|", "6D|",
     * "6E|", "6F|", "70|", "71|", "72|", "73|", "74|", "75|", "76|", "77|",
     * "78|", "79|", "7A|", "7B|", "7C|", "7D|", "7E|", "7F|", "80|", "81|",
     * "82|", "83|", "84|", "85|", "86|", "87|", "88|", "89|", "8A|", "8B|",
     * "8C|", "8D|", "8E|", "8F|", "90|", "91|", "92|", "93|", "94|", "95|",
     * "96|", "97|", "98|", "99|", "9A|", "9B|", "9C|", "9D|", "9E|", "9F|",
     * "A0|", "A1|", "A2|", "A3|", "A4|", "A5|", "A6|", "A7|", "A8|", "A9|",
     * "AA|", "AB|", "AC|", "AD|", "AE|", "AF|", "B0|", "B1|", "B2|", "B3|",
     * "B4|", "B5|", "B6|", "B7|", "B8|", "B9|", "BA|", "BB|", "BC|", "BD|",
     * "BE|", "BF|", "C0|", "C1|", "C2|", "C3|", "C4|", "C5|", "C6|", "C7|",
     * "C8|", "C9|", "CA|", "CB|", "CC|", "CD|", "CE|", "CF|", "D0|", "D1|",
     * "D2|", "D3|", "D4|", "D5|", "D6|", "D7|", "D8|", "D9|", "DA|", "DB|",
     * "DC|", "DD|", "DE|", "DF|", "E0|", "E1|", "E2|", "E3|", "E4|", "E5|",
     * "E6|", "E7|", "E8|", "E9|", "EA|", "EB|", "EC|", "ED|", "EE|", "EF|",
     * "F0|", "F1|", "F2|", "F3|", "F4|", "F5|", "F6|", "F7|", "F8|", "F9|",
     * "FA|", "FB|", "FC|", "FD|", "FE|", "FF|", };
     */

    private static char[] hexChars =
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
                    'd', 'e', 'f', };

    /**
     * Generates a printable string with a "pipe-hex" dump of a byte array.
     * 
     * @param ba byte array to dump.
     *@return String with the pipe-hex dump.
     */
    public static String hexDump(final byte[] ba) {
        char b;
        int lineByte = 0;
        final StringBuffer sb =
                new StringBuffer((ba.length + 15) / 16 * startLine.length() + 1);
        StringBuffer line = new StringBuffer(startLine);

        for(int i = 0; i < ba.length; i++) {
            if(lineByte == 0) {
                int start = i;
                if(start > 0) {
                    line.setCharAt(3, hexChars[start % 16]);
                    start /= 16;
                    if(start > 0) {
                        line.setCharAt(2, hexChars[start % 16]);
                        start /= 16;
                        if(start > 0) {
                            line.setCharAt(1, hexChars[start % 16]);
                            start /= 16;
                            if(start > 0) {
                                line.setCharAt(0, hexChars[start % 16]);
                            }
                        }
                    }
                }
            }
            b = (char) (ba[i] & 0xFF);
            if(Character.isLetterOrDigit(b)) {
                line.setCharAt(56 + lineByte, b);
            }
            line.setCharAt(5 + 3 * lineByte, hexChars[b / 16]);
            line.setCharAt(6 + 3 * lineByte, hexChars[b % 16]);
            ++lineByte;
            if(lineByte == 16) {
                lineByte = 0;
                sb.append(line);
                line = new StringBuffer(startLine);
            }
        }
        if(lineByte != 0) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static Date stringToDate (final String date)
    {
    	final DateFormat dateFormat = new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    	try
    	{
    	    	return dateFormat.parse(date);
    	}
    	catch (ParseException pe)
    	{
    		pe.printStackTrace();
    	}
    	return null;
    }
    
    
}
