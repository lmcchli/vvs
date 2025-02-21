/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import java.util.concurrent.atomic.AtomicInteger;

public class SipUtils {

    /**
     * List the follwing unreserved characters according to RFC3261:
     *  mark        =  "-" / "_" / "." / "!" / "~" / "*" / "'"
     *                / "(" / ")"
     */
    public static final String MARK = "-_.!~*´()";

    /**
     * Same as {@link MARK}
     */
    public static final String UNRESERVED = MARK;

    /**
     * Contains the {@link UNRESERVED} characters plus the following
     * characters according to RFC3261:
     *  user-unreserved  =  "&" / "=" / "+" / "$" / "," / ";" / "?" / "/"
     */
    public static final String USER_UNRESERVED = UNRESERVED + "&=+$,;?/";

    /**
     * Contains the {@link UNRESERVED} characters plus the following
     * characters according to RFC3261:
     *  "&" / "=" / "+" / "$" / ","
     */
    public static final String PASSWORD_UNRESERVED = UNRESERVED + "&=+$,";

    /**
     * Contains the {@link UNRESERVED} characters plus the following
     * characters according to RFC3261:
     *  param-unreserved  =  "[" / "]" / "/" / ":" / "&" / "+" / "$"
     */
    public static final String PARAM_UNRESERVED = UNRESERVED + "[]/:&+$";

    /**
     * Contains the {@link UNRESERVED} characters plus the following
     * characters according to RFC3261:
     *  hnv-unreserved  =  "[" / "]" / "/" / "?" / ":" / "+" / "$"
     */
    public static final String HNV_UNRESERVED = UNRESERVED + "[]/?:+$";
    
    /**
     * Contains the characters that will be mapped {@link #CHAR_HEADER_NAME_MAPPED} in SIP headers 
     * so they can be accessible from the CCXML application
     */
    public final static String CHAR_HEADER_NAME_DROPPED = "-"; //If more character are added here, SipUtils.mapInternalSipHeaderName(String ) must be re-implemented
    
    /**
     * Contains the mapping for {@link #CHAR_HEADER_NAME_DROPPED}
     */
    public final static String CHAR_HEADER_NAME_MAPPED = "_"; //If more character are added here, SipUtils.mapInternalSipHeaderName(String ) must be re-implemented
    
    /**
     * Remove all special character from headerName or the org.mozilla.javascript implementation 
     * will try to interpret them when it evaluates SIP Headers in the application.
     *  ie: in CCXML, evt.connection._header.History-Info will result in :
     *      evt.connection._header.History "minus" Info
     *     
     * The current implementation only maps '-' to '_'.
     *      
     * @param sipHeaderName header name
     * @return header name with {@link #CHAR_HEADER_NAME_DROPPED} replaced with {@link #CHAR_HEADER_NAME_MAPPED}
     */
    public static String mapInternalSipHeaderName(String sipHeaderName) {
        
        // We only change "-" to "_" since it is the only likely case to happen (Unlikely to have SIP headers with "+" in their names).
        // We use "_" in order to reduce the possibility of collision.  
        // Currently, there are no registered SIP headers using '_' (2012-05-16 http://www.iana.org/assignments/sip-parameters) 
        return sipHeaderName.replace(CHAR_HEADER_NAME_DROPPED, CHAR_HEADER_NAME_MAPPED);
    }

    /**
     * Check if the given char is unreserved. It is unreserved if it is either
     * a digit, a letter or if the char is listed in the given String of unreserved characters.
     * @param c - The char to test
     * @param unreserved - List of unreserved characters
     * @return true if c is unreserved, false otherwise
     */
    public static boolean isUnreserved(char c, String unreserved) {
        boolean digitOrLetter = (c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z');
        return (digitOrLetter || unreserved.indexOf(c) >= 0);
    }


    /**
     * Replaces all reserved characters in the provided string with the
     * escaped (%xx) representation.
     * An unreserved character is a character that is one of the following:
     *   - A digit (0-9)
     *   - A letter (a-z or A-Z)
     *   - A character that is included in the unreserved parameter
     *
     * The following predefined strings of unreserved characters are defined:
     *   {@link MARK}
     *   {@link USER_UNRESERVED}
     *   {@link PASSWORD_UNRESERVED}
     *   {@link PARAM_UNRESERVED}
     *   {@link HNV_UNRESERVED}
     *
     * @param unescaped - String to escape
     * @param unreserved - String of characters to use as additional unreserved characters
     * @return The string with escaped characters,
     *          returns null if any of the input parameters is null.
     */
    public static String escape(String unescaped, String unreserved) {
        if (unescaped == null || unreserved == null)
            return null;

        StringBuffer retval = new StringBuffer();
        char[] characters = unescaped.toCharArray();

        for (char ch : characters) {
            if (isUnreserved(ch,unreserved)) {
                retval.append(ch);
            } else {
                retval.append("%");
                retval.append(String.format("%02x",(int)ch));
            }
        }

        return retval.toString();
    }


    /**
     * Decode two ascii digits to integer.
     *
     * @param h1 most significant hex digit.
     * @param h2 least significant hex digit.
     * @return decoded number (0-255) or -1 if input is invalid.
     */
    public static int decodeHex(char h1, char h2) {

        h1 = Character.toLowerCase(h1);
        h2 = Character.toLowerCase(h2);
        int result;

        // Most significant digit
        if (h1 >= '0' && h1 <= '9') {
            result = (h1 - '0') << 4;
        } else if (h1 >= 'a' && h1 <= 'f') {
            result = (h1 - 'a' + 10) << 4;
        } else {
            // Not a valid escape sequence
            return -1;
        }

        // Least significant digit
        if (h2 >= '0' && h2 <= '9') {
            result += (h2 - '0');
        } else if (h2 >= 'a' && h2 <= 'f') {
            result += (h2 - 'a' + 10);
        } else {
            // Not a valid escape sequence
            return -1;
        }

        return result;

    }

    /**
     * Remove % escaped character from a String. Only unescapes valid
     * %xx sequences. Invalid combinations like "%g7" is left intact.
     * @param s - an input String to unescape
     * @return  unescaped String or null if input string is null
     */
    public static String unescape(String s) {

        if (s == null) {
            return s;
        }

        int idx = s.indexOf('%');
        if (idx < 0) {
            return s;
        }

        int len = s.length();

        // The unescaped result should always be shorter or equal in length
        StringBuffer result = new StringBuffer(len);
        result.append(s.substring(0, idx));

        while (idx < len) {

            if (s.charAt(idx) == '%') {

                if (idx + 2 >= len) {
                    // Cant unescape due to end of string
                    result.append(s.substring(idx));
                    break;
                }

                int ch = decodeHex(s.charAt(idx + 1), s.charAt(idx + 2));
                if (ch < 0) {
                    // Keep unmodified if not a valid escape sequence
                    result.append(s.charAt(idx++));
                } else {
                    result.append((char) ch);
                    idx += 3;
                }

            } else {
                result.append(s.charAt(idx++));
            }

        }

        return result.toString();
    }


    private static AtomicInteger icidCounter = new AtomicInteger(0);

    /**
     * Generates a unique ICID value. The requirement on global
     * uniqueness during at least one month from creation is fulfilled
     * if the following criterias are true:
     *  - A maximum of 65536 new ICID's may be generated during one second.
     *  - A restart of the application will take at least one second.
     *     (Since counter will be reset)
     *  - Only one instance of the application per host.
     * @return a globally unique ICID value.
     */
    public static String generateICID() {

        // Get current seconds since 1970 and crop it down to 24 bits
        // This time will wrap after approx. 194 days
        long msTime = (System.currentTimeMillis() / 1000) & 0xffffff;

        // Get a counter that increases by one for every generated ICID
        // Crop down to 16 bits
        int cnt = icidCounter.incrementAndGet() % 0xffff;

        return Long.toString(msTime,Character.MAX_RADIX) +
                Integer.toString(cnt,Character.MAX_RADIX) +
                "-" + CMUtils.getInstance().getLocalHost();
    }

}
