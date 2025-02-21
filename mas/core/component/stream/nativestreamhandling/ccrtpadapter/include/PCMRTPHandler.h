#ifndef _PCMRTPHANDLER_H_
#define _PCMRTPHANDLER_H_

#include "jni.h"
#include "RTPAudioHandler.h"
#include "silencedetector.h"

class PCMRTPHandler: public RTPAudioHandler
{
public:
    PCMRTPHandler(JNIEnv* env, StreamRTPSession& audioSession, java::StreamContentInfo& contentInfo,
            java::RTPPayload& audioPayloa, bool aLawCodec);
    virtual ~PCMRTPHandler();

    /**
     * Skips the first part of audio data according to 
     * <code>audioSkipMs</code>.
     * 
     * @param mediaData      Delete the first part of this media data.
     * @param audioSkipMs    Amount of audio to skip in milliseconds.
     * @param onlySkipWholePackets If <true> only whole packets will be
     *                       skipped, if <code>false</code> the first
     *                       part of the first packet in the resulting
     *                       list is skipped if necessary.
     * 
     */
    virtual void trimAudio(boost::ptr_list<AudioMediaData>& mediaData, long audioSkipMs, bool onlySkipWholePackets);

    /**
     * Cuts the last part of audio data according to
     * <code>audioCutMs</code>.
     *
     * @param mediaData      Delete the last part of this media data.
     * @param audioSkipMs    Amount of audio to cut in milliseconds.
     * @param onlySkipWholePackets If <true> only whole packets will be
     *                       skipped, if <code>false</code> the last
     *                       part of the last packet in the resulting
     *                       list is cut if necessary.
     *
     */
    virtual void cutAudioFile(boost::ptr_list<AudioMediaData>& mediaData, long audioSkipMs, bool onlySkipWholePackets);

    /**
     * Fill gaps of missing packets with silence packets.
     *
     * @param audioData     Media data to process.
     *
     */
    virtual void enhanceData(boost::ptr_list<AudioMediaData>& audioData);

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

    /**
     * Remove silent packets in end of audio media
     * 
     * @param mediaData      Remove silence from the last part of this media data.
     * 
     */
    void /*PCMRTPHandler::*/removeSilence(boost::ptr_list<AudioMediaData>& mediaData);

protected:
    virtual uint32 recordAudioPacketImpl(std::auto_ptr<const ost::AppDataUnit>& adu, uint32 extendedSeq,
            boost::ptr_list<AudioMediaData>::iterator pos, boost::ptr_list<AudioMediaData>& audioData);

    void checkPacketForSilence(std::auto_ptr<const ost::AppDataUnit>& adu);

    bool mALawCodec;
    uint32 mPreviousOriginalAudioPacketTimestamp;
    uint32 mTotalSilenceDuration;

    std::auto_ptr<SilenceDetector> mSilenceDetector;
};

#endif
