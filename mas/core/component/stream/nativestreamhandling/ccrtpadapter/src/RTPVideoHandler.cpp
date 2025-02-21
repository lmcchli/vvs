#include "RTPVideoHandler.h"

#include "jlogger.h"
#include "jniutil.h"
#include "videomediadata.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.RTPVideoHandler";

RTPVideoHandler::RTPVideoHandler(StreamRTPSession& videoSession, java::StreamContentInfo& contentInfo,
        java::RTPPayload& videoPayload) :
        RTPHandler(JNIUtil::getJavaEnvironment(), videoSession, contentInfo, videoPayload)
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "RTPVideoHandler - create at %#x", this);
}

RTPVideoHandler::~RTPVideoHandler()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mEnv), CLASSNAME, "~RTPVideoHandler - delete at %#x", this);
}

void RTPVideoHandler::recordVideoPacket(std::auto_ptr<const ost::AppDataUnit>& adu)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (adu->getType() == mPayloadType) {
        //jniLogDebug(NULL, CLASSNAME, "Record video packet: %d received: %d", adu->getSeqNum(), mReceivedPacket);
        if (!mReceivedPacket) {
            JLogger::jniLogTrace(env, CLASSNAME, "First video packet: %d", adu->getSeqNum());
            initializeRecordingSequence(adu->getSeqNum());
        }

        if (recordVideoPacketImpl(adu))
            mReceivedPacket = true;
    } else {
        JLogger::jniLogWarn(env, CLASSNAME, "Unexpected payload type: Expected: %d Actual: %d", (int) mPayloadType,
                (int) adu->getType());
    }
}
