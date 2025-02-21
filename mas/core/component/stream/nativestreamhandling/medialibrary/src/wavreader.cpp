/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <iostream>
#include <stdexcept>
#include <sstream>

#include "wavreader.h"
#include "platform.h"
#include "byteutilities.h"
#include "jlogger.h"
#include "jniutil.h"
#include "medialibraryexception.h"
#include "java/mediaobject.h"

using namespace std;

static const char* CLASSNAME = "masjni.medialibrary.WavReader";

WavReader::WavReader(java::MediaObject *mediaObject) :
        MediaObjectReader(mediaObject, Platform::isBigEndian()), mRiffSize(0), mIsInitiated(false)
{
    JLogger::jniLogDebug(mediaObject->getJniEnv(), CLASSNAME, "WavReader - create at %#x", this);
}

WavReader::~WavReader()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
            "~WavReader - deleteat %#x", this);
}

void WavReader::init()
{
    mIsInitiated = true;
    MediaObjectReader::init();

    if (!compareChunkId("RIFF", false)) {
        JLogger::jniLogError(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
                "WavReader::WavReader, Illegal type of data in passed MediaObject to WavReader. Not of type WAVE as RIFF chunk not found");

        // Throw MediaLibraryException if the RIFF chunk is not found
        throw MediaLibraryException(
                "WavReader::WavReader, Illegal type of data in passed MediaObject to WavReader. Not of type WAVE as RIFF chunk not found");
    }
    getChunkSize(mRiffSize);
}

void WavReader::getChunkId(base::String &str)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
                "Method init is not called for this WavReader instance.");

        throw logic_error("Method init is not called for this WavReader instance.");
    }

    char temp[4];
    size_t readBytes = readInto(temp, 4);
    if (readBytes < 4) {
        JLogger::jniLogError(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
                "WavReader::getChunkId: Failed to read the required four bytes for a chunk id. The data of the MediaObject ran out");
        throw out_of_range(
                "WavReader::getChunkId: Failed to read the required four bytes for a chunk id. The data of the MediaObject ran out");
    }
    str.append(temp, 4);
}
bool WavReader::compareChunkId(const char *str, bool ignoreCase)
{

    if (!mIsInitiated) {
        JLogger::jniLogError(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
                "Method init is not called for this WavReader instance.");
        throw logic_error("Method init is not called for this WavReader instance.");
    }
    //cout << "C++ side -> WavReader::compareChunkId, str:"<< str << endl;
    if (strlen(str) > 4) {
        JLogger::jniLogError(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
                "WavReader::compareChunkId: The length of the passed chunk id is greater than 4 bytes");
        throw out_of_range("WavReader::compareChunkId: The length of the passed chunk id is greater than 4 bytes");
    }
    bool result = compareStr(str, strlen(str), ignoreCase);
    return result;

}
void WavReader::getChunkSize(uint32_t &chunkSize)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
                "Method init is not called for this WavReader instance.");
        throw logic_error("Method init is not called for this WavReader instance.");
    }
    //cout << "WavReader::getChunkSize " << endl;
    if (bytesLeft() < 8) {
        JLogger::jniLogError(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
                "WavReader::getChunkSize, there is less than 8 bytes of data left, need 8 to get chunk size");
        throw out_of_range(
                "WavReader::getChunkSize, there is less than 8 bytes of data left, need 8 to get chunk size");
    }
    uint32_t temp32;
    jumpForward(4);
    this->readDW(temp32);
    jumpBackward(8);
    chunkSize = temp32;
}
void WavReader::getAlignedChunkSize(uint32_t &chunkSize)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
                "Method init is not called for this WavReader instance.");
        throw logic_error("Method init is not called for this WavReader instance.");
    }
    uint32_t temp32;
    getChunkSize(temp32);
    if (temp32 % 2 > 0) {
        //         cout << "WavReader::getAlignedChunkSize the size of the chunk "
        //                   << temp32 << "is odd, must word align to" << temp32+1 << endl;
        ByteUtilities::alignDW(temp32);
    }
    chunkSize = temp32;
}

bool WavReader::nextChunk()
{
    size_t jumped;

    if (!mIsInitiated) {
        JLogger::jniLogError(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
                "Method init is not called for this WavReader instance.");
        throw logic_error("Method init is not called for this WavReader instance.");
    }

    if (getCurrentLocation() == MediaObjectReader::EOB) {
        return false;
    }
    // First check to see if we are looking at the beginning of the buffer
    if (this->compareChunkId("RIFF", true)) {
        jumped = jumpForward(12);
        return (jumped == 12);
    }
    // get chunk size  
    uint32_t chunkSize;
    getChunkSize(chunkSize);
    if (chunkSize > bytesLeft()) {
        return false;
    }
    // Must word align if chunkSize is odd
    if (chunkSize % 2 > 0) {
        //        cout << "WavReader::nextChunk the size of the chunk "
        //                  << chunkSize << "is odd, must word align to" << chunkSize+1 << endl;
        chunkSize++;
    }
    uint32_t toJump = chunkSize + 8;
    jumped = jumpForward(toJump);
    // if at EOB return false
    if (getCurrentLocation() == MediaObjectReader::EOB) {
        return false;
    } else {
        return (jumped == toJump);
    }
}

bool WavReader::seekChunk(const char *chunkId, bool ignoreCase)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
                "Method init is not called for this WavReader instance.");
        throw logic_error("Method init is not called for this WavReader instance.");
    }

    if (strlen(chunkId) > 4) {
        JLogger::jniLogError(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
                "WavReader::seekChunk: The length of the passed chunk id is greater than 4 bytes");
        throw out_of_range("WavReader::seekChunk: The length of the passed chunk id is greater than 4 bytes");
    }
    // Remember current location to be able to go back if seek fails
    size_t lastMediaBufferIndex = mCurrentMediaBufferIndex;
    MediaBuffer *pMediaBuffer = getCurrentMediaBuffer();
    size_t internalIndex = pMediaBuffer->getCurrentIndex();
    size_t bytesLeft = mBytesLeft;
    size_t bytesRead = mBytesRead;
    //    cout << "seekCunk, internalIndex:" << internalIndex <<
    //            ", bytesLeft:" << bytesLeft <<
    //            ", bytesRead:" << bytesRead <<
    //            ", bufferIndex:" << lastMediaBufferIndex << endl;
    ostringstream msg;
    bool exception = false;
    // iterate over all chunks with nextChunk...
    try {
        do {
            if (compareChunkId(chunkId, ignoreCase)) {
                return true;
            }
        } while (nextChunk());
    } catch (out_of_range& e) {
        exception = true;
        msg << "WavReader::seekChunk, out_of_range exception thrown when calling WavReader::compareStr method." << endl;
        msg << e.what();
    }
    // Reset the location if not found
    mCurrentMediaBufferIndex = lastMediaBufferIndex;
    mpCurrentMediaBuffer = mediaBuffers.at(mCurrentMediaBufferIndex);
    pMediaBuffer = getCurrentMediaBuffer();

    try {
        pMediaBuffer->gotoIndex(internalIndex);
    } catch (std::invalid_argument &ia) {
        JLogger::jniLogError(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME, ia.what());
        throw;
    }
    mBytesLeft = bytesLeft;
    mBytesRead = bytesRead;
    if (exception) {
    }
    return false;
}
