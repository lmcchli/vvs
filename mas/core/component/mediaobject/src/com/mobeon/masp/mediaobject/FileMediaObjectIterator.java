/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.NoSuchElementException;
import java.nio.ByteBuffer;

/**
 * Iterator that iterates of the <code>ByteBuffer</code>s of
 * a {@link FileMediaObject}.
 * Implements the {@link IMediaObjectIterator} interface.
 *
 * Class is not thread-safe, i.e. only one thread per Iterator.
 *
 * @author Mats Egland
 */
public final class FileMediaObjectIterator implements IMediaObjectIterator {
    /**
     * The ILogger used.
     */
    private final static ILogger LOGGER =
            ILoggerFactory.getILogger(FileMediaObjectIterator.class);

    /**
     * The {@link FileMediaObject} that this iterator operates
     * on.
     */
    private final FileMediaObject FILE_MEDIA_OBJECT;
    /**
     * The nr of buffers the media objects consists of.
     */
    private final long NR_OF_BUFFERS;

    /**
     * The current position.
     */
    private int currentPosition = 0;
    /**
     * Creates a iterator that iterates over the given FileMediaObject's list
     * of <code>ByteBuffer</code>s.
     * Only FileMediaObject's is allowed to create
     * MediaObjectIterator's, so the constructor is package protected.
     * 
     * @param fileMediaObject The FileMediaObject that holds the byte buffers.
     */
    FileMediaObjectIterator(FileMediaObject fileMediaObject) {
        this.FILE_MEDIA_OBJECT = fileMediaObject;
        NR_OF_BUFFERS = fileMediaObject.getNrOfBuffers();
    }

    // javadoc in interface
    public boolean hasNext() {
        return (currentPosition < NR_OF_BUFFERS);
    }

    // javadoc in interface
    public ByteBuffer next() throws MediaObjectException {
        if (currentPosition >= NR_OF_BUFFERS) {
            throw new NoSuchElementException("No more ByteBuffers in MediaObject");
        }
        // first assure that the next buffer is retreived
        try {
            ByteBuffer byteBuffer = FILE_MEDIA_OBJECT.getBuffer(currentPosition++);
            ByteBuffer readOnlyBuffer = byteBuffer.asReadOnlyBuffer();
            readOnlyBuffer.rewind();
            return readOnlyBuffer;
        } catch (MediaObjectException e) {
            LOGGER.debug("Failed to iterate over FileMediaObject as it failed to read next buffer:"+
                e.getMessage());
            throw e;
        }
    }

    /**
     * Returns the current position of the iterator.
     * @return the current position.
     */
    int getPosition() {
        return this.currentPosition;
    }
}
