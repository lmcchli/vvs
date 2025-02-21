/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.mrcpstack.mock;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.io.InputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class is a simulation/mock of the input to the MRCP client.
 */
public class InputStreamMock extends InputStream {
    protected static ILogger logger = ILoggerFactory.getILogger(InputStreamMock.class);
    ConcurrentLinkedQueue<Vector<Integer>> queue = new ConcurrentLinkedQueue<Vector<Integer>>();
    Vector<Integer> buffer = null;

    public int available() {
        int count = buffer == null ? 0 : buffer.size();
        Vector<Integer>[] queueArray = new Vector[queue.size()];
        queue.toArray(queueArray);
        for (Vector<Integer> element : queueArray) count += element.size();
        return count;
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     * <p/>
     * <p> A subclass must provide an implementation of this method.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     *         stream is reached.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public int read() throws IOException {
        long delay = 1;
        if (buffer == null || buffer.isEmpty()) {
            if (logger.isDebugEnabled()) logger.debug("Retrieving next buffer ...");
            while (queue.isEmpty()) {
                try {
                    Thread.sleep(delay);
                    delay*=10;
                    if (delay > 10000000) {
                        logger.error("Timeout during read byte");
                        throw new IOException("Timeout");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            buffer = queue.poll();
        }
        int value = buffer.remove(0);
//        logger.debug("<-- read() : " + (char)value);
        return value;
    }

    public int read(byte[] bytes) throws IOException {
        long delay = 1;
        if (logger.isDebugEnabled()) logger.debug("--> read() : bytes");
        if (buffer == null || buffer.isEmpty()) {
            while (queue.isEmpty()) {
                try {
                    Thread.sleep(delay);
                    delay*=10;
                    if (delay > 1000) {
                        if (logger.isDebugEnabled()) logger.error("Timeout during read vector");
                        throw new IllegalStateException("Timeout");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            buffer = queue.poll();
        }
        if (bytes.length == buffer.size()) {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte)buffer.remove(0).intValue();
            }
        } else {
            if (logger.isDebugEnabled()) logger.debug("<-- read(fail) : bytes");
            return -1;
        }
        if (logger.isDebugEnabled()) logger.debug("<-- read() : bytes");
        return bytes.length;
    }

    public void setBuffer(String data) {
        if (logger.isDebugEnabled()) logger.debug("--> setBuffer() : [" + data + "]");
        Vector<Integer> buffer = new Vector<Integer>();
        for (char b : data.toCharArray()) {
            buffer.add((int)b);
        }
        queue.offer(buffer);
        if (logger.isDebugEnabled()) logger.debug("<-- setBuffer()");
    }
}
