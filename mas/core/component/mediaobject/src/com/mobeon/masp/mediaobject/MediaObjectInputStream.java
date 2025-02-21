/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * This class extends <code>InputStream</code> to represent an input
 * stream of bytes from a {@link IMediaObject}.
 *
 * @author Mats Egland
 */
class MediaObjectInputStream extends InputStream {

    /**
     * The iterator of the mediaObject.
     */
    private IMediaObjectIterator iterator;
    /**
     * The mediaobject to read from.
     */
    private final IMediaObject MEDIA_OBJECT;
    /**
     * The current bytebuffer.
     */
    private ByteBuffer currentBuffer;
    /**
     * Number of bytesRead, i.e. the current position (index)
     * in the mediaObject.
     */
    private int bytesRead = 0;

    /**
     * Creates a <code>MediaObjectInputStream</code> object
     * that reads from the specified <code>IMediaObject</code>.
     *
     * @param mediaObject The mediaobject to read from.
     * @throws IllegalArgumentException If mediaObject is null, or if
     *                                  mediaObject is mutable.
     */
    public MediaObjectInputStream(IMediaObject mediaObject) {
        super();
        if (mediaObject == null) {
            throw new IllegalArgumentException("Argument mediaObject is null");
        } else if (!mediaObject.isImmutable()) {
            throw new IllegalArgumentException(
                    "IMediaObject must be immutable in order to retreive " +
                            "MediaObjectInputStream from it.");
        }
        this.MEDIA_OBJECT = mediaObject;
        this.iterator = mediaObject.getNativeAccess().iterator();
    }

    /**
     * Returns the bytes left to read in mediaobject. A MediaObject
     * will never block.
     *
     * @return Bytes left.
     *
     */
    public final synchronized int available() {
        return (int) (MEDIA_OBJECT.getSize() - bytesRead);
    }

    /**
     * Skips over and discards n bytes of data from this input stream.
     *
     * @param n The requested of bytes to skip.
     * @return Returns the actual number of bytes skipped. This can
     *         be less than the number of bytes requested if
     *         end of media-object is reached.
     *
     * @throws IOException If error occurred while reading from MediaObject.
     */
    public final long skip(long n) throws IOException {
        int bytesRead = 0;
        int readByte;
        for (int i = 0; i < n; i++) {
            readByte = this.read();
            if (readByte == -1) {
                // reached end of stream.
                return bytesRead;
            }
            bytesRead++;
        }
        return bytesRead;
    }

    /**
     * Repositions this stream to the position the very
     * beginning of the mediaobject.
     *
     */
    public final synchronized void reset() {
        // Fetch a new iterator
        iterator = MEDIA_OBJECT.getNativeAccess().iterator();
        bytesRead = 0;
        currentBuffer = null;
    }

    /**
     * Mark is not supported.
     *
     * @param readlimit Insignificant.
     */
    public final synchronized void mark(int readlimit) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Mark is not supported!
     *
     * @return false
     */
    public final boolean markSupported() {
        return false;
    }

    //javadoc in InputStream
    public final synchronized int read() throws IOException {
        if (currentBuffer == null) {
            // Retreive first buffer
            if (!setNextCurrentBuffer()) {
                return -1;
            }
        }
        if (!currentBuffer.hasRemaining()) {
            // Retreive next buffer
            if (!setNextCurrentBuffer()) {
                // last buffers' last byte
                return -1;
            }
        }
        byte value = currentBuffer.get();
        bytesRead++;

        return (int) value & 0xFF;

    }

    /**
     * Retreives the next buffer from the media-object
     * and updates the <code>currentBuffer</code> member.
     * Returns false if no more buffers. If the next buffer
     * is retreived this method guarantees it is rewinded.
     *
     * @return true if buffer was fetched successfully.
     * @throws IOException If failed to read next buffer
     *                     from media-object.
     */
    private boolean setNextCurrentBuffer() throws IOException {
        if (!iterator.hasNext()) {
            return false;
        } else {
            try {
                currentBuffer = iterator.next();
                currentBuffer.rewind();
            } catch (MediaObjectException e) {
                throw new IOException("Failed to read buffer from MediaObject, nested exception was"
                        + e.getMessage());
            }

        }
        return true;
    }
}
