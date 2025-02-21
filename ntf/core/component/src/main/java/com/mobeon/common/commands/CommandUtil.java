/*
 * CommandUtil.java
 *
 * Created on den 26 augusti 2004, 20:28
 */

package com.mobeon.common.commands;

import java.util.Properties;

/**
 * Utilities needed by other classes in command package.
 * It is also used by test classes, but is not meant for general
 * use outside of command package, the operation signatures
 * might change without warning
 */
public class CommandUtil
{

    /** No instances should be made. */
    private CommandUtil()
    {
    }

    /**
     * Pack a short into 2 bytes of a byte array.
     * @param bytes The byte array
     * @param index Where to start in the array
     * @param value The value to pack
     * @return number of bytes used
     */
    public static int packShort(byte[] bytes, int index, short value)
    {
        bytes[index++] = (byte) ((value >>> 8) & 0xFF);
        bytes[index++] = (byte) (value & 0xFF);
        return 2;
    }

    /**
     * Get no of bytes it takes to pack a short.
     * @return The value 2.
     */
    public static int shortPackSize()
    {
        return 2;
    }

    /**
     * Pack an integer into 4 bytes of a byte array.
     * @param bytes The byte array
     * @param index Where to start in the array
     * @param value The value to pack
     * @return number of bytes used.
     */
    public static int packInt(byte[] bytes, int index, int value)
    {
        bytes[index++] = (byte) ((value >>> 24) & 0xFF);
        bytes[index++] = (byte) ((value >>> 16) & 0xFF);
        bytes[index++] = (byte) ((value >>> 8) & 0xFF);
        bytes[index++] = (byte) (value & 0xFF);
        return 4;
    }

    /**
     * Number of bytes it takes to pack an int (4).
     * @return 4
     */
    public static int intPackSize()
    {
        return 4;
    }


    /**
     * Character set to use when converting strings (Always UTF-8).
     * @return The character set name.
     */
    public static String getCharSet()
    {
        return "UTF-8";
    }

    /**
     * Pack a string into byte array.
     * @param bytes Byte arrray to pack into
     * @param index Where to start the packing in bytes.
     * @param str The string to pack. It may be null but the
     *            string will then unpack as the empty string.
     * @return Number of bytes used.
     */
    public static int packString(byte[] bytes, int index, String str)
    {

        if ((str == null) || (str.length() == 0)) {
            return packInt(bytes, index, 0);
        }

        byte[] strBytes;

        try {
            strBytes = str.getBytes(getCharSet());
        } catch (java.io.UnsupportedEncodingException uee) {
            // Should never happen
            strBytes = str.getBytes();
        }
        int pos = index + packInt(bytes, index, strBytes.length);

        for (int i = 0; i < strBytes.length; i++) {
            bytes[pos] = strBytes[i];
            pos++;
        }
        return pos - index;
    }


    /**
     * Determine no of bytes to pack a string
     * @param str The string that the packsize is calculated for
     * @return Number of bytes it will take to pack str
     */
    public static int stringPackSize(String str)
    {
        if ((str == null) || (str.length() == 0)) {
            return intPackSize();
        }
        byte[] strBytes;

        try {
            strBytes = str.getBytes(getCharSet());
        } catch (java.io.UnsupportedEncodingException uee) {
            // Should never happen
            strBytes = str.getBytes();
        }
        return intPackSize() + strBytes.length;

    }

    /**
     * Unpack a string from a byte array.
     * The string is returned in a stringbuffer, this is so we
     * can return the bytes read count.
     * @param bytes Byte array to unpack from
     * @param index Where to start unpacking in byte array
     * @param sb The result of unpacking is put here.
     * @return Number of bytes read from the arrray.
     */
    public static int unpackString(byte[] bytes, int index, StringBuffer sb)
    {
        int byteLen = unpackInt(bytes, index);
        if (byteLen == 0) {
            sb.setLength(0);
            return 4;
        }
        byte[] strBytes = new byte[byteLen];
        int pos = index + 4;
        for (int i = 0; i < strBytes.length; i++) {
            strBytes[i] = bytes[pos];
            pos++;
        }

        sb.setLength(0);
        String result;
        try {
            result = new String(strBytes, getCharSet());
        } catch (java.io.UnsupportedEncodingException uee) {
            // Should not happen
            result = new String(strBytes);
        }
        sb.append(result);
        return byteLen + 4;
    }


    /**
     * Get value of the short packed at given position.
     * @param bytes The packed data
     * @param index Position to start at
     */
    public static short unpackShort(byte[] bytes, int index)
    {
        return (short) ((bytes[index] << 8) | bytes[index + 1]);
    }

    /**
     * Get value of the integer packed at given position
     * @param bytes The packed data
     * @param index Position to start at
     */
    public static int unpackInt(byte[] bytes, int index)
    {
        byte b3 = bytes[index];
        byte b2 = bytes[index + 1];
        byte b1 = bytes[index + 2];
        byte b0 = bytes[index + 3];
        int result = 0;
        result |= (b3 << 24) & 0xFF000000;
        result |= (b2 << 16) & 0x00FF0000;
        result |= (b1 << 8) & 0x0000FF00;
        result |= b0 & 0x000000FF;
        return result;
    }

    /**
     * Print content of byte array to stdout.
     * This should be used for testing only.
     * @param bytes The bytes to print
     */
    public static void printByteArray(byte[] bytes)
    {
        for (int i = 0; i < bytes.length; i++) {
            char c = (char) bytes[i];
            System.out.print("[" + i + "](" + Integer.toHexString(bytes[i]) +
                             "," + c + ")");
        }
        
    }

    /**
     * Get value for a given property.
     * @param props Properties to search in
     * @param key Key to look for
     * @param defValue Default value if key not found or value in props is
     *        not an integer.
     * @return Integer that key is mapped to if it is a valid integer, defValue otherwise.
     */
    public static int getPropInt(Properties props, String key, int defValue)
    {
        int result = defValue;
        String value = props.getProperty(key);
        if (value != null) {
            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
            	CHLogger.log(CHLogger.INFO, "CommandUtil.getPropInt- Bad int format for " + key +
                                   " : " + value);
            }
        } else {
            CHLogger.log(CHLogger.INFO, "No value found for prop " + key);
        }
        return result;
    }
}


