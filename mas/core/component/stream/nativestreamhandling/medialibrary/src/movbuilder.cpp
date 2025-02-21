#include "jlogger.h"
#include "jniutil.h"
#include "movbuilder.h"

#include "AtomName.h"
#include "AtomWriter.h"

#include "MdatAtom.h"

#include "AudioTrackAtom.h"
#include "VideoTrackAtom.h"
#include "HintTrackAtom.h"

#include "movwriter.h"
#include "byteutilities.h"
#include "movaudiochunk.h"
#include "movaudiochunkcontainer.h"

#include <iostream>
#include <cc++/exception.h>
#include <boost/ptr_container/ptr_list.hpp>

using std::cout;
using std::endl;
using std::invalid_argument;

using namespace quicktime;

static const char* CLASSNAME = "masjni.medialibrary.MovBuilder";

MovBuilder::MovBuilder(JNIEnv* env) :
        m_movInfo(), m_videoFrames(0), m_audioChunks(0), m_movieDataAtomSize(0), m_movieAtom(), m_audioTimeScale(8000), // due to 8 kHz sample rate.
        m_movieTimeScale(1000), // ms
        m_audioDuration(0), m_videoDuration(0), m_movieDuration(0), m_isMoovAtomFirst(true)
{
    m_movieAtom.setAudioTrackAtom(new quicktime::AudioTrackAtom(new PCMSoundSampleDescription(ULAW)));
    m_movieAtom.setVideoTrackAtom(new quicktime::VideoTrackAtom());
    m_movieAtom.setHintTrackAtom(new quicktime::HintTrackAtom());

    JLogger::jniLogDebug(env, CLASSNAME, "MovBuilder - create at %#x", this);
}

MovBuilder::~MovBuilder()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
    JLogger::jniLogDebug(env, CLASSNAME, "~MovBuilder - delete at %#x", this);
}

bool MovBuilder::isMoovAtomFirst()
{
    return m_isMoovAtomFirst;
}

void MovBuilder::setMoovAtomFirst(bool moovAtomFirst)
{
    m_isMoovAtomFirst = moovAtomFirst;
}

void MovBuilder::setAudioCodec(const base::String& codecName)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    // The codec is hard coded.
    // TODO: handle different codecs
    //       for each new codec we need to have a Sample Description Atom
    if (!(codecName == "PCMU" || codecName == "PCMA" || codecName == "pcmu" || codecName == "pcma")) {
        JLogger::jniLogError(env, CLASSNAME, "Bad audio codec name [%s]", codecName.c_str());
        throw ost::Exception("Bad audio codec name [" + codecName + "]");
    }
    if (codecName == "PCMU" || codecName == "pcmu") {
        m_movieAtom.setAudioTrackAtom(new quicktime::AudioTrackAtom(new PCMSoundSampleDescription(ULAW)));
    }
    if (codecName == "PCMA" || codecName == "pcma") {
        m_movieAtom.setAudioTrackAtom(new quicktime::AudioTrackAtom(new PCMSoundSampleDescription(ALAW)));
    }
}

AudioTrackAtom& MovBuilder::getAudioTrackAtom()
{
    return *(AudioTrackAtom*) m_movieAtom.getAudioTrackAtom();
}
VideoTrackAtom& MovBuilder::getVideoTrackAtom()
{
    return *(VideoTrackAtom*) m_movieAtom.getVideoTrackAtom();
}
HintTrackAtom& MovBuilder::getHintTrackAtom()
{
    return *(HintTrackAtom*) m_movieAtom.getHintTrackAtom();
}

void MovBuilder::setVideoCodec(const base::String& codecName)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    // The codec is hard coded.
    // TODO: see setAudioCodec()
    if (codecName != "H263" && codecName != "h263") {
        JLogger::jniLogError(env, CLASSNAME, "Bad video codec name [%s]", codecName.c_str());
        throw ost::Exception("Bad video codec name [" + codecName + "]");
    }
}

void MovBuilder::setVideoFrames(const MovVideoFrameContainer& videoFrames)
{
    m_videoFrames = (MovVideoFrameContainer*) &videoFrames;
}

void MovBuilder::setAudioChunks(const MovAudioChunkContainer& audioChunks)
{
    m_audioChunks = (MovAudioChunkContainer*) &audioChunks;
}

bool MovBuilder::store(MediaObjectWriter& writer)
{
    if (m_videoFrames == 0 || m_audioChunks == 0)
        return false;

    MovWriter& movWriter = (MovWriter&) writer;

    // Retrieve the number of frames and intialize the track sizes
    MovAudioChunkContainer* audioChunks((MovAudioChunkContainer*) m_audioChunks);
    // Calculates number of resulting chunks
    // which will be used when calculating the sample time
    audioChunks->rechunkalize();
    int nOfFrames(m_videoFrames->size());
    unsigned nOfAudioChunks(m_audioChunks->nOfRechunked());

    getVideoTrackAtom().initialize(nOfFrames);
    getHintTrackAtom().initialize(nOfFrames);

    // Initialize the audio track
    AudioTrackAtom & audioTrkAtm = getAudioTrackAtom();
    audioTrkAtm.initialize(nOfAudioChunks);
    if (nOfAudioChunks > 0) {
        // Initializing the sample information according to
        // rechunked audio data.
        unsigned chunkSize((*m_audioChunks).getRequestedChunkSize());
        audioTrkAtm.setSampleSize(chunkSize * nOfAudioChunks);
        audioTrkAtm.setSampleTime(chunkSize * nOfAudioChunks);
        audioTrkAtm.setSampleToChunk(chunkSize);
    }

    // Calculate the track and movie durations.
    calculateDurations();

    // Save MOV onto media. First the meta model is updated with
    // movie data media offsets and saved onto media. Second the 
    // movie data is saved onto media.

    // Store calculated offsets in meta model
    MdatAtom mdatAtom;

    if (m_isMoovAtomFirst) {
        // Set the MDAT start offset "after" the MOOV atom
        mdatAtom.setOffset(m_movieAtom.getAtomSize());

        // Simulate writing the MDAT atom in order to update the meta model
        // with proper offsets. The size of the MDAT atom is also calculated.
        storeDataAndCalculateOffsets(mdatAtom);

        // Storing the MOOV atom (meta data) onto media.
        m_movieAtom.saveGuts(movWriter);

        // Store the MDAT header onto media
        movWriter.writeDW(m_movieDataAtomSize);
        movWriter.writeDW(quicktime::MDAT);

        // Store the MDAT atom data onto media.
        storeDataAndCalculateOffsets(movWriter);
    } else {
        // The offset of the MDAT atom is set to the beginning of the file
        mdatAtom.setOffset(0);

        // Simulate writing the MDAT atom in order to update the meta model
        // with proper offsets. The size of the MDAT atom is also calculated.
        storeDataAndCalculateOffsets(mdatAtom);

        // Store the MDAT header onto media
        movWriter.writeDW(m_movieDataAtomSize);
        movWriter.writeDW(quicktime::MDAT);

        // Store the MDAT atom data onto media.
        storeDataAndCalculateOffsets(movWriter);

        // Storing the MOOV atom (meta data) onto media.
        m_movieAtom.saveGuts(movWriter);
    }

    // Update MovInfo with updated meta data.
    m_movInfo.initialize(m_movieAtom);
    return true;
}

void MovBuilder::calculateDurations()
{
    m_videoDuration = getVideoTrackAtom().getStartTimeOffset();
    m_audioDuration = (getAudioTrackAtom().getStartTimeOffset() * m_audioTimeScale) / 1000;

    // Calculate Audio duration
    MovAudioChunkContainer::iterator iter = m_audioChunks->begin();
    for (; iter != m_audioChunks->end(); iter++) {
        // Audio duration is measured in number of samples.
        m_audioDuration += iter->getLength();
    }
    // Calculate Video duratio
    MovVideoFrameContainer::iterator frameIter = m_videoFrames->begin();
    for (; frameIter != m_videoFrames->end(); frameIter++) {
        boost::ptr_list<MovRtpPacket>& frame = *frameIter;
        boost::ptr_list<MovRtpPacket>::iterator pktIter = frame.begin();
        for (; pktIter != frame.end(); pktIter++) {
            m_videoDuration += pktIter->getFrameTime();
        }
    }

    float maxDuration(float(m_audioDuration) / m_audioTimeScale);
    if (maxDuration < float(m_videoDuration) / m_movieTimeScale) {
        maxDuration = float(m_videoDuration) / m_movieTimeScale;
    }

    m_movieDuration = (unsigned) (maxDuration * m_movieTimeScale);

    m_movieAtom.getMovieHeaderAtom().setDuration(m_movieDuration);

    AudioTrackAtom & audioTrkAtom = getAudioTrackAtom();
    audioTrkAtom.getTrackHeaderAtom().setDuration(m_movieDuration);
    audioTrkAtom.getEditAtom().getEditListAtom().getEditListEntries()[1].trackDuration = m_movieDuration;
    audioTrkAtom.getEditAtom().getEditListAtom().getEditListEntries()[1].mediaTime = 0;
    audioTrkAtom.getEditAtom().getEditListAtom().getEditListEntries()[0].mediaRate = 0x00010000;
    audioTrkAtom.getEditAtom().getEditListAtom().getEditListEntries()[1].mediaRate = 0x00010000;
    audioTrkAtom.getMediaAtom().getMediaHeaderAtom().setTimeScale(m_audioTimeScale);
    audioTrkAtom.getMediaAtom().getMediaHeaderAtom().setDuration(m_audioDuration);

    VideoTrackAtom & videoTrkAtom = getVideoTrackAtom();
    videoTrkAtom.getTrackHeaderAtom().setDuration(m_movieDuration);
    videoTrkAtom.getEditAtom().getEditListAtom().getEditListEntries()[1].trackDuration = m_movieDuration;
    videoTrkAtom.getEditAtom().getEditListAtom().getEditListEntries()[1].mediaTime = 0;
    videoTrkAtom.getEditAtom().getEditListAtom().getEditListEntries()[0].mediaRate = 0x00010000;
    videoTrkAtom.getEditAtom().getEditListAtom().getEditListEntries()[1].mediaRate = 0x00010000;
    videoTrkAtom.getMediaAtom().getMediaHeaderAtom().setTimeScale(m_movieTimeScale);
    videoTrkAtom.getMediaAtom().getMediaHeaderAtom().setDuration(m_movieDuration);

    HintTrackAtom & hintTrkAtom = getHintTrackAtom();
    hintTrkAtom.getTrackHeaderAtom().setDuration(m_movieDuration);
    hintTrkAtom.getEditAtom().getEditListAtom().getEditListEntries()[1].trackDuration = m_movieDuration;
    hintTrkAtom.getEditAtom().getEditListAtom().getEditListEntries()[1].mediaTime = 0;
    hintTrkAtom.getEditAtom().getEditListAtom().getEditListEntries()[0].mediaRate = 0x00010000;
    hintTrkAtom.getEditAtom().getEditListAtom().getEditListEntries()[1].mediaRate = 0x00010000;
    hintTrkAtom.getMediaAtom().getMediaHeaderAtom().setTimeScale(m_movieTimeScale);
    hintTrkAtom.getMediaAtom().getMediaHeaderAtom().setDuration(m_movieDuration);
}

unsigned MovBuilder::storeDataAndCalculateOffsets(quicktime::AtomWriter& atomWriter)
{
    unsigned mdatSize(8);
    unsigned audioSize(storeAudio(atomWriter));
    unsigned videoSize(storeVideo(atomWriter));
    unsigned hintSize(storeHint(atomWriter));

    mdatSize += audioSize + videoSize + hintSize;
    m_movieDataAtomSize = mdatSize;

    return mdatSize;
}

unsigned MovBuilder::storeAudio(quicktime::AtomWriter& atomWriter)
{
    // TODO: need to handle non origo play start
    // Handled by edit atoms?
    unsigned size(0);
    MovAudioChunkContainer* audioChunks((MovAudioChunkContainer*) m_audioChunks);
    /*
     for (unsigned a(0); a < m_audioChunks->size(); a++) {
     MovAudioChunk& audioChunk(*(*m_audioChunks)[a]);
     size += audioChunk.getLength();
     m_audioTrackAtom.setChunkOffset(atomWriter.tell(), a);
     atomWriter.write(audioChunk.getData(), audioChunk.getLength());
     }
     */
    audioChunks->rechunkalize();
    int chunkSize(audioChunks->getRequestedChunkSize());
    char* chunk(new char[chunkSize]);
    for (unsigned a(0); a < audioChunks->nOfRechunked(); a++) {
        size += chunkSize;
        getAudioTrackAtom().setChunkOffset(atomWriter.tell(), a);
        audioChunks->getNextRechunked(chunk);
        atomWriter.write(chunk, chunkSize);
    }
    delete[] chunk;
    return size;
}

unsigned MovBuilder::storeVideo(quicktime::AtomWriter& atomWriter)
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
        getVideoTrackAtom().setChunkOffset(videoOffset, nOfFrames);
        getVideoTrackAtom().setSampleSize(videoChunkSize, nOfFrames);
        getVideoTrackAtom().setSampleTime(videoFrameTime, nOfFrames);
        nOfFrames++;

    }
    return size;
}

unsigned MovBuilder::storeHint(quicktime::AtomWriter& atomWriter)
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
        getHintTrackAtom().setChunkOffset(hintChunkOffset, frameNumber);
        getHintTrackAtom().setSampleSize(hintChunkSize, frameNumber);
        getHintTrackAtom().setSampleTime(hintFrameTime, frameNumber);
        frameNumber++;
    }
    return size;
}

void MovBuilder::setAudioStartTimeOffset(unsigned offset)
{
    getAudioTrackAtom().setStartTimeOffset(offset);
}

void MovBuilder::setVideoStartTimeOffset(unsigned offset)
{
    getVideoTrackAtom().setStartTimeOffset(offset);
}

bool MovBuilder::isIFrame(const char* payload)
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

MediaInfo& MovBuilder::getInfo()
{
    return m_movInfo;
}

const unsigned MovBuilder::getDuration()
{
    return m_movieAtom.getMovieHeaderAtom().getDuration();
}

bool MovBuilder::isAudioOnlyBuilder()
{
    return false;
}

