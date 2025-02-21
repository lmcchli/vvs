package com.mobeon.ntf.slamdown;

import com.mobeon.ntf.text.ByteArrayUtils;

/**
 * Contains the payload for a slamdown, either in bytes or in Strings.
 */
public class SlamdownPayload {

    /** The header part of this message, in bytes. */
    private final byte[] headerBytes;
    
    /** The body part of this message, in bytes. */
    private final byte[][] bodyBytes;
    
    /** The footer part of this message, in bytes. */
    private final byte[] footerBytes;

    /** The header part of this message, in bytes. */
    private final String header;
    
    /** The body part of this message, in bytes. */
    private final String[] body;
    
    /** The footer part of this message, in bytes. */
    private final String footer;
    
    /** Whether or not this {@link SlamdownPayload} object has its information stored in bytes or in Strings.*/
    public final boolean isBytes;
    
    /** The number of "lines" on the payload. */
    public final int bodyParts;
    
    /**
     * Default constructor for a byte {@link SlamdownPayload}.
     * @param headerBytes The header part of this message.
     * @param bodyBytes The body part of this message.
     * @param footerBytes The footer part of this message.
     */
    public SlamdownPayload(byte[] headerBytes, byte[][] bodyBytes, byte[] footerBytes)
    {
        this.headerBytes = headerBytes;
        this.bodyBytes = bodyBytes;
        this.footerBytes = footerBytes;
        this.bodyParts = bodyBytes.length;
        this.header = null;
        this.body = null;
        this.footer = null;
        isBytes = true;
    }
    
    /**
     * Default constructor for a String {@link SlamdownPayload}.
     * @param header The header part of this message.
     * @param body The body part of this message.
     * @param footer The footer part of this message.
     */
    public SlamdownPayload(String header, String[] body, String footer)
    {
        this.headerBytes = null;
        this.bodyBytes = null;
        this.footerBytes = null;
        this.header = header;
        this.body = body;
        this.footer = footer;
        this.bodyParts = body.length;
        this.isBytes = false;
    }
    
    /**
     * Returns a flat array containing the data of this SlamdownBytePayload.
     */
    public byte[] getFlatArray()
    {
        byte[] array = {};
        
        array = ByteArrayUtils.append(array, header);
        
        for (byte[] bodyPart : bodyBytes)
        {
            array = ByteArrayUtils.append(array, bodyPart);
        }

        array = ByteArrayUtils.append(array, footer);
        
        return array;
    }
    
    /**
     * Returns a 2D array containing the data of this SlamdownBytePayload.
     */
    public byte[][] get2DArray()
    {
        byte[][] array = new byte[bodyBytes.length + 2][];
        
        array[0] = headerBytes;
        
        for (int i = 0; i < bodyBytes.length; i++)
        {
            array[i+1] = bodyBytes[i];
        }

        array[array.length - 1] = footerBytes;
        
        return array;
    }
    
    public String getStringHeader() {
        return header;
    }
    
    public String[] getStringBody() {
        return body;
    }
    
    public String getStringFooter() {
        return footer;
    }
    
}