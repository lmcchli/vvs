#include "amrbuilder.h"

#include "AtomName.h"
#include "AtomWriter.h"

#include "MdatAtom.h"
#include "FtypAtom.h"
#include "AmrTrackAtom.h"
#include "VideoTrackAtom.h"
#include "HintTrackAtom.h"
#include "AmrSampleDescription.h"
#include "H263SampleDescription.h"
#include "AmrSpecificAtom.h"

#include "movwriter.h"
#include "byteutilities.h"
#include "movaudiochunk.h"
#include "movaudiochunkcontainer.h"

#include "jlogger.h"
#include "jniutil.h"

#include <iostream>
#include <stdexcept>
#include <boost/ptr_container/ptr_list.hpp>
#include <stdio.h>

class setModeSet;
using std::cout;
using std::endl;
using std::invalid_argument;

using namespace quicktime;

const char* AmrBuilder::NB_CLASSNAME = "masjni.medialibrary.AmrBuilder";

AmrBuilder::AmrBuilder(JNIEnv* env) : 
        m_amrSampleDescription(new AmrSampleDescription(quicktime::SAMR)),
		m_movInfo(), 
		m_videoFrames(0), 
		m_audioChunks(0),
		m_movieDataAtomSize(0),
		m_fileTypeAtom(),
		m_movieAtom(), 
		m_videoTrackAtom(new VideoTrackAtom(new H263SampleDescription())),
		m_hintTrackAtom(new HintTrackAtom()),
		m_audioTimeScale(AMR_AUDIO_TIME_SCALE), // due to 8 kHz sample rate.
        m_movieTimeScale(H2623_TIME_SCALE), // ms
		m_samplesPer20Ms(SAMPLES_PER_FRAME_20MS_NB),
        m_audioDuration(0),
		m_videoDuration(0), 
		m_movieDuration(0),
		m_modeSet(quicktime::AmrSpecificAtom::modeSetNB),
		m_env(env)
{
    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "m_amrSampleDescription(SAMR) - create at %#x", m_amrSampleDescription);
    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "m_videoTrackAtom - create at %#x", m_videoTrackAtom);
    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "m_hintTrackAtom - create at %#x", m_hintTrackAtom);

    m_amrTrackAtom = new AmrTrackAtom(m_amrSampleDescription);
    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "m_amrTrackAtom - create at %#x", m_amrTrackAtom);

    m_movieAtom.setAudioTrackAtom(m_amrTrackAtom);

    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "AmrBuilder - create at %#x", this);
    // The video tracks are only created if there is video present ...
}

AmrBuilder::AmrBuilder(JNIEnv* env,quicktime::AtomName name,unsigned audioTimeScale, unsigned movieTimeScale, unsigned samplesPer20MsFrame, unsigned modeSet) : 
        m_amrSampleDescription(new AmrSampleDescription(name)),
		m_movInfo(), 
		m_videoFrames(0), 
		m_audioChunks(0),
		m_movieDataAtomSize(0),
		m_fileTypeAtom(),
		m_movieAtom(), 
		m_videoTrackAtom(new VideoTrackAtom(new H263SampleDescription())),
		m_hintTrackAtom(new HintTrackAtom()),
		m_audioTimeScale(audioTimeScale),
        m_movieTimeScale(movieTimeScale), // ms
		m_samplesPer20Ms(samplesPer20MsFrame),
        m_audioDuration(0),
		m_videoDuration(0), 
		m_movieDuration(0),
		m_modeSet(modeSet),
		m_env(env)
{
	std::string *cname=m_amrSampleDescription->getAMRSpecificAtom()->getAtomNameAsString();
	JLogger::jniLogDebug(m_env, NB_CLASSNAME, "m_amrSampleDescription(%s) - create at %#x",cname->c_str(), m_amrSampleDescription);
	JLogger::jniLogDebug(m_env, NB_CLASSNAME, "m_videoTrackAtom - create at %#x", m_videoTrackAtom);
	JLogger::jniLogDebug(m_env, NB_CLASSNAME, "m_hintTrackAtom - create at %#x", m_hintTrackAtom);

    m_amrTrackAtom = new AmrTrackAtom(m_amrSampleDescription);
    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "m_amrTrackAtom - create at %#x", m_amrTrackAtom);

    m_movieAtom.setAudioTrackAtom(m_amrTrackAtom);

    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "AmrBuilder - create at %#x", this);
    // The video tracks are only created if there is video present ...
}

AmrBuilder::~AmrBuilder()
{
    //delete m_amrSampleDescription; -this is deleted by the stsd atom indirectly inside the trak atom.
    //delete m_amrTrackAtom; - this is deleted by the m_movieAtom being removed from the stack.
	if (m_videoTrackAtom != NULL) {
		delete m_videoTrackAtom;
	}
	if (m_hintTrackAtom !=NULL) {
		delete m_hintTrackAtom;
	}
	
    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "~m_hintTrackAtom - delete m_hintTrackAtom at %#x", m_hintTrackAtom);
    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "~m_videoTrackAtom - delete m_videoTrackAtom at %#x", m_videoTrackAtom);
    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "~AmrBuilder - delete at %#x", this);
	m_videoTrackAtom=NULL;
	m_hintTrackAtom=NULL;
}

void AmrBuilder::setAudioCodec(const base::String& codecName)
{
    // The codec is hard coded.
    // TODO: handle different codecs
    //       for each new codec we need to have a Sample Description Atom
    if (codecName != "AMR" && codecName != "amr") {
        JLogger::jniLogWarn(m_env, NB_CLASSNAME, "Attempting to set wrong audio codec: %s", codecName.c_str());
        throw base::String("Bad audio codec name [" + codecName + "]");
    }
}

void AmrBuilder::setVideoCodec(const base::String& codecName)
{
    // The codec is hard coded.
    // TODO: see setAudioCodec()
    if (codecName != "H263" && codecName != "h263") {
        JLogger::jniLogWarn(m_env, NB_CLASSNAME, "Attempting to set wrong video codec: %s", codecName.c_str());
        throw base::String("Bad video codec name [" + codecName + "]");
    }
}

void AmrBuilder::setVideoFrames(const MovVideoFrameContainer& videoFrames)
{
    m_videoFrames = (MovVideoFrameContainer*) &videoFrames;
}

void AmrBuilder::setAudioChunks(const MovAudioChunkContainer& audioChunks)
{
	JLogger::jniLogTrace(m_env, NB_CLASSNAME, "setAudioChunks()");
    m_audioChunks = (MovAudioChunkContainer*) &audioChunks;
}

bool AmrBuilder::store(MediaObjectWriter& writer)
{
    JLogger::jniLogTrace(m_env, NB_CLASSNAME, "Storing 3GPP ...");
    if (m_audioChunks == NULL)
        return false;

    MovWriter& movWriter = (MovWriter&) writer;
    movWriter.setQuicktimeVariant(AtomWriter::QT);

    // Retrieve the number of frames and intialize the track sizes
    int nOfFrames(0);

    if (m_videoFrames != NULL)
        nOfFrames = m_videoFrames->size();
    unsigned nOfAudioChunks(m_audioChunks->size());
    JLogger::jniLogTrace(m_env, NB_CLASSNAME, "store() Video frames: %d", nOfFrames);
    JLogger::jniLogTrace(m_env, NB_CLASSNAME, "store() Audio chunks: %d", nOfAudioChunks);

    if (m_videoFrames != NULL) {
        JLogger::jniLogTrace(m_env, NB_CLASSNAME, "store() Initializing video tracks ...");

        m_movieAtom.setVideoTrackAtom(m_videoTrackAtom);
        m_movieAtom.setHintTrackAtom(m_hintTrackAtom);
        m_videoTrackAtom->initialize(nOfFrames);
        m_hintTrackAtom->initialize(nOfFrames);
    } else {
        if (m_videoTrackAtom != NULL) {
            JLogger::jniLogDebug(m_env, NB_CLASSNAME, "~m_videoTrackAtom - delete at %#x", m_videoTrackAtom);
            delete m_videoTrackAtom;
            m_videoTrackAtom = NULL;
        }

        if (m_hintTrackAtom != NULL) {
            JLogger::jniLogDebug(m_env, NB_CLASSNAME, "~m_hintTrackAtom - delete at %#x", m_hintTrackAtom);
            delete m_hintTrackAtom;
            m_hintTrackAtom = NULL;
        }
    }

    JLogger::jniLogTrace(m_env, NB_CLASSNAME, "store() Initializing audio tracks ...");
    // Initialize the audio track
    m_amrTrackAtom->initialize(nOfAudioChunks);
    unsigned index = 0;
	JLogger::jniLogDebug(m_env, NB_CLASSNAME, "store() samplesPer20MsFrame %d , NoofAudioChunks %d",m_samplesPer20Ms, nOfAudioChunks);
    m_amrTrackAtom->setSampleTime(m_samplesPer20Ms, nOfAudioChunks);
	JLogger::jniLogTrace(m_env, NB_CLASSNAME, "store() populate Audio chunk samples.");
    for (boost::ptr_list<MovAudioChunk>::iterator iter = m_audioChunks->begin(); iter != m_audioChunks->end();
            ++iter, index++) {
        unsigned chunkSize(iter->getLength() - 1);

        // This assumes one frame/sample per chunk ...
        // Each sample contains one and only one AMR frame.
        // Each chunk contains one and only one sample
		JLogger::jniLogTrace(m_env, NB_CLASSNAME, "store() sample to chunk index: %d chunksize: %d ",index, chunkSize);
        m_amrTrackAtom->setSampleToChunk(index, chunkSize);
    }

    // Calculate the track and movie durations.
    calculateDurations();

    // Save MOV onto media. First the meta model is updated with
    // movie data media offsets and saved onto media. Second the 
    // movie data is saved onto media.

    // Store calculated offsets in meta model
    MdatAtom mdatAtom;
    mdatAtom.setOffset(m_fileTypeAtom.getAtomSize() + m_movieAtom.getAtomSize());
    // The movie data is stored with mdatAtom as a dummy AtomWriter.
    // Note: the atom writer is only used for retrieving file positions
    //       which are stored in the meta model.
    storeDataAndCalculateOffsets(mdatAtom);
    // Store meta model in media
    m_fileTypeAtom.saveGuts(movWriter);
    m_movieAtom.saveGuts(movWriter); 

    // Store media data in media
    movWriter.writeDW(m_movieDataAtomSize);
    movWriter.writeDW(quicktime::MDAT);
    // Now the media data is written onto the media.
    // Note: the meta model is updated but the values are
    //       already saved/stored.
    storeDataAndCalculateOffsets(movWriter);

    // Update MovInfo with updated meta data.
    m_movInfo.initialize(m_movieAtom);
    return true;
}

void AmrBuilder::calculateDurations()
{
    JLogger::jniLogTrace(m_env, NB_CLASSNAME, "Calculate media track durations ...");

    m_videoDuration = m_videoTrackAtom != 0 ? m_videoTrackAtom->getStartTimeOffset() : 0;
    m_audioDuration = (m_amrTrackAtom->getStartTimeOffset() * m_audioTimeScale) / 1000;

    JLogger::jniLogTrace(m_env, NB_CLASSNAME, "Video duration (offset): %d", m_videoDuration);
    JLogger::jniLogTrace(m_env, NB_CLASSNAME, "Audio duration (offset): %d", m_audioDuration);

    // Calculate Audio duration (in raw samples)
    // This only hold for AMR RTP frames corresponding to 20 ms audio
    // Ensure that each chunk correspond to one AMR frame since
    // there can be more than one AMR frame in one RTP frame and
    // it is not always true that an AMR frame is 20 ms.
    // TODO: Should have a time stamp on each audio chunk.
	m_audioDuration += m_audioChunks->size() * m_samplesPer20Ms;

    // Calculate Video duration
    // The calculation is based upon the frame time which is the difference
    // between this frame and the previous one (defines for how a frame is 
    // visual).
    if (m_videoFrames != 0) {
        MovVideoFrameContainer::iterator frameIter = m_videoFrames->begin();
        for (; frameIter != m_videoFrames->end(); frameIter++) {
            boost::ptr_list<MovRtpPacket>& frame = *frameIter;
            boost::ptr_list<MovRtpPacket>::iterator pktIter = frame.begin();
            for (; pktIter != frame.end(); pktIter++) {
                m_videoDuration += pktIter->getFrameTime();
            }
        }
    }

    // The duration is scaled from sample to time (seconds)
    float maxDuration(float(m_audioDuration) / m_audioTimeScale);
    if (maxDuration < float(m_videoDuration) / m_movieTimeScale) {
        maxDuration = float(m_videoDuration) / m_movieTimeScale;
    }

    m_movieDuration = (unsigned) (maxDuration * m_movieTimeScale);

    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "Video duration (final): %d", m_videoDuration);
    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "Audio duration (final): %d", m_audioDuration);
    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "Movie duration:         %d", m_movieDuration);
    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "Movie timescale:        %d", m_movieTimeScale);
    JLogger::jniLogDebug(m_env, NB_CLASSNAME, "Audio timescale:        %d", m_audioTimeScale);

    m_movieAtom.getMovieHeaderAtom().setDuration(m_movieDuration);

    m_amrTrackAtom->getTrackHeaderAtom().setDuration(m_movieDuration);
    m_amrTrackAtom->getEditAtom().getEditListAtom().getEditListEntries()[1].trackDuration = m_movieDuration;
    m_amrTrackAtom->getEditAtom().getEditListAtom().getEditListEntries()[1].mediaTime = 0;
    m_amrTrackAtom->getEditAtom().getEditListAtom().getEditListEntries()[0].mediaRate = 0x00010000;
    m_amrTrackAtom->getEditAtom().getEditListAtom().getEditListEntries()[1].mediaRate = 0x00010000;
    m_amrTrackAtom->getMediaAtom().getMediaHeaderAtom().setTimeScale(m_audioTimeScale);
    m_amrSampleDescription->setTimeScale(m_audioTimeScale);
    m_amrSampleDescription->getAMRSpecificAtom()->setModeSet(m_modeSet);
    m_amrTrackAtom->getMediaAtom().getMediaHeaderAtom().setDuration(m_audioDuration);

    if (m_videoFrames != 0) {
        m_videoTrackAtom->getTrackHeaderAtom().setDuration(m_movieDuration);
        m_videoTrackAtom->getEditAtom().getEditListAtom().getEditListEntries()[1].trackDuration = m_movieDuration;
        m_videoTrackAtom->getEditAtom().getEditListAtom().getEditListEntries()[1].mediaTime = 0;
        m_videoTrackAtom->getEditAtom().getEditListAtom().getEditListEntries()[0].mediaRate = 0x00010000;
        m_videoTrackAtom->getEditAtom().getEditListAtom().getEditListEntries()[1].mediaRate = 0x00010000;
        m_videoTrackAtom->getMediaAtom().getMediaHeaderAtom().setTimeScale(m_movieTimeScale);
        m_videoTrackAtom->getMediaAtom().getMediaHeaderAtom().setDuration(m_movieDuration);

        m_hintTrackAtom->getTrackHeaderAtom().setDuration(m_movieDuration);
        m_hintTrackAtom->getEditAtom().getEditListAtom().getEditListEntries()[1].trackDuration = m_movieDuration;
        m_hintTrackAtom->getEditAtom().getEditListAtom().getEditListEntries()[1].mediaTime = 0;
        m_hintTrackAtom->getEditAtom().getEditListAtom().getEditListEntries()[0].mediaRate = 0x00010000;
        m_hintTrackAtom->getEditAtom().getEditListAtom().getEditListEntries()[1].mediaRate = 0x00010000;
        m_hintTrackAtom->getMediaAtom().getMediaHeaderAtom().setTimeScale(m_movieTimeScale);
        m_hintTrackAtom->getMediaAtom().getMediaHeaderAtom().setDuration(m_movieDuration);

    }
}

unsigned AmrBuilder::storeDataAndCalculateOffsets(quicktime::AtomWriter& atomWriter)
{
    unsigned mdatSize(8);
    unsigned audioSize(storeAudio(atomWriter));
    unsigned videoSize(0);
    unsigned hintSize(0);

    if (m_videoFrames != 0) {
        videoSize = storeVideo(atomWriter);
        hintSize = storeHint(atomWriter);
    }

    mdatSize += audioSize + videoSize + hintSize;
    m_movieDataAtomSize = mdatSize;
    return mdatSize;
}

unsigned AmrBuilder::storeAudio(quicktime::AtomWriter& atomWriter)
{
    // TODO: need to handle non origo play start
    // Handled by edit atoms?
    unsigned size(0);
    unsigned index(0);
    MovAudioChunkContainer* audioChunks((MovAudioChunkContainer*) m_audioChunks);

    for (MovAudioChunkContainer::iterator iter(audioChunks->begin()); iter != audioChunks->end(); ++iter, index++) {
        MovAudioChunk& chunk(*iter);
        size += chunk.getLength() - 1; //skip toc
        m_amrTrackAtom->setChunkOffset(atomWriter.tell(), index);
        atomWriter.write(chunk.getData() + 1, chunk.getLength() - 1); //skip the toc.
    }
    return size;
}

unsigned AmrBuilder::storeVideo(quicktime::AtomWriter& atomWriter)
{
    // TODO: need to handle non origo play start
    // Handled by edit atoms?
    // TODO: need time scale and duration

    // Retrieve the number of frames and intialize the track sizes
    int nOfFrames(0);
    unsigned size(0);

    // Looping over the frames
    MovVideoFrameContainer::iterator frameIter = m_videoFrames->begin();
    for (; frameIter != m_videoFrames->end(); frameIter++) {

        boost::ptr_list<MovRtpPacket>& frame = *frameIter;
        unsigned videoOffset(atomWriter.tell());
        unsigned videoChunkSize(0);
        unsigned videoFrameTime(0);

        boost::ptr_list<MovRtpPacket>::iterator pktIter = frame.begin();
        for (; pktIter != frame.end(); pktIter++) {

            MovRtpPacket& packet = *pktIter;
            videoChunkSize += packet.getLength() - 4;
            atomWriter.write((const char *) packet.getData() + 4, packet.getLength() - 4);

            if (pktIter == frame.begin())
                videoFrameTime = packet.getFrameTime();
        }

        size += videoChunkSize;
        m_videoDuration += videoFrameTime;
        m_videoTrackAtom->setChunkOffset(videoOffset, nOfFrames);
        m_videoTrackAtom->setSampleSize(videoChunkSize, nOfFrames);
        m_videoTrackAtom->setSampleTime(videoFrameTime, nOfFrames);
        nOfFrames++;

    }
    return size;
}

unsigned AmrBuilder::storeHint(quicktime::AtomWriter& atomWriter)
{
    // TODO: need to handle non origo play start
    // TODO: need time scale and duration

    // Retrieve the number of frames and intialize the track sizes
    unsigned size(0);

    // Looping over the frames
    int frameNumber = 0;
    MovVideoFrameContainer::iterator frameIter = m_videoFrames->begin();
    for (; frameIter != m_videoFrames->end(); frameIter++) {
        unsigned videoChunkOffset(0);
        unsigned hintChunkOffset(atomWriter.tell());
        unsigned hintFrameTime(0);
        // Get current hint, video and packet frame
        boost::ptr_list<MovRtpPacket>& frame = *frameIter;
        // 1) Packetization hint sample data
        //    Entry count
        unsigned short packetEntryCount((unsigned short) frame.size());
        atomWriter.writeW(packetEntryCount);
        //    Reserved
        unsigned short reserved(0);
        atomWriter.writeW(reserved);
        //    Packet Entry Table -> 2)
        unsigned short bytesPCB(0);

        //TODO: Remove this loop, bytesPCB isn't for an entire frame, it's
        //per packet, and should in most cases be constant for all packets...
        for (boost::ptr_list<MovRtpPacket>::iterator pktIter = frame.begin(); pktIter != frame.end(); ++pktIter) {
            MovRtpPacket& packet = *pktIter;
            unsigned short packetLength(packet.getLength() - 4);
            bytesPCB += packetLength;
        }
        int sampleNumber = 0;
        for (boost::ptr_list<MovRtpPacket>::iterator pktIter = frame.begin(); pktIter != frame.end(); ++pktIter) {
            MovRtpPacket& packet = *pktIter;
            // 2) Packet Entry
            //    Relative packet transmission time
            atomWriter.writeDW(packet.getTransmissionTime());
            //    RTP header info
            atomWriter.writeW(packet.getHeaderInfo());
            //    RTP sequence number
            atomWriter.writeW(packet.getSequenceNumber());
            //    Flags
            unsigned short flags(0);
            atomWriter.writeW(flags);
            //    Entry count
            unsigned short entryCount(2);
            atomWriter.writeW(entryCount);
            // 3) Data table entry : Immediate Data Mode
            char immediateData[16] = { '\0' };
            //    Data source
            immediateData[0] = 1;
            //    Immediate length
            immediateData[1] = 4;
            //    Immediate data
            for (int index(0); index < immediateData[1]; index++) {
                immediateData[2 + index] = packet.getData()[index];
            }
            atomWriter.write(immediateData, 16);
            // 4) Data table entry : Sample Mode
            char header[2];
            //    Data Source
            header[0] = 2;
            //    Track ref index
            header[1] = 0;
            //    Length
            unsigned short packetLength(packet.getLength() - 4);
            //    Sample number
            //    offset
            unsigned offset(videoChunkOffset);
            videoChunkOffset += packetLength;
            //    bytes per compression block
            //    samples per compression block
            unsigned short samplesPCB(1);
            atomWriter.write(header, 2);
            atomWriter.writeW(packetLength);
            sampleNumber++;
            atomWriter.writeDW(sampleNumber);
            atomWriter.writeDW(offset);
            atomWriter.writeW(bytesPCB);
            atomWriter.writeW(samplesPCB);

            // -) Separate payload and payload header
            // -) Save payload header in hint frame
            // -) Save payload in video frame.
            // -) Save reference to payload in hint frame.
        }
        unsigned hintChunkSize(atomWriter.tell() - hintChunkOffset);
        size += hintChunkSize;
        /*
         cout << "Hint Frame #" << f << " "
         << hintChunkSize << " : "
         << size << endl;
         */
        m_hintTrackAtom->setChunkOffset(hintChunkOffset, frameNumber);
        m_hintTrackAtom->setSampleSize(hintChunkSize, frameNumber);
        m_hintTrackAtom->setSampleTime(hintFrameTime, frameNumber);
        frameNumber++;
    }
    return size;
}

void AmrBuilder::setAudioStartTimeOffset(unsigned offset)
{
    m_amrTrackAtom->setStartTimeOffset(offset);
}

void AmrBuilder::setVideoStartTimeOffset(unsigned offset)
{
    if (m_videoFrames != 0)
        m_videoTrackAtom->setStartTimeOffset(offset);
}

void AmrBuilder::setModeSet(unsigned short value)
{
    m_modeSet = value;
}

bool AmrBuilder::isIFrame(const char* payload)
{
    // The "I"-bit indicates that the frame is intra-codes and
    // thus is an I-frame (See RFC 2190 for details).
    // In mode A, the I-bit is number 12 in the first word
    // In mode B and C, the I-bit is number 1 in the third word
    bool isLittleEndian(Platform::isLittleEndian());
    uint16_t word(0);

    ByteUtilities::readW((const char*) payload, word, isLittleEndian);
    bool isModeA((word & 0x8000) == 0);
    bool isIntraCoded(false);
    if (isModeA) {
        isIntraCoded = (word & 0x10) == 0;
    } else {
        // First read the third word
        ByteUtilities::readW((const char*) (payload + 4), word, isLittleEndian);
        isIntraCoded = (word & 0x8000) == 0;
    }
    return isIntraCoded;
}

MediaInfo& AmrBuilder::getInfo()
{
    return m_movInfo;
}

const unsigned AmrBuilder::getDuration()
{
    return m_movieAtom.getMovieHeaderAtom().getDuration();
}

bool AmrBuilder::isAudioOnlyBuilder()
{
    return mIsAudioOnly;
}

void AmrBuilder::setAudioOnlyBuilder(bool isAudioOnly)
{
    mIsAudioOnly = isAudioOnly;
}

