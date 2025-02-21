package com.mobeon.ntf.text;

import com.mobeon.common.trafficeventsender.TrafficEventSenderException;

/**
 * Various utility methods for byte array manipulation while creating and
 * parsing template files in byte arrays instead of strings. Also contains
 * various methods for single byte.
 * @author ejonpit
 */
public class ByteArrayUtils {
    
    /** An empty byte array (to be used instead of empty string. */
    public static byte[] ARRAY_EMPTY = {};
    
    /**
     * Returns the representation of a byte array in a hexadecimal String. Suppose
     * the array is represented by the following values:<br>
     * <code>[1] 00<br>
     * [2] 0f<br>
     * [3] 11<br></code>
     * then the resulting String is <code>000F11</code>.
     */
    public static String byteArrayToHexString(byte[] array)
    {
        String s = "";
        for (int i = 0; i < array.length; i++)
        {
            if (array[i] == 0) s += "00";
            else
            {
                if (array[i] < 16) s += "0";
                s += Integer.toString(array[i] & 0xff, 16).toUpperCase() + "";
            }
        }
        return s;
    }
    
    /**
     * Returns whether or not the byte is a
     * number, a character, or the following
     * punctuation marks:
     * <ul>
     * <li>,</li>
     * <li>.</li>
     * <li>:</li>
     * <li>;</li>
     * <li>-</li>
     * <li>=</li>
     * <li>+</li>
     * </ul>
     */
    public static boolean byteMatch(byte b)
    {
        if (b >= 48 && b <= 57) return true; // number
        if (b >= 65 && b <= 90) return true; // uppercase
        if (b >= 97 && b <= 122) return true; // lowercase
        if (b == 44 || b == 46 || b == 58 || b == 59 || b == 45 || b == 61 || b == 43) return true;
        return false;
    }
    
    /**
     * Appends <code>toAdd</code> to </code>original</code> and returns
     * the resulting byte array.
     */
    public static byte[] append(byte[] original, byte[] toAdd)
    {
        if (original == null && toAdd == null) return null;
        if (original == null) return toAdd;
        if (toAdd == null) return original;
        byte[] newArray = new byte[original.length + toAdd.length];
        for (int i = 0; i < original.length; i++)
        {
            newArray[i] = original[i];
        }
        for (int i = 0; i < toAdd.length; i++)
        {
            newArray[i + original.length] = toAdd[i];
        }
        return newArray;
    }
    
    /**
     * Appends the bytes of <code>toAdd</code> to </code>original</code> and returns
     * the resulting byte array.
     */
    public static byte[] append(byte[] original, String toAdd)
    {
        if (original == null && toAdd == null) return null;
        if (original == null) return toAdd.getBytes();
        if (toAdd == null) return original;
        byte[] toAddBytes = toAdd.getBytes();
        byte[] newArray = new byte[original.length + toAddBytes.length];
        for (int i = 0; i < original.length; i++)
        {
            newArray[i] = original[i];
        }
        for (int i = 0; i < toAddBytes.length; i++)
        {
            newArray[i + original.length] = toAddBytes[i];
        }
        return newArray;
    }
    
    /**
     * Appends the bytes of <code>toAdd</code> to </code>original</code> and returns
     * the resulting byte array.
     */
    public static byte[] append(String original, byte[] toAdd)
    {
        if (original == null && toAdd == null) return null;
        if (original == null) return toAdd;
        if (toAdd == null) return original.getBytes();
        byte[] originalBytes = original.getBytes();
        byte[] newArray = new byte[originalBytes.length + toAdd.length];
        for (int i = 0; i < originalBytes.length; i++)
        {
            newArray[i] = originalBytes[i];
        }
        for (int i = 0; i < toAdd.length; i++)
        {
            newArray[i + originalBytes.length] = toAdd[i];
        }
        return newArray;
    }
    
    /**
     * Performs a naive verification of two byte arrays and returns
     * whether or not their values are equal.
     */
    public static boolean arrayEquals(byte[] b1, byte[] b2)
    {
        if (b1 == null && b2 == null) return true;
        if (b1 == null || b2 == null) return false;
        if (b1.length != b2.length) return false;
        for (int i = 0; i < b1.length; i++)
        {
            if (b1[i] != b2[i]) return false;
        }
        return true;
    }
    
    /**
     * Returns the subarray which starts at index <code>index</code> and goes to
     * the end. If index is larger than the highest index of the array, the returned
     * array will have size 0. If it is smaller than 0, the whole array is returned.
     */
    public static byte[] subArray(byte[] array, int index)
    {
        if (index >= array.length) return new byte[0];
        if (index < 0) return array;
        byte[] newArray = new byte[array.length - index];
        for (int i = 0; i < newArray.length; i++)
        {
            newArray[i] = array[i + (array.length - newArray.length)];
        }
        return newArray;
    }
    
    /**
     * Returns the subarray which starts at index <code>start</code> and goes to
     * the <code>end</code>, exclusive.
     */
    public static byte[] subArray(byte[] array, int start, int end)
    {
        if (start >= array.length || end < 0 || start >= end) return new byte[0];
        if (start < 0) start = 0;
        if (end >= array.length) end = array.length;
        byte[] newArray = new byte[end - start];
        for (int i = 0; i < newArray.length; i++)
        {
            newArray[i] = array[start + i];
        }
        return newArray;
    }
    
    /**
     * Trims leading and trailing whitespace from the given array.
     */
    public static byte[] trimWS(byte[] array)
    {
        byte SP = ' ', TB = '\t';
        int lead = 0, trail = 0;
        for (int i = 0; array[i] == SP || array[i] == TB; i++)
        {
            lead++;
        }
        for (int i = array.length - 1; array[i] == SP || array[i] == TB; i--)
        {
            trail++;
        }
        byte[] trimmed = new byte[array.length - (lead + trail)];
        for (int i = 0; i < trimmed.length; i++)
        {
            trimmed[i] = array[i + lead];
        }
        return trimmed;
    }
    
    /**
     * Returns whether or not the pseudo-regex characters specified in
     * <code>pseudoREGEX</code> are contained in <code>array</code>. If they are
     * contained, the index where the first instance of pseudoREDEX is returned.
     * If it is not contained, then -1 is returned.<p>
     * <code>pseudoREGEX</code> is NOT a complete regular expression as defined
     * by Java specifications; rather it is a subset of a regular expression
     * language which suits our needs. The following are the rules for the pseudo-
     * regular expression:
     * <ul>
     * <li>Any character not specified below is a specific character. For example,
     * the regex *c* will match "ooco" or "c", but not "ooCo".</li>
     * <li>* matches everything any number of times (wildcard).</li>
     * </ul>
     * Please note that the regex cannot finish with a wildcard. Additionally, an
     * empty pseudoREGEX matches everything. Finally, an empty input array will
     * never match anything (except the empty pseudoREGEX).
     */
    public static int matches(byte[] array, String pseudoREGEX)
    {
        return matches(array, pseudoREGEX, false);
    }
    /**
     * See {@link #matches(byte[] array, String pseudoREGEX)}. The difference is
     * that this method keeps track of whether or not we are "searching", i.e.
     * whether or not we have a potential match.
     */
    private static int matches(byte[] array, String pseudoREGEX, boolean searching)
    {
        // If pseudoREGEX is empty, return 0 since the first position matches
        // empty
        if (pseudoREGEX.length() == 0) return 0;
        if (array.length == 0) return -1;
        char next = pseudoREGEX.charAt(0);
        for (int i = 0; i < (searching ? 1 : array.length); i++)
        {
            if (match(array[i], next))
            {
                // If there is a direct match, return value
                if (matches(subArray(array, i+1), pseudoREGEX.substring(1), true) != -1) return i;
                // In the case of a wildcard, give a chance to the next character
                else if (next == '*' && matches(subArray(array, i+1), pseudoREGEX, true) != -1) return i;
            }
        }
        return -1;
    }
    /**
     * Returns whether or not the byte provided is a match for the
     * given pseudo-regex value (as defined in {@link #matches(byte[] array, String pseudoREGEX)}).
     */
    private static boolean match(byte b, char regexVal)
    {
        // * matches everything
        if (regexVal == '*') return true;
        // Direct match
        else if (b == (byte)regexVal) return true;
        // Not direct
        else return false;
    }
    
    /**
     * Returns the first index at which <code>toMatch</code> is found
     * within <code>array</code>, or -1 if it is not found.
     */
    public static int contains(byte[] array, byte[] toMatch)
    {
        for (int i = 0; i < array.length; i++)
        {
            if (array[i] == toMatch[0])
            {
                boolean found = false;
                for (int j = 0; j < toMatch.length && j < array.length; j++)
                {
                    if (array[i+j] != toMatch[j]) break;
                    if (j == (toMatch.length - 1)) found = true;
                }
                if (found) return i;
            }
        }
        return -1;
    }
    
    /**
     * Returns the String format of a byte array, each byte represented
     * by a hexidecimal number.
     */
    public static String byteArrayToString(byte[] array)
    {
        String s = "";
          for (int i = 0; i < array.length; i++)
          {
              s += Integer.toString(array[i] & 0xff, 16).toUpperCase() + " ";
          }
          return s;
    }

    /**
     * Replaces all instances of <code>a</code> in <code>array</code>
     * by <code>b</code> and returns the result.
     */
    public static byte[] replaceAll(byte[] array, byte a, byte b) {
        byte[] result = new byte[array.length];
        for (int i = 0; i < result.length; i++)
        {
            if (array[i] == a) result[i] = b;
            else result[i] = array[i];
        }
        return result;
    }

    /**
     * Returns the byte array represented by the string. The string must be of the format
     * <code>"xx yy zz..."</code> where <code>xx</code>, <code>yy</code> and <code>zz</code>
     * are hexadecimal numbers (00, a7, ff, etc.) separated by spaces.
     * 
     * If there is a problem in parsing the String, and empty array ([]) is returned.
     * 
     * @param code The hexadecimal code String.
     * @return The array of bytes corresponding to the given String.
     */
    public static byte[] stringToByteArray(String code)
    {
        if (code == null || code.length() < 2) return ARRAY_EMPTY;
        String[] individualBytes = code.split(" ");
        byte[] array = new byte[individualBytes.length];
        try
        {
            for (int i = 0; i < array.length; i++)
            {
                array[i] = (byte) ((Character.digit(individualBytes[i].charAt(0), 16) << 4)
                        + Character.digit(individualBytes[i].charAt(1), 16));
            }
        }
        catch (Exception ex)
        {
            return ARRAY_EMPTY;
        }
        return array;
    }
    
    /**
     * Returns the byte array represented by the string. The string must be of the format
     * <code>"xxyyzz..."</code> where <code>xx</code>, <code>yy</code> and <code>zz</code>
     * are hexadecimal numbers (00, a7, ff, etc.).
     * 
     * If there is a problem in parsing the String, and empty array ([]) is returned.
     * 
     * @param code The hexadecimal code String.
     * @return The array of bytes corresponding to the given String.
     */
    public static byte[] stringToByteArrayNoSpaces(String code)
    {
        try {
            String lowercase = code.toLowerCase();
            byte[] empty = {};
            if (lowercase == null || lowercase.length() == 0) return empty;
            byte[] array = new byte[lowercase.length() / 2];
            for (int i = 0; i < array.length; i++) {
                array[i] = (byte) ((Character.digit(lowercase.charAt(2*i), 16) << 4) + Character.digit(lowercase.charAt(2*i + 1), 16));
            }
            return array;
        } catch (Exception e) {
            return ARRAY_EMPTY;
        }
    }
    
    /**
     * Converts a number to a byte array. For example, suppose the number
     * is 37, then the resulting byte[] would be {0x33,0x37}.
     * @param i The number to convert to a byte[].
     */
    public static byte[] intToByteArray(int i)
    {
        return (""+i).getBytes();
    }
    
}
