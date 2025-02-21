#ifndef _RTPHANDLER_H_
#define _RTPHANDLER_H_

#include "jni.h"

#include "mediabuilder.h"
#include "MediaValidator.h"
#include "streamrtpsession.h"

#include <base_std.h>
#include <ccrtp/rtp.h>

namespace java {
class RTPPayload;
class StreamContentInfo;
}

class RTPHandler: public MediaValidator
{
public:
    RTPHandler(JNIEnv* env, StreamRTPSession& session, java::StreamContentInfo& contentInfo, java::RTPPayload& payload);

    virtual ~RTPHandler();

    void initializeRecording()
    {
        mReceivedPacket = false;
    }

    void initializeRecordingSequence(uint16 seq)
    {
        mCycles = 0;
        mMaxSeq = seq - 1;
    };

    virtual void validateMediaProperties(MediaParser* mediaParser) {};

    virtual void initializeBuilderProperties(MediaBuilder *mediaBuilder) {};

    static const uint32 MAX_DROPOUT;
    static const uint32 MAX_MISORDER;

protected:
    int mPayloadType;
    StreamRTPSession& mSession;
    java::StreamContentInfo& mContentInfo;

    bool getExtendedSequenceNumber(std::auto_ptr<const ost::AppDataUnit>& adu, uint32& extendedSeq, bool& restart);
    bool getExtendedSeqNumNonRecording(std::auto_ptr<const ost::AppDataUnit>& adu, uint32& extendedSeq, bool& restart);

    bool mReceivedPacket;
    bool mReceivedPacketNonRecording;
    uint16 mCyclesNonRecording;
    uint16 mMaxSeqNonRecording;

    JNIEnv* mEnv;

private:
    uint16 mCycles;
    uint16 mMaxSeq;
    uint16 mBadSeq;
    uint16 mBadSeqNonRecording;
};

#endif
