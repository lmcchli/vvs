#ifndef _RTPAUDIOHANDLER_H_
#define _RTPAUDIOHANDLER_H_

#include "RTPHandler.h"

#include <base_std.h> 
#include <base_include.h>
#include <boost/ptr_container/ptr_list.hpp>

class AudioMediaData;

class RTPAudioHandler: public RTPHandler
{
public:
    RTPAudioHandler(JNIEnv*env, StreamRTPSession& audioSession, java::StreamContentInfo& contentInfo,
            java::RTPPayload& audioPayload);

    virtual ~RTPAudioHandler();

    virtual void initializeRecording()
    {
        RTPHandler::initializeRecording();
        mRecordedTime = 0;
    }

    void recordAudioPacket(std::auto_ptr<const ost::AppDataUnit>& adu, boost::ptr_list<AudioMediaData>& audioData);
    virtual void defaultPacketHandler(std::auto_ptr<const ost::AppDataUnit>& adu) {};
    virtual bool requireNonRecordingPackets()
    {
        return false;
    };
    virtual void trimAudio(boost::ptr_list<AudioMediaData>& mediaData, long audioSkipMs, bool onlySkipWholePackets) = 0;

    virtual void enhanceData(boost::ptr_list<AudioMediaData>& audioData) {};

    unsigned getRecordedTime()
    {
        return mRecordedTime;
    };
    uint32 getAudioPacketSize()
    {
        return mAudioPacketSize;
    };
    uint32 getAudioPTime()
    {
        return mPTime;
    };

    virtual void initializeSilenceDetection(unsigned long packetdur, unsigned long mindur, base::String codec,
            unsigned long initialsilencedur, unsigned long silencedur, int mode, int threshold,
            int initialSilenceFrames, int detectionFrames, int signalDeadband, int silenceDeadband, int debugLevel)
    {
        setSilenceDetected(false);
    };

    virtual void removeSilence(boost::ptr_list<AudioMediaData>& mediaData) {};

    bool getSilenceDetected()
    {
        return mSilenceDetected;
    };
    void setSilenceDetected(bool silenceDetected)
    {
        mSilenceDetected = silenceDetected;
    };
    virtual void onTimerTick(uint64 timeref) {};
    virtual void onStopRecording() {};
protected:
    virtual uint32 recordAudioPacketImpl(std::auto_ptr<const ost::AppDataUnit>& adu, uint32 extendedSeq,
            boost::ptr_list<AudioMediaData>::iterator pos, boost::ptr_list<AudioMediaData>& audioData) = 0;

    boost::ptr_list<AudioMediaData>::iterator getInsertPosition(uint32 extendedSeq,
            boost::ptr_list<AudioMediaData>& audioData, bool& duplicate);

    java::RTPPayload *getCNPayload()
    {
        return mCNPayload;
    };

    int mClockRate;

    uint32 mInitialTimestamp;

    // parameters needed specifically for PCMU when storing the media.
    /** Expected audio packet size. */
    size_t mAudioPacketSize;

    /** pTime used for audio. */
    uint32 mPTime;

    /** max pTime used for audio. */
    uint32 mMaxPTime;

private:
    unsigned mRecordedTime;
    bool mSilenceDetected;
    java::RTPPayload *mCNPayload;
};

#endif
