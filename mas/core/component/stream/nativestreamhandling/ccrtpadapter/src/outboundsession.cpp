/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

#include <base_include.h>

#include "outboundsession.h"
#include "jniutil.h"
#include "jlogger.h"
#include "playjob.h"
#include "streamcontentinfo.h"
#include "inboundsession.h"
#include "streamrtpsession.h"
#include "Processor.h"
#include "CallbackQueue.h"
#include "Callback.h"

#include "java/mediaobject.h"
#include "rtppayload.h"
#include "mediaenvelope.h"

#include <cc++/process.h>

using namespace std;
using namespace ost;

static const char* CLASSNAME = "masjni.ccrtpadapter.OutboundSession";

//I know letting this escape is somewhat dangerous, but with DTMFSender it should
//be safe ( for now ... )
OutboundSession::OutboundSession(JNIEnv* env, std::auto_ptr<JavaMediaStream>& javaMediaStream) :
        SessionSupport(env, javaMediaStream), mDtmfSender(0), mPlayer(0), mSessionToJoin(0), mComfortNoise(0),
        mHandleDtmfAtInbound(true), mForwardDtmfToOutbound(false), mIsAttached(false), mIsInboundDeleted(false),
        mInboundSession(NULL)
{
    mDtmfSender.reset(new DTMFSender(env, *this));
    mPlayer.reset(new Player(env, *this));

    JLogger::jniLogDebug(env, CLASSNAME, "OutboundSession - create at %#x", this);
}

void OutboundSession::create(JNIEnv* env, std::auto_ptr<java::StreamContentInfo> contentInfo, int localAudioPort,
        int localVideoPort, const char* audioHost, int remoteAudioPort, const char* videoHost,
        int remoteVideoPort, int mtu, InboundSession* inboundSession)
{
    mMtu = mtu;
    try {
        mDtmfSender->setClockRate(contentInfo->getDTMFPayload().getClockRate());
        mDtmfSender->setPTime(contentInfo->getPTime());
        setContentInfo(contentInfo);
        JLogger::jniLogTrace(env, CLASSNAME, "OutboundSession create inboundSession=%#x.", inboundSession);
        try {
            if ((inboundSession == NULL) || (inboundSession <= 0)) {
                createLocalSessions(env, localAudioPort, localVideoPort, getContentInfo().getCNAME());
            } else {
                createLocalSessions(env, localAudioPort, localVideoPort, inboundSession->getContentInfo().getCNAME(),
                        inboundSession->getAudioSession().getLocalSSRC(),
                        (inboundSession->hasVideo() ? inboundSession->getVideoSession().getLocalSSRC() : 0),
                        &(inboundSession->getAudioSession()),
                        (inboundSession->hasVideo() ? &(inboundSession->getVideoSession()) : 0));
            }
        } catch (...) {
            // Catch the exception that createLocalSessions throws when Socket creation fails
            JLogger::jniLogError(env, CLASSNAME, "OutboundSession::create(): Failed to call createLocalSessions().");
            ostringstream message("OutboundSession::create(): Failed to call createLocalSessions().");
            buildErrorMessage(message, NULL, audioHost, remoteAudioPort, remoteAudioPort + 1);
            JNIUtil::throwStackException(StackException::CREATE_LOCAL_SESSION_FAILURE, message.str().c_str(), env);
            return;
        }

        // Setup audio session
        InetHostAddress remoteAudioHost(audioHost);

        if (!getAudioSession().addDestination(remoteAudioHost, remoteAudioPort, remoteAudioPort + 1)) {
            ostringstream message("OutboundSession::create(): Failed to add audiosession destination.");
            buildErrorMessage(message, NULL, remoteAudioHost.getHostname(), remoteAudioPort, remoteAudioPort + 1);
            JNIUtil::throwStackException(StackException::STACK_EXCEPTION, message.str().c_str(), env);
            return;
        }

        JLogger::jniLogTrace(env, CLASSNAME, "Added audio destination host=%d, port=%d", remoteAudioHost.getAddress(),
                remoteAudioPort);

        int mtu = (mMtu > 0) ? mMtu : getConfiguration().getMaximumTransmissionUnit();
        JLogger::jniLogTrace(env, CLASSNAME, "MaximumTransmissionUnit=%d", mtu);

        getAudioSession().setMaxSendSegmentSize(mtu);

        java::RTPPayload* audioPayload = getContentInfo().getAudioPayload();
        if (audioPayload != NULL) {
            JLogger::jniLogTrace(env, CLASSNAME, "AudioPayload: %s", audioPayload->toString().c_str());
            getAudioSession().setBandwidthModifiers(env, audioPayload->getBwSender(), audioPayload->getBwReceiver());
            getAudioSession().setPayload(audioPayload->getPayloadType(), audioPayload->getClockRate());
        } else {
            JLogger::jniLogWarn(env, CLASSNAME, "No audio payload!");
        }

        // Setup video session
        if (localVideoPort != -1) {
            InetHostAddress remoteVideoHost(videoHost);

            if (!getVideoSession().addDestination(remoteVideoHost, remoteVideoPort, remoteVideoPort + 1)) {
                ostringstream message("OutboundSession::create: Failed to add videosession destination: ");
                buildErrorMessage(message, NULL, remoteVideoHost.getHostname(), remoteVideoPort, remoteVideoPort + 1);
                JNIUtil::throwStackException(StackException::CREATE_LOCAL_SESSION_FAILURE, message.str().c_str(), env);
                return;
            }

            JLogger::jniLogTrace(env, CLASSNAME, "Added video destination host=%s, port=%d",
                    remoteVideoHost.getHostname(), remoteVideoPort);

            getVideoSession().setMaxSendSegmentSize(mtu);

            java::RTPPayload* videoPayload = getContentInfo().getVideoPayload();
            if (videoPayload != NULL) {
                JLogger::jniLogTrace(env, CLASSNAME, "VideoPayload: %s", videoPayload->toString().c_str());
                getVideoSession().setBandwidthModifiers(env, videoPayload->getBwSender(),
                        videoPayload->getBwReceiver());
                getVideoSession().setPayload(videoPayload->getPayloadType(), videoPayload->getClockRate());
            } else {
                JLogger::jniLogWarn(env, CLASSNAME, "No video payload!");
            }
        }
        // Let the referenced inboundsession know which outbound session that refers to it.
        if (inboundSession != 0) {
            JLogger::jniLogDebug(env, CLASSNAME, "the outboundsession %#x is stored in the inboundSession %#x", this,
                    inboundSession);
            inboundSession->setOutboundSession(this);
            mInboundSession = inboundSession;
        }

        attach(env);
        JLogger::jniLogTrace(env, CLASSNAME, "Added StreamSenderJob to pool");
    } catch (...) {
        JNIUtil::throwStackException(StackException::STACK_EXCEPTION,
                "OutboundSession::create: Unknown exception occured", env);
    }
}

OutboundSession::~OutboundSession()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    // All dynamically allocated references are handles by auto-pointers.
    mInboundSession->setOutboundSession(NULL);
    ProcessorGroup::instance().removeSession(*this);
    JLogger::jniLogDebug(env, CLASSNAME, "~OutboundSession - delete at %#x", this);
}

void OutboundSession::play(JNIEnv *env, unsigned requestId, std::auto_ptr<MediaEnvelope>& mediaObject, int playOption,
        long cursor)
{
    // This is a new media object that shall be played so create a new
    // play job.
    try {
        std::auto_ptr<PlayJob> job(new PlayJob(env, *this, requestId, playOption, cursor, mediaObject));
        std::auto_ptr<Command> cmd(new PlayCommand(*this, job));
        ProcessorGroup::instance().dispatchCommand(env, cmd);
    } catch (exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "OutboundSession::play: Exception while creating play job: %s", e.what());
    } catch (...) {
        JLogger::jniLogError(env, CLASSNAME, "OutboundSession::play: Unknown Exception while creating play job");
    }
}

void OutboundSession::beforeJoin()
{
    if (mPlayer->isPlaying()) {
        mPlayer->joined();
    }
}

long OutboundSession::stop(JNIEnv* env, jobject callId)
{
    std::auto_ptr<Command> cmd(new StopPlayCommand(*this));
    std::auto_ptr<StopPlayCommand> result((StopPlayCommand*) ProcessorGroup::instance().dispatchCommand(env, cmd).release());
    return result->getResult();
}

void OutboundSession::cancel(JNIEnv* env, jobject callId)
{
    stop(env, callId);
}

void OutboundSession::send(JNIEnv* env, std::auto_ptr<boost::ptr_list<ControlToken> >& tokens)
{
    try {
        mDtmfSender->addToSendList(tokens);
    } catch (exception& e) {
        base::String msg("OutboundSession::send: Exception while sending tokens: ");
        msg += e.what();
        JLogger::jniLogError(env, CLASSNAME, "%s", msg.c_str());
        JNIUtil::throwStackException(StackException::STACK_EXCEPTION, msg.c_str(), env);
    } catch (...) {
        base::String msg("OutboundSession::send: Unknown Exception while sending tokens.");
        JLogger::jniLogError(env, CLASSNAME, "%s", msg.c_str());
        JNIUtil::throwStackException(StackException::STACK_EXCEPTION, msg.c_str(), env);
    }
}

DTMFSender& OutboundSession::getDtmfSender()
{
    return *mDtmfSender;
}

bool OutboundSession::isOutbound()
{
    return true;
}

void OutboundSession::attach(JNIEnv* env)
{
    mIsAttached = true;
    getAudioSession().enableStack();
    if (hasVideo()) {
        getVideoSession().enableStack();
    }
    ProcessorGroup::instance().addSession(env, *this);
}

bool OutboundSession::isComfortNoiseEnabled()
{
    return false;
}

ComfortNoiseGenerator& OutboundSession::getComfortNoiseGenerator()
{
    return *mComfortNoise;
}

Player& OutboundSession::getPlayer()
{
    return *mPlayer;
}

void OutboundSession::shutdownAndDelete(JNIEnv *env, int requestId)
{
    joinLock();
    stop(env, 0);
    deleteJavaStream(env);
    SessionSupport::shutdownAndDelete(env, requestId);
}

void OutboundSession::performJoin()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    beforeJoin();
    try {
        if (mSessionToJoin == 0) {
            JLogger::jniLogWarn(env, CLASSNAME, "Attemption to join to a NULL inbound!");
            return;
        }

        java::StreamContentInfo & joinedContentInfo = mSessionToJoin->getContentInfo();

        std::auto_ptr<StreamConnection> conn(new StreamConnection(*this, joinedContentInfo));
        mJoinHandle = mSessionToJoin->getStreamMixer().addConnection(conn);
        mSessionToJoin->setHandleDtmf(mHandleDtmfAtInbound);
        setHandleDtmf(mForwardDtmfToOutbound);

        mDtmfSender->setMasterPayloadFormat(joinedContentInfo.getAudioPayload()->getPayloadType(),
                joinedContentInfo.getAudioPayload()->getClockRate());
        // HL64943: Keep the out-bound pay load type to what has been negotiated during session
        // initialization (See SIP negotiation).

        joinUnlock();
        mSessionToJoin->joinUnlock();
        mSessionToJoin = 0;
        return;
    } catch (...) {
        joinUnlock();
        if (mSessionToJoin != 0) {
            mSessionToJoin->joinUnlock();
            mSessionToJoin = 0;
        }
    }
}

void OutboundSession::prepareJoin(InboundSession& sessionToJoin, bool handleDtmfAtInbound, bool forwardDtmfToOutbound)
{
    mSessionToJoin = &sessionToJoin;
    mHandleDtmfAtInbound = handleDtmfAtInbound;
    mForwardDtmfToOutbound = forwardDtmfToOutbound;
}

size_t OutboundSession::getJoinHandle()
{
    return mJoinHandle;
}

void OutboundSession::performUnjoin()
{
    mJoinHandle = 0;
    mHandleDtmfAtInbound = true;
    mForwardDtmfToOutbound = false;
    setHandleDtmf(false);
    joinUnlock();
}

void OutboundSession::setCallbackQueue(CallbackQueue* queue)
{
    mCallbackQueue = queue;
}

void OutboundSession::postCallback(Callback* callback)
{
    mCallbackQueue->push(callback);
}

int OutboundSession::getMtu()
{
    return mMtu;
}

bool OutboundSession::isInboundDeleted()
{
    // guarded by the joinlock
    return mIsInboundDeleted;
}

void OutboundSession::setInboundDeleted(bool value)
{
    ost::MutexLock lock(mInboundDeletedMutex);
    mIsInboundDeleted = value;
}

void OutboundSession::onControlTick(uint64 timeref)
{
    TryMutexLock lock(mInboundDeletedMutex);

    // Only transmit RTCP SR if the inbound session is still available.
    if (lock.succeded() && !mIsInboundDeleted) {
        // In case of exception - reuse inboudsession abandoned event
        try {
            SessionSupport::onControlTick(timeref);
        } catch (ost::Socket* error) {
            mInboundSession->onAbandoned();
            throw;
        } catch (ost::SockException const& error) {
            mInboundSession->onAbandoned();
            throw;
        }
    }
}
