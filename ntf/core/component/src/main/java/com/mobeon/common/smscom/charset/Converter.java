/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.charset;

import com.mobeon.common.sms.request.Request;
import com.mobeon.common.smscom.SMSComConfigException;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.ntf.slamdown.SlamdownPayload;

import java.text.ParseException;
import java.util.*;


/**
 * Converter converts characters from the unicode character set used internally
 * in java programs to an output character set used in communicating with
 * external entities. It is initially developed to convert from the Unicode
 * character set to bytes in the GSM default character set, but since it is
 * possible to configure the operation and even plug in alternative conversion
 * algorithms, it may have other uses as well.
 *
 * The current implementation supports a single conversion for each process.
 *
 * <H2>Character Conversion Configuration File</H2> This file is a java
 * properties file, which defines the general format.

 * The following properties can be used to control character conversion:
 * <DL>
 * <DT>DataCodingScheme=&lt;number&gt;<DD>Defines the data coding scheme
 * (see ETSI TS 100 900 <I>Alphabets and language-specific information</I>)
 * parameter that shall be sent with messages encoded with this character
 * conversion.<P>The number can be 0-255 with a default of 0.

 * <DT>Converter=&lt;class name&gt;<DD>Specifies the name of a class
 * implementing a character converter. The default converter <I>Converter</I> is
 * a general converter controlled by configuration parameters and can convert to
 * any output character set.  This parameter is used to plug in customized
 * converters for different character sets, to reduce memory usage or improve
 * conversion speed.

 * <DT>&lt;unicode character list&gt;=&lt;action&gt;<DD>Specifies an action to
 * apply to one or more unicode characters. The available actions are <UL>
 * <LI>Delete. Delete these characters from the String.
 * <LI>Keep. Keep these characters unchanged, i.e. the output is the unicode
 * characters.
 * <LI>Chop. Convert characters by chopping off the most significant byte.
 * <LI>UCS2. Abort conversion and restart, converting the entire string to UCS2
 * instead.
 * <LI>&lt;bytes&gt;. Convert the characters by replacing them with this
 * byte sequence.</UL></DL>

 * <P>A unicode character is specified as four hexadecimal digits, e.g. 0041
 * for the letter A.

 * <P>A unicode character list consists of a single unicode character or
 * multiple unicode characters and character ranges, separated by comma. A range
 * is specified as two unicode characters separated by a
 * dash. E.g. 0041-005C,00C4,00C5,00D6 specifies all swedish capital
 * letters. <I>Note that a unicode character list can only contain the
 * characters "0-9a-fA-F,-" and in particular must not contain any spaces</I>

 * <P>Output bytes are specified as a number of hexadecimal digits,
 * i.e. one or more bytes. E.g. 41 for the letter A or 1B3D for the "tilde"
 * character in the default GSM character set.

 * <P>For convenience, it is possible to specify several conflicting actions for one
 * unicode character, since the character can be part of many lists. Conflicts
 * are resolved with the following priority:<OL>

 * <LI>Entries where a character list consists of a single character have the
 * highest priority.

 * <LI>The chop action, when the character is one of many in a list.

 * <LI>Conversion to a byte sequence, when the character is one of many in a list.

 * <LI>The keep action, when the character is one of many in a list.

 * <LI>The delete action, when the character is one of many in a list.</OL>

 * <P>The order of the lines in the configuration file does not matter, with
 * one exception: if an identical unicode character list (i.e. property name) occurs more
 * then once, only the last one is used.

 * <P>The default action for unspecified unicode characters is chop;

 * <P>Example for a conversion file for the GSM default character set:<P>

 * <CODE>DataCodingScheme=0<BR>
 * Converter=Converter<BR>
 * #Convert unsupported characters to hash-marks.<BR>
 * 0000-FFFF=60<BR>
 * #Just chop characters that are the same as ASCII<BR>
 * 000A,000D,0020-007A=Chop<BR>
 * <BR>
 * 0060=60<BR>
 * #Currency symbols and accented characters<BR>
 * 0040=00<BR>
 * 00A3=01<BR>
 * 00A4=24<BR>
 * 0024=02<BR>
 * 00A5=03<BR>
 * 00E8=04<BR>
 * 00E9=05<BR>
 * 00F9=06<BR>
 * 00EC=07<BR>
 * 00F2=08<BR>
 * 00C7=09<BR>
 * 20AC=1B65<BR>
 * # 0A is line feed<BR>
 * 00D8=0B<BR>
 * 00F8=0C<BR>
 * # 0C is form feed<BR>
 * 00C5=0E<BR>
 * 00E5=0F<BR>
 * #Greek characters<BR>
 * 0394=10<BR>
 * 005F=11<BR>
 * 03A6=12<BR>
 * 0393=13<BR>
 * 039B=14<BR>
 * 03A9=15<BR>
 * 03A0=16<BR>
 * 03A8=17<BR>
 * 03A3=18<BR>
 * 0398=19<BR>
 * 039E=1A<BR>
 * # 1B is escape to extension table<BR>
 * # Misc. european characters<BR>
 * 00C6=1C<BR>
 * 00E6=1D<BR>
 * 00DF=1E<BR>
 * 00C9=1F<BR>
 * 00A1=40<BR>
 * 00C4=5B<BR>
 * 00D6=5C<BR>
 * 00D1=5D<BR>
 * 00DC=5E<BR>
 * 00A7=5F<BR>
 * 00BF=60<BR>
 * 00E4=7B<BR>
 * 00F6=7C<BR>
 * 00F1=7D<BR>
 * 00FC=7E<BR>
 * 00E0=7F<BR>
 * #Brackets etc.<BR>
 * 005E=1B14<BR>
 * 007B=1B28<BR>
 * 007D=1B29<BR>
 * 005C=1B2F<BR>
 * 005B=1B3C<BR>
 * 007E=1B3D<BR>
 * 005D=1B3E<BR>
 * 007C=1B40
 * </CODE>
 */
public class Converter {

    /**Constants for the actions*/
    protected static final byte LOOKUP = (byte) 0xFF; //Look up in outbytes
    protected static final byte CHOP = (byte) 0xFE; //Chop off MSB
    protected static final byte KEEP = (byte) 0xFD; //Keep entire unicode char
    protected static final byte DELETE = (byte) 0xFC; //Delete char
    protected static final byte UCS2 = (byte) 0xFB; //UCS2 conversion

    protected static byte defaultAction = CHOP;

    /**
     * The conversion is controlled by two tables. Actions has 65536 one-byte
     * entries, one for each unicode character. This byte specifies one of
     * <UL>
     *   <LI>a single byte to output for that character
     *   <LI>that the character should be deleted
     *   <LI>that both bytes of the character should be used
     *   <LI>that only the least significant byte of the character should be
     *   used
     *   <LI>that the character is outside the indented character set and
     *   conversion to UCS2 shall be used instead (for the entire message).
     *   <LI>the byte sequence to output is found in the hash table
     *   <i>outbytes</i>, either because there are many bytes, or because
     *   the character code is occupied by one of the special conversions.
     * </UL>
     * This makes it possible to handle all kinds of conversions for all unicode
     * characters. As an example, you could specify for all characters
     * unsupported in the output character set that they should be replaced by
     * the text "<unsupported character>". This is possible but not very
     * practical. For performance reasons, multi-byte output conversions should
     * be avoided. If they are needed, it might be better to make a specialized
     * Converter subclass for that conversion.
     */
    protected static byte[] actions;
    protected static Hashtable<Character, byte[]> outbytes;

    protected static int dcs = 0;
    protected static int dcsUcs2 = 8;
    protected static boolean pack = false;
    protected static String convClass = "Converter";
    protected static boolean strictDcs = false;

    /**The single Converter instance*/
    protected static Converter inst = null;
    /** The specification of the current converter. */
    protected static Properties convSpec = null;


    /**
     * unicodeToMessage is the central method in Converter. It takes a java
     * string and creates an SMSMessage containing a byte array with the
     * characters encoded according to the character conversion specification
     * supplied when the Converter was created. If characters are encountered
     * that are not supported by the conversion, a message in the UCS2 character
     * coding will be created. The generated SMSMessage contains the correct
     * DCS. Subclasses may override this function to provide a more efficient
     * implementation.
     * @param s The string to convert.
     * @param maxBytes the maximum number of bytes in the result. Longer
     * messages are truncated. 0 means there should not be a limit.
     * @return an SMSMessage with the converted string.
     */
    public SMSMessage unicodeToMessage(String s, int maxBytes) {
        return unicodeToMessage(s, maxBytes, false);
    }
    
    /**
     * unicodeToMessage is the central method in Converter. It takes a java
     * string and creates an SMSMessage containing a byte array with the
     * characters encoded according to the character conversion specification
     * supplied when the Converter was created. If characters are encountered
     * that are not supported by the conversion, a message in the UCS2 character
     * coding will be created. The generated SMSMessage contains the correct
     * DCS. Subclasses may override this function to provide a more efficient
     * implementation.
     * @param s The string to convert.
     * @param maxBytes the maximum number of bytes in the result. Longer
     * messages are truncated. 0 means there should not be a limit.
     * @param packAllowed Enables 7-bit packing when set to true. Converter has 
     *        also to be configured with property StrictDcs={yes|true|1|on} for
     *        packing to take place.
     * @return an SMSMessage with the converted string.
     * @see #init(Properties)
     */
    public SMSMessage unicodeToMessage(String s, int maxBytes, boolean packAllowed) {
        SMSMessage msg;
        try {
            /*
             * Strictly respect SMPP SMS dcs and packing if specified in
             * configuration. Else use the relaxed dcs and packing mode coming
             * from legacy implementation. This way legacy applications won't be
             * broken.
             */
            boolean calculatedPack = false;
            int calculatedDcs = dcs;
            if (strictDcs) {
                if (dcs == Request.GSM_DCS_GENERAL_DATA_CODING_INDICATION_GSM_7_BIT_DEFAULT_ALPHABET &&
                        packAllowed == false) {
                    calculatedPack = false;
                    calculatedDcs = Request.GSM_DCS_GENERAL_DATA_CODING_INDICATION_8_BIT_DATA; 
                } else if (dcs == Request.GSM_DCS_GENERAL_DATA_CODING_INDICATION_GSM_7_BIT_DEFAULT_ALPHABET) {
                    calculatedPack = true;
                }
            }
            
            byte[] bytes = unicodeToBytes(s, calculatedPack ? maxBytes * 8 / 7 : maxBytes);
            if (calculatedPack) { bytes = packCharIn7Bits(bytes); }
            msg = new SMSMessage(bytes, calculatedDcs);
            msg.setConverter(this);
            msg.setPacked(calculatedPack);
        } catch (ParseException e) {
            return new SMSMessage(unicodeToUcs2Bytes(s, maxBytes), dcsUcs2);
        }
        return msg;
    }
    
    /**
     * Returns the value of <code>pack</code>.
     */
    public boolean getPack()
    {
        return pack;
    }
    
    /**
     * Creates an array of SMSMessages, where each starts with the header,
     * followed by a number of body lines and ends with the footer. The
     * charaters in the strings are converted to bytes according to the
     * specified conversion.
     *@param header - the beginning of each message. Null means header part is
     * empty
     *@param footer - the end of each message. Null means footer part is empty.
     *@param bodyParts - the message content between header and footer. Null
     * means body part is empty. 
     *@param maxBytes - the maximum number of bytes in one message.
     *@param maxBodyParts - the maximum number of body parts in one message.
     */
    public ConvertedInfo unicodeToMessages(int maxBytes,
                                           int maxBodyParts,
                                           SlamdownPayload payload) {
        byte[][] bytes = null;
        boolean ucs2 = false;
        int[] crossRef = new int[payload.bodyParts];
        String header = null;
        String footer = null;
        String[] bodyParts = null;
        if (!payload.isBytes) {
            header = payload.getStringHeader();
            footer = payload.getStringFooter();
            bodyParts = payload.getStringBody();
            
            try {
                bytes = partsToBytes(header, footer, bodyParts, ucs2);
            } catch (ParseException e) {
                //Oops, must use UCS2
                ucs2 = true;
                try {
                    bytes = partsToBytes(header, footer, bodyParts, ucs2);
                } catch (ParseException pe) {
                    bytes = new byte[0][];
                }
            }
        }
        else
        {
            bytes = payload.get2DArray();
        }
        
        /*
         * Strictly respect SMPP SMS dcs and packing if specified in
         * configuration. Else use the relaxed dcs and packing mode coming
         * from legacy implementation. This way legacy applications won't be
         * broken.
         */
        boolean calculatedPack = pack;
        if (strictDcs) {
            calculatedPack = (dcs == Request.GSM_DCS_GENERAL_DATA_CODING_INDICATION_GSM_7_BIT_DEFAULT_ALPHABET);
        }

        int maxBodyBytes = calculatedPack && !ucs2
            ? maxBytes * 8 / 7 - bytes[0].length - bytes[1].length
            : maxBytes - bytes[0].length - bytes[1].length;
        
        if (maxBodyBytes < 0) {
            return new ConvertedInfo(new SMSMessage[0], crossRef);
        }

        //Then compose messages with header, body parts and footer
        ArrayList<SMSMessage> result = new ArrayList<SMSMessage>();
        int nextPart = 2; //Index of the next unused body part. The first two
                          //are occupied by the converted header and footer
        
        while (nextPart < bytes.length || nextPart == 2) {
            int parts = numberOfPartsInNextMessage(bytes, maxBodyBytes, maxBodyParts, nextPart);
            byte[] msgBytes = getBytesForNextMessage(bytes,
                                                     nextPart,
                                                     parts);
            if (calculatedPack && !ucs2) { msgBytes = packCharIn7Bits(msgBytes); }
            if (msgBytes.length > 0) {
                for (int part = 0; part < parts; part++) {
                    crossRef[nextPart - 2 + part] = result.size();
                }
                result.add(new SMSMessage(msgBytes, ucs2 ? dcsUcs2 : dcs));
            }
            nextPart += Math.max(1, parts); //Skip too big body parts and
                                            //terminate loop for 0 body parts
        }
        return new ConvertedInfo(result.toArray(new SMSMessage[result.size()]), crossRef);
    }

    /**
     * Converts all strings that can be part of a message into byte arrays. The
     * first two byte arrays are the converted header and footer respectively,
     * the rest are the converted body parts.
     *@param header - the string with header contents.
     *@param footer - the string with footer contents.
     *@param bodyParts - the strings with body contents.
     *@param ucs2 - if true, converts to ucs2, otherwise converts as configured.
     *@return an array of the byte arrays from the converted strings.
     *@throws ParseException if som string needs conversion to UCS2.
     */
    protected byte[][] partsToBytes(String header, String footer, String[] bodyParts, boolean ucs2)
        throws ParseException {
        byte[][] result;
        
        if (bodyParts == null || bodyParts.length == 0) {
            result = new byte[2][];
        } else {
            result = new byte[2 + bodyParts.length][];
            for (int part = 0; part < bodyParts.length; part++) {
                result[part + 2] = ucs2
                    ? unicodeToUcs2Bytes(bodyParts[part], 0)
                    : unicodeToBytes(bodyParts[part], 0);
            }
        }
        result[0] = unicodeToBytes(header, 0);
        result[1] = unicodeToBytes(footer, 0);
        return result;
    }
    
    /**
     * Calculates the number of parts in the next message.
     *@param bytes - the bytes of all bodyparts.
     *@param maxBytes - the maximum number of bytes remaining for a message
     * excluding the header and footer bytes.
     *@param maxBodyParts - the maximum of body parts in one message.
     *@param firstPart - the index of the first body part in the next message.
     *@return how many body parts that fit in the next message.
     */
    protected int numberOfPartsInNextMessage(byte[][] bytes,
                                             int maxBytes,
                                             int maxBodyParts,
                                             int firstPart) {
        int byteCount = 0;
        int part = firstPart;
        //        while (maxBodyParts > 0 ? part < firstPart + maxBodyParts : true
        //      && part < bytes.length
        //      && byteCount + bytes[part].length <= maxBytes) {
        //   byteCount += bytes[part].length;
        //   part++;
        //}
        while (true) {
            //Here, part has not yet been included in the next message
            if (//We have used all parts there are
                //or we have reached the allowed number of body parts
                //or the next part does not fit
                part == bytes.length 
                || maxBodyParts > 0 && part - firstPart >= maxBodyParts
                || byteCount + bytes[part].length > maxBytes) {
                return part - firstPart;
            }

            //Here, part has been included in the next message
            byteCount += bytes[part].length;
            part++;
        }
    }

    /**
     * Makes a byte array with bytes from the header, footer and the specified
     * number of body parts.
     *@param bytes - the bytes of header, footer and all bodyparts.
     *@param firstPart - the index of the first body part in the next message.
     *@param parts - the number of parts in the next message.
     */
    protected byte[] getBytesForNextMessage(byte[][] bytes,
                                            int firstPart,
                                            int parts) {
        int part;
        int size = bytes[0].length + bytes[1].length;
        for (part = firstPart; part < firstPart + parts; part++) {
            size += bytes[part].length;
        }
        
        byte[] result = new byte[size];
        int pos = 0;
        System.arraycopy(bytes[0], 0, result, pos, bytes[0].length);
        pos += bytes[0].length;
        for (part = firstPart; part < firstPart + parts; part++) {
            System.arraycopy(bytes[part], 0, result, pos, bytes[part].length);
            pos += bytes[part].length;
        }
        System.arraycopy(bytes[1], 0, result, pos, bytes[1].length);
        return result;
    }

    /**
     * Converts a string to bytes according to the configured conversion.
     *@param s - the String to convert.
     *@param maxBytes the maximum number of bytes in the result.
     *@return the bytes of the converted string.
     *@throws ParseException if some character in the string is not compatible
     * with the selected character conversion. This should probably be handled
     * by converting the string to UCS2 instead.
     */
    public byte[] unicodeToBytes(String s, int maxBytes) throws ParseException {
        if (s == null) { return "".getBytes(); } 
        
        char[] chars = s.toCharArray();
        byte[] outb;
        int charpos;
        byte[] result = new byte[chars.length]; //The result byte array to return
        int bytepos = 0; //Where in ret are we appending?
        
        if (maxBytes == 0) { maxBytes = Integer.MAX_VALUE; }
        
        //Process all characters in the input string
        for (charpos = 0; charpos < chars.length && bytepos < maxBytes; charpos++) {
            switch (actions[chars[charpos]]) {
            case KEEP:
                if (result.length < bytepos + 2) { result = extend(result, chars.length); }
                result[bytepos++] = (byte) ((chars[charpos] >> 8) & 0xFF);
                result[bytepos++] = (byte) (chars[charpos] & 0xFF);
                break;
            case DELETE:
                break;
            case LOOKUP:
                outb = outbytes.get(new Character(chars[charpos]));
                if (result.length < bytepos + outb.length) { result = extend(result, outb.length); }
                System.arraycopy(outb, 0, result, bytepos, outb.length);
                bytepos += outb.length;
                break;
            case CHOP:
                if (result.length == bytepos) { result = extend(result, 1); }
                result[bytepos++] = (byte) (chars[charpos] & 0xFF);
                break;
            case UCS2:
                throw (new ParseException("Character not compatible with conversion", 0));
            default:
                if (result.length == bytepos) { result = extend(result, 1); }
                result[bytepos++] = actions[chars[charpos]];
                break;
            }
        }
        
        return truncateBuffer(result, bytepos);
    }

    /**
     * unicodeToUcs2Bytes is a help method that converts a String to UCS2 if it
     * contains characters outside the intended character set. 
     * @param s The string to convert.
     * @param maxBytes the maximum number of bytes in the result. Longer
     * messages are truncated. 0 means there should not be a limit.
     * @return a byte array with the converted characters.
     */
    protected byte[] unicodeToUcs2Bytes(String s, int maxBytes) {
        char[] chars = s.toCharArray();

        int size = 2 * chars.length;
        if (maxBytes != 0 && maxBytes < size) {
            size = ((maxBytes % 2 == 1) //No half UCS2 characters
                ? maxBytes - 1
                : maxBytes);
        }
        byte[] result = new byte[size]; //The result byte array to return
        int bytepos = 0; //Where in result are we appending?
        int charpos = 0; //Where in chars are we looking?


        //Process all characters in the input string
        while (bytepos < size && charpos < chars.length) {
            if (actions[chars[charpos]] != DELETE) {
                if (result.length < bytepos + 2) { result = extend(result, 2); }
                result[bytepos++] = (byte) ((chars[charpos] >> 8) & 0xFF);
                result[bytepos++] = (byte) (chars[charpos] & 0xFF);
            }
            charpos++;
        }
        return truncateBuffer(result, bytepos);
    }

    /**
     * Returns a byte array with only the used bytes in.
     *@param buf - the buffer with the contents.
     *@param used - the number of butis in buf that are used.
     *@return buf if all bytes are used, a new shorter array otherwise.
     */
    protected byte[] truncateBuffer (byte[] buf, int used) {
        if (used == buf.length) {
            return buf;
        } else {
            //truncate the array to the correct length
            byte[] result = new byte[used];
            System.arraycopy(buf, 0, result, 0, used);
            return result;
        }
    }

    
    /**
     * Packs 7-bits characters in bytes in accordance with GSM 03.38.
     *@param b - byte array with one 7-bit character per byte.
     *@return a new array with 1 1/7 7-bit characters per byte.
     */
    private byte[] packCharIn7Bits(byte[] b) {
        byte[] result = new byte[(b.length * 7 + 7) / 8];
        int charNo = 0; //Index in the input array
        int byteNo = 0; //Byte index in the output array
        int bitInByte = 0; //Bit position in byte of LSB of next character
        
        if (b.length > 0) {
            result[0] = 0;
            for (charNo = 0; charNo < b.length; charNo++) {
                result[byteNo] |= b[charNo] << bitInByte;
                
                bitInByte += 7;
                if (bitInByte > 7) {
                    bitInByte -= 8;
                    ++byteNo;
                    if (byteNo < result.length) {
                        result[byteNo] = (byte) ((b[charNo] & 0x7F) >> (7 - bitInByte));
                    }
                }
            }
        }
        
        return result;
    }        

    /**
     * Packs 7-bits characters in bytes in accordance with GSM 03.38.
     *@param b - byte array with one 7-bit character per byte.
     *@param fillBits - number of bits to leave at start of byte array
     *@return a new array with 1 1/7 7-bit characters per byte.
     */
    public byte[] packCharIn7Bits(byte[] b, int fillBits) {
        byte[] result = new byte[(b.length * 7 + 7) / 8];
        int charNo = 0; //Index in the input array
        int byteNo = 0; //Byte index in the output array
        int bitInByte = fillBits; //Bit position in byte of LSB of next character
        
        if (b.length > 0) {
            result[0] = 0;
            for (charNo = 0; charNo < b.length; charNo++) {
                result[byteNo] |= b[charNo] << bitInByte;
                
                bitInByte += 7;
                if (bitInByte > 7) {
                    bitInByte -= 8;
                    ++byteNo;
                    if (byteNo < result.length) {
                        result[byteNo] = (byte) ((b[charNo] & 0x7F) >> (7 - bitInByte));
                    }
                }
            }
        }
        
        return result;
    }  

    /**
     * get returns the single Converter instance, creating it if necessary.
     * @param conv Properties specifying the conversion. The actual class of the
     * converter is determined by the  property named "Converter".
     * @return the converter instance.
     */
    public static synchronized Converter get(Properties conv) throws SMSComConfigException {
        if (inst != null && convSpec != null && convSpec == conv) { return inst; }
        
        convSpec = conv;
        if (conv.getProperty("Converter", "Converter").equals("Converter")) {
            inst = new Converter();
        } else {
            String className = "com.mobeon.common.smscom.charset."
                + conv.getProperty("Converter");
            try {
                Class<?> protocolClass = Class.forName(className);
                inst = (Converter) protocolClass.newInstance();
            } catch (IllegalAccessException e) {
                throw new SMSComConfigException("Converter has no right to access " + className
                                                + " or its constructor");
            } catch (ClassNotFoundException e) {
                throw new SMSComConfigException("Converter can not find " + className);
            } catch (InstantiationException e) {
                throw new SMSComConfigException("Converter can not instantiate " + className);
            }
        }
        inst.init(conv);
        return inst;
    }


    /**
     * Supports toString by "printing" bytes in a hex format.
     * @param sb StringBuffer to "print" to.
     * @param ba bytes to "print".
     * @param ch char that shall be a 4-hex-digit header for the line.
     * @param start the start of bytes to "print".
     * @param len how many bytes to "print".
     */
    private void appendByteLine(StringBuffer sb, byte[] ba, char ch, int start, int len) {
        int b;

        //Make ch always print 4 digits
        if (ch < 0x1000) { sb.append("0"); }
        if (ch < 0x100) { sb.append("0"); }
        if (ch < 0x10) { sb.append("0"); }
        sb.append(Integer.toHexString(ch)).append(" ");

        for (int i = start; i < start + len; i++) {
            b = ba[i] & 0xFF;
            //Make byte always print 2 digits
            if (b < 0x10) { sb.append("0"); }
            sb.append(Integer.toHexString(b));
            if (i + 1 < start + len) {
                sb.append("|");
            }
        }
        sb.append("\n");
    }

    /**
     * toString makes a string with the major data structures, suppressing
     * copies of identical lines.
     * @return a string with the conversion tables in hex format
     */
    public String toString() {
        int cpl = 32; //chars per line
        byte[] lastLine = new byte[cpl]; //Remember last line
        int ch;
        StringBuffer sb = new StringBuffer("{Converter: DCS=" + dcs
                                           + " DCS(UCS2)=" + dcsUcs2
                                           + (pack ? " pack" : "") + "\n");
        int sameLines = 0; //Number of times a line has occured

        //"print" the action table
        sb.append("ONE-BYTE CONVERSION TABLE\n--------------------------------\nff=LOOKUP fe=CHOP fd=KEEP fc=DELETE fb=UCS2\n\n");
        for (ch = 0; ch <= 0xFFFF; ch += cpl) { //For each line
            if (sameLines == 0 || !arraySliceEquals(lastLine, 0, actions, ch, cpl)) {
                //Remeber line if it is not the same as the last line
                System.arraycopy(actions, ch, lastLine, 0, cpl);
                if (sameLines > 1) {
                    sb.append("\t\t").append(sameLines - 1)
                        .append(" more lines like the last one\n");
                }
                appendByteLine(sb, actions, (char) ch, ch, cpl); //This line was different, so print it
                sameLines = 1;
            } else {
                ++sameLines;
            }
        }
        if (sameLines > 1) {
            sb.append("\t\t").append(sameLines - 1).append(" more lines like the last one\n");
        }

        //"print" the outbyte hash table
        sb.append("\nMULTI-BYTE CONVERSION TABLE\n--------------------------------\n");
        for (Enumeration<Character> e = outbytes.keys(); e.hasMoreElements();) {
            Character key = e.nextElement();
            byte[] ba = outbytes.get(key);
            appendByteLine(sb, ba, key.charValue(), 0, ba.length);
        }

        sb.append("}\n");

        return sb.toString();
    }


    /**
     * Compares parts of two byte arrays.
     * @param arr1 first array to compare.
     * @param start1 which byte in arr1 to start from.
     * @param arr2 second array to compare.
     * @param start2 which byte in arr2 to start from.
     * @param len how many bytes to compare
     * @return true iff the array slices are equal.
     */
    protected boolean arraySliceEquals(byte[] arr1, int start1, byte[] arr2, int start2, int len) {
        if (start1 + len > arr1.length) { return false; }
        if (start2 + len > arr2.length) { return false; }
        for (int i = 0; i < len; i++) {
            if (arr1[start1 + i] != arr2[start2 + i]) return false;
        }

        return true;
    }


    /**
     * @return the data coding scheme specified in the conversion configuration file.
     */
    public int getDataCodingScheme() {
        return dcs;
    }


    /**
     * Constructor
     */
    protected Converter() {
    }


    /**
     * init initialises the converter and should be overridden by subclasses.
     */
    protected void init(Properties conv) throws SMSComConfigException {
        actions = new byte[Character.MAX_VALUE + 1];
        outbytes = new Hashtable<Character, byte[]>();
        makeConversionTables(conv);
    }


    /**
     * makeConversionEntry processes one action specification. It inserts the
     * appropriate action code into the actions array for all requested
     * characters, and creates a byte array in outbytes for multi-byte
     * characters.
     * @param action the string specifying an action or output byte sequence
     * @param cl the CharList specifying a set of characters.
     */
    protected static void  makeConversionEntry(String action, CharList cl)
        throws SMSComConfigException {
        char ch;
        byte act = 0;
        byte chopped;
        byte[] outb = null;

        //Prepare the action once per character list
        if (action.equalsIgnoreCase("delete")) {
            act = DELETE;
        } else if (action.equalsIgnoreCase("keep")) {
            act = KEEP;
        } else if (action.equalsIgnoreCase("chop")) {
            act = CHOP;
        } else if (action.equalsIgnoreCase("ucs2")) {
            act = UCS2;
        } else {
            //action is to output bytes
            outb = hexToBytes(action);
            if (action.length() > 2
                || outb[0] == LOOKUP
                || outb[0] == CHOP
                || outb[0] == KEEP
                || outb[0] == DELETE
                || outb[0] == UCS2) {
                act = LOOKUP;
            } else {
                act = outb[0];
            }
        }

        //And set the action for each character in the list
        for (; cl.hasMoreElements(); ) {
            ch = cl.nextElement();
            //LOOKUP means there are bytes in outbytes that now become unused
            if (actions[ch] == LOOKUP) { outbytes.remove(new Character(ch)); }

            if (act == LOOKUP) {
                outbytes.put(new Character(ch), outb);
            }
            if (act == CHOP) {
                //if possible, we can just as well pre-chop the character when
                //creating the table
                chopped = (byte) (ch & 0xFF);
                switch (chopped) {
                case LOOKUP:
                case CHOP:
                case KEEP:
                case DELETE:
                case UCS2:
                    actions[ch] = act; //Reserved characters must be chopped later
                    break;
                default:
                    actions[ch] = chopped;
                }
            } else {
                actions[ch] = act;
            }
        }
    }


    /**
     * makeConversionTables processes all action specifications in the correct
     * priority order, and fills the actions and outbytes structure.
     * @param conv properties from the configuration file specifying the
     * conversion.
     */
    protected static void makeConversionTables(Properties conv) throws SMSComConfigException {
        String val;
        String whichChars;
        CharList cl;
        int rulePrio;
        String action;

        val = conv.getProperty("DataCodingScheme", "0");
        try {
            dcs = Integer.parseInt(val);
            conv.remove("DataCodingScheme");
        } catch (NumberFormatException e) {
            dcs = 0;
        }
        val = conv.getProperty("DataCodingSchemeForUcs2", "8");
        try {
            dcsUcs2 = Integer.parseInt(val);
            conv.remove("DataCodingSchemeForUcs2");
        } catch (NumberFormatException e) {
            dcsUcs2 = 0;
        }
        val = conv.getProperty("Pack", "no");
        pack = convertToBoolean(val);
        
        convClass = conv.getProperty("Converter", "Converter");
        conv.remove("Converter");
        
        val = conv.getProperty("StrictDcs", "no");
        strictDcs = convertToBoolean(val);

        Arrays.fill(actions, defaultAction);

        //There are six levels of priority for the action specifications. This
        //loop processes the list of actions once for each priority, starting
        //with the lowest. Thus higher priority action specifications overwrite
        //lower priority ones.
        for (int prio = 0; prio < 6; prio++) {
            for (Enumeration<?> e = conv.propertyNames(); e.hasMoreElements();) {
                whichChars = (String) e.nextElement();
                //Ignore invalid char lists, probably unknown property
                cl = new CharList(whichChars);
                if (cl.isOk()) {
                    action = conv.getProperty(whichChars);
                    if (!cl.hasMany()) {
                        rulePrio = 5;
                    } else {
                        if (action.equalsIgnoreCase("ucs2")) { rulePrio = 0; }
                        else if (action.equalsIgnoreCase("delete")) { rulePrio = 1; }
                        else if (action.equalsIgnoreCase("keep")) { rulePrio = 2; }
                        else if (action.equalsIgnoreCase("chop")) { rulePrio = 4; }
                        else rulePrio = 3;
                    }
                    if (prio == rulePrio) {
                        if (prio == 3 && !isHexDigits(action)) {
                            throw new SMSComConfigException("Character converter: bad output byte list "
                                                            + action + " for characters " + whichChars);
                        }
                        makeConversionEntry(action, cl);
                        conv.remove(action); //Entry used for this prio, will not be used any more
                    }
                }
            }
        }
    }
    
    /**
     * Returns true if the value is yes, true, on or 1.
     * @param val Value to convert.
     * @return Value converted to boolean.
     */
    private static boolean convertToBoolean(String val) {
        return ("yes".equalsIgnoreCase(val)
                || "true".equalsIgnoreCase(val)
                || "on".equalsIgnoreCase(val)
                || "1".equals(val));
    }


    /**
     * hexToBytes converts a String with hex-digits to a new byte array with the
     * byte corresponding to hex-digit pairs in it.
     * @param hex the hex digits corresponding to bytes.
     * @return a new byte array with the bytes in.
     * @throws SMSComConfigException Thrown on number parsing error.
     */
    protected static byte[] hexToBytes(String hex) throws SMSComConfigException {
        byte[] bytes;
        int inpos = 0;
        int outpos = 0;
        int step;

        if (hex.length() % 2 == 0) {
            bytes = new byte[hex.length() / 2];
            step = 2;
        } else {
            bytes = new byte[hex.length() / 2 + 1];
            step = 1;
        }
        try {
            while (inpos < hex.length()) {
                bytes[outpos++] = (byte) (Integer.parseInt(hex.substring(inpos, inpos + step), 16));
                inpos += step;
                step = 2;
            }
        } catch (NumberFormatException e) { 
        	throw new SMSComConfigException("Cannot parse Int, NumberFormatException"); }

        return bytes;
    }


    /**
     * @param s string to check
     * @return true iff all characters in s are hex digits
     */
    protected static boolean isHexDigits(String s) {
        char ch;
        for (int i = 0; i < s.length(); i++) {
            ch = s.charAt(i);
            if (ch > 'f') { return false; }
            if (ch > 'F' && ch < 'a') { return false; }
            if (ch > '9' && ch < 'A') { return false; }
            if (ch < '0') { return false; }
        }
        return true;
    }


    /**
     * extends makes a byte array longer.
     * @param ba the byte array that needs to grow.
     * @param min minimum growth. This parameter is not so interesting, but
     * ensures that the growth is enough even in extreme cases.
     */
    private byte[] extend(byte[] ba, int min) {
        byte[] ret = new byte[(int) (ba.length * 1.5 + min)];
        System.arraycopy(ba, 0, ret, 0, ba.length);
        return ret;
    }




    /**
     * CharList is an Enumerationish interface to a character list specification
     * of the form "0003,0009,0012-00c6,0234"
     */
    protected static class CharList {
        String charspec; //The original string
        boolean ok = true; //The format is valid
        int charTokens = 0; //The number of unicode characters found
        StringTokenizer st;

        boolean inRange = false; //We are currently processing characters in a range
        char curChar; //The current character
        char stopChar; //The range end


        /**
         * Constructor.
         * @param s the string specifying the character range
         */
        public CharList(String s) {
            charspec = s;

            String tok;
            st = new StringTokenizer(s, ",-");
            while (st.hasMoreTokens()) {
                tok = st.nextToken().trim();
                if (tok.length() != 4 || !isHexDigits(tok)) {
                    ok = false;
                    break;
                }
                ++charTokens;
            }
            st = new StringTokenizer(s, ",-", true);
        }


        /**
         * @return true iff there are more characters in the list.
         */
        public boolean hasMoreElements() {
            return inRange || st.hasMoreTokens();
        }

        /**
         * @return the next character from the list.
         */
        public char nextElement() {
            String tok;

            if (inRange) {
                //We are chewing on a range, and do not need to read any more tokens yet
                if (curChar >= stopChar) {
                    inRange = false;
                }
                return curChar++;
            } else {
                if (!st.hasMoreTokens()) { return '#'; }

                //Get the next character and the delimiter after it
                try {
                    curChar = (char) (Integer.parseInt(st.nextToken().trim(), 16));
                } catch (NumberFormatException e) { ; }
                if (!st.hasMoreTokens()) { return curChar; }
                tok = st.nextToken().trim();

                //If not a range, return the character we got.
                if (!tok.equals("-")) { return curChar; } //Must be ,
                if (!st.hasMoreTokens()) { return curChar; } //Open-ended range e.g. 03FA-

                //Setup range and return the first character.
                try {
                    stopChar = (char) (Integer.parseInt(st.nextToken().trim(), 16));
                } catch (NumberFormatException e) { ; }
                if (st.hasMoreTokens()) {
                    tok = st.nextToken().trim(); //Get the next ,
                }
                if (curChar == stopChar) {
                    //Silly range e.g. 03F3-03F3
                    return curChar;
                }
                inRange = true;
                if (stopChar < curChar) {
                    //Reverse range e.g. 0300-0001
                    char tmp = stopChar;
                    stopChar = curChar;
                    curChar = tmp;
                }
                return curChar++;
            }
        }


        /****************************************************************
         * @return true iff the character list is valid
         */
        public boolean isOk() {
            return ok;
        }


        /****************************************************************
         * @return true iff the character list has more than one character.
         */
        public boolean hasMany() {
            return charTokens > 1;
        }
    }
}
