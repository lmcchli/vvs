/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MEDIAOBJECTWRITER_H_
#define MEDIAOBJECTWRITER_H_

#include "int.h"
#include "platform.h"
#include "java/mediaobject.h"
#include "jni.h"

#include <list>
#include <memory>
#include <stdexcept>
#include <string.h>

namespace java {
class MediaObject;
};

/**
 * Class that writes data to a MediaObject. 
 * <p>
 * When writing to a media object there are a few things to keep in mind.
 * Each write to a media object involves several JNI-calls. This indicates
 * that it is probably a good idea not to write to often. On the other hand,
 * if always all data is collected into one big buffer there might be problems
 * if the memory becomes fragmented and the media data size is large.
 * <p>
 * This class gives the possibility to specify an approximate size of the 
 * buffers that are added to the media object and hides the details of when
 * the actual writing takes place. Note that the actual size of the buffers
 * stored in the media object might be larger than the specified buffer size
 * depending on the size on the last chunk passed to the <code>write</code>-
 * method.
 * 
 * @author Jorgen Terner
 */
class MEDIALIB_CLASS_EXPORT MediaObjectWriter
{
private:
    /** Default size of each ByteBuffer. */
    static int DEFAULT_BUFFER_SIZE;

    /** The requested size of each byte buffer. */
    size_t mBufferSize;

    /** The MediaObject to write to. */
    java::MediaObject *mMediaObject;

    /** 
     * Internal buffer where data chunks that shall be inserted in the next 
     * byte buffer are stored. 
     */
    uint8* mInternalBuffer;

    /** Total size of all data chunks written to the internal buffer. */
    size_t mTotalChunkSize;

    /**
     * Stores all chunks received so far in the media object.
     * 
     * @param buffer The lastest buffer, <code>NULL</code> if no latest buffer.
     * @param size   Size of <code>buffer</code>, <code>0</code> if
     *               <code>buffer==NULL</code>
     */
    void flush(const char* buffer, unsigned size);

public:
    /**
     * Constructs a writer with the given buffer size. 
     * 
     * @param mediaObject Data destination.
     * @param bufferSize  The size of each byte buffer is greater or equal to
     *                    this size, except for the last byte buffer whose
     *                    size is less than or equal to this size.
     */
    MediaObjectWriter(java::MediaObject *mediaObject, size_t bufferSize = DEFAULT_BUFFER_SIZE);

    /**
     * Destructor.
     */
    virtual ~MediaObjectWriter();

    /**
     * Prepares this instance for writing. Must be called before the
     * <code>write</code>-method.
     * 
     * @return true.
     * 
     * @throws runtime_error If the method failes. This can only happen
     *         if this thread failed to attach itself to the JVM.
     */
    virtual bool open();

    /**
     * Writes the given data to the media object if the target buffer size
     * has been reached, otherwise the data is buffered and written later.
     * 
     * @param buffer Buffer data.
     * @param size   Number of octets in the buffer.
     * 
     * @return Number of octets written.
     * 
     * @throws runtime_error If the <code>open</code>-method has not been
     *         called.
     */
    virtual unsigned write(const char* buffer, unsigned size);

    /**
     * Closes the media object for writing.
     *
     * @return true;
     * 
     * @throws runtime_error If the methods <code>setContentType</code> and
     *         <code>setFileExtension</code> has not been called on the 
     *         media object.
     */
    virtual bool close();
};

#endif /*MEDIAOBJECTWRITER_H_*/
