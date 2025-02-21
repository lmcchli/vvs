/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "mediaobjectwriter.h"

#include "java/mediaobject.h"
#include "jlogger.h"
#include "jniutil.h"

int MediaObjectWriter::DEFAULT_BUFFER_SIZE = 8000;

static const char* CLASSNAME = "masjni.medialibrary.MediaObjectWriter";

using namespace std;
using java::MediaObject;

MediaObjectWriter::MediaObjectWriter(java::MediaObject* mo, size_t size) :
        mBufferSize(size), mMediaObject(mo), mInternalBuffer(NULL), mTotalChunkSize(0)
{
    mInternalBuffer = new uint8[mBufferSize];

    JLogger::jniLogDebug(mo->getJniEnv(), CLASSNAME, "mInternalBuffer - create at %#x - size %d", mInternalBuffer,
            mBufferSize);

    JLogger::jniLogDebug(mo->getJniEnv(), CLASSNAME, "MediaObjectWriter - create at %#x", this);
}

MediaObjectWriter::~MediaObjectWriter()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    JLogger::jniLogDebug(env, CLASSNAME, "~mInternalBuffer - delete at %#x", mInternalBuffer);
    delete[] mInternalBuffer;
    mInternalBuffer = NULL;

    JLogger::jniLogDebug(env, CLASSNAME, "~MediaObjectWriter - delete at %#x", this);
}

bool MediaObjectWriter::open()
{
    mMediaObject->open(true);
    return true;
}

unsigned MediaObjectWriter::write(const char* buffer, unsigned size)
{
    if (mTotalChunkSize + size >= mBufferSize) {
        // It is time to store a new byte buffer
        flush(buffer, size);
    } else {
        // Store this chunk in the internal buffer until the total
        // chunk size is >= the requested buffer size.
        memcpy(mInternalBuffer + mTotalChunkSize, buffer, size);
        mTotalChunkSize += size;
    }
    return size;
}

bool MediaObjectWriter::close()
{
    if (mTotalChunkSize > 0) {
        flush(NULL, 0);
    }
    mMediaObject->close();
    return true;
}

void MediaObjectWriter::flush(const char* latestBuffer, unsigned latestBufferSize)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
    size_t newBufferSize(mTotalChunkSize + latestBufferSize);

    // The append method may return 0 from the java side - so we need to protect this
    // even if we loose the packet.
    uint8* buffer(mMediaObject->append(newBufferSize));
    if (buffer != NULL) {
        size_t offset(0);
        if ((buffer != NULL) && (mTotalChunkSize > 0)) {
            memcpy(buffer, mInternalBuffer, mTotalChunkSize);
            offset = mTotalChunkSize;
        }
        if ((buffer != NULL) && (latestBuffer != NULL)) {
            memcpy(buffer + offset, latestBuffer, latestBufferSize);
        }
    } else {
        JLogger::jniLogError(env, CLASSNAME, "mMediaObject->append returned buffer siez = 0. Cannot save packet.");
    }

    mTotalChunkSize = 0;
}
