/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.javamail;

import java.io.OutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author QHAST
 */
public abstract class JavamailDebugOutputStream extends OutputStream {

    private static final String SYSTEM_LINE_SEPARATOR = System.getProperty("line.separator");
    /**
     * Compiled pattern for non content data line string.
     */

    public static final Pattern BASE64_ENCODED_LINE = Pattern.compile("^[A-Za-z0-9+/=]+$");

    private StringBuilder encodedDataBuffer = null;
    private StringBuilder buffer = new StringBuilder();
    public static final String MESSAGE_HEADER = "JavaMail Debug: ";

    public void write(int b) throws IOException {
        buffer.append(Character.toChars(b));
        if(buffer.length()>=SYSTEM_LINE_SEPARATOR.length()) {
            String tail = buffer.substring(buffer.length()-SYSTEM_LINE_SEPARATOR.length());
            if(tail.equals(SYSTEM_LINE_SEPARATOR)) {
                this.flush();
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if(buffer.length()>0) {
            String line = buffer.toString();
            if(BASE64_ENCODED_LINE.matcher(line.trim()).matches() && line.length()>50) {   //Is considered as Base64 encoded data
                if(encodedDataBuffer == null) {
                    encodedDataBuffer = new StringBuilder();
                }
                encodedDataBuffer.append(line);
            } else {
                if(encodedDataBuffer != null) {
                    flushMessage(MESSAGE_HEADER +
                            encodedDataBuffer.substring(0,10)+
                            "<-Skipping "+
                            (encodedDataBuffer.length()-20)+
                            " characters->"+
                            encodedDataBuffer.substring(encodedDataBuffer.length()-10).trim()
                    );
                    encodedDataBuffer = null;
                }
                flushMessage(MESSAGE_HEADER +line.trim());
            }

            buffer.delete(0,buffer.length());
        }
    }

    @Override
    public void close() throws IOException {
        this.flush();
    }

    protected abstract void flushMessage(String message);
}
