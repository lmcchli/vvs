#include "PCMRTPHandler.h"
#include "jlogger.h"
#include "jniutil.h"
#include "audiomediadata.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.PCMRTPHandler";
static const unsigned char ULAW_VALUE_FOR_ZERO = 0xff;
static const unsigned char ALAW_VALUE_FOR_ZERO = 0xd5;
static const unsigned int SILENCE_CUTOFF_MARGIN = 900; // Have 900ms when removing silence

PCMRTPHandler::PCMRTPHandler(JNIEnv* env, StreamRTPSession& audioSession, java::StreamContentInfo& contentInfo,
        java::RTPPayload& audioPayload, bool aLawCodec) :
        RTPAudioHandler(env, audioSession, contentInfo, audioPayload), mALawCodec(aLawCodec),
        mPreviousOriginalAudioPacketTimestamp(0), mTotalSilenceDuration(0)
{
    mAudioPacketSize = getAudioPTime() * (audioSession.getCurrentRTPClockRate() / 1000);

    mSilenceDetector.reset(new SilenceDetector(env));

    JLogger::jniLogDebug(env, CLASSNAME, "PCMRTPHandler - create at %#x", this);
}

PCMRTPHandler::~PCMRTPHandler()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mEnv), CLASSNAME, "~PCMRTPHandler - delete at %#x", this);
}

uint32 PCMRTPHandler::recordAudioPacketImpl(std::auto_ptr<const ost::AppDataUnit>& adu, uint32 extendedSeq,
        boost::ptr_list<AudioMediaData>::iterator pos, boost::ptr_list<AudioMediaData>& audioData)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
    //JLogger::jniLogTrace(env, CLASSNAME, "Handling PCMU RTP packet ... %d,%d", adu->getSeqNum(), adu->getOriginalTimestamp());

    // Correct payload
    if (adu->getSize() != mAudioPacketSize) {
        JLogger::jniLogTrace(env, CLASSNAME, "Unexpected packet size: Expected: %d  Actual: %d", mAudioPacketSize,
                adu->getSize());

        if (adu->getSize() >= 20 * mSession.getCurrentRTPClockRateMs()) {
            // Adjust the packetsize used later when storing the audio 
            // data. Note that this is only done if the used pTime is 
            // greater or equal to 20 to avoid an error if the last packet 
            // is a small one.
            mAudioPacketSize = adu->getSize();
            mPTime = mAudioPacketSize / mSession.getCurrentRTPClockRateMs();
            JLogger::jniLogTrace(env, CLASSNAME, "New Audio packet size=%d, New audio pTime=%d", mAudioPacketSize,
                    mPTime);
        }
    }

    uint32 frameTime = adu->getSize() / mSession.getCurrentRTPClockRateMs();

    if (mSilenceDetector->IsSilenceDetectionEnabled()) {
        // Ignore out of order packets when testing for silence 
        if (pos == audioData.end()) {
            //JLogger::jniLogTrace(NULL, CLASSNAME, "recordAudioPacketImpl: Checking recorded packet for silence");
            checkPacketForSilence(adu);
        } else {
            JLogger::jniLogTrace(env, CLASSNAME,
                    "recordAudioPacketImpl: Skip checking out of order packet for silence");
        }
    }

    uint8 *data = new uint8[adu->getSize()];
    memcpy(data, adu->getData(), adu->getSize());

    audioData.insert(pos, new AudioMediaData(env, data, adu->getSize(), adu->getOriginalTimestamp(), adu->getOriginalTimestamp(), extendedSeq));
    return frameTime;
}

// Only call this method if the SilenceDetector have been properly initialized
void PCMRTPHandler::checkPacketForSilence(std::auto_ptr<const ost::AppDataUnit>& adu)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (mPreviousOriginalAudioPacketTimestamp > 0) {

        uint32 timestampDiff = adu->getOriginalTimestamp() - mPreviousOriginalAudioPacketTimestamp;

        // Check if there is a gap between last received packet and this packet
        // Handle that gap as silence
        if (timestampDiff > mAudioPacketSize) {
            uint32 gapLengthMs = (timestampDiff - mAudioPacketSize) / (mSession.getCurrentRTPClockRateMs());
            JLogger::jniLogTrace(env, CLASSNAME,
                    "CheckPacketForSilence: Gap detected, handled as silence. Gap was %d ms. Current timestamp=%d ms. Previous timestamp=%d ms.",
                    gapLengthMs, adu->getOriginalTimestamp(), mPreviousOriginalAudioPacketTimestamp);

            // TODO: Have to take maxRecTime into account???
            mTotalSilenceDuration = mSilenceDetector->AddToSilenceDuration(gapLengthMs);
        }
    }
    mPreviousOriginalAudioPacketTimestamp = adu->getOriginalTimestamp();

    bool abort = false;
    mTotalSilenceDuration = mSilenceDetector->CheckSilence((char*) adu->getData(), adu->getSize(), abort);
    if (abort) {
        JLogger::jniLogTrace(env, CLASSNAME,
                "Period of silence detected. mTotalSilenceDuration=%d adu->getOriginalTimestamp=%d",
                mTotalSilenceDuration, mPreviousOriginalAudioPacketTimestamp);
        setSilenceDetected(abort);
    }
}

void PCMRTPHandler::enhanceData(boost::ptr_list<AudioMediaData>& audioData)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (!audioData.empty()) {
        boost::ptr_list<AudioMediaData>::iterator packet = audioData.begin();

        uint32 expextedTstamp = audioData.front().getRTPTimestamp();

        while (packet != audioData.end()) {
            uint32 tstamp = packet->getRTPTimestamp();

            // insert silence
            if (tstamp != expextedTstamp) {
                JLogger::jniLogTrace(env, CLASSNAME, "Inserting silence: %d - %d", tstamp, expextedTstamp);
                uint32 nPackets = (tstamp - expextedTstamp) / mAudioPacketSize;

                if (nPackets <= MAX_DROPOUT) {
                    if ((tstamp - expextedTstamp) % mAudioPacketSize > 0)
                        ++nPackets;
                    uint32 octets(tstamp - expextedTstamp);

                    for (uint32 i = 0; i < nPackets; ++i) {
                        uint32 psize = (mAudioPacketSize > octets ? octets : mAudioPacketSize);
                        uint8* payload(new uint8[psize]);

                        // Create silent packet
                        if (mALawCodec)
                            memset(payload, ALAW_VALUE_FOR_ZERO, psize);
                        else
                            memset(payload, ULAW_VALUE_FOR_ZERO, psize);

                        audioData.insert(packet,
                                new AudioMediaData(env, payload, psize, expextedTstamp, packet->getExtendedSeqNum()));
                        octets -= psize;
                        expextedTstamp += psize;
                    }
                } else {
                    JLogger::jniLogWarn(env, CLASSNAME,
                            "Trying to insert silence corresponding to %d packets, only %d is allowed!", nPackets,
                            MAX_DROPOUT);
                }
            }

            expextedTstamp = tstamp + packet->getAudioChunk().getLength();
            ++packet;
        }
    }
}

void PCMRTPHandler::trimAudio(boost::ptr_list<AudioMediaData>& mediaData, long audioSkipMs, bool onlySkipWholePackets)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    // Skip configurable amount of ms in beginning of audio data
    size_t audioSkipOctets = 8 * audioSkipMs; // 8 samples/ms
    size_t amountToSkip(audioSkipOctets);
    size_t packetSize(0);
    if (amountToSkip > 0) {
        JLogger::jniLogTrace(env, CLASSNAME, "trimAudio: Skipping %d octets from beginning of media.", amountToSkip);

        while ((amountToSkip > 0) && !mediaData.empty()) {
            AudioMediaData& elem = mediaData.front();
            packetSize = elem.getAudioChunk().getLength();
            if (packetSize <= amountToSkip) {
                // This packet shall be skipped
                mediaData.pop_front();
                amountToSkip -= packetSize;
            } else if (!onlySkipWholePackets) {
                // The first part of this packet shall be skipped
                elem.skip(amountToSkip);
                amountToSkip = 0;
            } else {
                amountToSkip = 0;
            }
        }
    }
}

void PCMRTPHandler::cutAudioFile(boost::ptr_list<AudioMediaData>& mediaData, long audioSkipMs,
        bool onlySkipWholePackets)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    size_t audioSkipOctets = 8 * audioSkipMs;  // 8 samples/ms
    size_t amountToSkip(audioSkipOctets);
    size_t packetSize(0);
    if (amountToSkip > 0) {
        JLogger::jniLogTrace(env, CLASSNAME, "cutAudioFile: Removing %d octets from end of media.", amountToSkip);

        while ((amountToSkip > 0) && !mediaData.empty()) {
            AudioMediaData& elem = mediaData.back();
            packetSize = elem.getAudioChunk().getLength();
            if (packetSize <= amountToSkip) {
                // This packet shall be skipped
                mediaData.pop_back();
                amountToSkip -= packetSize;
            } else if (!onlySkipWholePackets) {
                // The last part of this packet shall be cut
                elem.cut(amountToSkip);
                amountToSkip = 0;
            } else {
                amountToSkip = 0;
            }
        }
    }
}

void PCMRTPHandler::removeSilence(boost::ptr_list<AudioMediaData>& mediaData)
{
    // We leave some silence in the end in order to provide a better user experience
    if (mTotalSilenceDuration > SILENCE_CUTOFF_MARGIN) {
        // Remove mTotalSilenceDuration [ms] from media data, cutting part of a packet
        // if necessary
        cutAudioFile(mediaData, mTotalSilenceDuration - SILENCE_CUTOFF_MARGIN, false);
    }
}

void PCMRTPHandler::initializeSilenceDetection(unsigned long packetdur, unsigned long mindur, base::String codec,
        unsigned long initialsilencedur, unsigned long silencedur, int mode, int threshold, int initialSilenceFrames,
        int detectionFrames, int signalDeadband, int silenceDeadband, int debugLevel)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    setSilenceDetected(false);
    mPreviousOriginalAudioPacketTimestamp = 0;
    mTotalSilenceDuration = 0;
    mSilenceDetector->initialize(env, packetdur, mindur, codec, initialsilencedur, silencedur, mode, threshold,
            initialSilenceFrames, detectionFrames, signalDeadband, silenceDeadband, debugLevel);
    JLogger::jniLogTrace(env, CLASSNAME, "initialsilencedur=%d", initialsilencedur);
}
