/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "mediabuffer.h"

#include "int.h"
#include "byteutilities.h"
#include "jlogger.h"
#include "jniutil.h"
#include <iostream>
#include <stdexcept>
#include <assert.h>

static const char* CLASSNAME = "masjni.medialibrary.MediaBuffer";

const char* MediaBuffer::EOB = NULL;

MediaBuffer::MediaBuffer(JNIEnv* env, const char * const pBuf, const size_t s, bool sw) :
        swap(sw), bufferSize(s), mpFirstByte(pBuf), mpLastByte(pBuf + (s - 1)), mEnv(env)
{
    mpLocation = mpFirstByte;
    mark = mpLocation;

    JLogger::jniLogDebug(mEnv, CLASSNAME, "MediaBuffer - create at %#x", this);
}

MediaBuffer::~MediaBuffer()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mEnv), CLASSNAME, "MediaBuffer - delete at %#x", this);
}

bool MediaBuffer::isOk()
{
    return mpLocation != 0;
}

const char*
MediaBuffer::gotoIndex(int index) throw (std::invalid_argument)
{
    if (index < 0) {
        throw std::invalid_argument("MediaBuffer::gotoIndex: index must be equal or greater than 0");
    }
    if ((mpFirstByte + index) > mpLastByte) {
        return MediaBuffer::EOB;
    }
    mpLocation = mpFirstByte + index;
    return mpLocation;
}

const char*
MediaBuffer::readW(uint16_t &uw)
{
    if (bytesLeft() < 2) {
        mpLocation = MediaBuffer::EOB;
        return MediaBuffer::EOB;
    } else {
        ByteUtilities::readW(mpLocation, uw, swap);
        jumpForward(sizeof(uint16_t));
        return mpLocation;
    }
}

const char*
MediaBuffer::readDW(uint32_t &udw)
{
    if (bytesLeft() < 4) {
        mpLocation = MediaBuffer::EOB;
        return MediaBuffer::EOB;
    }
    ByteUtilities::readDW(mpLocation, udw, swap);
    jumpForward(sizeof(uint32_t));
    return mpLocation;
}
size_t MediaBuffer::jumpForward(size_t nrOfBytes)
{
    size_t left = bytesLeft();
    //std::cout << "jumpForward(" << nrOfBytes << "), bytesLeft:" << left << std::endl;
    if (nrOfBytes >= left) {
        mpLocation = MediaBuffer::EOB;
        return left;
    } else {
        mpLocation += nrOfBytes;
        return nrOfBytes;
    }
}
size_t MediaBuffer::jumpBackward(size_t nrOfBytes)
{
    size_t jumpedBytes = 0;
    if (mpLocation == MediaBuffer::EOB) {
        mpLocation = mpLastByte;
        nrOfBytes--;
        jumpedBytes = 1;
    }
    size_t read = bytesRead();
    if (nrOfBytes > read) {
        mpLocation = mpFirstByte;
        return read + jumpedBytes;
    } else {

        mpLocation -= nrOfBytes;
        return nrOfBytes + jumpedBytes;
    }

}

const char*
MediaBuffer::getCurrentLocation() const
{
    return mpLocation;
}

const size_t MediaBuffer::bytesRead() const
{
    if (mpLocation == EOB) {
        return bufferSize;
    } else {
        return mpLocation - mpFirstByte;
    }
}

const size_t MediaBuffer::bytesLeft() const
{
    if (mpLocation == EOB) {
        return 0;
    } else {
        return (mpLastByte - mpLocation) + 1;
    }
}

const char*
MediaBuffer::firstByte() const
{
    return mpFirstByte;
}

const char*
MediaBuffer::lastByte() const
{
    return mpLastByte;
}

const size_t MediaBuffer::getCurrentIndex() const
{
    return bytesRead();
}

size_t MediaBuffer::getBufferSize()
{
    return bufferSize;
}

const char*
MediaBuffer::gotoFirstByte()
{
    mpLocation = mpFirstByte;
    return mpLocation;
}

const char*
MediaBuffer::gotoLastByte()
{
    mpLocation = mpLastByte;
    return mpLocation;
}

const char*
MediaBuffer::gotoEOB()
{
    mpLocation = EOB;
    return EOB;
}
