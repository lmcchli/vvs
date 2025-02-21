#include "amrinfo.h"

#include "MoovAtom.h"
#include "TrakAtom.h"
#include "AmrSampleDescription.h"
#include "jlogger.h"
#include "jniutil.h"

using namespace quicktime;

const char* AmrInfo::CLASSNAME = "masjni.medialibrary.AmrInfo";

/* TS 126 101 - section 4.2.2 table 2
 * values are devived from the number of bits
 * rounded up to the nearest byte as the
 * info is stored as bytes on disk and in mem.
*/
const unsigned AmrInfo::nbModeSize[] = { 
	    12, //  #0: 95 bits/8 (bytes)=11.875 4.75 
        13, //  #1: 103 bits/8 (bytes)=12.875 5.15
        15, //  #2: 118 bits/8 (bytes)=14.75 5.90
        17, //  #3: 134 bits/8 (bytes)=16.75 6.70
        19, //  #4: 148 bits/8 (bytes)=18.5 7.40
        20, //  #5: 159 bits/8 (bytes)=19.875 7.95
        26, //  #6: 204 bits/8 (bytes)=25.5 10.2
        31, //  #7: 244 bits/8 (bytes)=30.5 12.2
        5,  //  #8: 39/5=4.875 AMR-SID (confort noise)SID 
		5,	//  #9: 38/8=4.75 GSM-EFR SID (confort noise)SID 
        6,  //  #10: 43/8=5.375 TDMA-EFR SID (confort noise)SID 
		5,  //  #11: 37/8=4.625 PDC-EFR SID
		0,  //  #12 future use
		0,  //  #13 future use
		0,	//  #14 future use
		0  	//  #15 no_data
        };

const unsigned AmrInfo::FT_NO_DATA = 0xf;
const unsigned AmrInfo::FT_SID_NB = 0x8;
const unsigned AmrInfo::MAX_FRAME_SIZE_NB = 31; //mode 7 31 as above table.
const unsigned AmrInfo::SAMPLES_PER_FRAME_20MS_NB=160;

AmrInfo::AmrInfo(JNIEnv* env) :
	m_env(env)
{
	modeSize=nbModeSize;
}


AmrInfo::~AmrInfo()
{
}

void AmrInfo::initialize(MoovAtom& moovAtom)
{
    m_videoTrack.initialize(moovAtom.getVideoTrackAtom());
    m_audioTrack.initialize(moovAtom.getAudioTrackAtom());
    m_hintTrack.initialize(moovAtom.getHintTrackAtom());
	

    // Calculating number of AMRWB/RTP packets from number of samples per chunk.
    m_nOfAudioPackets = 0;
    TrakAtom* audioTrackAtom(moovAtom.getAudioTrackAtom());
    if (audioTrackAtom != 0) {
        quicktime::StscAtom& stsc(audioTrackAtom->getSampleTableAtom().getSampleToChunkAtom());
        const int N = stsc.getEntryCount();
		JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::initialize() STSC entry Count %d",N);
        for (int i(0); i < N; i++) {
			unsigned samplesPerChunk = stsc.getSampleToChunkEntries()[i].samplesPerChunk;
            m_nOfAudioPackets += samplesPerChunk;
			JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::initialize() samaplesPerChunk %d for entry %d",samplesPerChunk,i);
        }
		JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::initialize() total nOfAudioPackets %d",m_nOfAudioPackets);
        quicktime::StsdAtom& stsd(audioTrackAtom->getSampleTableAtom().getSampleDescriptionAtom());
		//fetch the frames per Sample from the DAMR (amr specific box) atom.
		sampleDescriptionAtom *sampleInfo=stsd.getSampleDescription();
		if (sampleInfo->getName() == quicktime::SAWB || sampleInfo->getName() == quicktime::SAMR ) {
			//upcast to more specific type.
			AmrSampleDescription *amrSampleDesc = dynamic_cast<AmrSampleDescription*>(sampleInfo);
			AmrSpecificAtom	*damr=amrSampleDesc->getAMRSpecificAtom();
			
			//get the info from the damr.
			m_framesPerSample=damr->getFramesPerSample();
			m_modeSet=damr->getModeSet();
			JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::initialize() FramesPerSample %d, Mode set %x",m_framesPerSample,m_modeSet);
			
		} else {
			m_framesPerSample=1;
			m_modeSet=AmrSpecificAtom::modeSetNB;
			JLogger::jniLogWarn(m_env, CLASSNAME, "amrInfo::initialize() wrong sampleType for AMR - setting default FramesPerSample %d, Mode set %x",m_framesPerSample,m_modeSet);
		}
    }
}

bool AmrInfo::check() const
{
    if (m_videoTrack.check() == false)
        return false;
    if (m_audioTrack.check() == false)
        return false;
    if (m_hintTrack.check() == false)
        return false;

    return true;
}

int AmrInfo::getFrameCount() const
{
	JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::getFrameCount()");
    if (m_hintTrack.check())
        return m_hintTrack.getChunkOffsetCount();
    return 0;
}

int AmrInfo::getFramesPerSample() const
{
	JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::getFramesPerSample()");
    return m_framesPerSample;
}

int AmrInfo::getModeSet() const
{
	JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::getModeSet()");
    return m_modeSet;
}

int AmrInfo::getAudioChunkCount() const
{
	JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::getAudioChunkCount()");
    if (m_audioTrack.check())
        return m_audioTrack.getChunkOffsetCount();
    return 0;
}

AmrTrackInfo* AmrInfo::getHintTrack()
{
	JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::getHintTrack()");
    if (m_hintTrack.check())
        return &m_hintTrack;
    return 0;
}

AmrTrackInfo* AmrInfo::getAudioHintTrack()
{
	JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::getAudioHintTrack()");
    if (m_audioHintTrack.check())
        return &m_audioHintTrack;
    return 0;
}

AmrTrackInfo* AmrInfo::getVideoTrack()
{
	JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::getVideoTrack()");
    if (m_videoTrack.check())
        return &m_videoTrack;
    return 0;
}

AmrTrackInfo* AmrInfo::getAudioTrack()
{
	JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::getAudioTrack()");

    if (m_audioTrack.check())
        return &m_audioTrack;
    return 0;
}

bool AmrInfo::isWideBand()
{
	JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::isWideBand() false");
    return false;
}

const unsigned AmrInfo::get_sid() {
//JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::get_sid() %d", FT_SID_NB);
	return FT_SID_NB;
}
const unsigned AmrInfo::get_noData() {
	//JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::get_noData() %d ",FT_NO_DATA );
	return FT_NO_DATA;
}

const unsigned AmrInfo::get_MaxFrameSize() {
	//JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::get_MaxFrameSize() %d", MAX_FRAME_SIZE_NB);
	return MAX_FRAME_SIZE_NB;
}

const unsigned AmrInfo::get_samplesPer20msFrame() {
	//JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::get_samplesPer20msFrame() %d ", SAMPLES_PER_FRAME_20MS_NB );
	return SAMPLES_PER_FRAME_20MS_NB;
}
	
unsigned AmrInfo::getFrameSize(unsigned mode)
{
	unsigned size=0;
	if (mode <= 15) {
        size=modeSize[mode];
	}
		
	//JLogger::jniLogTrace(m_env, CLASSNAME, "amrInfo::getFrameSize() for mode %d is %d",mode,size);
	return size;
}
