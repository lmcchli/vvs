#include "RTPAudioHandler.h"

#include "rtppayload.h"
#include "streamcontentinfo.h"
#include "audiomediadata.h"
#include "jlogger.h"
#include "jniutil.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.RTPAudioHandler";

RTPAudioHandler::RTPAudioHandler(JNIEnv* env, StreamRTPSession& audioSession, java::StreamContentInfo& contentInfo,
        java::RTPPayload& audioPayload) :
        RTPHandler(env, audioSession, contentInfo, audioPayload), mClockRate(audioPayload.getClockRate()), mAudioPacketSize(0),
        mPTime(contentInfo.getPTime()), mMaxPTime(contentInfo.getMaxPTime()), mRecordedTime(0), mSilenceDetected(false),
        mCNPayload(contentInfo.getCNPayload())
{
    JLogger::jniLogDebug(env, CLASSNAME, "RTPAudioHandler - create at %#x", this);
}

RTPAudioHandler::~RTPAudioHandler()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mEnv), CLASSNAME, "~RTPAudioHandler - delete at %#x", this);
}

void RTPAudioHandler::recordAudioPacket(std::auto_ptr<const ost::AppDataUnit>& adu,
        boost::ptr_list<AudioMediaData>& audioData)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
    if (adu->getType() == mPayloadType) {
        if (!mReceivedPacket) {
            initializeRecordingSequence(adu->getSeqNum());
            mInitialTimestamp = adu->getOriginalTimestamp();
        }

        uint32 extendedSeq;
        bool restart;
        if (getExtendedSequenceNumber(adu, extendedSeq, restart)) {
            if (restart)
                audioData.clear();

            bool duplicate;
            boost::ptr_list<AudioMediaData>::iterator pos = getInsertPosition(extendedSeq, audioData, duplicate);

            if (!duplicate) {
                // modulo arithmetic on timestamp, assume max one wrap around
                //unsigned tempRecordedTime = 1000 * (adu->getOriginalTimestamp() - mInitialTimestamp) / mClockRate;
                uint32 tempRecordedTime = (uint32)( (((uint64)(adu->getOriginalTimestamp() - mInitialTimestamp)) * 1000) / ((uint64)mClockRate) );

                // format specific implementation in subclass
                tempRecordedTime += recordAudioPacketImpl(adu, extendedSeq, pos, audioData);

                if (tempRecordedTime > mRecordedTime)
                    mRecordedTime = tempRecordedTime;
            } else {
                JLogger::jniLogWarn(env, CLASSNAME, "Duplicate packet, ignored...");
            }
            mReceivedPacket = true;
        } else {
            JLogger::jniLogWarn(env, CLASSNAME, "Sequence number gap to large, packet ignored...");
        }
    } // ignore CN packets
    else if (getCNPayload() == 0 || adu->getType() != getCNPayload()->getPayloadType()) {
        JLogger::jniLogWarn(env, CLASSNAME, "Unexpected payload type: Expected: %d Actual: %d", (int) mPayloadType,
                (int) adu->getType());
    }
}

boost::ptr_list<AudioMediaData>::iterator RTPAudioHandler::getInsertPosition(uint32 extendedSeq,
        boost::ptr_list<AudioMediaData>& audioData, bool& duplicate)
{
    duplicate = false;

    if (audioData.empty() || extendedSeq > audioData.back().getExtendedSeqNum()) {
        return audioData.end();
    }

    bool found = false;
    bool done = false;

    boost::ptr_list<AudioMediaData>::iterator p = audioData.end();
    --p;

    do {
        if (extendedSeq > p->getExtendedSeqNum()) {
            found = true;
        } else if (extendedSeq < p->getExtendedSeqNum()) {
            if (p == audioData.begin()) {
                done = true;
            } else {
                --p;
            }
        } else {
            duplicate = true;
        }
    } while (!done && !found && !duplicate);

    if (found)
        ++p;

    return p;
}
