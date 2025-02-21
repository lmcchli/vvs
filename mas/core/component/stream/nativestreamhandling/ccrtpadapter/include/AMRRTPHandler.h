#ifndef _AMRRTPHANDLER_H_
#define _AMRRTPHANDLER_H_

#include "jni.h"
#include "mediabuilder.h"
#include "RTPAudioHandler.h"
#include "amrinfo.h"

class AMRRTPHandler: public RTPAudioHandler
{
public:
    AMRRTPHandler(JNIEnv* env, StreamRTPSession& audioSession, java::StreamContentInfo& contentInfo,
            java::RTPPayload& audioPayload);
    virtual ~AMRRTPHandler();

    virtual void initializeRecording();

    virtual void trimAudio(boost::ptr_list<AudioMediaData>& mediaData, long audioSkipMs, bool onlySkipWholePackets);
    virtual void defaultPacketHandler(std::auto_ptr<const ost::AppDataUnit>& adu);
    virtual bool requireNonRecordingPackets()
    {
        return true;
    }
    ;
    virtual void enhanceData(boost::ptr_list<AudioMediaData>& audioData);

    virtual void validateMediaProperties(MediaParser* mediaParser);
    virtual void initializeBuilderProperties(MediaBuilder* mediaBuilder);
    virtual void onTimerTick(uint64 timeref);
    virtual void onStopRecording();
    virtual void removeSilence(boost::ptr_list<AudioMediaData>& mediaData);
    virtual void cutAudioFile(boost::ptr_list<AudioMediaData>& mediaData, unsigned long totalPacketsToCut);
    /**
     * Initialize silence detection paramaters.
     * This method must be called before every record to reset
     * silence detection state. Must be implemented per codec. The
     * base implementation can be used for codecs where silence detection
     * is not implemented.
     *
     */
    void initializeSilenceDetection(unsigned long packetdur, unsigned long mindur, base::String codec,
            unsigned long initialsilencedur, unsigned long silencedur, int mode, int threshold,
            int initialSilenceFrames, int detectionFrames, int signalDeadband, int silenceDeadband, int debugLevel);

protected:
	/* constructor for super classes) */
	AMRRTPHandler(JNIEnv* env, StreamRTPSession& audioSession,
		java::StreamContentInfo& contentInfo, java::RTPPayload& audioPayload,AmrInfo *amrinfo);
		
    virtual uint32 recordAudioPacketImpl(std::auto_ptr<const ost::AppDataUnit>& adu, uint32 extendedSeq,
            boost::ptr_list<AudioMediaData>::iterator pos, boost::ptr_list<AudioMediaData>& audioData);

    void insertDTXNoData(uint32 maxFrames, uint32 tstampStart, uint32 currentTstamp, uint32 extendedSeq,
            boost::ptr_list<AudioMediaData>::iterator pos, boost::ptr_list<AudioMediaData>& audioData);
    void onReceiveSpeechFrame(bool isRecording);
    void onReceiveSIDFrame(bool isRecording);

    /** <code>true</code> if the current period is a silent one. */

    unsigned short mConfiguredModeSet;
    unsigned short mRecordedModeSet;
    uint64 mLastSpeechTimeRef;
    unsigned long mInitialSilenceThreshold;
    unsigned long mFinalSilenceThreshold;
	
    enum StreamMode
    {
        SILENCE_MODE, SPEECH_MODE
    };
    StreamMode mStreamMode;
    enum SilenceDetectorState
    {
        UNKNOWN_STATE, SPEECH_STATE, INITIAL_SILENCE_STATE, FINAL_SILENCE_STATE
    };
    SilenceDetectorState mSilenceDetectorState;
	bool mSilenceDetectionEnabled;
    uint32 mLastSpeechExtendedSeq;
    unsigned long mTotalFrames;
    unsigned long mLastSpeechFrame;
    uint32 mLastSpeechTstamp;
	AmrInfo *mAmrInfo;
	
	static const unsigned int SILENCE_CUTOFF_MARGIN;
	static const unsigned int SAMPLE_PERIOD_MS;

private:
	static const char *CLASSNAME;
};

#endif
