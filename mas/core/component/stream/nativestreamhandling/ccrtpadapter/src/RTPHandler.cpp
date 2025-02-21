#include "RTPHandler.h"

#include "jlogger.h"
#include "jniutil.h"
#include "sessionsupport.h"
#include "streamrtpsession.h"
#include "streamcontentinfo.h"
#include "rtppayload.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.RTPHandler";

const uint32 RTPHandler::MAX_DROPOUT = 3000;
const uint32 RTPHandler::MAX_MISORDER = 100;

RTPHandler::RTPHandler(JNIEnv* env, StreamRTPSession& session, java::StreamContentInfo& contentInfo,
        java::RTPPayload& payload) :
        mPayloadType(payload.getPayloadType()), mSession(session), mContentInfo(contentInfo), mEnv(env)
{
    mReceivedPacketNonRecording = false;
    JLogger::jniLogDebug(env, CLASSNAME, "RTPHandler - create at %#x", this);
}

RTPHandler::~RTPHandler()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mEnv), CLASSNAME, "~RTPHandler - delete at %#x", this);
}

bool RTPHandler::getExtendedSequenceNumber(std::auto_ptr<const ost::AppDataUnit>& adu, uint32& extendedSeq,
        bool& restart)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment(mEnv);

    static const int RTP_SEQ_MOD = (1 << 16);
    restart = false;
    uint16 seq = adu->getSeqNum();
    uint16 udelta = seq - mMaxSeq;

    if (udelta < MAX_DROPOUT) {
        /* in order, with permissible gap */
        if (seq < mMaxSeq) {
            /*
             * Sequence number wrapped.
             */
            ++mCycles;
        }
        mMaxSeq = seq;
    } else if (udelta <= RTP_SEQ_MOD - MAX_MISORDER) {
        /* the sequence number made a very large jump */
        if (mReceivedPacket && seq == mBadSeq) {
            /*
             * Two sequential packets -- assume that the other side
             * restarted without telling us so just re-sync
             * (i.e., pretend this was the first packet).
             */
            initializeRecordingSequence(seq);
            restart = true;
        } else {
            mBadSeq = (seq + 1) & (RTP_SEQ_MOD - 1);
            return false;
        }
    } else {
        /* duplicate or reordered packet */
        JLogger::jniLogTrace(env, CLASSNAME, "duplicate or reordered packet");
    }

    extendedSeq = seq | (mCycles << 16);

    return true;
}

bool RTPHandler::getExtendedSeqNumNonRecording(std::auto_ptr<const ost::AppDataUnit>& adu, uint32& extendedSeq,
        bool& restart)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment(mEnv);

    static const int RTP_SEQ_MOD = (1 << 16);
    restart = false;
    uint16 seq = adu->getSeqNum();

    if (!mReceivedPacketNonRecording) {
        mMaxSeqNonRecording = seq;
        mReceivedPacketNonRecording = true;
        extendedSeq = seq;
        mBadSeqNonRecording = seq;
        JLogger::jniLogTrace(env, CLASSNAME,
                "getExtendedSeqNumNonRecording init mMaxSeqNonRecording, mBadSeqNonRecording to %d", seq);
        return true;
    }

    uint16 udelta = seq - mMaxSeqNonRecording;

    if (udelta < MAX_DROPOUT) {
        /* in order, with permissible gap */
        if (seq < mMaxSeqNonRecording) {
            /*
             * Sequence number wrapped.
             */
            ++mCyclesNonRecording;
            JLogger::jniLogTrace(env, CLASSNAME, "getExtendedSeqNumNonRecording sequence wrap mCyclesNonRecording=%d",
                    mCyclesNonRecording);
        }
        mMaxSeqNonRecording = seq;
    } else if (udelta <= RTP_SEQ_MOD - MAX_MISORDER) {
        /* the sequence number made a very large jump */
        if (seq == mBadSeqNonRecording) {
            JLogger::jniLogTrace(env, CLASSNAME, "getExtendedSeqNumNonRecording sequence reset seq=%d", seq);
            mMaxSeqNonRecording = seq;
        } else {
            mBadSeqNonRecording = (seq + 1) & (RTP_SEQ_MOD - 1);
            JLogger::jniLogTrace(env, CLASSNAME, "getExtendedSeqNumNonRecording Bad seq=%d", seq);
            return false;
        }
    } else {
        /* duplicate or reordered packet */
        JLogger::jniLogTrace(env, CLASSNAME, "duplicate or reordered packet");
    }

    extendedSeq = seq | (mCyclesNonRecording << 16);

    return true;
}
