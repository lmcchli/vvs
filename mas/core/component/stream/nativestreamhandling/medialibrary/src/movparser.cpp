#include "movparser.h"

#include "AtomName.h"
#include "MoovAtom.h"
#include "TrakAtom.h"

#include "movreader.h"
#include "movtrackinfo.h"
#include "movrtppacketinfo.h"

#include "medialibraryexception.h"
#include "java/mediaobject.h"

#include "rtpblockhandler.h"

#include "jlogger.h"
#include "jniutil.h"

#include <stdio.h>
#include <boost/ptr_container/ptr_list.hpp>

using namespace quicktime;

const char* MovParser::MOV_CLASSNAME = "masjni.medialibrary.MovParser";

struct ImmediateDataMode
{
    unsigned char length;
    char data[14];
};

struct SampleMode
{
    unsigned char trackRefIndex;
    unsigned short length;
    unsigned sampleNumber;
    unsigned offset;
    unsigned short bytesPerCompressionBlock;
    unsigned short samplesPerCompressionBlock;
};

static int load(MovReader* reader, ImmediateDataMode& atom);
static int load(MovReader* reader, SampleMode& atom);

MovParser::MovParser(java::MediaObject* mediaObject) :
        MediaParser(mediaObject), mCursor(0), m_startVideoFrame(0), m_startAudioChunk(0), m_msToSampleIndexFactor(8),
        m_videoBlockSize(0), m_videoPacketCount(0)
{
    m_reader = new MovReader(mediaObject);
    JLogger::jniLogDebug(m_env, MOV_CLASSNAME, "m_reader - create at %#x", m_reader);
    JLogger::jniLogDebug(m_env, MOV_CLASSNAME, "MovParser - create at %#x", this);
}

MovParser::~MovParser()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(m_env), MOV_CLASSNAME,
            "~m_reader - delete at %#x", m_reader);
    delete m_reader;
    m_reader = NULL;
}

void MovParser::init()
{
    m_reader->init();
}

bool MovParser::parse()
{
    unsigned atomId;
    unsigned atomSize;
    AtomReader& atomReader(*m_reader);
    unsigned mediaDataSize;

    while (m_reader->getAtomInformation(atomSize, atomId) != 0) {
        switch (atomId)
        {
        case quicktime::MDAT:
            JLogger::jniLogTrace(m_env, MOV_CLASSNAME, "MDAT %u", atomSize);
            // Store information about the media data area here.
            mediaDataSize = atomSize - 8;
            m_reader->jumpForward(mediaDataSize);
            break;

        case quicktime::MOOV:
            JLogger::jniLogTrace(m_env, MOV_CLASSNAME, "MOOV %u", atomSize);
            m_moovAtom.restoreGuts(atomReader, atomSize);
            break;

        case quicktime::FREE:
            JLogger::jniLogTrace(m_env, MOV_CLASSNAME, "FREE %u", atomSize);
            m_reader->jumpForward(atomSize - 8);
            break;

        case quicktime::SKIP:
            JLogger::jniLogTrace(m_env, MOV_CLASSNAME, "SKIP %u", atomSize);
            m_reader->jumpForward(atomSize - 8);
            break;

        case quicktime::WIDE:
            JLogger::jniLogTrace(m_env, MOV_CLASSNAME, "WIDE %u", atomSize);
            m_reader->jumpForward(atomSize - 8);
            break;

        case quicktime::PNOT:
            JLogger::jniLogTrace(m_env, MOV_CLASSNAME, "PNOT %u", atomSize);
            m_reader->jumpForward(atomSize - 8);
            break;

        default:
            JLogger::jniLogWarn(m_env, MOV_CLASSNAME, "Unrecognized atom. ID: '%c%c%c%c' Size: %u",
                    char(atomId >> 24), char(atomId >> 16), char(atomId >> 8), char(atomId >> 0), atomSize);
            JLogger::jniLogTrace(m_env, MOV_CLASSNAME, "atomId %u atomSize %u", atomId, atomSize);
            return false;
            break;
        }
    }

    m_movInfo.initialize(m_moovAtom);

    if (m_moovAtom.getAudioTrackAtom() != 0) {
        switch (m_moovAtom.getAudioTrackAtom()->getDataFormat())
        {
        case quicktime::ULAW:
            m_audioCodec = "PCMU";
            break;
        case quicktime::ALAW:
            m_audioCodec = "PCMA";
            break;
        default:
            JLogger::jniLogWarn(m_env, MOV_CLASSNAME, "Unknown audio codec in QT audio track");
            m_audioCodec = "UNKNOWN";
            break;
        }
    } else {
        JLogger::jniLogError(m_env, MOV_CLASSNAME,
                "Mandatory audio track is missing in QuickTime media!");
        return false;
    }
    if (m_moovAtom.getVideoTrackAtom() != 0) {
        switch (m_moovAtom.getVideoTrackAtom()->getDataFormat())
        {
        case quicktime::H263:
            m_isVideo = true;
            m_videoCodec = "H263";
            break;

        default:
            break;
        }
    }
    if (m_videoCodec.size() == 0 && m_audioCodec.size() == 0) {
        JLogger::jniLogError(m_env, MOV_CLASSNAME,
                "Could not retrieve any codec information from QuickTime media!");
        return false;
    }
    if (m_isVideo && m_movInfo.getHintTrack()->check()) {
        calculateVideoRtp(m_videoBlockSize, m_videoPacketCount);
    }
    return true;
}

const base::String MovParser::getAudioCodec()
{
    return m_audioCodec;
}

bool MovParser::check()
{
    return m_movInfo.check();
}

// TODO: should also return the delay ...
void MovParser::getFrame(boost::ptr_list<MovRtpPacket>& rtpPackets, int frameIndex)
{
    // Adjust the index according to the one calculated from the cursor.
    int adjustedFrameIndex(m_startVideoFrame + frameIndex);

    MovRtpPacketInfo packetInfo;
    ImmediateDataMode immediateDataMode;
    SampleMode sampleMode;
    char* buffer;
    // Get the offset/start of the hint frame
    int hintFrameStart = m_movInfo.getHintTrack()->getChunkOffset(adjustedFrameIndex);
    int videoFrameStart = m_movInfo.getVideoTrack()->getChunkOffset(adjustedFrameIndex);
    unsigned frameTime = m_movInfo.getVideoTrack()->getFrameTime(adjustedFrameIndex);
    // Set the media pointer to point at the frame start
    m_reader->seek(hintFrameStart);
    unsigned short entryCount;
    // Retrieve Packetization hint sample data
    // Retrieve the packet entry count
    m_reader->readW(entryCount);
    // Skip reserved (short)
    m_reader->jumpForward(2);
    // Retrieve the packet entries
    for (int index(0); index < entryCount; index++) {
         // Retrieve a packet entry
        packetInfo.loadInfo(m_reader);

        // Assuming that there are no Extra information TLVs
        // TODO: ensure that there are no TLVs (check the flag)
        // Retrieve entries from  the data table
        //	printf("Entry count %d\n", packetInfo.entryCount);
        //	if (packetInfo.entryCount > 3) exit(42);
        for (int i(0); i < packetInfo.entryCount; i++) {
            unsigned nOfBytesRead(0);
            char* sample;
            MovRtpPacket* rtpPacket;
            int position;
            // Assuming that there are one immediate and one sample entry
            // for each packet entry.
            buffer = (char*) m_reader->getData(1, nOfBytesRead);
            // Check data source
            switch ((int) *buffer)
            {
            case 0:
                //		printf("No-Op Data Mode\n");
                // No-Op Data Mode
                // Skipping this ...
                break;
            case 1:
                //		printf("Immediate Data Mode\n");
                // Immediate Data Mode
                // According to existing code in mtap this an RTP header
                // We just take care of the immeadiate data and look for the
                // sample data in the next step of the loop.
                load(m_reader, immediateDataMode);
                break;
            case 2:
                //		printf("==> Sample Mode\n");
                // Sample Mode
                // Now we merge the immediate data and the sample data into
                // an RTP package.
                load(m_reader, sampleMode);

                // Copy header to buffer
                rtpPacket = new MovRtpPacket(packetInfo);
                rtpPackets.push_back(rtpPacket);

                memcpy(rtpPacket->getData(), immediateDataMode.data, immediateDataMode.length);
                // Retrieve the video sample data
                position = m_reader->tell();
                m_reader->seek((size_t) videoFrameStart + sampleMode.offset);
                
                sample = (char*) m_reader->getData(sampleMode.length, nOfBytesRead);
                // TODO: improve error handling and logging
                if (sampleMode.length != nOfBytesRead) {
                    JLogger::jniLogWarn(m_env, MOV_CLASSNAME, "Length mismatch: %d:%d",
                            sampleMode.length, nOfBytesRead);
                }

                memcpy(rtpPacket->getData() + immediateDataMode.length, sample, sampleMode.length);
                
                rtpPacket->setLength(immediateDataMode.length + sampleMode.length);
                rtpPacket->setFrameTime(frameTime);
                m_reader->seek((size_t) position);
                
                break;
            case 3:
                //printf("Sample Descriotion Mode\n");
                // Sample Description Mode
                // Skipping this
                break;
            default:
                // Unknown mode ... Should bail out
                // This should actually never happen ;-)
                // TODO: improve error handling and logging
                JLogger::jniLogWarn(m_env, MOV_CLASSNAME, "Unknown RTP data type: %ul",
                        (int) *buffer);
                break;
            }
        }
    }
}

const unsigned char*
MovParser::getAudioChunk(unsigned& chunkSize, int chunkIndex)
{
    MovTrackInfo* audioTrack(m_movInfo.getAudioTrack());

    chunkSize = audioTrack->getSamplesPerChunk(0);
    int adjustedChunkIndex(m_startAudioChunk + chunkIndex);

    // If this is the first packet after the cursor, the first part of this
    // packet should probably be skipped.
    size_t interChunkOffset = (chunkIndex == 0) ? m_msToSampleIndexFactor * mCursor % chunkSize : 0;

    int chunkOffset(audioTrack->getChunkOffset(adjustedChunkIndex));
    m_reader->seek(chunkOffset + interChunkOffset);
    return reinterpret_cast<const unsigned char*>(m_reader->getData(chunkSize - interChunkOffset, chunkSize));
}

unsigned MovParser::getDuration()
{
    return m_moovAtom.getMovieHeaderAtom().getDuration();
}

void MovParser::setCursor(long cursor)
{
    if (cursor < 0) {
        mCursor = 0;
    } else {
        mCursor = cursor;
    }

    if (mCursor > 0) {
        // A cursor can only start at the beginning of
        // an intra frame. The closest intra frame should
        // be choosen.
        m_startVideoFrame = findStartVideoFrame();

        // Calculate index of the first audio packet as well. The first part 
        // of the first packet should probably be skipped as well but that
        // is handled when the audio is read.
        MovTrackInfo* audioTrack(m_movInfo.getAudioTrack());
        size_t chunkSize(audioTrack->getSamplesPerChunk(0));
        m_startAudioChunk = m_msToSampleIndexFactor * mCursor / chunkSize;
    }
}

long MovParser::getCursor()
{
    return mCursor;
}

int MovParser::getAudioChunkCount()
{
    return m_movInfo.getAudioChunkCount() - m_startAudioChunk;
}

int MovParser::getFrameCount()
{
    return m_movInfo.getFrameCount() - m_startVideoFrame;
}

unsigned MovParser::findStartVideoFrame()
{
    // If cursor is > 0 the play shall start from the closest intra frame.
    // This code finds the closest intra frame and updates the cursor
    // value to be equal to the length of video data in milliseconds up to
    // this frame.

    if (mCursor == 0) {
        return 0;
    }

    unsigned totalFrameTime(0);
    int currentVideoFrame(0);
    boost::ptr_list<MovRtpPacket> rtpPackets;
    bool done(false);
    while (!done) {
        getFrame(rtpPackets, currentVideoFrame);
        boost::ptr_list<MovRtpPacket>::iterator iter;

        unsigned currentFrameTime(0);
        for (iter = rtpPackets.begin(); iter != rtpPackets.end(); ++iter) {
            MovRtpPacket& packet = *iter;
            currentFrameTime += packet.getFrameTime();
        }
        rtpPackets.clear();
        if (totalFrameTime + currentFrameTime > mCursor) {
            // We have passed the cursor time. Now, decide which frame
            // is the closest; the current or the next frame.
            if ((mCursor - currentFrameTime) < (totalFrameTime + currentFrameTime - mCursor)) {
                // The current frame is closest
                JLogger::jniLogTrace(m_env, MOV_CLASSNAME, "Current frame is the closest one");
            } else {
                // The next frame is the closest
                currentVideoFrame++;
                totalFrameTime += currentFrameTime;
                JLogger::jniLogTrace(m_env, MOV_CLASSNAME, "Next frame is the closest one");
            }
            done = true;
        } else {
            currentVideoFrame++;
            totalFrameTime += currentFrameTime;
            if (currentVideoFrame >= m_movInfo.getFrameCount()) {
                done = true;
            }
        }
    }
    if (currentVideoFrame < m_movInfo.getFrameCount()) {
        // If the cursor is past all video data there is no need to adjust
        // the cursor to the closest intra frame.
        mCursor = totalFrameTime;
        JLogger::jniLogTrace(m_env, MOV_CLASSNAME,
                "Adjusting cursor to the closest intra frame: index=%d, time=%d", currentVideoFrame, totalFrameTime);
    }
    return currentVideoFrame;
}

void MovParser::calculateVideoRtp(unsigned& blockSize, unsigned& packetCount)
{
    blockSize = 0;
    packetCount = 0;

    MovRtpPacketInfo packetInfo;
    ImmediateDataMode immediateDataMode;
    SampleMode sampleMode;
    char* buffer;
    const unsigned nOfFrames(m_movInfo.getFrameCount());

    for (unsigned frameIndex(0); frameIndex < nOfFrames; frameIndex++) {
        // Get the offset/start of the hint frame
        int hintFrameStart = m_movInfo.getHintTrack()->getChunkOffset(frameIndex);
        // Set the media pointer to point at the frame start
        m_reader->seek(hintFrameStart);
        unsigned short entryCount;
        // Retrieve Packetization hint sample data
        // Retrieve the packet entry count
        m_reader->readW(entryCount);
        // Skip reserved (short)
        m_reader->jumpForward(2);
        // Retrieve the packet entries
        for (int index(0); index < entryCount; index++) {
            unsigned payloadSize(0);
            // Retrieve a packet entry
            packetInfo.loadInfo(m_reader);

            // Assuming that there are no Extra information TLVs
            // TODO: ensure that there are no TLVs (check the flag)
            // Retrieve entries from  the data table
            for (int i(0); i < packetInfo.entryCount; i++) {
                unsigned nOfBytesRead(0);
                // Assuming that there are one immediate and one sample entry
                // for each packet entry.
                buffer = (char*) m_reader->getData(1, nOfBytesRead);
                // Check data source
                switch ((int) *buffer)
                {
                case 1: // Immediate Data Mode
                    // According to existing code in mtap this an RTP header
                    // We just take care of the immeadiate data and look for the
                    // sample data in the next step of the loop.
                    load(m_reader, immediateDataMode);
                    break;
                case 2: // Sample Mode
                    // Now we merge the immediate data and the sample data into
                    // an RTP package.
                    load(m_reader, sampleMode);
                    // Calculating packet size in RTP Block Handler
                    payloadSize = immediateDataMode.length + sampleMode.length;
                    blockSize += payloadSize;
                    packetCount++;
                    break;
                default: // Unknown mode ... Should bail out
                    // This should actually never happen ;-)
                    // TODO: improve error handling and logging
                    JLogger::jniLogWarn(m_env, MOV_CLASSNAME, "Handled RTP data type: %ul",
                            (int) *buffer);
                    break;
                }
            }
        }
    }
}

void MovParser::collectVideoRtp(RtpBlockHandler& blockHandler)
{
    MovRtpPacketInfo packetInfo;
    ImmediateDataMode immediateDataMode;
    SampleMode sampleMode;
    char* buffer;
    const unsigned nOfFrames(m_movInfo.getFrameCount());

    JLogger::jniLogTrace(m_env, MOV_CLASSNAME, "Number of video frames: %d", nOfFrames);

    for (unsigned frameIndex(0); frameIndex < nOfFrames; frameIndex++) {
        // Get the offset/start of the hint frame
        int hintFrameStart = m_movInfo.getHintTrack()->getChunkOffset(frameIndex);
        int videoFrameStart = m_movInfo.getVideoTrack()->getChunkOffset(frameIndex);
        unsigned frameTime = m_movInfo.getVideoTrack()->getFrameTime(frameIndex);
        // Set the media pointer to point at the frame start
        m_reader->seek(hintFrameStart);
        unsigned short entryCount;
        // Retrieve Packetization hint sample data
        // Retrieve the packet entry count
        m_reader->readW(entryCount);
        // Skip reserved (short)
        m_reader->jumpForward(2);
        // Retrieve the packet entries
        for (int index(0); index < entryCount; index++) {
            // Retrieve a packet entry
            packetInfo.loadInfo(m_reader);

            // Assuming that there are no Extra information TLVs
            // TODO: ensure that there are no TLVs (check the flag)
            // Retrieve entries from  the data table
            for (int i(0); i < packetInfo.entryCount; i++) {
                unsigned nOfBytesRead(0);
                char* sample;
                unsigned position;

                // Assuming that there are one immediate and one sample entry
                // for each packet entry.
                buffer = (char*) m_reader->getData(1, nOfBytesRead);
                // Check data source
                switch ((int) *buffer)
                {
                case 1: // Immediate Data Mode
                    // According to existing code in mtap this an RTP header
                    // We just take care of the immeadiate data and look for the
                    // sample data in the next step of the loop.
                    load(m_reader, immediateDataMode);
                    break;
                case 2: // Sample Mode
                    // Now we merge the immediate data and the sample data into
                    // an RTP package.
                    load(m_reader, sampleMode);
                    // Calculating packet size in RTP Block Handler
                    // Retrieve the video sample data
                    position = m_reader->tell();
                    m_reader->seek((size_t) videoFrameStart + sampleMode.offset);
                    sample = (char*) m_reader->getData(sampleMode.length, nOfBytesRead);
                    // TODO: improve error handling and logging
                    if (sampleMode.length != nOfBytesRead) {
                        JLogger::jniLogWarn(m_env, MOV_CLASSNAME, "Length mismatch: %d:%d",
                                sampleMode.length, nOfBytesRead);
                    }
                    blockHandler.addVideoPayload(index + 1 == entryCount ? frameTime : 0, immediateDataMode.data,
                            immediateDataMode.length, sample, sampleMode.length);
                    m_reader->seek((size_t) position);
                    break;
                default: // Unknown mode ... Should bail out
                    // This should actually never happen ;-)
                    // TODO: improve error handling and logging
                    JLogger::jniLogWarn(m_env, MOV_CLASSNAME, "Handled RTP data type: %ul",
                            (int) *buffer);
                    break;
                }
            }
        }
    }
}

unsigned MovParser::getAudioStartTimeOffset()
{
    TrakAtom* track(m_moovAtom.getAudioTrackAtom());

    // Handling null pointer
    if (track == 0)
        return 0;
    // Returning the requested value.
    return track->getStartTimeOffset();
}

unsigned MovParser::getVideoStartTimeOffset()
{
    TrakAtom* track(m_moovAtom.getVideoTrackAtom());

    // Handling null pointer
    if (track == 0)
        return 0;
    // Returning the requested value.
    return track->getStartTimeOffset();
}

unsigned MovParser::getAudioBlockSize()
{
    MovTrackInfo* audioTrack(m_movInfo.getAudioTrack());

    if (audioTrack == 0) {
        JLogger::jniLogError(m_env, MOV_CLASSNAME, "Accessing a NULL audio track");
        return 0;
    }

    return audioTrack->getSamplesPerChunk(0) * audioTrack->getChunkOffsetCount();
}

unsigned MovParser::getAudioPacketCount()
{
    MovTrackInfo* audioTrack(m_movInfo.getAudioTrack());

    if (audioTrack == 0) {
        JLogger::jniLogError(m_env, MOV_CLASSNAME, "Accessing a NULL audio track");
        return 0;
    }

    return audioTrack->getChunkOffsetCount();
}

unsigned MovParser::getVideoBlockSize()
{
    return m_videoBlockSize;
}

unsigned MovParser::getVideoPacketCount()
{
    return m_videoPacketCount;
}

void MovParser::getData(RtpBlockHandler& blockHandler)
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
            JLogger::jniLogTrace(m_env, MOV_CLASSNAME,
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
    collectVideoRtp(blockHandler);
}

int load(MovReader* reader, ImmediateDataMode& atom)
{
    unsigned nOfBytesRead(0);
    const char* size(reader->getData(1, nOfBytesRead));
    if (*size != 4 && nOfBytesRead == 1)
        return 0;
    atom.length = 4;
    const char* buffer(reader->getData(14, nOfBytesRead));
    if (nOfBytesRead != 14)
        return 0;
    for (int i(0); i < 4; i++)
        atom.data[i] = buffer[i];
    return nOfBytesRead + 1;
}

static int load(MovReader* reader, SampleMode& atom)
{
    unsigned nOfBytesRead(0);
    const char* index(reader->getData(1, nOfBytesRead));
    atom.trackRefIndex = *index;
    reader->readW(atom.length);
    reader->readDW(atom.sampleNumber);
    reader->readDW(atom.offset);
    reader->readW(atom.bytesPerCompressionBlock);
    reader->readW(atom.samplesPerCompressionBlock);
    return nOfBytesRead;
}

/**
 * Inline MovInfo getter.
 */
const MovInfo& MovParser::getMediaInfo()
{
    return m_movInfo;
}

