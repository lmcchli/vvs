#include <cc++/config.h> // NULL ccrtp/queuebase.h depends upon this.

#include "streamconnection.h"
#include "controltoken.h"
#include "streamrtpsession.h"
#include "streamcontentinfo.h"
#include "rtppayload.h"
#include "outboundsession.h"
#include "streamrtpsession.h"

#include "jlogger.h"
#include "jniutil.h"

#include <ccrtp/queuebase.h>

using ost::PayloadFormat;
using ost::RTPQueueBase;

static const char* CLASSNAME = "masjni.ccrtpadapter.StreamConnection";

StreamConnection::StreamConnection(OutboundSession& session, java::StreamContentInfo& inboundStreamContent) :
        mSession(&session), mContentInfo(inboundStreamContent), mSending(false), mAudioTimestamp(0), mVideoTimestamp(0),
        mPreviousInboundAudioTimestamp(0), mPreviousInboundVideoTimestamp(0)
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "StreamConnection - create at %#x", this);
}

void StreamConnection::close()
{
    mSession->performUnjoin();
}

void StreamConnection::open()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
    mSendVideo = mContentInfo.isVideo() && mSession->hasVideo();

    JLogger::jniLogTrace(env, CLASSNAME, "--> initConnection()");

    if (mSession->hasVideo()) {
        mSession->getVideoSession().purgeOutgoingQueue();
    }

    JLogger::jniLogTrace(env, CLASSNAME, "<-- initConnection()");
}

void StreamConnection::onFirstSend()
{
    mSession->getAudioSession().purgeOutgoingQueue();
    if (mSendVideo)
        mSession->getVideoSession().purgeOutgoingQueue();
}

void StreamConnection::sendAudioPacket(uint32 timestamp, std::auto_ptr<const ost::AppDataUnit>& adu)
{
    if (!mSending) {
        onFirstSend();
        mSending = true;
    }
    if (mPreviousInboundAudioTimestamp == 0) {
        mAudioTimestamp = mSession->getAudioSession().getCurrentTimestamp();
    } else {
        mAudioTimestamp += (timestamp - mPreviousInboundAudioTimestamp);
    }
    mPreviousInboundAudioTimestamp = timestamp;

    //JLogger::jniLogTrace(env, CLASSNAME, "Audio packet timestamp in inbound: %d", timestamp);
    //JLogger::jniLogTrace(env, CLASSNAME, "m_audioTimestamp: %d", mAudioTimestamp);
    //JLogger::jniLogTrace(env, CLASSNAME, "Current audio timestamp: %d", mSession->getAudioSession().getCurrentTimestamp());

    mSession->getAudioSession().putData(mAudioTimestamp, adu->getData(), adu->getSize(), true);
}

void StreamConnection::sendVideoPacket(unsigned timestamp, std::auto_ptr<const ost::AppDataUnit>& adu)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    // If there is no video session no video packets should be sent.
    if (!mSendVideo)
        return;

    if (!mSending) {
        onFirstSend();
        mSending = true;
    }

    if (mPreviousInboundVideoTimestamp == 0) {
        mVideoTimestamp = mSession->getVideoSession().getCurrentTimestamp();
    } else {
        mVideoTimestamp += (timestamp - mPreviousInboundVideoTimestamp);
    }
    mPreviousInboundVideoTimestamp = timestamp;
    JLogger::jniLogTrace(env, CLASSNAME, "Video packet timestamp in inbound: %d", timestamp);
    JLogger::jniLogTrace(env, CLASSNAME, "m_videoTimestamp: %d", mVideoTimestamp);
    JLogger::jniLogTrace(env, CLASSNAME, "Current video timestamp: %d", mSession->getVideoSession().getCurrentTimestamp());

    if (adu->isMarked()) {
        mSession->getVideoSession().setMark(true);
        JLogger::jniLogTrace(env, CLASSNAME, "Marker is set");
    }

    mSession->getVideoSession().putData(mVideoTimestamp, adu->getData(), adu->getSize(), true);
}

void StreamConnection::sendDTMFPacket(uint32 timestamp, std::auto_ptr<const ost::AppDataUnit>& adu,
        int masterPayloadType, unsigned clockrate)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
    JLogger::jniLogTrace(env, CLASSNAME, "sending DTMF packet: %d", timestamp);

    if (!mSending) {
        onFirstSend();
        mSending = true;
    }
    if (mPreviousInboundAudioTimestamp == 0) {
        mAudioTimestamp = mSession->getAudioSession().getCurrentTimestamp();
    } else {
        mAudioTimestamp += (timestamp - mPreviousInboundAudioTimestamp);
    }
    mPreviousInboundAudioTimestamp = timestamp;

    //JLogger::jniLogTrace(env, CLASSNAME, "Audio packet timestamp in inbound: %d", timestamp);
    //JLogger::jniLogTrace(env, CLASSNAME, "m_audioTimestamp: %d", mAudioTimestamp);
    //JLogger::jniLogTrace(env, CLASSNAME, "Current audio timestamp: %d", mSession->getAudioSession().getCurrentTimestamp());

    // update payloadtype
    // HL64943: Change the pay load type to the output stream session's pay load type. The DTMF packet will
    //          then be recognised as a control packet as negotiated during SIP initialisation.
    mSession->getAudioSession().setPayload(mSession->getContentInfo().getDTMFPayload().getPayloadType(), clockrate);

    mSession->getAudioSession().setMark(adu->isMarked());

    mSession->getAudioSession().putData(mAudioTimestamp, adu->getData(), adu->getSize(), true);

    // restore payloadtype for session
    mSession->getAudioSession().setPayload(masterPayloadType, clockrate);
}

void StreamConnection::sendControlToken(ControlToken* controlToken)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    // TODO: a pointer is returned as reference ... 
    // should have been a pointer
    if (mSession->getHandleDtmf()) {
        JLogger::jniLogTrace(env, CLASSNAME, "sendControlToken(%d)", controlToken->getDigit());
        //        mSession->getDtmfSender().sendToken(controlToken, mContentInfo.getAudioPayload());
        mSession->getDtmfSender().sendToken(controlToken);
    }
}

StreamConnection::~StreamConnection()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~StreamConnection - delete at %#x", this);
}
