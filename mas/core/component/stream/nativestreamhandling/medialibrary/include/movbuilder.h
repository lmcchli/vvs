#ifndef _MovBuilder_h_
#define _MovBuilder_h_

#include "movinfo.h"
#include "MdatAtom.h"
#include "MoovAtom.h"
#include "AudioTrackAtom.h"
#include "PCMSoundSampleDescription.h"
#include "VideoTrackAtom.h"
#include "HintTrackAtom.h"
#include "mediabuilder.h"
#include "platform.h"
#include "jni.h"

namespace quicktime {
class AtomWriter;
}

class MovWriter;

/**
 * MovBuilder hold the information needed for MOV file assembly.
 * This class is used when creating a MOV file. Usage:
 * 1) Video frames are added as RTP packages (one call for each frame). 
 * 2) The audio is added once as one list of audio chunks. 
 * 3) Once all frames and audio is added the MOV data is stored.
 */
class MEDIALIB_CLASS_EXPORT MovBuilder: public MediaBuilder
{
public:
    /**
     * Default constructor.
     */
    MovBuilder(JNIEnv* env);
    virtual ~MovBuilder();

    /* Returns the state of the MOOV atom first flag.
     if true the MOOV atom will occur before the MDAT atom. */
    bool isMoovAtomFirst();

    /* Set the the state of the MOOV atom flag */
    void setMoovAtomFirst(bool moovAtomFirst);

    /* Doc in baseclass. */
    void setAudioCodec(const base::String& codecName);

    /* Doc in baseclass. */
    void setVideoCodec(const base::String& codecName);

    /* Doc in baseclass. */
    void setVideoFrames(const MovVideoFrameContainer& videoFrames);

    /* Doc in baseclass. */
    void setAudioChunks(const MovAudioChunkContainer& audioChunks);

    /* Doc in baseclass. */
    const unsigned getDuration();

    /* Doc in baseclass. */
    void setAudioStartTimeOffset(unsigned offset);

    /* Doc in baseclass. */
    void setVideoStartTimeOffset(unsigned offset);

    /* Doc in baseclass. */
    bool isIFrame(const char* payload);

    /* Doc in baseclass. */
    bool isAudioOnlyBuilder();

    /**
     * Stores video and audio as MOV data.
     */
    bool store(MediaObjectWriter& movWriter);

    /**
     * Returns the MOV infomation.
     */
    MediaInfo& getInfo();

private:
    void calculateDurations();
    unsigned storeDataAndCalculateOffsets(quicktime::AtomWriter& atomWriter);
    unsigned storeAudio(quicktime::AtomWriter& atomWriter);
    unsigned storeVideo(quicktime::AtomWriter& atomWriter);
    unsigned storeHint(quicktime::AtomWriter& atomWriter);
    /**
     * The MoovAtom doesn't know about the track atom type before it's
     * parsed, and thus the track fields of the movie atom has the type TrakAtom*.
     * But in the builder we _do_ know. And these methods does the correct cast for us.
     * TODO: Rebuild the mov parser and TrakAtom so this abhorrent mess isn't needed !
     */
    quicktime::AudioTrackAtom& getAudioTrackAtom();
    quicktime::VideoTrackAtom& getVideoTrackAtom();
    quicktime::HintTrackAtom& getHintTrackAtom();

private:
    MovInfo m_movInfo;
    MovVideoFrameContainer* m_videoFrames;
    MovAudioChunkContainer* m_audioChunks;
    unsigned m_movieDataAtomSize;
    quicktime::MoovAtom m_movieAtom;
    unsigned m_audioTimeScale;
    unsigned m_movieTimeScale;
    unsigned m_audioDuration;
    unsigned m_videoDuration;
    unsigned m_movieDuration;
    bool m_isMoovAtomFirst;
};
#endif

