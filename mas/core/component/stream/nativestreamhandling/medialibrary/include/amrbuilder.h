#ifndef _AmrBuilder_h_
#define _AmrBuilder_h_

#include <movinfo.h>
#include <AmrSampleDescription.h>
#include <MdatAtom.h>
#include <MoovAtom.h>
#include <FtypAtom.h>

#include "mediabuilder.h"

#include <platform.h>

#include <memory>

#include "jni.h"

namespace quicktime {
class AtomWriter;
class AmrTrackAtom;
class VideoTrackAtom;
class HintTrackAtom;
}

class MovWriter;

/**
 * AmrBuilder holds the information needed for 3gp file assembly and
 * functions to encode the file.
 * 
 * This class is used when creating a 3gp for wb file. Usage:
 * 1) Video frames are added as RTP packages (one call for each frame). 
 * 2) The audio is added once as one list of audio chunks. 
 * 3) Once all frames and audio is added the 3gpp data is stored.
 */
class MEDIALIB_CLASS_EXPORT AmrBuilder: public MediaBuilder
{
public:
    /**
     * Default constructor.
     */
    AmrBuilder(JNIEnv* env);

    /* Destructor */
    virtual ~AmrBuilder();

    /* Doc in baseclass. */
    virtual void setAudioCodec(const base::String& codecName);

    /* Doc in baseclass. */
    void setVideoCodec(const base::String& codecName);

    /* Doc in baseclass. */
    void setVideoFrames(const MovVideoFrameContainer& videoFrames);

    /* Doc in baseclass. */
    void setAudioChunks(const MovAudioChunkContainer& audioChunks);

    /* Doc in baseclass. */
    const unsigned getDuration();

    void setModeSet(unsigned short value);

    /* Doc in baseclass. */
    void setAudioStartTimeOffset(unsigned offset);

    /* Doc in baseclass. */
    void setVideoStartTimeOffset(unsigned offset);

    /* Doc in baseclass. */
    bool isIFrame(const char* payload);

    /* Doc in baseclass. */
    bool isAudioOnlyBuilder();

    /* Set wether or not the builder is audio only */
    void setAudioOnlyBuilder(bool isAudioOnly);

    /**
     * Stores video and audio as MOV data.
     */
    bool store(MediaObjectWriter& movWriter);

    /**
     * Returns the MOV infomation.
     */
    MediaInfo& getInfo();
	
	
	static const unsigned H2623_TIME_SCALE=1000;
	static const unsigned AMR_AUDIO_TIME_SCALE=8000; //the sample time for amr-wb - 8khz in hz.
	static const unsigned SAMPLES_PER_FRAME_20MS_NB=160; //number of rtp samples that make up 20ms time frame.
	static const char* NB_CLASSNAME;

protected:
	//constructor for super classes 
	AmrBuilder(JNIEnv* env,quicktime::AtomName name,unsigned audioTimeScale, unsigned movieTimeScale, unsigned samplesPer20MsFrame, unsigned modeSet);
	
    quicktime::AmrSampleDescription* m_amrSampleDescription;
    MovInfo m_movInfo;
    MovVideoFrameContainer* m_videoFrames;
    MovAudioChunkContainer* m_audioChunks;
    unsigned m_movieDataAtomSize;
    quicktime::FtypAtom m_fileTypeAtom;
    quicktime::MoovAtom m_movieAtom;
    quicktime::AmrTrackAtom* m_amrTrackAtom;
    quicktime::VideoTrackAtom* m_videoTrackAtom;
    quicktime::HintTrackAtom* m_hintTrackAtom;
    unsigned m_audioTimeScale;
    unsigned m_movieTimeScale;
	unsigned m_samplesPer20Ms; //rtp samples per 20 ms -varies depending on nb/wb
    unsigned m_audioDuration;
    unsigned m_videoDuration;
    unsigned m_movieDuration;
    unsigned short m_modeSet;
    bool mIsAudioOnly;
    JNIEnv* m_env;

private:
    void calculateDurations();
    unsigned storeDataAndCalculateOffsets(quicktime::AtomWriter& atomWriter);
    unsigned storeAudio(quicktime::AtomWriter& atomWriter);
    unsigned storeVideo(quicktime::AtomWriter& atomWriter);
    unsigned storeHint(quicktime::AtomWriter& atomWriter);

};
#endif
