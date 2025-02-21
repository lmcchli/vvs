/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "wavparser.h"

#include "platform.h"
#include "jlogger.h"
#include "jniutil.h"
#include "wavinfo.h"
#include "wavreader.h"
#include "medialibraryexception.h"
#include "rtpblockhandler.h"

#include <iostream>
#include <sstream>
#include <stdexcept>

using namespace std;

const char* WavParser::WAV_CLASSNAME = "masjni.medialibrary.WavParser";

WavParser::WavParser(java::MediaObject* mediaObject) :
        MediaParser(mediaObject), mPacketSize(0), mPacketCount(0), mWavReader(new WavReader(mediaObject)),
        mCursor(0), mDataChunkSize(0), mDataChunkStartPosition(0), mIsInitiated(false)
{
    JLogger::jniLogDebug(m_env, WAV_CLASSNAME, "WavParser - create at %#x", this);
}

WavParser::~WavParser()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(m_env), WAV_CLASSNAME,
            "WavParser - delete at %#x", this);
}

void WavParser::init()
{
    mIsInitiated = true;
    mWavReader->init();
}

const unsigned char* WavParser::getAudioChunk(unsigned& actualSize, int chunkIndex)
{
    if (!mIsInitiated) {
        JLogger::jniLogError(m_env, WAV_CLASSNAME,
                "Method init is not called for this WavParser instance.");
        throw logic_error("Method init is not called for this WavParser instance.");
    }

    int startPosition(mDataChunkStartPosition + mCursor + chunkIndex * mPacketSize);

    if ((startPosition < 0) || (startPosition > (int) (mDataChunkStartPosition + mDataChunkSize))) {
        actualSize = 0;
        return 0;
    }

    mWavReader->seek(startPosition);
    const char* buffer(0);
    unsigned long bytesLeft(mDataChunkStartPosition + mDataChunkSize - startPosition);
    if (bytesLeft < mPacketSize) {
        // In this operation the end of the data chunk will
        // be reached, only read until the end
        buffer = mWavReader->getData(bytesLeft, actualSize);
    } else {
        buffer = mWavReader->getData(mPacketSize, actualSize);
    }
    return (const unsigned char *) buffer;
}

bool WavParser::parse()
{
    if (!mIsInitiated) {
        JLogger::jniLogError(m_env, WAV_CLASSNAME, "Method init is not called for this WavParser instance.");
        throw logic_error("Method init is not called for this WavParser instance.");
    }

    if (!(mWavReader->compareChunkId("RIFF", true))) {
        // TODO replace when loggin exist
        JLogger::jniLogError(m_env, WAV_CLASSNAME, "WavParser::parse() WAVE header not found");
        throw MediaLibraryException("Could not find RIFF chunk in WAVE file");
    }

    mWavInfo.setRiffLength(mWavReader->getRiffSize());
    //std::cout << "WavParser::parser:  " << "riff length=" << mWavInfo.getRiffLength() <<std::endl;
    // Find and parse chunks
    parseFmtChunk();
    // NOTE the fact chunk is not parsed as it is not(?) needed in MAS
    parseDataChunk();

    // Jump past the id and size information to set the location as specified by the parse function
    mWavReader->jumpForward(8);

    mPacketSize = mWavInfo.getAudioSampleRate() / 1000 * m_pTime;

    JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "SAMPLERATE in wav-file=%d. Packet size=%d",
            mWavInfo.getAudioSampleRate(), mPacketSize);

    // Calculate the number of packets.
    // First, adjust the size according to cursor.
    size_t octetsAfterCursor(mDataChunkSize - mCursor);
    if ((octetsAfterCursor % mPacketSize) == 0) {
        mPacketCount = octetsAfterCursor / mPacketSize;
    } else {
        mPacketCount = (octetsAfterCursor / mPacketSize) + 1;
    }

    return true;
}

const base::String WavParser::getAudioCodec()
{
    switch (mWavInfo.getCompressionCode())
    {
    case CompressionCode::ALAW:
        return "PCMA";
    case CompressionCode::ULAW:
        return "PCMU";
    default:
        return "UNKNOWN";
    }
}

int WavParser::getAudioChunkCount()
{
    return mPacketCount;
}

const WavInfo&
WavParser::getMediaInfo()
{
    return mWavInfo;
}

unsigned WavParser::getAudioBlockSize()
{
    return mPacketCount * mPacketSize;
}

unsigned WavParser::getAudioPacketCount()
{
    return mPacketCount;
}

unsigned WavParser::getVideoBlockSize()
{
    return 0;
}

unsigned WavParser::getVideoPacketCount()
{
    return 0;
}

void WavParser::getData(RtpBlockHandler& blockHandler)
{
    // The payload size is the number of bytes transferred in one payload frame
    // payloadSize = pTime*frequency = { ms*kbyte/s = byte}
    // Typically the pTime is 20ms and the PCMU sampling frequence is 8Khz
    // Hence 20*8 yeld a 160 byte payload size.
    const unsigned payloadSize(m_pTime * 8);
    unsigned lastPayloadSize = 0;
    for (unsigned index(0); index < getAudioPacketCount(); index++) {
        unsigned dataLength(payloadSize);
        const char* data((const char*) getAudioChunk(dataLength, index));
        if ((index == (getAudioPacketCount() - 1)) && (dataLength < lastPayloadSize)) {
            JLogger::jniLogTrace(m_env, WAV_CLASSNAME,
                    "Packet size in media differs from size based on requested pTime, adjusting Packet size to: %d",
                    payloadSize);
            char* dataAdjusted = new char[lastPayloadSize];
            memcpy(dataAdjusted, data, dataLength);
            memset(dataAdjusted + dataLength, 0xfe, lastPayloadSize - dataLength);
            dataLength = lastPayloadSize;
            //Note: here data (dataAdjusted) has been allowed to change, i.e. has been padded
            blockHandler.addAudioPayload(dataAdjusted, dataLength, lastPayloadSize, dataLength);
            delete[] dataAdjusted;
            dataAdjusted = NULL;
        } else {
            //Note: here data has not been allowed to change
            blockHandler.addAudioPayload(data, dataLength, lastPayloadSize, dataLength);
        }
        lastPayloadSize = dataLength;
    }
}

unsigned WavParser::getDuration()
{
    // Assuming that there is only one big chunk of audio.
    // Calculates the audio duration in milli seconds.
    // n/(s/t) = t*n/s = t (s)
    JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "mDataChunkSize= %d", mDataChunkSize);
    JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "sample rate =   %d", mWavInfo.getAudioSampleRate());

    if (mDataChunkSize <= 0)
        return 0;
    return mDataChunkSize / (mWavInfo.getAudioSampleRate() / 1000);
}

void WavParser::setCursor(long cursor /* milli seconds */)
{
    // Converting cursor to number of octets
    // t*fs = t*8000 = { since t is in ms} = t*8 = cursor*8
    cursor *= 8;
    JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "Set cursor: %d octets", cursor);

    if (cursor < 0) {
        mCursor = 0;
    } else {
        mCursor = ((uint32_t) cursor <= mDataChunkSize) ? (uint32_t) cursor : mDataChunkSize;
    }
    // If a cursor is set, the number of packets must be
    // recalculated.
    size_t octetsAfterCursor(mDataChunkSize - mCursor);
    if ((octetsAfterCursor % mPacketSize) == 0) {
        mPacketCount = octetsAfterCursor / mPacketSize;
    } else {
        mPacketCount = (octetsAfterCursor / mPacketSize) + 1;
    }
}

long WavParser::getCursor()
{
    return mCursor;
}

void WavParser::parseFmtChunk()
{
    JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "--> WavParser::parseFmtChunk()");
    mWavReader->setMark();
    // Find and parse the Format (fmt) chunk
    if (mWavReader->seekChunk("fmt", true)) {
        uint16_t temp16_t;
        uint32_t temp32_t;

        mWavReader->getAlignedChunkSize(temp32_t);
        JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "fmt size=%d", temp32_t);
        // Read fmt header
        mWavReader->setMark();
        mWavReader->jumpForward(8);

        // compression code
        mWavReader->readW(temp16_t);
        JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "compression code=%d", temp16_t);

        switch (temp16_t)
        {
        case 1:
            mWavInfo.setCompressionCode(CompressionCode::PCM);
            break;
        case 6:
            mWavInfo.setCompressionCode(CompressionCode::ALAW);
            break;
        case 7:
            mWavInfo.setCompressionCode(CompressionCode::ULAW);
            break;
        default:
            mWavInfo.setCompressionCode(CompressionCode::UNKNOWN);
        }
        // Num Channels
        mWavReader->readW(temp16_t);
        JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "num of channels=%d", temp16_t);
        mWavInfo.setNumChannels(temp16_t);

        // Sample Rate
        mWavReader->readDW(temp32_t);
        JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "sample rate=%d", temp32_t);
        if (temp32_t > 0)
            mWavInfo.setAudioSampleRate(temp32_t);
        else {
            JLogger::jniLogError(m_env, WAV_CLASSNAME, "Could not get a valid SampleRate from WAVE file");
            throw MediaLibraryException("Could not get a valid SampleRate from WAVE file");
        }

        // Byte Rate
        mWavReader->readDW(temp32_t);
        JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "byte rate=%d", temp32_t);
        mWavInfo.setByteRate(temp32_t);

        // Block Align
        mWavReader->readW(temp16_t);
        JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "block align (bytes/sample)=%d", temp16_t);
        mWavInfo.setBlockAlign(temp16_t);

        // Bits Per Sample
        mWavReader->readW(temp16_t);
        JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "bits per sample=%d", temp16_t);
        mWavInfo.setBitsPerSample(temp16_t);
    } else {
        mWavReader->gotoMark();
        JLogger::jniLogError(m_env, WAV_CLASSNAME, "WavParser::parseFmt() fmt header not found");
        throw MediaLibraryException("Could not find fmt chunk in WAVE file");
    }
    mWavReader->gotoMark();
    JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "<-- WavParser::parseFmtChunk()");
}

void WavParser::parseFactChunk()
{
    mWavReader->setMark();
    // Find and parse the Format (fmt) chunk
    if (mWavReader->seekChunk("fact", true)) {
        uint16_t temp16_t(0);
        uint32_t temp32_t;

        mWavReader->getAlignedChunkSize(temp32_t);
        JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "WavParser::parseFactChunk: fact size=%d",
                temp32_t);

        // Read fact header
        mWavReader->setMark();
        mWavReader->jumpForward(8);

        // Number of samples in data chunk
        mWavReader->readDW(temp32_t);
        JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "WavParser::parseFactChunk: number of samples=%d",
                temp32_t);

        mWavInfo.setNumberOfSamples(temp16_t);

    } else {
        mWavReader->gotoMark();

        JLogger::jniLogError(m_env, WAV_CLASSNAME,
                "WavParser::parseFactChunk() fact header not found");
        throw MediaLibraryException("Could not find fact chunk in WAVE file");
    }
    mWavReader->gotoMark();

}
void WavParser::parseDataChunk()
{
    JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "--> parseDataChunk()");

    // Find and parse the data (data) chunk
    if (mWavReader->seekChunk("data", true)) {
        mWavReader->getChunkSize(mDataChunkSize);
        mWavInfo.setDataChunkSize(mDataChunkSize);
        mDataChunkStartPosition = mWavReader->tell() + 8;
        JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "data chunk size=%d", mWavInfo.getDataChunkSize());
        JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "data chunk start=%d", mDataChunkStartPosition);
    } else {
        JLogger::jniLogError(m_env, WAV_CLASSNAME, "data chunk not found");
        throw MediaLibraryException("WavParser::parse"
                "- Could not find required data chunk");
    }
    JLogger::jniLogTrace(m_env, WAV_CLASSNAME, "<-- parseDataChunk()");
}

const WavReader*
WavParser::getWavReader() const
{
    if (!mIsInitiated) {
        JLogger::jniLogError(m_env, WAV_CLASSNAME, "Method init is not called for this WavParser instance.");
        throw logic_error("Method init is not called for this WavParser instance.");
    }
    return mWavReader.get();
}
