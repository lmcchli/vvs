/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

#include <cc++/config.h> // F�r att __EXPORT ska vara definierad (Pointer.h)
#include <ccrtp/queuebase.h>

#include "audiomediadata.h"

#include "jlogger.h"
#include "jniutil.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.AudioMediaData";

AudioMediaData::AudioMediaData(JNIEnv* env, const uint8* data, size_t size) :
        mTimestamp(0), mOriginalTimeStamp(0), mChunk(new MovAudioChunk((char*) data, (int) size)), mExtendedSeqNum(0)
{
    JLogger::jniLogDebug(env, CLASSNAME, "AudioMediaData - create at %#x, size %u", this,size);
}

AudioMediaData::AudioMediaData(JNIEnv* env, const uint8* data, size_t size, uint32 timestamp, uint32 extendedSeqNum) :
        mTimestamp(timestamp), mOriginalTimeStamp(0), mChunk(new MovAudioChunk((char*) data, (int) size)),
        mExtendedSeqNum(extendedSeqNum)
{
    JLogger::jniLogDebug(env, CLASSNAME, "AudioMediaData - create at %#x size", this,size);
}

AudioMediaData::AudioMediaData(JNIEnv* env, const uint8* data, size_t size, uint32 timestamp, uint32 originalTimestamp, uint32 extendedSeqNum) :
        mTimestamp(timestamp), mOriginalTimeStamp(originalTimestamp), mChunk(new MovAudioChunk((char*) data, (int) size)),
        mExtendedSeqNum(extendedSeqNum)
{
    JLogger::jniLogDebug(env, CLASSNAME, "AudioMediaData - create at %#x, size", this,size);
}

AudioMediaData::~AudioMediaData()
{
    // do NOT delete - the Processor thread will take care
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~AudioMediaData - delete at %#x", this);
}

void AudioMediaData::skip(size_t amount)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    size_t size = mChunk->getLength();
    if (amount < size) {
        size -= amount;
        uint8* data = new uint8[size];
        memcpy(data, mChunk->getData() + amount, size);
        JLogger::jniLogDebug(env, CLASSNAME, "data - create at %#x - size %d", data, size);

        // Update chunk information
        mChunk->setData((char*) data, (int) size);
    }
}

void AudioMediaData::cut(size_t amount)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    size_t size = mChunk->getLength();
    if (amount < size) {
        size -= amount;
        uint8* data = new uint8[size];
        memcpy(data, mChunk->getData(), size);
        JLogger::jniLogDebug(env, CLASSNAME, "data - create at %#x - size %d", data, size);

        // Update chunk information
        mChunk->setData((char*) data, (int) size);
    }
}

MovAudioChunk* AudioMediaData::releaseAudioChunk()
{
    return mChunk.release();
}

MovAudioChunk& AudioMediaData::getAudioChunk()
{
    return *mChunk;
}

uint32 AudioMediaData::getRTPTimestamp()
{
    if (mOriginalTimeStamp != 0) {
        return mOriginalTimeStamp;
    }
    return mTimestamp;
}

uint32 AudioMediaData::getExtendedSeqNum()
{
    return mExtendedSeqNum;
}
