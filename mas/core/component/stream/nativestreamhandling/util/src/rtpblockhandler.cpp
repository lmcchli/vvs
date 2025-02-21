#include <string.h>
#include <stdlib.h>

#include "rtpblockhandler.h"

#include "backtrace.h"
#include "ccrtp/outgoingrtppkt.h"
#include "ccrtp/outgoingrtppktlink.h"
#include "ccrtp/rtppkt.h"

using namespace ost;

const unsigned RtpBlockHandler::m_packageSize = (sizeof(OutgoingRTPPkt) + 4) + (sizeof(OutgoingRTPPktLink) + 4);
const unsigned RtpBlockHandler::m_fixedHeaderSize = RTPPacket::getSizeOfFixedHeader();

boost::thread_specific_ptr<RtpBlockHandler> RtpBlockHandler::s_pseudoSingleton;

RtpBlockHandler::RtpBlockHandler() :
        m_blockStart(NULL), m_audioBlockStart(0), m_audioBlockEnd(0), m_nextAudioPayload(0), m_videoBlockStart(0),
        m_videoBlockEnd(0), m_nextVideoPayload(0), m_heapStart(0), m_heapEnd(0), m_heapFree(0), m_blockEnd(0),
        m_audioPayloadBlockSize(0), m_videoPayloadBlockSize(0), m_heapSize(0), m_nOfAllocated(0), m_nOfDeallocated(0)
{
}

RtpBlockHandler::~RtpBlockHandler()
{
    if (m_blockStart != NULL) {
        delete[] m_blockStart;
        m_blockStart = NULL;
    }
}

// Block handling
// Input
//   nOfAudioPackages    number of audio packages in the media
//   audioDataBlockSize  total amount of audio data
//   nOfVideoPackages    number of video packages in the media
//   videoDataBlockSize  total amount of video data
//                       one RTP packet (typically OutputRtpPkt and OutputRtpPktLink)
void RtpBlockHandler::initialize(unsigned nOfAudioPackages, unsigned audioDataBlockSize, unsigned nOfVideoPackages,
        unsigned videoDataBlockSize)
{
    // The additional 4 is for brute force block allign
    // Calculating required amount of memory (actual data plus header)
    // The audio data contains and packet sizes.
    m_audioPayloadBlockSize = audioDataBlockSize + nOfAudioPackages * (4 + 4 * sizeof(unsigned) + m_fixedHeaderSize);
    unsigned oddity = m_audioPayloadBlockSize % 4;
    if (oddity > 0)
        m_audioPayloadBlockSize += (4 - oddity);

    // The video block and frame times and packet sizes. 
    m_videoPayloadBlockSize += videoDataBlockSize + nOfVideoPackages * (4 + 3 * sizeof(unsigned) + m_fixedHeaderSize);
    // Here is the total amount of issued RTP packages
    unsigned nOfPackages(nOfAudioPackages + nOfVideoPackages);
    // Here is the total amount of memory required for the payload data
    unsigned payloadBlockSize(m_audioPayloadBlockSize + m_videoPayloadBlockSize);
    // Here is the total amount of memory required for the RTP packages
    // The additional '4' is because the allocator reserves 4 chars/allocation for it's own use !
    m_heapSize = nOfPackages * (4 * sizeof(char) + m_packageSize);

    if (m_blockStart != NULL) {
        delete[] m_blockStart;
        m_blockStart = NULL;
    }

    m_blockStart = new char[payloadBlockSize + m_heapSize];

    m_heapStart = m_blockStart;
    m_heapEnd = m_heapStart + m_heapSize;
    m_heapFree = m_heapStart;
    m_blockEnd = m_blockStart + payloadBlockSize + m_heapSize;
    m_audioBlockStart = m_blockStart + m_heapSize;
    m_audioBlockEnd = m_audioBlockStart;
    m_nextAudioPayload = m_audioBlockStart;
    m_videoBlockStart = m_audioBlockStart + m_audioPayloadBlockSize;
    m_videoBlockEnd = m_videoBlockStart;
    m_nextVideoPayload = m_videoBlockStart;
}

void RtpBlockHandler::reset()
{
    s_pseudoSingleton.reset(this);
}

/*
 * The purpose here is to remove the object from the local scope of the 
 * thread.
 */
void RtpBlockHandler::release()
{
    s_pseudoSingleton.release();
}

void RtpBlockHandler::addAudioPayload(const char* payload, unsigned length, unsigned rtpTimestampDelta,
        unsigned timeDelta)
{
    if (length == 0) {
        return;
    }

    unsigned packetLength(length + m_fixedHeaderSize);
    unsigned alignment(0);
    unsigned oddity(packetLength % 4);
    if (oddity > 0)
        alignment = 4 - oddity;
    *m_audioBlockEnd++ = (char) (packetLength >> 24) & 0xff;
    *m_audioBlockEnd++ = (char) (packetLength >> 16) & 0xff;
    *m_audioBlockEnd++ = (char) (packetLength >> 8) & 0xff;
    *m_audioBlockEnd++ = (char) (packetLength >> 0) & 0xff;

    *m_audioBlockEnd++ = (char) (rtpTimestampDelta >> 24) & 0xff;
    *m_audioBlockEnd++ = (char) (rtpTimestampDelta >> 16) & 0xff;
    *m_audioBlockEnd++ = (char) (rtpTimestampDelta >> 8) & 0xff;
    *m_audioBlockEnd++ = (char) (rtpTimestampDelta >> 0) & 0xff;

    *m_audioBlockEnd++ = (char) (timeDelta >> 24) & 0xff;
    *m_audioBlockEnd++ = (char) (timeDelta >> 16) & 0xff;
    *m_audioBlockEnd++ = (char) (timeDelta >> 8) & 0xff;
    *m_audioBlockEnd++ = (char) (timeDelta >> 0) & 0xff;

    *m_audioBlockEnd++ = (char) (alignment >> 24) & 0xff;
    *m_audioBlockEnd++ = (char) (alignment >> 16) & 0xff;
    *m_audioBlockEnd++ = (char) (alignment >> 8) & 0xff;
    *m_audioBlockEnd++ = (char) (alignment >> 0) & 0xff;
    memcpy(m_audioBlockEnd + m_fixedHeaderSize, payload, length);
    m_audioBlockEnd += packetLength + alignment;

    if (m_audioBlockEnd > m_blockEnd) {
       BackTrace::dump();
       abort();
    }
}

bool RtpBlockHandler::getNextAudioPayload(char*& payload, unsigned& length, unsigned& rtpTimestampDelta,
        unsigned& timeDelta)
{
    char* data = (char*) 0;
    unsigned size = (unsigned) 0;
    unsigned alignment = (unsigned) 0;
    rtpTimestampDelta = 0;
    timeDelta = 0;

    if (m_audioBlockEnd != m_nextAudioPayload) {
        size |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 24;
        size |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 16;
        size |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 8;
        size |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 0;

        rtpTimestampDelta |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 24;
        rtpTimestampDelta |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 16;
        rtpTimestampDelta |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 8;
        rtpTimestampDelta |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 0;

        timeDelta |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 24;
        timeDelta |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 16;
        timeDelta |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 8;
        timeDelta |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 0;

        alignment |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 24;
        alignment |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 16;
        alignment |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 8;
        alignment |= (unsigned) ((unsigned char) *m_nextAudioPayload++) << 0;
        data = m_nextAudioPayload;
        m_nextAudioPayload += size + alignment;
    }

    payload = data;
    length = size;

    return !(size == 0 || data == 0);
}

void RtpBlockHandler::addVideoPayload(unsigned frameTime, const char* header, unsigned headerLength, const char* data,
        unsigned dataLength)
{
    unsigned length(m_fixedHeaderSize + headerLength + dataLength);
    unsigned alignment(0);
    unsigned oddity(length % 4);
    if (oddity > 0)
        alignment = 4 - oddity;
    *m_videoBlockEnd++ = (char) (frameTime >> 24) & 0xff;
    *m_videoBlockEnd++ = (char) (frameTime >> 16) & 0xff;
    *m_videoBlockEnd++ = (char) (frameTime >> 8) & 0xff;
    *m_videoBlockEnd++ = (char) (frameTime >> 0) & 0xff;
    *m_videoBlockEnd++ = (char) (length >> 24) & 0xff;
    *m_videoBlockEnd++ = (char) (length >> 16) & 0xff;
    *m_videoBlockEnd++ = (char) (length >> 8) & 0xff;
    *m_videoBlockEnd++ = (char) (length >> 0) & 0xff;
    *m_videoBlockEnd++ = (char) (alignment >> 24) & 0xff;
    *m_videoBlockEnd++ = (char) (alignment >> 16) & 0xff;
    *m_videoBlockEnd++ = (char) (alignment >> 8) & 0xff;
    *m_videoBlockEnd++ = (char) (alignment >> 0) & 0xff;
    memcpy(m_videoBlockEnd + m_fixedHeaderSize, header, headerLength);
    m_videoBlockEnd += headerLength + m_fixedHeaderSize;
    memcpy(m_videoBlockEnd, data, dataLength);
    m_videoBlockEnd += dataLength + alignment;

    if (m_videoBlockEnd > m_blockEnd) {
       BackTrace::dump();
       abort();
    }
}

bool RtpBlockHandler::getNextVideoPayload(unsigned& frameTime, char*& payload, unsigned& length)
{
    char* data = (char*) 0;
    unsigned size = (unsigned) 0;
    unsigned alignment = (unsigned) 0;

    if (m_videoBlockEnd != m_nextVideoPayload) {
        frameTime = (unsigned) ((unsigned char) *m_nextVideoPayload++) << 24;
        frameTime |= (unsigned) ((unsigned char) *m_nextVideoPayload++) << 16;
        frameTime |= (unsigned) ((unsigned char) *m_nextVideoPayload++) << 8;
        frameTime |= (unsigned) ((unsigned char) *m_nextVideoPayload++) << 0;
        size |= (unsigned) ((unsigned char) *m_nextVideoPayload++) << 24;
        size |= (unsigned) ((unsigned char) *m_nextVideoPayload++) << 16;
        size |= (unsigned) ((unsigned char) *m_nextVideoPayload++) << 8;
        size |= (unsigned) ((unsigned char) *m_nextVideoPayload++) << 0;
        alignment |= (unsigned) ((unsigned char) *m_nextVideoPayload++) << 24;
        alignment |= (unsigned) ((unsigned char) *m_nextVideoPayload++) << 16;
        alignment |= (unsigned) ((unsigned char) *m_nextVideoPayload++) << 8;
        alignment |= (unsigned) ((unsigned char) *m_nextVideoPayload++) << 0;
        data = m_nextVideoPayload;
        m_nextVideoPayload += size + alignment;
    }

    payload = data;
    length = size;

    return size != 0 && data != 0;
}

unsigned RtpBlockHandler::peekFrameTime()
{
    unsigned frameTime(0);
    unsigned char* ptr((unsigned char*) m_nextVideoPayload);
    if ((unsigned char*) m_videoBlockEnd != ptr) {
        frameTime = (unsigned) (*ptr++) << 24;
        frameTime |= (unsigned) (*ptr++) << 16;
        frameTime |= (unsigned) (*ptr++) << 8;
        frameTime |= (unsigned) (*ptr++) << 0;
    }
    return frameTime;
}

// Getters
unsigned RtpBlockHandler::getAudioPayloadBlockSize()
{
    return m_audioPayloadBlockSize;
}

unsigned RtpBlockHandler::getVideoPayloadBlockSize()
{
    return m_videoPayloadBlockSize;
}

unsigned RtpBlockHandler::getHeapSize()
{
    return m_heapSize;
}

unsigned RtpBlockHandler::getAllocateCount()
{
    return m_nOfAllocated;
}

unsigned RtpBlockHandler::getDeallocateCount()
{
    return m_nOfDeallocated;
}

unsigned RtpBlockHandler::getFixedHeaderSize()
{
    return m_fixedHeaderSize;
}

bool RtpBlockHandler::isEmpty()
{
    return m_nOfAllocated == m_nOfDeallocated;
}

void RtpBlockHandler::deallocate()
{
    m_nOfDeallocated++;
}

// Global allocator interface for new/delete implementations
void*
RtpBlockHandler::allocate(size_t size)
{
    RtpBlockHandler* instance(s_pseudoSingleton.get());
    char* ptr(0);

    if (instance == NULL) {

        ptr = new char[size + 4];
        *ptr = 1;
    } else {
        ptr = (char*) instance->allocateFragment(size + 4);
        *ptr = 0;
    }

    return (void*) (ptr + 4);
}

void RtpBlockHandler::deallocate(void* fragment)
{
    char* ptr((char*) fragment);
    if (isOnHeap(ptr)) {
        ptr -= 4;
        delete[] ptr;
        ptr = NULL;
    }
}

void RtpBlockHandler::deallocate(void* fragment, size_t size)
{
    char* ptr((char*) fragment);
    RtpBlockHandler* instance(s_pseudoSingleton.get());

    if (!isOnHeap(ptr)) {
        if (instance == NULL) {
            return;
        }

        instance->deallocateFragment((char*) fragment, size);
        if (instance->isEmpty()) {
            s_pseudoSingleton.release();
            s_pseudoSingleton.reset(0);

            delete instance;
            instance = NULL;
        }
    } else {
    }
}

RtpBlockHandler*
RtpBlockHandler::getSingleton()
{
    return s_pseudoSingleton.get();
}

// Private/protected methods
char*
RtpBlockHandler::allocateFragment(unsigned size)
{
    char* data = (char*) 0;

    if (m_heapEnd >= m_heapFree + size) {
        data = m_heapFree;
        m_heapFree += size;
        m_nOfAllocated++;
    }

    return data;
}

void RtpBlockHandler::deallocateFragment(char* fragment, unsigned size)
{
    if (m_heapStart <= fragment && fragment + size <= m_heapEnd) {
        *fragment = 0;
        m_nOfDeallocated++;
    }
}

unsigned RtpBlockHandler::setVideoFrameHeader(unsigned frameTime, unsigned length)
{
    unsigned alignment(0);
    unsigned oddity(length % 4);
    if (oddity > 0)
        alignment = 4 - oddity;
    *m_videoBlockEnd++ = (char) (frameTime >> 24) & 0xff;
    *m_videoBlockEnd++ = (char) (frameTime >> 16) & 0xff;
    *m_videoBlockEnd++ = (char) (frameTime >> 8) & 0xff;
    *m_videoBlockEnd++ = (char) (frameTime >> 0) & 0xff;
    *m_videoBlockEnd++ = (char) (length >> 24) & 0xff;
    *m_videoBlockEnd++ = (char) (length >> 16) & 0xff;
    *m_videoBlockEnd++ = (char) (length >> 8) & 0xff;
    *m_videoBlockEnd++ = (char) (length >> 0) & 0xff;
    *m_videoBlockEnd++ = (char) (alignment >> 24) & 0xff;
    *m_videoBlockEnd++ = (char) (alignment >> 16) & 0xff;
    *m_videoBlockEnd++ = (char) (alignment >> 8) & 0xff;
    *m_videoBlockEnd++ = (char) (alignment >> 0) & 0xff;
    return alignment;
}

bool RtpBlockHandler::isOnHeap(char* ptr)
{
    return *(ptr - 4) == 1;
}
