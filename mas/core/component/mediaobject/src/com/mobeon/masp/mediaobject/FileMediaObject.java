/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * An implementation of the {@link IMediaObject} that extends {@link AbstractMediaObject} to
 * add functionality for reading data from a file. The file is loaded into a ordered sequence of
 * {@link ByteBuffer}'s and kept in a list. Each bytebuffer is created when data is
 * asked for, i.e. the file is loaded in portions, with the size given by the bufSize member.
 * <p>
 * This class is thread-safe, i.e. supports many clients. 
 *
 * <p>
 * The read file is mapped into memory with NIO direct buffers.
 * A direct byte buffer is allocated with the method
 * {@link ByteBuffer#allocateDirect(int)}.
 *
 * @author Mats Egland
 *
 */
public final class FileMediaObject extends AbstractMediaObject {
    /**
     * The ILogger used.
     */
    private static final ILogger LOGGER = ILoggerFactory.getILogger(FileMediaObject.class);

    /**
     * The size of each <code>ByteBuffer</code> in bytes. Should be set to a value that makes
     * it efficient to read from disk but not too large as it will then consume
     * more unnessecary memory.
     */
    private long bufSize;
    /**
     * The underlying file to fetch data from.
     */
    private File file;

    /**
     * The size of the file.
     */
    private long size;
    /**
     * The number of <code>ByteBuffer</code>s that makes up the media data.
     * This is the buffers needed to
     * represent the entire file.
     * It isn't necessarily the current number of <code>ByteBuffer</code>s loaded from
     * the file.
     */
    private long nrOfBuffers = 0;

    /**
     * Creates a <code>FileMediaObject</code> with the given source file.
     * The resulting <code>FileMediaObject</code> is immutable.
     *
     * @param file          The resource to fetch data from
     * @param bufferSize    The size of each ByteBuffer that the file is read into
     *
     * @throws IllegalArgumentException If buffersize is equal or less than zero,if the
     * file does not exist or is null.
     *
     * @throws MediaObjectException If fails to read from file
     */
    public FileMediaObject(File file, long bufferSize) throws MediaObjectException  {
        this(file, null, bufferSize);
    }

    /**
     * Creates a FileMediaObject with the given source file and file format and
     * MediaProperties. Note that the content of the given mediaproperties will be copied.
     *
     * The resulting <code>FileMediaObject</code> is immutable.
     *
     * @param file              The resource to fetch data from
     * @param mediaProperties   The media properties for the MediaObject. The content
     *                          of the passed media properties is copied to the internal mediaproperties
     *                          of the <code>FileMediaObject</code>
     * @param bufferSize        The size of each ByteBuffer that the file is read into
     *
     * @throws IllegalArgumentException If buffersize is equal or less than zero, if the
     *                                  file does not exist or is null.
     *
     * @throws MediaObjectException If fails to read from file.
     */
    public FileMediaObject(File file,
                           MediaProperties mediaProperties,
                           long bufferSize) throws MediaObjectException {
        super(mediaProperties);
        if (bufferSize <= 0) {
        	throw new IllegalArgumentException("BufferSize must be greater than zero");
        } else if (file == null) {
            throw new IllegalArgumentException("The file is null");
        } else if (!file.exists()) {
            throw new IllegalArgumentException("The file:" +file.getAbsolutePath()+" does not exist");
        }

        this.file = file;
        this.bufSize = bufferSize;
        this.setImmutable();
        // The initReading method will extract the size of the file calculate the nrOfBuffers required.
        initReading();
    }

    /**
     * Returns native access for this media object.
     *
     * @return Interface that provides access to the
     *         internal byte buffers.
     */
    public MediaObjectNativeAccess getNativeAccess() {
        return new FileMediaObjectNativeAccessImpl(this);
    }

    /**
     * Returns the underlying file that this MediaObject loads
     * its content from.
     * @return The underlying file.
     */
    public File getFile() {
        return this.file;    
    }
    /**
     * Overriden to return the size of the file. Note that the size returned may not be the size
     * currently loaded into memory as FileMediaObject only loads the portions asked for.
     * @return The total size of the file.
     */
    public long getSize() {
        return getMediaProperties().getSize();
    }
    /**
     * Returns the number of buffers needed to read the file into memory. Note that the
     * buffers may not have been read yet.
     *
     * This method is package protected as only the iterator <code>FileMediaObjectIterator</code>
     * should have access to it (and test classes).
     *
     * @return  The number of buffers needed to read the file inte memory.
     *
     */
    long getNrOfBuffers() {
        return nrOfBuffers;
    }
    /**
     * Returns the <code>ByteBuffer</code> at the position given by the
     * parameter <code>index</code.
     * <p>
     * Reads the buffer from disk if not yet read.
     *
     * This method should only be called by a FileMediaIterator returned
     * from the iterator method.
     *
     * Index starts with 0 and goes to nrOfBuffers-1.
     *
     * The method is thread-safe as many readers can request the same buffer
     * simultaneously.
     *
     * This method is package protected as only the iterator <code>FileMediaObjectIterator</code>
     * should have access to it (and test classes).
     *
     * @param index     The index of the byte buffer to read.
     *
     * @return          The ByteBuffer at the index given.
     *
     * @throws IllegalArgumentException if the index given
     *                                  is out of bounds.
     *
     * @throws MediaObjectException     If failed to read from file.
     */
    ByteBuffer getBuffer(int index ) throws MediaObjectException {
        if (index < 0) {
             throw new IllegalArgumentException("Index must be greater than 0");
        } else if (index > (nrOfBuffers-1)) {
            throw new IllegalArgumentException(
                    "Index to high. Requested index="+index+", nrOfBuffers="+nrOfBuffers);
        }

        // First check to see if it exist!! NOTE: It is intentional to have this
        // check outside the synchronized block as it will be the most common path executed
        ByteBuffer byteBuffer;
        synchronized (LOCK) {
            byteBuffer = BYTEBUFFER_LIST.get(index);
        }
        if (byteBuffer != null) {
            return byteBuffer;
        } else {
            synchronized (LOCK) {
                try {
                    // Ok, it seems as we are the first one requesting this index,
                    // but we must check again as another thread may have run this
                    // synchronized block before us.
                    byteBuffer = BYTEBUFFER_LIST.get(index);
                    if (byteBuffer == null) {
                        // Need to read the buffer from disk
                        FileChannel fileChannel = getFileChannel();
                        try {
                            long position = index*bufSize;
                            long readSize;
                            if (position+bufSize > size) {
                               readSize = size-position;
                            } else {
                               readSize = bufSize;
                            }
//                            LOGGER.debug("Thread:" + Thread.currentThread().getName()+
//                                    " Mapping buffer:"+index+", readSize:" +readSize+
//                                    " bytes at position "+position+", filesize="+size);

                            byteBuffer = ByteBuffer.allocateDirect((int)readSize);
                                                
                            if (fileChannel.read(byteBuffer, position) != readSize) {
                            	throw new MediaObjectException(
                        			"File has changed on disc since its size was read:"+file.getAbsolutePath()
                                    );
                            }
                        
                            byteBuffer.flip();

                            BYTEBUFFER_LIST.set(index, byteBuffer);
                        } finally {
                            fileChannel.close();
                        }
                    }
                    return byteBuffer;

                } catch (IOException e) {
                    throw new MediaObjectException(
                            "Failed to read region of file:"+file.getAbsolutePath()+
                            ". index="+index+
                            ", position="+index*bufSize+
                            ", size="+bufSize, e);
                }
            }
        }
    }

    /**
     * Inits the reading from file. Check if this is the first read, reads the
     * size of the file and calculates the member nrOfBuffers accordingly.
     * Also fills the byte buffer list with null so that the size matches
     * the nrOfBuffers.
     *
     * @throws MediaObjectException If failed to read size from file or if read size is zero.
     *
     */
    private void initReading() throws MediaObjectException {

        FileChannel fileChannel = getFileChannel();
        try {
            size =  fileChannel.size();
            // update size in properties
            getMediaProperties().setSize(size);
            fileChannel.close();
            // adjust bufSize 
            if (bufSize > size) {
                bufSize = size;
            }
        } catch (IOException e1) {
            LOGGER.debug("Failed to read the size from underlying file "
                    +file.getAbsolutePath()
                    +" for FileMediaObject.");
            throw new MediaObjectException(
                    "Failed to read the size from underlying file "
                    +file.getAbsolutePath()
                    +" for FileMediaObject.", e1);
        }
        nrOfBuffers = size/bufSize + ((size % bufSize) > 0? 1 : 0);
        for (int i = 0; i < nrOfBuffers; i++) {
              BYTEBUFFER_LIST.add(null);
        }
    }

    /**
     * Creates a FileChannel for the member file.
     * @return A new FileChannel for the member file.
     * @throws MediaObjectException If the file does not exist.
     */
    private FileChannel getFileChannel() throws MediaObjectException {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return fileInputStream.getChannel();

        } catch (FileNotFoundException e) {
            if(LOGGER.isDebugEnabled())
            LOGGER.debug("The underlying file "+file.getAbsolutePath() +
                    " for FileMediaObject does not exist even though it did so when this object was created." +
                    "Could it have been removed under runtime?");
            throw new MediaObjectException("The underlying file "+file.getAbsolutePath() +
                    " for FileMediaObject does not exist even though it did so when this object was created." +
                    "Could it have been removed under runtime?", e);
        }
    }
}
