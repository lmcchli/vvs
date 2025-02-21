/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "rawparser.h"

#include "platform.h"
#include "jlogger.h"
#include "jniutil.h"
#include "mediaobjectreader.h"
#include "java/mediaobject.h"
#include "medialibraryexception.h"

#include <iostream>
#include <sstream>
#include <stdexcept>

using namespace std;

const char* RawParser::CLASSNAME = "masjni.medialibrary.RawParser";

RawParser::RawParser(java::MediaObject* mediaObject, const base::String& codec) :
        MediaParser(mediaObject), m_cursor(0), m_dataChunkSize(0), m_isInitialized(false), m_packetSize(0),
        m_packetCount(0)
{
    m_audioCodec = codec;
    m_reader.reset(new MediaObjectReader(mediaObject, Platform::isBigEndian()));
    if (mediaObject != (java::MediaObject*) 0) {
        m_dataChunkSize = mediaObject->getTotalSize();
    }

    JLogger::jniLogDebug(mediaObject->getJniEnv(), CLASSNAME, "RawParser - create at %#x", this);
}

RawParser::~RawParser()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
            "~RawParser - delete at %#x", this);
}

void RawParser::init()
{
    m_isInitialized = true;
    m_reader->init();
}

unsigned RawParser::getDuration()
{
    // Calculating duration in milliseconds (assuming 8kHz sampling frequency).
    // The sampling frequency is divided by 1000 in order to get samples per 
    // millisecond.
    return m_dataChunkSize / 8;
}

const unsigned char*
RawParser::getAudioChunk(unsigned& actualSize, int chunkIndex)
{
    if (!m_isInitialized) {
        throw logic_error("Method init is not called for this RawParser instance.");
    }

    int startPosition(m_cursor + chunkIndex * m_packetSize);
    if ((startPosition < 0) || (startPosition > (int) m_dataChunkSize)) {
        return (unsigned char*) 0;
    }

    m_reader->seek(startPosition);
    const char* buffer(0);
    if (startPosition + m_packetSize > m_dataChunkSize) {
        // In this operation the end of the data chunk will
        // be reached, only read until the end
        buffer = m_reader->getData(m_dataChunkSize - startPosition, actualSize);
    } else {
        buffer = m_reader->getData(m_packetSize, actualSize);
    }
    return (unsigned char *) buffer;
}

bool RawParser::parse()
{
    JLogger::jniLogTrace(mediaObject->getJniEnv(), CLASSNAME, "--> parse()");
    if (!m_isInitialized) {
        throw logic_error("Method init is not called for this RawParser instance.");
    }
    calculatePacketCount();
    JLogger::jniLogTrace(mediaObject->getJniEnv(), CLASSNAME, "<-- parse()");
    return true;
}

void RawParser::setCursor(long cursor)
{
    if (cursor < 0) {
        m_cursor = 0;
    } else {
        m_cursor = ((uint32_t) cursor <= m_dataChunkSize) ? (uint32_t) cursor : m_dataChunkSize;
    }
    // If a cursor is set, the number of packets must be
    // recalculated.
    calculatePacketCount();
}

long RawParser::getCursor()
{
    return m_cursor;
}

int RawParser::getAudioChunkCount()
{
    return m_packetCount;
}

unsigned RawParser::getAudioBlockSize()
{
    return m_packetCount * m_packetSize;
}

unsigned RawParser::getAudioPacketCount()
{
    return m_packetCount;
}

unsigned RawParser::getVideoBlockSize()
{
    return 0;
}

const base::String RawParser::getAudioCodec()
{
    return m_audioCodec;
}

unsigned RawParser::getVideoPacketCount()
{
    return 0;
}

void RawParser::getData(RtpBlockHandler& blockHandler)
{
}

void RawParser::calculatePacketCount()
{
    JLogger::jniLogTrace(mediaObject->getJniEnv(), CLASSNAME, "--> calculatePacketCount()");

    // Calculating packet size (assuming 8kHz sampling frequency)
    // (sampling frequence)*(packet time) = n/t*t = n
    // The sampling frequency is divided by 1000 in order to get
    // samples per milli second since the packet time is in milli
    // seconds.
    m_packetSize = 8 * m_pTime;

    // First, adjust the size according to cursor.
    size_t octetsAfterCursor(m_dataChunkSize - m_cursor);

    // Calculate packet count.
    m_packetCount = octetsAfterCursor / m_packetSize;

    // Checking if the size is a multiple of the packet size.
    // If not the count is incremented by one in order to
    // handle the remaining data (one additional package).
    if ((octetsAfterCursor % m_packetSize) != 0)
        m_packetCount++;

    JLogger::jniLogTrace(mediaObject->getJniEnv(), CLASSNAME, "Sampling frequency: 8000");
    JLogger::jniLogTrace(mediaObject->getJniEnv(), CLASSNAME, "Packet time: %d", m_pTime);
    JLogger::jniLogTrace(mediaObject->getJniEnv(), CLASSNAME, "Packet size: %d", m_packetSize);
    JLogger::jniLogTrace(mediaObject->getJniEnv(), CLASSNAME, "Data size: %d", m_dataChunkSize);
    JLogger::jniLogTrace(mediaObject->getJniEnv(), CLASSNAME, "Data size (from cursor): %d", octetsAfterCursor);
    JLogger::jniLogTrace(mediaObject->getJniEnv(), CLASSNAME, "Cursor: %d", m_cursor);
    JLogger::jniLogTrace(mediaObject->getJniEnv(), CLASSNAME, "Packet count: %d", m_packetCount);
    JLogger::jniLogTrace(mediaObject->getJniEnv(), CLASSNAME, "<-- calculatePacketCount()");
}
