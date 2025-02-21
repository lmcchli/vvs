/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "mediaobjectreader.h"
#include "byteutilities.h"
#include "medialibraryexception.h"
#include "java/mediaobject.h"
#include "jniutil.h"
#include "jlogger.h"

#include <iostream>

const char* MediaObjectReader::EOB = NULL;

static const char* CLASSNAME = "masjni.medialibrary.MediaObjectReader";

using namespace std;
// Constructor
MediaObjectReader::MediaObjectReader(java::MediaObject *mo, bool sw) :
        mediaObject(mo), swap(sw), mCurrentMediaBufferIndex(0), mpCurrentMediaBuffer(NULL), mpFirstByte(NULL),
        markMediaBufferIndex(0), markBufferIndex(0), markBytesRead(0), markBytesLeft(0), mBytesRead(0), mBytesLeft(0),
        mLastBuffer(NULL), mLastBufferShouldBeDeleted(false), mTotalSize(0), mIsInitiated(false)
{
    JLogger::jniLogDebug(mediaObject->getJniEnv(), CLASSNAME, "MediaObjectReader - create at %#x", this);
}

void MediaObjectReader::init()
{
    if (!mediaObject->isImmutable()) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "MediaObjectReader, Read MediaObject must be immutable");
        throw std::invalid_argument("MediaObjectReader, Read MediaObject must be immutable");
    }
    mIsInitiated = true;

    // Read the first buffer from the MediaObject
    const char* buffer = mediaObject->getData();
    const size_t bufferSize = mediaObject->getSize();
    if (bufferSize <= 0) {

    }

    MediaBuffer* pMediaBuffer = new MediaBuffer(mediaObject->getJniEnv(), buffer, bufferSize, swap);
    JLogger::jniLogDebug(mediaObject->getJniEnv(), CLASSNAME, "pMediaBuffer - create at %#x", pMediaBuffer);
    mediaBuffers.push_back(pMediaBuffer);

    if (!pMediaBuffer->isOk()) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Failed to initialize MediaBuffer!");
    }

    // Set some members
    mCurrentMediaBufferIndex = 0;
    mpCurrentMediaBuffer = pMediaBuffer;
    mpFirstByte = mpCurrentMediaBuffer->firstByte();
    mTotalSize = mediaObject->getTotalSize();
    mBytesRead = 0;
    mBytesLeft = mTotalSize;
}

// Destructor
MediaObjectReader::~MediaObjectReader()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment(mediaObject->getJniEnv());
    MediaBuffer *pMB;
    for (size_t i = 0; i < mediaBuffers.size(); i++) {
        pMB = mediaBuffers.at(i);
        JLogger::jniLogDebug(env, CLASSNAME, "~mediaBuffers.at[%d] - delete at %#x", i, pMB);
        delete pMB;
        pMB = NULL;
    }

    if ((mLastBuffer != NULL) && mLastBufferShouldBeDeleted) {
        JLogger::jniLogDebug(env, CLASSNAME, "~mLastBuffer - delete at %#x", mLastBuffer);
        delete[] mLastBuffer;
        mLastBuffer = NULL;
    }

    JLogger::jniLogDebug(env, CLASSNAME, "~MediaObjectReader - delete at %#x", this);
}

size_t MediaObjectReader::jumpForward(int nrOfByte)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    size_t bytesToJump = nrOfByte;
    size_t totalJumped = 0;
    if (nrOfByte < 0) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME,
                "MediaObjectReader::jumpForward, jumpForward must be called with a non-negative value");
        throw invalid_argument("jumpForward must be called with a non-negative value");
    } else if (bytesToJump == 0) {
        return 0;
    }
    // jump to next buffer until we find the buffer the location is in
    // OR until we reach EOB
    size_t bytesLeftInCurrentBuffer = mpCurrentMediaBuffer->bytesLeft();
    while (bytesLeftInCurrentBuffer <= bytesToJump) {
        if (mCurrentMediaBufferIndex == (mediaBuffers.size() - 1)) {
            // Last buffer
            if (mBytesLeft > bytesLeftInCurrentBuffer) {
                // There are another buffer, so read it
                if (!readNextBuffer()) {
                    JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME,
                            "Failed to read next buffer form MediaObject although there should be one");
                    throw MediaLibraryException(
                            "Failed to read next buffer form MediaObject although there should be one");
                }
            } else {
                // This is the last buffer, so jump to EOB
                bytesToJump = bytesLeftInCurrentBuffer;
                break;
            }
        }
        // GOTO NEXT BUFFER buffer, and make sure it is at the start
        mCurrentMediaBufferIndex++;
        mpCurrentMediaBuffer = mediaBuffers.at(mCurrentMediaBufferIndex);
        mpCurrentMediaBuffer->gotoFirstByte();
        mBytesLeft -= bytesLeftInCurrentBuffer;
        mBytesRead += bytesLeftInCurrentBuffer;
        totalJumped += bytesLeftInCurrentBuffer;
        // we have jumped what was left in last buffer
        bytesToJump -= bytesLeftInCurrentBuffer;
        // Get bytes left in the new buffer
        bytesLeftInCurrentBuffer = mpCurrentMediaBuffer->bytesLeft();
    }
    // update location
    size_t jumpedBytes = mpCurrentMediaBuffer->jumpForward(bytesToJump);
    mBytesLeft -= jumpedBytes;
    mBytesRead += jumpedBytes;
    return jumpedBytes + totalJumped;
}
size_t MediaObjectReader::jumpBackward(int nrOfByte)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    if (nrOfByte < 0) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME,
                "MediaObjectReader::jumpBackward, jumpBackward must be called with a non-negative value");
        throw invalid_argument("jumpBackward must be called with a non-negative value");
    } else if (nrOfByte == 0) {
        return 0;
    }
    size_t bytesToJump = nrOfByte;
    size_t totalJumped = 0;
    size_t currentJump = 0;
    // jump to prior buffer until we find the buffer the location is in
    // OR until we reach the very first buffer
    size_t bytesReadInCurrentBuffer = mpCurrentMediaBuffer->bytesRead();

    //JLogger::jniLogTrace(mediaObject->getJniEnv(), CLASSNAME, "jumpBackward: %d, bytesReadInCurrentBuffer: %d", nrOfByte, bytesReadInCurrentBuffer);
    while ((bytesReadInCurrentBuffer < bytesToJump) && (mCurrentMediaBufferIndex > 0)) {
        // GOTO PRIOR BUFFER buffer, and make sure it is at the last byte
        //JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "nrOfByte: %d, bytesReadInCurrentBuffer:%d", nrOfByte, bytesReadInCurrentBuffer);
        mCurrentMediaBufferIndex--;
        mpCurrentMediaBuffer = mediaBuffers.at(mCurrentMediaBufferIndex);
        mpCurrentMediaBuffer->gotoLastByte();
        // we have jumped what was read in last buffer +1 as we set the location to last byte of
        // buffer before
        currentJump = (bytesReadInCurrentBuffer + 1);
        mBytesLeft += currentJump;
        mBytesRead -= currentJump;
        totalJumped += currentJump;

        bytesToJump -= currentJump;
        // Get bytes left in the new buffer
        bytesReadInCurrentBuffer = mpCurrentMediaBuffer->bytesRead();
    }
    // update location
    size_t jumpedBytes = mpCurrentMediaBuffer->jumpBackward(bytesToJump);
    mBytesLeft += jumpedBytes;
    mBytesRead -= jumpedBytes;
    return jumpedBytes + totalJumped;
}
void MediaObjectReader::reset()
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    mCurrentMediaBufferIndex = 0;
    mpCurrentMediaBuffer = mediaBuffers.at(mCurrentMediaBufferIndex);
    mpCurrentMediaBuffer->gotoFirstByte();
    mBytesRead = 0;
    mBytesLeft = mTotalSize;
}

const char*
MediaObjectReader::readW(uint16_t &uw)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    size_t bytesLeft = mpCurrentMediaBuffer->bytesLeft();
    const char* internalLoc;
    const size_t SIZE_UINT16 = sizeof(uint16_t);
    // Check to see if the word crosses buffers
    if (bytesLeft < SIZE_UINT16) {
        if (mBytesLeft < SIZE_UINT16) {
            // There isn't bytes enough at all!
            // goto EOB
            jumpForward(mBytesLeft);
            return MediaObjectReader::EOB;
        } else {
            // Create a temp array and read into it
            char temp[SIZE_UINT16];
            readInto(temp, SIZE_UINT16);
            ByteUtilities::readW(temp, uw, swap);
            jumpForward(SIZE_UINT16);
        }
    } else {
        internalLoc = mpCurrentMediaBuffer->readW(uw);
        if (internalLoc == MediaBuffer::EOB) {
            // A little tricky: if current buffer at EOB we should go to next buffer, this
            // is easiest accomplished by using the jumpForward method of this class
            mpCurrentMediaBuffer->jumpBackward(SIZE_UINT16);
            jumpForward(SIZE_UINT16);
        } else {
            mBytesLeft -= SIZE_UINT16;
            mBytesRead += SIZE_UINT16;
        }
    }
    if (mpCurrentMediaBuffer->getCurrentLocation() == MediaBuffer::EOB) {
        return MediaObjectReader::EOB;
    } else {
        return mpCurrentMediaBuffer->getCurrentLocation();
    }
}

const char*
MediaObjectReader::readDW(uint32_t &duw)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    const char* internalLoc;
    size_t bytesLeft = mpCurrentMediaBuffer->bytesLeft();
    const size_t SIZE_UINT32 = sizeof(uint32_t);
    // Check to see if the word crosses buffers
    if (bytesLeft < SIZE_UINT32) {
        // Is there bytes enough?
        if (mBytesLeft < SIZE_UINT32) {
            // goto EOB
            jumpForward(mBytesLeft);
            return MediaObjectReader::EOB;
        }

        // Create a temp array and read into it
        char temp[SIZE_UINT32];
        readInto(temp, SIZE_UINT32);
        ByteUtilities::readDW(temp, duw, swap);
        jumpForward(SIZE_UINT32);
    } else {
        internalLoc = mpCurrentMediaBuffer->readDW(duw);
        if (internalLoc == MediaBuffer::EOB) {
            // A little tricky: if current buffer at EOB we should go to next buffer, this
            // is easiest accomplished by using the jumpForward method of this class
            mpCurrentMediaBuffer->jumpBackward(SIZE_UINT32);
            jumpForward(SIZE_UINT32);
        } else {
            mBytesLeft -= SIZE_UINT32;
            mBytesRead += SIZE_UINT32;
        }
    }
    if (mpCurrentMediaBuffer->getCurrentLocation() == MediaBuffer::EOB) {
        return MediaObjectReader::EOB;
    } else {
        return mpCurrentMediaBuffer->getCurrentLocation();
    }
}
const char*
MediaObjectReader::getData(size_t size, unsigned& actualSize)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    if ((mLastBuffer != NULL) && mLastBufferShouldBeDeleted) {
        JLogger::jniLogDebug(mediaObject->getJniEnv(), CLASSNAME, "~mLastBuffer - delete at %p", mLastBuffer);
        delete[] mLastBuffer;
        mLastBuffer = NULL;
    }

    unsigned bytesLeft = mpCurrentMediaBuffer->bytesLeft();
    if (bytesLeft >= size || bytesLeft == mBytesLeft) {
        // The current buffer contains the data OR the current buffer
        // is the last one
        actualSize = (bytesLeft >= size) ? size : bytesLeft;
        // so...no need to cross over to a new buffer
        mLastBufferShouldBeDeleted = false;
        mLastBuffer = getCurrentLocation();
    } else {
        // We need to read data from two or more buffers, allocate space
        // and copy over the requested data.
        char* buffer = new char[size];
        actualSize = readInto(buffer, size);
        if (actualSize == 0) {
            // We are at EOB
            delete[] buffer; // EC200, TR HJ84150
            return MediaObjectReader::EOB;
        }
        mLastBufferShouldBeDeleted = true;
        mLastBuffer = buffer;
    }
    jumpForward(actualSize);
    return mLastBuffer;
}

bool MediaObjectReader::compareStr(const char *str, size_t length, bool ignoreCase)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    if (length == 0) {
        return true;
    }
    if (length > strlen(str)) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME,
                "MediaObjectReader::compareStr, The passed length is longer than the strlen of the passed string");
        throw out_of_range(
                "MediaObjectReader::compareStr, The passed length is longer than the strlen of the passed string");
    }
    // first see if the bytes exist
    if (mBytesLeft < length) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "MediaObjectReader::compareStr, The length is outside MediaObject");
        throw out_of_range("MediaObjectReader::compareStr, The length is outside MediaObject");
    }
    size_t i = 0;
    bool result = true;
    for (i = 0; i < length - 1; i++) {
        if (!compareTo(str[i], ignoreCase)) {
            result = false;
            break;
        }
        // move to next character if not done
        this->jumpForward(1);
    }
    // Last check
    if (result) {
        result = compareTo(str[i], ignoreCase);
    }
    // jumpBack the number of bytes jumped forward
    jumpBackward(i);
    return result;
}

void MediaObjectReader::setMark()
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    // Index the current buffer
    markBufferIndex = mCurrentMediaBufferIndex;
    // ... and get the index of that buffer
    markMediaBufferIndex = mpCurrentMediaBuffer->getCurrentIndex();
    markBytesRead = mBytesRead;
    markBytesLeft = mBytesLeft;
}

size_t MediaObjectReader::seek(size_t position)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    size_t currentPos(mBytesRead);

    if (position < currentPos)
        jumpBackward(currentPos - position);
    else if (position > currentPos)
        jumpForward(position - currentPos);
    return mBytesRead;
}

size_t MediaObjectReader::tell()
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    return mBytesRead;
}

void MediaObjectReader::gotoMark()
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    // update current buffer if different
    if (markBufferIndex != mCurrentMediaBufferIndex) {
        mCurrentMediaBufferIndex = markBufferIndex;
        mpCurrentMediaBuffer = mediaBuffers.at(mCurrentMediaBufferIndex);
    }

    // ... and goto the mark in that buffer
    mpCurrentMediaBuffer->gotoIndex(markMediaBufferIndex);

    mBytesLeft = markBytesLeft;
    mBytesRead = markBytesRead;
}

size_t MediaObjectReader::readInto(char buf[], size_t size)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    const char* pCurChar;
    size_t i = 0;
    size_t toRead = 0;

    // Number of bytes to read
    toRead = (mBytesLeft < size) ? mBytesLeft : size;
    // const char* initPos = getCurrentLocation();  // EC200, TR HJ84150, never used.

    for (i = 0; i < toRead; i++) {
        pCurChar = getCurrentLocation();
        buf[i] = *pCurChar;
        jumpForward(1);
        //cout << "bytesJumped:" << bytesJumped << endl;
    }
    jumpBackward(toRead);
    return toRead;
}

bool MediaObjectReader::readNextBuffer()
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    bool hasMore = mediaObject->readNextBuffer();
    if (!hasMore) {
        return false;
    }
    const char* buffer = mediaObject->getData();
    const size_t bufferSize = mediaObject->getSize();
    MediaBuffer *mediaBuffer = new MediaBuffer(mediaObject->getJniEnv(), buffer, bufferSize, swap);
    mediaBuffers.push_back(mediaBuffer);
    return true;
}

bool MediaObjectReader::compareTo(const char c, bool ignoreCase)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    const char *pCurChar = this->getCurrentLocation();

    if (pCurChar == MediaObjectReader::EOB) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "MediaObjectReader::compareTo: End of buffer is reached!");
        throw out_of_range("MediaObjectReader::compareTo: End of buffer is reached!");
    }

    if (ignoreCase) {
        if (tolower(*pCurChar) != tolower(c)) {
            return false;
        }
    } else {
        if (*pCurChar != c) {
            return false;
        }
    }
    return true;
}

const char*
MediaObjectReader::getCurrentLocation()
{
    if (!mIsInitiated) {
        JLogger::jniLogError(mediaObject->getJniEnv(), CLASSNAME, "Method init is not called for this MediaObjectReader instance.");
        throw std::logic_error("Method init is not called for this MediaObjectReader instance.");
    }

    const char* loc = mpCurrentMediaBuffer->getCurrentLocation();
    if (loc == MediaBuffer::EOB) {
        return MediaObjectReader::EOB;
    } else {
        return loc;
    }
}

const char*
MediaObjectReader::firstByte() const
{
    return mpFirstByte;
}

const size_t MediaObjectReader::bytesRead() const
{
    return mBytesRead;
}

const size_t MediaObjectReader::bytesLeft() const
{
    return mBytesLeft;
}

long MediaObjectReader::getTotalSize() const
{
    return mTotalSize;
}

MediaBuffer*
MediaObjectReader::getCurrentMediaBuffer() const
{
    return mpCurrentMediaBuffer;
    //return mediaBuffers.at(mCurrentMediaBufferIndex);
}

