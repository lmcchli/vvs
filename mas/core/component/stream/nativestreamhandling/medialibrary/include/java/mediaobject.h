/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MEDIAOBJECT_H_
#define MEDIAOBJECT_H_

#include "jni.h"
#include "platform.h"
#include "int.h"

//#include <cc++/config.h>
#include <base_include.h>
#include <memory>

/**
 * Wrapper class for a Java MediaObject that hides the JNI details.
 * 
 * IMPORTANT: Not thread-safe. This class only supports one client. If a Java Media
 * object is to be used by many C++ clients, each client must create it's own instance
 * of this class.
 * 
 * <p>
 * Example of use:
 * <pre>
 *    JNIEnv* env = ...
 *    jobject jMediaObject = ...
 *    MediaObject* mediaObject = new MediaObject(env, jMediaObject);
 * 
 *    const char* data(NULL);
 *    while ((data = mediaObject->getData()) != NULL) {
 *        ...
 *        mediaObject->readNextBuffer();
 *    }
 * </pre>
 * 
 * @author Jorgen Terner
 */
namespace java {
class MediaObject
{
private:
    /** Global reference to the corresponding Java-object. */
    jobject mMediaObject;

    /** Global reference to an iterator over the MediaBuffers. */
    jobject mIterator;

    /** 
     * Media content type. Global reference to a Java instance. 
     * This global reference is not owned by this instance and is
     * thus not deleted by this instance.
     */
    jobject mContentType;

    /** File extension that shall be set in the Java MediaObject. */
    base::String mFileExtension;

    /** String representation of the contentType for an immutable MO. */
    base::String mContentTypeStr;

    /**
     * For an immutable media object, content type should always be specified.
     * For a mutable media object, content type may be specified but is not
     * required. It might for example be specified if the client want to 
     * record audio only on a video-enabled stream.
     */
    bool mIsContentTypeSpecified;

    /** Reference to the current data buffer. */
    const char* mBuffer;

    /** Size of the current data buffer. */
    size_t mSize;

    /** Total size of the data */
    size_t mTotalSize;

    /** Length of media in milliseconds. */
    size_t mMediaLengthMs;

    /** <code>true</code> if the Java media object is immutable. */
    bool mIsImmutable;

    /** 
     * If <code>true</code>, this media object is opened for writing and should
     * be set to immutable when closed.
     */
    bool mForWriting;

    /**
     * Moves to the next data buffer. 
     * 
     * @param env Reference to Java environment.
     * @param shouldThrowJavaExceptionOnFailure If <code>true</code> a failure
     *        will lead to a Java exception. This is only allowed if the call
     *        is made synchronously from Java, otherwise there is no one there
     *        to receive the exception.
     * 
     * @return <code>true</code> if a new data buffer existed. 
     *         <code>false</code> if no more data exists.
     */
    bool readNextBuffer(JNIEnv* env, bool shouldThrowJavaExceptionOnFailure);

    /**
     * Initiates a immutable mediaobject and prepares it 
     * for reading. Will read first buffer from media object.
     * 
     * @param env Reference to Java environment.
     * @param shouldThrowJavaExceptionOnFailure If <code>true</code> a failure
     *        will lead to a Java exception. This is only allowed if the call
     *        is made synchronously from Java, otherwise there is no one there
     *        to receive the exception.
     */
    void initiateImmutable(JNIEnv* env, bool shouldThrowJavaExceptionOnFailure);

    JNIEnv* mSessionJniEnv;

public:
    /**
     * Creates a new MediaObject and tries to read the first buffer of data.
     * The given jobject is cached in a global reference.
     * 
     * @param env                Reference to Java environment.
     * @param mediaObject        Java object containing the data buffers.
     */
    MediaObject(JNIEnv* env, jobject mediaObject);

    /**
     * Deletes the global references to Java objects.
     */
    ~MediaObject();

    /** Gets the current data buffer. Will read first buffer
     * if not read. 
     */
    const char* getData();

    /** Gets the size of the current data buffer. */
    size_t getSize();

    /** Gets the total number of bytes in the Java MediaObject */
    size_t getTotalSize();

    /**
     * Moves to the next data buffer. 
     * 
     * @return <code>true</code> if a new data buffer existed. 
     *         <code>false</code> if no more data exists.
     * 
     * @throws runtime_exception If <code>isImmutable() == false</code>.
     */
    bool readNextBuffer();

    /**
     * Prepares this instance for writing or reading. Must be called before the
     * <code>write</code>-method.
     * 
     * @param forWriting If <code>true</code> the media object is opened
     *        for writing, if <code>false</code> it is opened for reading.
     * 
     * @throws runtime_error If the method failes. This can only happen
     *         if this thread failed to attach itself to the JVM.
     */
    void open(bool forWriting);

    /**
     * Allocates a new Java direct ByteBuffer and appends it to the Java
     * MediaObject.
     * 
     * @param size   Number of octets in the buffer.
     * 
     * @return Buffer.
     * 
     * @throws runtime_error If the <code>open</code>-method has not been
     *         called.
     */
    uint8* append(size_t size);

    /**
     * Sets the represented Java MediaObject to immutable if it was opened
     * for writing.
     *
     * @throws runtime_error If the methods <code>setContentType</code> and
     *         <code>setFileExtension</code> has not been called.
     */
    void close();

    /**
     * Returns whether the underlying Java MediaObject
     * is immutable or not.
     * 
     * @retun true if MediaObject is immutable.
     */
    bool isImmutable();

    /**
     * Sets the string representation of the content type into the
     * given parameter.
     * 
     * @param contentType Content type.
     */
    base::String getContentType() const;

    /**
     * This method should be called once before the <code>close</code>-method
     * if this media object was opened for writing.
     * 
     * @param contentType Content type that shall be set in the 
     *                    Java MediaObject.
     */
    void setContentType(jobject contentType);

    /**
     * Sets the string representation of the file extension into the
     * given parameter.
     * 
     * @param fileExtension File extension.
     */
    base::String getFileExtension() const;

    /**
     * This method should be called once before the <code>close</code>-method
     * if this media object was opened for writing.
     * 
     * @param fileExtension File extension that shall be set in the 
     *                      Java MediaObject.
     */
    void setFileExtension(base::String& fileExtension);

    /**
     * Tests if this instance wraps the same Java Media Object instance
     * as the given instance.
     * 
     * @param mo The other instance.
     * 
     * @return <code>true</code> if this instance wraps the same Java instance
     *         as <code>mo</code>.
     */
    bool equals(MediaObject* mo);

    /**
     * Sets the length of the media in milliseconds. 
     * 
     * @param length Media length in milliseconds.
     */
    void setLength(size_t mediaLengthMs);

    inline JNIEnv* const getJniEnv() const { return mSessionJniEnv;} ;

    inline void resetJniEnv(JNIEnv* env) { mSessionJniEnv = env; return;};
};

};

#endif /*MEDIAOBJECT_H_*/
