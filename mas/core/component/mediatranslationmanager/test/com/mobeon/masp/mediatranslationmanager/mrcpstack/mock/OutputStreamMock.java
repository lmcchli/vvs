/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager.mrcpstack.mock;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class is a simulation/mock of the output from the MRCP client.
 */
public class OutputStreamMock extends OutputStream {
    protected static ILogger logger = ILoggerFactory.getILogger(OutputStreamMock.class);
    Queue<Vector<Byte>> queue = new ConcurrentLinkedQueue<Vector<Byte>>();

    /**
     * Write message to server. Implements the write method of OutputStream.
     * @param data
     * @throws IOException
     */
    public void write(byte[] data)  throws IOException {
        if (logger.isDebugEnabled()) logger.debug("write(byte[])");
        setBuffer(data);
    }

    private void setBuffer(byte[] data) {
        Vector<Byte> buffer = new Vector<Byte>();
        for (byte b : data) buffer.add(b);
        queue.offer(buffer);
    }

     public void write(int b) throws IOException {
        if (logger.isDebugEnabled()) logger.debug("write(int)");
        throw new IOException("Buuu");
    }

    public String getBuffer() {
        int delay = 1;
        if (logger.isDebugEnabled()) logger.debug("--> getBuffer()");
        String data = "";
        Vector<Byte> buffer;
        while (queue.isEmpty()) {
            try {
                Thread.sleep(delay);
                delay*=10;
                if (delay > 100000000) {
                    logger.error("Timeout during read buffer");
                    throw new IllegalStateException("Timeout");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        buffer = queue.poll();
        for (byte b : buffer) {
            data += (char)b;
        }
        if (logger.isDebugEnabled()) logger.debug("<-- getBuffer(): [" + data + "]");
        return data;
    }
}
