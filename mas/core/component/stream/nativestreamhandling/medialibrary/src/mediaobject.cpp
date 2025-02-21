/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "java/mediaobject.h"
#include "jniutil.h"
#include "jnimediaobject.h"
#include "jlogger.h"

#include <stdexcept> 
#include <iostream> 

using namespace std;

static const char* CLASSNAME = "masjni.medialibrary.MediaObject";

using namespace java;

MediaObject::MediaObject(JNIEnv* env, jobject mediaObject) :
        mMediaObject(NULL), mIterator(NULL), mContentType(NULL), mFileExtension(""),
        mContentTypeStr(""), mIsContentTypeSpecified(false), mBuffer(NULL), mSize(0), mMediaLengthMs(0),
        mIsImmutable(false), mForWriting(false), mSessionJniEnv(env)
{
    // Create a Global reference to the MediaObject
    // This is to prevent the garbage collector from destroying the 
    // MediaObject before we are done
    mMediaObject = env->NewGlobalRef(mediaObject);
    JLogger::jniLogDebug(env, CLASSNAME, "MediaObject - create at %#x", mMediaObject);

    // Most data is only available for an immutable media object, 
    // the file extension and content type, however, might already be set.
    // getMediaProperties
    jobject mediaProp = JNIUtil::callObjectMethod(env, mMediaObject, JNIMediaObject::getGetMediaPropertiesMID());
    JNIUtil::checkException(env, JNIMediaObject::GET_MEDIA_PROPERTIES_METHOD, true);

    // Call getFileExtension
    jstring fileExt((jstring) JNIUtil::callObjectMethod(env, mediaProp, JNIMediaObject::getGetFileExtensionMID()));
    JNIUtil::checkException(env, JNIMediaObject::GET_FILE_EXTENSION_METHOD, true);

    if (fileExt != NULL) {
        const char* fileExtStr(env->GetStringUTFChars(fileExt, 0));
        mFileExtension = fileExtStr;
        env->ReleaseStringUTFChars(fileExt, fileExtStr);
        JNIUtil::deleteLocalRef(env, (jobject) fileExt);
    }

    // Call getContentType
    jobject cType = JNIUtil::callObjectMethod(env, mediaProp, JNIMediaObject::getGetContentTypeMID());
    JNIUtil::checkException(env, JNIMediaObject::GET_CONTENT_TYPE_METHOD, true);

    if (cType != NULL) {
        mIsContentTypeSpecified = true;
        // Call toString
        jstring cTypeStrObj((jstring) JNIUtil::callObjectMethod(env, cType, JNIMediaObject::getToStringMID()));
        JNIUtil::checkException(env, JNIMediaObject::TO_STRING_METHOD, true);
        const char* cTypeStr(env->GetStringUTFChars(cTypeStrObj, 0));
        mContentTypeStr = cTypeStr;
        env->ReleaseStringUTFChars(cTypeStrObj, cTypeStr);

        JNIUtil::deleteLocalRef(env, cType);
        JNIUtil::deleteLocalRef(env, (jobject) cTypeStrObj);
    }

    // Check if the Java object is immutable
    mIsImmutable = (JNI_TRUE == JNIUtil::callBooleanMethod(env, mediaObject, JNIMediaObject::getIsImmutableMID()));
    JNIUtil::checkException(env, JNIMediaObject::IS_IMMUTABLE_METHOD, true);

    // Free local ref on media prop
    JNIUtil::deleteLocalRef(env, mediaProp);

    // If immutable, there is data to be read. Preload the first buffer.
    if (mIsImmutable) {
        try {
            initiateImmutable(env, true);
        } catch (exception& e) {
            JLogger::jniLogError(env, CLASSNAME, "%s", e.what());
            return;
        }

        if (!mIsContentTypeSpecified) {
            JLogger::jniLogError(env, CLASSNAME, "No content type set in media object.");
            throw runtime_error("No content type set in media object.");
        }
    }

    JLogger::jniLogDebug(env, CLASSNAME, "MediaObject - create at %#x", this);
}

bool MediaObject::readNextBuffer(JNIEnv* env, bool shouldThrowJavaExceptionOnFailure)
{
    bool ret = false;

    // This section calls Java methods via JNI
    try {
        // IMediaObject.iterator().hasNext()
        jboolean hasNext = JNIUtil::callBooleanMethod(env, mIterator, JNIMediaObject::getHasNextMID());
        JNIUtil::checkException(env, JNIMediaObject::HAS_NEXT_METHOD, shouldThrowJavaExceptionOnFailure);

        if (hasNext == JNI_TRUE) {
            // IMediaObject.iterator().next()
            jobject dataBuf = JNIUtil::callObjectMethod(env, mIterator, JNIMediaObject::getNextMID());
            JNIUtil::checkException(env, JNIMediaObject::NEXT_METHOD, shouldThrowJavaExceptionOnFailure);

            // ByteBuffer.limit()
            // Cast address to a NIO ByteBuffer to a const char*
            mSize = (size_t) JNIUtil::callIntMethod(env, dataBuf, JNIMediaObject::getLimitMID());
            JNIUtil::checkException(env, JNIMediaObject::LIMIT_METHOD, shouldThrowJavaExceptionOnFailure);

            mBuffer = (const char*) env->GetDirectBufferAddress(dataBuf);

            JNIUtil::deleteLocalRef(env, dataBuf);
            ret = true;
        } else {
            mSize = 0;
            mBuffer = NULL;
        }
    } catch (exception& e) {
        mSize = 0;
        mBuffer = NULL;

        JLogger::jniLogError(env, CLASSNAME, "%s", e.what());
        throw;
    }

    return ret;
}

bool MediaObject::readNextBuffer()
{
    if (!mIsImmutable) {
        throw runtime_error("Cannot readNextBuffer on a mutable object.");
    }

    // from java or from native side
    JNIEnv* env = JNIUtil::getJavaEnvironment(mSessionJniEnv);

    // Read next buffer via JNI    
    bool res(readNextBuffer(env, false));

    return res;
}

void MediaObject::open(bool forWriting)
{
    mForWriting = forWriting;
}

uint8* MediaObject::append(size_t size)
{
    // As the call comes from the native side, retrieve a Java Environment
    // from the current Processor
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (mIsImmutable) {
        JLogger::jniLogError(env, CLASSNAME, "Cannot append to an immutable media object");
        throw runtime_error("Cannot call append on an immutable object.");
    }

    // Call getNativeAccess
    jobject nativeAccess = JNIUtil::callObjectMethod(env, mMediaObject, JNIMediaObject::getGetNativeAccessMID());
    JNIUtil::checkException(env, JNIMediaObject::GET_NATIVE_ACCESS_METHOD, false);

    // Call append
    jobject byteBuffer(JNIUtil::callObjectMethod(env, nativeAccess, JNIMediaObject::getAppendMID(), size));
    JNIUtil::checkException(env, JNIMediaObject::APPEND_METHOD, false);

    uint8* result = (uint8*) env->GetDirectBufferAddress(byteBuffer);

    JNIUtil::deleteLocalRef(env, nativeAccess);
    JNIUtil::deleteLocalRef(env, byteBuffer);

    return result;
}

void MediaObject::close()
{
    // from java or from native side
    JNIEnv* env = JNIUtil::getJavaEnvironment(mSessionJniEnv);

    if (mForWriting) {
        if (!mIsContentTypeSpecified) {
            throw runtime_error("Method setContentType must be called before close");
        }
        if (mFileExtension == "") {
            throw runtime_error("Method setFileExtension must be called before close");
        }

        // getMediaProperties
        jobject mediaProp = JNIUtil::callObjectMethod(env, mMediaObject, JNIMediaObject::getGetMediaPropertiesMID());
        JNIUtil::checkException(env, JNIMediaObject::GET_MEDIA_PROPERTIES_METHOD, false);

        // Call setFileExtension
        jstring fileExtString(env->NewStringUTF(mFileExtension.c_str()));
        JNIUtil::callVoidMethod(env, mediaProp, JNIMediaObject::getSetFileExtensionMID(), fileExtString);
        JNIUtil::checkException(env, JNIMediaObject::SET_FILE_EXTENSION_METHOD, false);

        // Call setContentType
        // Yes, event if mIsContentTypeSpecified is true, mContentType might
        // be NULL. This occurs if the content type is set for a mutable
        // media object sent to a record-operation
        if (mContentType != NULL) {
            JNIUtil::callVoidMethod(env, mediaProp, JNIMediaObject::getSetContentTypeMID(), mContentType);
            JNIUtil::checkException(env, JNIMediaObject::SET_CONTENT_TYPE_METHOD, false);
        }

        JLogger::jniLogTrace(env, CLASSNAME, "setting length to %d", mMediaLengthMs);

        jlong length = mMediaLengthMs;
        jobject unit(env->GetStaticObjectField(JNIMediaObject::getLengthUnitCls(), JNIMediaObject::getMillisecondsFID()));

        jobject mediaLength = JNIUtil::newObject(env, JNIMediaObject::getMediaLengthCls(), JNIMediaObject::getInitMID(), unit, length);
        JNIUtil::checkException(env, JNIMediaObject::MEDIALENGTH_CLASSNAME, false);

        // Call addLength
        JNIUtil::callVoidMethod(env, mediaProp, JNIMediaObject::getAddLengthMID(), mediaLength);
        JNIUtil::checkException(env, JNIMediaObject::ADD_LENGTH_METHOD, false);

        // Call setImmutable
        JNIUtil::callVoidMethod(env, mMediaObject, JNIMediaObject::getSetImmutableMID());
        JNIUtil::checkException(env, JNIMediaObject::SET_IMMUTABLE_METHOD, false);

        mIsImmutable = true;

        initiateImmutable(env, false);
        JNIUtil::deleteLocalRef(env, (jobject) fileExtString);
        JNIUtil::deleteLocalRef(env, mediaProp);
        JNIUtil::deleteLocalRef(env, mediaLength);

        JLogger::jniLogTrace(env, CLASSNAME, "MediaObject set to immutable");
    }
}

MediaObject::~MediaObject()
{
    // from java or from native side
    JNIEnv* env = JNIUtil::getJavaEnvironment(mSessionJniEnv);

    // Remove global references to let JVM to garbage
    // collect MediaObject    
    if (mMediaObject != NULL) {
        JLogger::jniLogDebug(env, CLASSNAME, "~mMediaObject - delete at %#x", mMediaObject);
        JNIUtil::deleteGlobalRef(env, mMediaObject);
    }
    if (mIterator != NULL) {
        JLogger::jniLogDebug(env, CLASSNAME, "~mIterator - delete at %#x", mIterator);
        JNIUtil::deleteGlobalRef(env, mIterator);
    }

    JLogger::jniLogDebug(env, CLASSNAME, "~MediaObject - delete at %#x", this);
}

const char*
MediaObject::getData()
{
    if (mBuffer == NULL) {
        (void) readNextBuffer();
    }
    return mBuffer;
}
void MediaObject::initiateImmutable(JNIEnv* env, bool shouldThrowJavaExceptionOnFailure)
{
    // Perform getNativeAccess
    jobject nativeAccess = JNIUtil::callObjectMethod(env, mMediaObject, JNIMediaObject::getGetNativeAccessMID());
    JNIUtil::checkException(env, JNIMediaObject::GET_NATIVE_ACCESS_METHOD, shouldThrowJavaExceptionOnFailure);

    // Perform the method call (MediaObjectNativeAccess.iterator())        
    jobject iter = JNIUtil::callObjectMethod(env, nativeAccess, JNIMediaObject::getIteratorMID());
    JNIUtil::checkException(env, JNIMediaObject::ITERATOR_METHOD, shouldThrowJavaExceptionOnFailure);

    // Create a Global reference to the iterator
    // This is to prevent the garbage collector from destroying the 
    // MediaObject before we are done
    JNIUtil::deleteGlobalRef(env, mIterator);
    mIterator = env->NewGlobalRef(iter);
    JLogger::jniLogDebug(env, CLASSNAME, "mIterator - create at %#x", mIterator);

    // Retrieve the size from the Java media object
    jlong totalSize = JNIUtil::callLongMethod(env, mMediaObject, JNIMediaObject::getGetSizeMID());
    JNIUtil::checkException(env, JNIMediaObject::GET_SIZE_METHOD, shouldThrowJavaExceptionOnFailure);
    mTotalSize = (long) totalSize;

    JNIUtil::deleteLocalRef(env, iter);
    JNIUtil::deleteLocalRef(env, nativeAccess);
    readNextBuffer(env, shouldThrowJavaExceptionOnFailure);
}

bool MediaObject::equals(MediaObject* mo)
{
    if (mo == NULL) {
        return false;
    }
    return mMediaObject == mo->mMediaObject;
}

size_t MediaObject::getSize()
{
    return mSize;
}

size_t MediaObject::getTotalSize()
{
    return mTotalSize;
}

bool MediaObject::isImmutable()
{
    return mIsImmutable;
}

void MediaObject::setContentType(jobject contentType)
{
    mContentType = contentType;
    mIsContentTypeSpecified = true;
}

void MediaObject::setFileExtension(base::String& fileExtension)
{
    mFileExtension = fileExtension;
}

base::String MediaObject::getContentType() const
{
    return mContentTypeStr;
}

base::String MediaObject::getFileExtension() const
{
    return mFileExtension;
}

void MediaObject::setLength(size_t mediaLengthMs)
{
    mMediaLengthMs = mediaLengthMs;
}
