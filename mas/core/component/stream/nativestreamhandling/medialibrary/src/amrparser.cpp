#include "amrparser.h"

#include "amrrtpbuilder.h"

#include "AtomName.h"
#include "MoovAtom.h"
#include "TrakAtom.h"

#include "movreader.h"
#include "amrtrackinfo.h"
#include "movaudiochunk.h"
#include "movrtppacketinfo.h"
#include "movrtppacket.h"

#include "rtpblockhandler.h"

#include "medialibraryexception.h"
#include "jlogger.h"
#include "jniutil.h"
#include "java/mediaobject.h"

#include <stdio.h>
#include <boost/ptr_container/ptr_list.hpp>

using namespace quicktime;

struct ImmediateDataMode
{
    ImmediateDataMode() :
            length(0)
    {
    }
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

const char* AmrParser::AMR_CLASSNAME = "masjni.medialibrary.AmrParser";

AmrParser::AmrParser(java::MediaObject* mediaObject) :
        MediaParser(mediaObject), mCursor(0), m_startVideoFrame(0), m_startAudioFrame(0), m_msToSampleIndexFactor(8), m_audioBlockSize(
                0), m_audioPacketCount(0), m_videoBlockSize(0), m_videoPacketCount(0)
{
    
	m_reader = new MovReader(mediaObject);
    JLogger::jniLogDebug(m_env, AMR_CLASSNAME, "AmrParser() m_reader - create at %#x", m_reader);
	
	m_movInfo = new AmrInfo(m_env);
	JLogger::jniLogDebug(m_env, AMR_CLASSNAME, "AmrParser() m_movInfo(amrInfo) - create at %#x", m_movInfo);

    JLogger::jniLogDebug(m_env, AMR_CLASSNAME, "AmrParser(MediaObject*) AmrParser - create at %#x", this);
}

AmrParser::AmrParser(java::MediaObject* mediaObject,AmrInfo *amrinfo) :
        MediaParser(mediaObject), mCursor(0), m_startVideoFrame(0), m_startAudioFrame(0), m_msToSampleIndexFactor(8), m_audioBlockSize(
                0), m_audioPacketCount(0), m_videoBlockSize(0), m_videoPacketCount(0)
{
	m_reader = new MovReader(mediaObject);
    JLogger::jniLogDebug(m_env, AMR_CLASSNAME, "AmrParser() m_reader - create at %#x", m_reader);
	
	if ( amrinfo == 0 ) {
		m_movInfo = new AmrInfo(m_env);
		JLogger::jniLogDebug(m_env, AMR_CLASSNAME, "AmrParser() m_movInfo(amrInfo) - create at %#x", m_movInfo);
	} else {
		m_movInfo=amrinfo;
		JLogger::jniLogDebug(m_env, AMR_CLASSNAME, "AmrParser() m_movInfo - set at %#x", m_movInfo);
	}
	
    JLogger::jniLogDebug(m_env, AMR_CLASSNAME, "AmrParser(MediaObject*,AmrInfo*) - create at %#x", this);
}

AmrParser::~AmrParser()
{
	if ( m_reader != 0 ) {
		JLogger::jniLogDebug(m_env, AMR_CLASSNAME, "~AmrParser() ~m_reader - delete at %#x", m_reader);
		delete m_reader;
		m_reader = 0;
	}
	
	if ( m_movInfo != 0 ) {
		JLogger::jniLogDebug(m_env, AMR_CLASSNAME, "~AmrParser() ~m_movInfo - delete at %#x", &m_movInfo);
		delete m_movInfo;
		m_movInfo = 0;
	}

    JLogger::jniLogDebug(m_env, AMR_CLASSNAME, "~AmrParser - delete at %#x", this);
}

void AmrParser::init()
{
    m_reader->init();
}

bool AmrParser::parse()
{
    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "Parsing 3GP-file...");

    unsigned atomId;
    unsigned atomSize;
    AtomReader& atomReader(*m_reader);
    unsigned mediaDataSize;

    if (getMaxPTime() < 20 || getMaxPTime() % 20 != 0) {
        JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "MaxPTime must be a multiple of 20");
        return false;
    }

    m_amrAudioData.clear();
    atomReader.setQuicktimeVariant(AtomReader::QT);

    while (m_reader->getAtomInformation(atomSize, atomId) != 0) {
        JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "Read atom with id: %d", atomId);

        switch (atomId)
        {
        case quicktime::MDAT:
            JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "MDAT %d", atomSize);
            // Store information about the media data area here.
            mediaDataSize = atomSize - 8;
            m_reader->jumpForward(mediaDataSize);
            break;

        case quicktime::MOOV:
            JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "MOOV %d", atomSize);
            m_moovAtom.restoreGuts(atomReader, atomSize);
            break;

        case quicktime::FTYP:
            JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "FTYP %d", atomSize);
            m_fileTypeAtom.restoreGuts(atomReader, atomSize);
            break;

        case quicktime::FREE:
            JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "FREE %d", atomSize);
            m_reader->jumpForward(atomSize - 8);
            break;

        case quicktime::SKIP:
            JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "SKIP %d", atomSize);
            m_reader->jumpForward(atomSize - 8);
            break;

        case quicktime::WIDE:
            JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "WIDE %d", atomSize);
            m_reader->jumpForward(atomSize - 8);
            break;

        case quicktime::PNOT:
            JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "PNOT %d", atomSize);
            m_reader->jumpForward(atomSize - 8);
            break;

        default:
            JLogger::jniLogWarn(m_env, AMR_CLASSNAME, "Unrecognized atom. ID: '%c%c%c%c' Size: %d",
                    char(atomId >> 24), char(atomId >> 16), char(atomId >> 8), char(atomId >> 0), atomSize);
            return false;
            // break;
        }
    }

    m_movInfo->initialize(m_moovAtom);
	JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "Initializing moov Atom");
    if (m_moovAtom.getAudioTrackAtom() != 0) {
        switch (m_moovAtom.getAudioTrackAtom()->getDataFormat())
        {
        case quicktime::SAMR:
            m_audioCodec = "AMR";
			JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "Audio data format: SAMR (AMR)");
            break;
		case quicktime::SAWB:
            m_audioCodec = "AMR-WB";
			JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "Audio data format: SAWB (AMR-WB)");
            break;
        default:
            JLogger::jniLogWarn(m_env, AMR_CLASSNAME, "Unknown audio codec in QT(ISO) audio track");
            break;
        }
    } else {
        JLogger::jniLogError(m_env, AMR_CLASSNAME,
                "Mandatory audio track is missing in QuickTime(ISO) media!");
        return false;
    }
    if (m_moovAtom.getVideoTrackAtom() != 0) {
        switch (m_moovAtom.getVideoTrackAtom()->getDataFormat())
        {
        case quicktime::S263:
            m_isVideo = true;
            m_videoCodec = "H263";
            break;

        default:
            unsigned c = m_moovAtom.getVideoTrackAtom()->getDataFormat();
            char n = c >> 24 & 0xff;
            char a = c >> 16 & 0xff;
            char m = c >> 8 & 0xff;
            char e = c & 0xff;
            JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "VideoCodec: %c%c%c%c", n, a, m, e);
            break;
        }
    }
    if (m_videoCodec.size() == 0 && m_audioCodec.size() == 0) {
        JLogger::jniLogError(m_env, AMR_CLASSNAME,
                "Could not retrieve any codec information from QuickTime media!");
        return false;
    }
    calculateAudioRtp(m_audioBlockSize, m_audioPacketCount);

    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "Hint track: %d", m_movInfo->getHintTrack());

    if (m_isVideo && m_movInfo->getHintTrack() != 0 && m_movInfo->getHintTrack()->check()) {
        calculateVideoRtp(m_videoBlockSize, m_videoPacketCount);
    }

    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "Parse 3GPP ok.");

    return true;
}

bool AmrParser::check()
{
    return m_movInfo->check();
}

void AmrParser::getFrame(boost::ptr_list<MovRtpPacket>& rtpPackets, int frameIndex)
{
}

const unsigned char*
AmrParser::getAudioChunk(unsigned& chunkSize, int chunkIndex)
{
    return 0;
}

unsigned AmrParser::getDuration()
{
    return m_moovAtom.getMovieHeaderAtom().getDuration();
}

void AmrParser::setCursor(long cursor)
{
    if (cursor < 0) {
        mCursor = 0;
    } else {
        mCursor = cursor;
    }

    if (mCursor > 0) {
        // A cursor can only start at the beginning of
        // an frame. The closest frame should
        // be choosen.
        m_startAudioFrame = findStartAudioFrame();
    }

    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "AmrParser m_startAudioFrame=%d, mCursor=%d",
            m_startAudioFrame, mCursor);
}

long AmrParser::getCursor()
{
    return mCursor;
}

int AmrParser::getAudioChunkCount()
{
    return 0;
}

int AmrParser::getFrameCount()
{
    return m_movInfo->getFrameCount() - m_startVideoFrame;
}

unsigned AmrParser::getAudioBlockSize()
{
    return m_audioBlockSize;
}

unsigned AmrParser::getAudioPacketCount()
{
    return m_audioPacketCount;
}

unsigned AmrParser::getVideoBlockSize()
{
    return m_videoBlockSize;
}

unsigned AmrParser::getVideoPacketCount()
{
    return m_videoPacketCount;
}

void AmrParser::getData(RtpBlockHandler& blockHandler)
{
    collectAudioRtp(blockHandler);
    if (m_isVideo)
        collectVideoRtp(blockHandler);
}

unsigned AmrParser::findStartAudioFrame()
{
    if (m_movInfo->getAudioTrack() == 0)
        return 0;

    StcoAtom* stco(m_movInfo->getAudioTrack()->m_chunkOffsetAtom);
    StscAtom* stsc(m_movInfo->getAudioTrack()->m_sampleToChunkAtom);
    int framesPerSample = m_movInfo->getFramesPerSample();
    unsigned nOfChunks = stco->getChunkOffsetCount();
    unsigned nOfSampleToChunkEntries = stsc->getEntryCount();
    unsigned sampleToChunkIndex = 0;

    unsigned startAudioFrame = 0;
    unsigned frameIndex = 0;
    unsigned skippedFrames = 0;
    bool foundSpeachFrame = false;
    long startTime;

    for (unsigned chunkIndex(0), sampleCount(0); chunkIndex < nOfChunks; chunkIndex++) {
        // Check if we should take text entry (if there are any)
        if (nOfSampleToChunkEntries > sampleToChunkIndex + 1) {
            unsigned firstChunk = stsc->getSampleToChunkEntries()[sampleToChunkIndex + 1].firstChunk;
            if (chunkIndex >= firstChunk - 1)
                sampleToChunkIndex++;
        }

        unsigned chunkOffset = stco->getChunkOffsetEntries()[chunkIndex];

        unsigned sampleSubCount = stsc->getSampleToChunkEntries()[sampleToChunkIndex].samplesPerChunk;
        // Enter chunk start ...
        m_reader->seek(chunkOffset);
        for (unsigned sampleSubIndex(0); sampleSubIndex < sampleSubCount; sampleSubIndex++, sampleCount++) {
            for (int frameNr(0); frameNr < framesPerSample; frameNr++) {
                unsigned char toc;
                m_reader->read(&toc, 1);
                unsigned frameType = (unsigned) (toc >> 3) & 0xf;

                if (frameType != m_movInfo->get_noData() && frameType != m_movInfo->get_sid() ) {
                    foundSpeachFrame = true;

                    startTime = frameIndex * 20;
                    if (startTime > mCursor) {
                        long lastDiff = mCursor - startAudioFrame * 20;
                        lastDiff = (lastDiff < 0 ? -lastDiff : lastDiff);

                        if (startTime - mCursor < lastDiff) {
                            mCursor = startTime;
                            return frameIndex + skippedFrames;
                        } else {
                            mCursor = startAudioFrame * 20;
                            return startAudioFrame + skippedFrames;
                        }
                    }

                    startAudioFrame = frameIndex;
                }

                if (foundSpeachFrame)
                    ++frameIndex;
                else
                    ++skippedFrames;

                m_reader->jumpForward(m_movInfo->getFrameSize(frameType));
            }
        }
    }
    startTime = frameIndex * 20;
    return frameIndex + skippedFrames;
}

void AmrParser::calculateAudioRtp(unsigned& blockSize, unsigned& packetCount)
{
    if (m_movInfo->getAudioTrack() == 0) {
        blockSize = 0;
        packetCount = 0;
        return;
    }

    unsigned framesPerPacket = getMaxPTime() / 20;

    AmrRtpPacketBuilder packetBuilder(m_env, framesPerPacket, m_movInfo, *m_reader);

    unsigned frameIndex = 0;

    StcoAtom* stco(m_movInfo->getAudioTrack()->m_chunkOffsetAtom);
    StscAtom* stsc(m_movInfo->getAudioTrack()->m_sampleToChunkAtom);
    int framesPerSample = m_movInfo->getFramesPerSample();
    unsigned nOfChunks = stco->getChunkOffsetCount();
    unsigned nOfSampleToChunkEntries = stsc->getEntryCount();
	JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "calculateAudioRtp #framesPerSample = %d, nOfChunks = %d, nOfSampleToChunkEntries = %d",
            framesPerSample,nOfChunks,nOfSampleToChunkEntries);
	if (nOfSampleToChunkEntries == 0) {
		JLogger::jniLogWarn(m_env, AMR_CLASSNAME, "nOfSampleToChunkEntries is 0, cannot calculate number of packets should be at least 1.");
	} else {
		unsigned sampleToChunkIndex = 0;		
		for (unsigned chunkIndex(0), sampleCount(0); chunkIndex < nOfChunks; chunkIndex++) {
			// Check if we should take text entry (if there are any) ?????
			if (nOfSampleToChunkEntries > sampleToChunkIndex + 1) {
				unsigned firstChunk = stsc->getSampleToChunkEntries()[sampleToChunkIndex + 1].firstChunk;
				if (chunkIndex >= firstChunk - 1)
					sampleToChunkIndex++;
			}
			JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "calculateAudioRtp #chunkindex = %d, sampleToChunkIndex = %d",
				chunkIndex,sampleToChunkIndex);

			unsigned chunkOffset = stco->getChunkOffsetEntries()[chunkIndex];

			unsigned sampleSubCount = stsc->getSampleToChunkEntries()[sampleToChunkIndex].samplesPerChunk;
			
			JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "calculateAudioRtp #chunkOffset = %d,sampleSubCount = %d ",
				chunkOffset,sampleSubCount);
				
			// Enter chunk start ...
			m_reader->seek(chunkOffset);
			unsigned sampleSubIndex = 0;
			int frameNr=0;
			for (sampleSubIndex = 0; sampleSubIndex < sampleSubCount; sampleSubIndex++, sampleCount++) {
				for (frameNr=0; frameNr < framesPerSample; frameNr++) {
					if (frameIndex >= m_startAudioFrame) {
						packetBuilder.readAndCalculateSizes();
					}
					++frameIndex;
				}
			}
			JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "calculateAudioRtp #frameIndex = %d,sampleSubIndex = %d, frameNr = %d, chunkIndex = %d ",
				frameIndex,sampleSubIndex,frameNr,chunkIndex);
		}
	}


    packetCount = packetBuilder.getNumberOfPackets();
	JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "calculateAudioRtp #packetCount = %d",
            packetCount);
    blockSize = packetCount + packetBuilder.getNumberOfFrames() + packetBuilder.getPayloadSize();

    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "calculateAudioRtp #f = %d",
            packetBuilder.getNumberOfFrames());
    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "calculateAudioRtp PC = %d", packetCount);
    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "calculateAudioRtp PLSz = %d",
            packetBuilder.getPayloadSize());
    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "calculateAudioRtp BLSz = %d", blockSize);
}

void AmrParser::collectAudioRtp(RtpBlockHandler& blockHandler) 
{
    if (m_movInfo->getAudioTrack() == 0)
        return;

    unsigned framesPerPacket = getMaxPTime() / 20;
    AmrRtpPacketBuilder packetBuilder(m_env, framesPerPacket, m_movInfo, *m_reader);

    unsigned frameIndex = 0;
    unsigned collectedFrames = 0;

    StcoAtom* stco(m_movInfo->getAudioTrack()->m_chunkOffsetAtom);
    StscAtom* stsc(m_movInfo->getAudioTrack()->m_sampleToChunkAtom);
    int framesPerSample = m_movInfo->getFramesPerSample();
    unsigned nOfChunks = stco->getChunkOffsetCount();
    unsigned nOfSampleToChunkEntries = stsc->getEntryCount();
    unsigned sampleToChunkIndex = 0;

    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "collectAudioRtp() framesPerSample = %d",
            framesPerSample);
    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "collectAudioRtp nOfChunks = %d", nOfChunks);
    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "collectAudioRtp nOfSampleToChunkEntries = %d",
            nOfSampleToChunkEntries);
    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "collectAudioRtp m_startAudioFrame = %d",
            m_startAudioFrame);
	if (nOfSampleToChunkEntries == 0) {
		JLogger::jniLogWarn(m_env, AMR_CLASSNAME, "nOfSampleToChunkEntries is 0, cannot calculate number of packets should be at least 1.");
	} else {
		for (unsigned chunkIndex(0), sampleCount(0); chunkIndex < nOfChunks; chunkIndex++) {
			// Check if we should take text entry (if there are any)
			if (nOfSampleToChunkEntries > sampleToChunkIndex + 1) {
				unsigned firstChunk = stsc->getSampleToChunkEntries()[sampleToChunkIndex + 1].firstChunk;
				if (chunkIndex >= firstChunk - 1)
					sampleToChunkIndex++;
			}

			unsigned chunkOffset = stco->getChunkOffsetEntries()[chunkIndex];
			unsigned sampleSubCount = stsc->getSampleToChunkEntries()[sampleToChunkIndex].samplesPerChunk;

			// Enter chunk start ...
			m_reader->seek(chunkOffset);
			for (unsigned sampleSubIndex(0); sampleSubIndex < sampleSubCount; sampleSubIndex++, sampleCount++) {
				for (int frameNr(0); frameNr < framesPerSample; frameNr++) {
					if (frameIndex >= m_startAudioFrame) {
						packetBuilder.readAndAddFrame(blockHandler);
						++collectedFrames;
					} else {
						unsigned char toc;
						m_reader->read(&toc, 1);
						unsigned frameType = (unsigned) (toc >> 3) & 0xf;
						m_reader->jumpForward(m_movInfo->getFrameSize(frameType));
					}
					++frameIndex;
				}
			}
		}
	}

    packetBuilder.flushLastPacket(blockHandler);

    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "collected audio packets no of frames = %d",
            collectedFrames);
}

void AmrParser::calculateVideoRtp(unsigned& blockSize, unsigned& packetCount)
{
    blockSize = 0;
    packetCount = 0;

    MovRtpPacketInfo packetInfo;
    ImmediateDataMode immediateDataMode;
    SampleMode sampleMode;
    char* buffer;
    const unsigned nOfFrames(m_movInfo->getFrameCount());

    for (unsigned frameIndex(0); frameIndex < nOfFrames; frameIndex++) {
        // Get the offset/start of the hint frame
        int hintFrameStart = m_movInfo->getHintTrack()->getChunkOffset(frameIndex);

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
                    (void) load(m_reader, immediateDataMode);
                    break;
                case 2: // Sample Mode
                    // Now we merge the immediate data and the sample data into
                    // an RTP package.
                    (void) load(m_reader, sampleMode);
                    // Calculating packet size in RTP Block Handler
                    payloadSize = immediateDataMode.length + sampleMode.length;
                    blockSize += payloadSize;
                    packetCount++;
                    break;
                default: // Unknown mode ... Should bail out
                    // This should actually never happen ;-)
                    // TODO: improve error handling and logging
                    JLogger::jniLogWarn(m_env, AMR_CLASSNAME, "Handled RTP data type: %d",
                            (int) *buffer);
                    break;
                }
            }
        }
    }
}

void AmrParser::collectVideoRtp(RtpBlockHandler& blockHandler)
{
    JLogger::jniLogTrace(m_env, AMR_CLASSNAME, "collectVideoRtp");

    MovRtpPacketInfo packetInfo;
    ImmediateDataMode immediateDataMode;
    SampleMode sampleMode;
    char* buffer;
    const unsigned nOfFrames(m_movInfo->getFrameCount());

    for (unsigned frameIndex(0); frameIndex < nOfFrames; frameIndex++) {
        // Get the offset/start of the hint frame
        int hintFrameStart = m_movInfo->getHintTrack()->getChunkOffset(frameIndex);

        int videoFrameStart = m_movInfo->getVideoTrack()->getChunkOffset(frameIndex);

        unsigned frameTime = m_movInfo->getVideoTrack()->getFrameTime(frameIndex);

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
                    (void) load(m_reader, immediateDataMode);
                    break;
                case 2: // Sample Mode
                    // Now we merge the immediate data and the sample data into
                    // an RTP package.
                    (void) load(m_reader, sampleMode);
                    // Calculating packet size in RTP Block Handler
                    // Retrieve the video sample data
                    position = m_reader->tell();
                    m_reader->seek((size_t) videoFrameStart + sampleMode.offset);
                    sample = (char*) m_reader->getData(sampleMode.length, nOfBytesRead);
                    // TODO: improve error handling and logging
                    if (sampleMode.length != nOfBytesRead) {
                        JLogger::jniLogWarn(m_env, AMR_CLASSNAME, "Length mismatch: %d:%d",
                                sampleMode.length, nOfBytesRead);
                    }
                    blockHandler.addVideoPayload(index + 1 == entryCount ? frameTime : 0, immediateDataMode.data,
                            immediateDataMode.length, sample, sampleMode.length);

                    m_reader->seek((size_t) position);
                    break;
                default: // Unknown mode ... Should bail out
                    // This should actually never happen ;-)
                    // TODO: improve error handling and logging
                    JLogger::jniLogWarn(m_env, AMR_CLASSNAME, "Handled RTP data type: %d",
                            (int) *buffer);
                    break;
                }
            }
        }
    }
}

unsigned AmrParser::getAudioStartTimeOffset()
{
    TrakAtom* track(m_moovAtom.getAudioTrackAtom());

    // Handling null pointer
    if (track == 0)
        return 0;
    // Returning the requested value.
    return track->getStartTimeOffset();
}

unsigned AmrParser::getVideoStartTimeOffset()
{
    TrakAtom* track(m_moovAtom.getVideoTrackAtom());

    // Handling null pointer
    if (track == 0)
        return 0;
    // Returning the requested value.
    return track->getStartTimeOffset();
}

static int load(MovReader* reader, ImmediateDataMode& atom)
{
    unsigned nOfBytesRead(0);
    const char* size(reader->getData(1, nOfBytesRead));

    if (*size != 4 && nOfBytesRead == 1)
        return 0;
    atom.length = 4;
    const char* buffer(reader->getData(14, nOfBytesRead));

    if (nOfBytesRead != 14)
        return 0;
    for (int i(0); i < 4; i++) {
        atom.data[i] = buffer[i];
    }

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
 * MovInfo getter.
 */
const AmrInfo& AmrParser::getMediaInfo()
{
    	if (m_movInfo == 0 ); {
		//this should never happen as it is created in the constructor
		//but you never know.
		JLogger::jniLogWarn(m_env, AMR_CLASSNAME, "AmrParser::getMediaInfo() created m_movInfo as does not exist!");
		m_movInfo = new AmrInfo(m_env);
	}
    return *m_movInfo;
}

const base::String AmrParser::getAudioCodec()
{
    return m_audioCodec;
}
