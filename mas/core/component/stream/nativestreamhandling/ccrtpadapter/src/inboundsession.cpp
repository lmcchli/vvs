/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <base_std.h>

#include "inboundsession.h"
#include "outboundsession.h"
#include "jniutil.h"
#include "jlogger.h"
#include "playjob.h"
#include "recordjob.h"
#include "rtppayload.h"
#include "streamcontentinfo.h"
#include "streamrtpsession.h"
#include "InputProcessor.h"
#include "streamutil.h"
#include "streamconnection.h"
#include "java/mediaobject.h"
#include "RTPAudioHandler.h"
#include "RTPVideoHandler.h"
#include "RTPHandlerFactory.h"

#include <cc++/process.h>
#include <boost/ptr_container/ptr_list.hpp>

using namespace std;
using namespace ost;

static const char* CLASSNAME = "masjni.ccrtpadapter.InboundSession";

InboundSession::InboundSession(JNIEnv* env, std::auto_ptr<JavaMediaStream>& javaMediaStream) :
        SessionSupport(env, javaMediaStream), mComfortNoise(0), mRecorder(new Recorder(env)),
        mMixer(new StreamMixer(env)), mAdapters(4), mLastActivityTimeRef(0), mDTMFPayloadType(101),
        mRTPAudioHandler(0), mRTPVideoHandler(0), mOutboundSession(0)
{
    JLogger::jniLogDebug(env, CLASSNAME, "InboundSession - create at %#x", this);
}

void InboundSession::create(JNIEnv* env, std::auto_ptr<java::StreamContentInfo> contentInfo, jobject eventNotifier,
        int localAudioPort, int localVideoPort)
{
    setEventNotifier(eventNotifier);
    setContentInfo(contentInfo);
    base::String cname = getContentInfo().getCNAME();

    try {
        createLocalSessions(env, localAudioPort, localVideoPort, cname, 0, 0, NULL, NULL);
    } catch (ost::Socket* error) {
        JNIUtil::throwStackException(StackException::CREATE_LOCAL_SESSION_FAILURE,
                "InboundSession::create(): unable to open socket.", env);
        return; // if there is any error no need to continue as we will crash later
    } catch (ost::SockException const& error) {
        JNIUtil::throwStackException(StackException::CREATE_LOCAL_SESSION_FAILURE,
                "InboundSession::create(): unable to open socket.", env);
        return; // if there is any error no need to continue as we will crash later
    } catch (...) {
        JNIUtil::throwStackException(StackException::CREATE_LOCAL_SESSION_FAILURE, "InboundSession::create failed.",
                env);
        return; // if there is any error no need to continue as we will crash later
    }

    try {
        JLogger::jniLogTrace(env, CLASSNAME, "Added StreamReceiverJob to pool");

        java::RTPPayload* audioPayload(getContentInfo().getAudioPayload());
        if (audioPayload == NULL) {
            JLogger::jniLogTrace(env, CLASSNAME, "NO AUDIO PAYLOAD!");
        } else {
            JLogger::jniLogTrace(env, CLASSNAME, "AudioPayload: %s", audioPayload->toString().c_str());
            /*
             if (getAudioSession())
             {
             JNIUtil::throwStackException(
             StackException::STACK_EXCEPTION,
             "InboundSession::create: AudioSession is null");
             return;
             }
             */
            audioPayload->setPayloadFormat(getAudioSession());
        }
        java::RTPPayload* videoPayload(getContentInfo().getVideoPayload());
        if (videoPayload == NULL) {
            JLogger::jniLogTrace(env, CLASSNAME, "NO VIDEO PAYLOAD!");
        } else {
            JLogger::jniLogTrace(env, CLASSNAME, "VideoPayload: %s", videoPayload->toString().c_str());
            videoPayload->setPayloadFormat(getVideoSession());
        }

        // Initialize rtp handler
        mRTPAudioHandler = RTPHandlerFactory::createAudioHandler(env, *this);
        mRTPVideoHandler = RTPHandlerFactory::createVideoHandler(env, *this);

        attach(env);
    } catch (...) {
        JNIUtil::throwStackException(StackException::STACK_EXCEPTION,
                "InboundSession::create: Unknown exception occured", env);
    }
}

InboundSession::~InboundSession()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    // All dynamically allocated references are handles by auto-pointers.
    JLogger::jniLogTrace(env, CLASSNAME, "Calling releasePorts over JNI");
    getJavaMediaStream().releasePorts(env);
    JNIUtil::deleteGlobalRef(env, mEventNotifier);
    ProcessorGroup::instance().removeSession(*this);

    if (mRTPAudioHandler != NULL) {
        delete mRTPAudioHandler;
        mRTPAudioHandler = NULL;
    }

    if (mRTPVideoHandler != NULL) {
        delete mRTPVideoHandler;
        mRTPVideoHandler = NULL;
    }

    JLogger::jniLogDebug(env, CLASSNAME, "InboundSession - delete at %#x", this);
}

void InboundSession::record(JNIEnv* env, jobject callId, std::auto_ptr<java::MediaObject>& playMediaObject,
        OutboundSession* outboundSession, std::auto_ptr<java::MediaObject>& recordMediaObject,
        std::auto_ptr<RecordingProperties>& properties)
{
    java::StreamContentInfo& contentInfo = getContentInfo();

    base::String fileExt = recordMediaObject->getFileExtension();
    if (fileExt == "") {
        recordMediaObject->setFileExtension(contentInfo.getFileExtension());
        recordMediaObject->setContentType(contentInfo.getContentType());
    } else {
        JLogger::jniLogTrace(env, CLASSNAME, "File extension already set in media object: [%s]", fileExt.c_str());
    }

    try {
        std::auto_ptr<RecordJob> recJob(
                new RecordJob(env, *this, getEventNotifier(), callId, properties, recordMediaObject));
        std::auto_ptr<Command> cmd(new RecordCommand(*this, recJob));

        if (playMediaObject.get() != 0) {
            JLogger::jniLogTrace(env, CLASSNAME, "Play during record %s ...", fileExt.c_str());
            //            outboundSession->play(NULL, playMediaObject, 0, 0);
            /*mRecordJob->setJobToCancelWhenRecordingStarts(
             outboundSession->getCurrentPlayJob());*/
        }
        JLogger::jniLogTrace(env, CLASSNAME, "Sent record command");
        ProcessorGroup::instance().dispatchCommand(env, cmd);
    } catch (exception& e) {
        base::String msg("InboundSession::record: Exception while creating record job: ");
        msg += e.what();
        JLogger::jniLogError(env, CLASSNAME, "%s", msg.c_str());
        JNIUtil::throwStackException(StackException::STACK_EXCEPTION, msg.c_str(), callId, env);
    } catch (...) {
        base::String msg("InboundSession::record: Unknown Exception while creating record job");
        JLogger::jniLogError(env, CLASSNAME, "%s", msg.c_str());
        JNIUtil::throwStackException(StackException::STACK_EXCEPTION, msg.c_str(), callId, env);
    }
}

void InboundSession::reNegotiatedSdp(JNIEnv* env, java::RTPPayload dtmfPayload)
{
    //handling of re-negotiated DTMF Payload Type as provided in 200 OK, SDP of an Outbound call
    mDTMFPayloadType = dtmfPayload.getPayloadType();
    JLogger::jniLogTrace(env, CLASSNAME, "reNegotiatedSdp DTMFPayloadType=%d", mDTMFPayloadType);
    //DTMF is received on the audio stream
    StreamRTPSession& session = getAudioSession();
    session.getDTMFReceiver().setDTMFPayloadType(mDTMFPayloadType);

    //If needed handling for other re-negotiated fields can be added (and added to method: "reNegotiatedSdp"
}

void InboundSession::join(JNIEnv *env, bool handleDtmfAtInbound, SessionSupport* outboundSessionIn,
        bool forwardDtmfToOutbound)
{
    JLogger::jniLogTrace(env, CLASSNAME, "--> join()");
    JLogger::jniLogTrace(env, CLASSNAME, "handleDtmfAtInbound: %s", (handleDtmfAtInbound ? "true" : "false"));
    JLogger::jniLogTrace(env, CLASSNAME, "forwardDtmfToOutbound: %s", (forwardDtmfToOutbound ? "true" : "false"));

    joinLock();
    outboundSessionIn->joinLock();

    std::auto_ptr<Command> incomingJoinCommand(new IncomingJoinCommand(*this));
    ProcessorGroup::instance().dispatchCommand(env, incomingJoinCommand);

    std::auto_ptr<Command> joinToInputCommand(
            new JoinToInputCommand(*(OutboundSession*) outboundSessionIn, *this, handleDtmfAtInbound,
                    forwardDtmfToOutbound));
    ProcessorGroup::instance().dispatchCommand(env, joinToInputCommand);

    JLogger::jniLogTrace(env, CLASSNAME, "<-- join()");
}

void InboundSession::unjoin(JNIEnv *env, SessionSupport* outboundSessionIn)
{
    JLogger::jniLogTrace(env, CLASSNAME, "--> unjoin()");
    OutboundSession& outboundSession = *static_cast<OutboundSession*>(outboundSessionIn);

    joinLock();
    outboundSessionIn->joinLock();

    std::auto_ptr<Command> unjoinCommand(new UnJoinCommand(*this, outboundSession));
    ProcessorGroup::instance().dispatchCommand(env, unjoinCommand);

    JLogger::jniLogTrace(env, CLASSNAME, "<-- unjoin()");
}

long InboundSession::stop(JNIEnv* env, jobject callId)
{
    //TODO: make dispatchCommand a template..so we can do away with all casts here..
    std::auto_ptr<Command> cmd(new StopRecordCommand(*this));
    ProcessorGroup::instance().dispatchCommand(env, cmd);
    return 0;
}

void InboundSession::onControlTick(uint64 timeref)
{
    if (mLastActivityTimeRef == 0)
        mLastActivityTimeRef = timeref;

    if ((long) timeref - (long) mLastActivityTimeRef > getConfiguration().getAbandonedStreamDetectedTimeout()) {
        onAbandoned();
        mLastActivityTimeRef = timeref;
    } else {
        try {
            SessionSupport::onControlTick(timeref);
        } catch (ost::Socket* error) {
            onAbandoned();
            throw;
        } catch (ost::SockException const& error) {
            onAbandoned();
            throw;
        }
    }

    if (getRecorder().isRecording()) {
        getRecorder().onTimerTick(timeref);
    }
}

void InboundSession::onAbandoned()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    JLogger::jniLogTrace(env, CLASSNAME, "Stream abandoned, notifying CallManager");

    StackEventDispatcher::streamAbandoned(getEventNotifier(), getJavaMediaStream().getJavaInstance(), env);

    if (mRecorder->isRecording()) {
        mRecorder->abandon();
    }
}

void InboundSession::receiveData(bool isVideo, uint64 timeRef)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    uint32 timestamp(0);
    mLastActivityTimeRef = timeRef;

    try {
        StreamRTPSession& session = isVideo ? getVideoSession() : getAudioSession();
        while (session.takeInDataPacket() != -1) {
            if (session.isWaiting()) {

                timestamp = session.getFirstTimestamp();
                std::auto_ptr<const ost::AppDataUnit> adu(session.getData(timestamp));
                if (adu.get() != 0) {
                    //JLogger::jniLogTrace(env, CLASSNAME, "TEST DTMF");
                    //JLogger::jniLogTrace(env, CLASSNAME, "Type in RTP: %d", ((int)adu->getType()));
                    //JLogger::jniLogTrace(env, CLASSNAME, "Type in Config: %d", session.getDTMFReceiver().getPayloadType());
                    //JLogger::jniLogTrace(env, CLASSNAME, "Type in SDP: %d", mDTMFPayloadType);

                    if ((((int) adu->getType()) == session.getDTMFReceiver().getPayloadType()) ||
                    //TODO: Should this check only apply to outbound calls ??
                            (((int) adu->getType()) == mDTMFPayloadType)) {
                        JLogger::jniLogTrace(env, CLASSNAME, "Processing DTMF");
                        ControlToken* token(session.getDTMFReceiver().handleDTMFPacket(adu, getHandleDtmf(), env));
                        //if (token != 0) {
                        //   JLogger::jniLogTrace(env, CLASSNAME, "Redirecting DTMF [%d]", token->getDigit());
                        //   mMixer.redirect(token);
                        //   delete token;
                        //}
                        //else {
                        //   JLogger::jniLogTrace(env, CLASSNAME, "DTMF packet did not yield a token.");
                        //}
                        if (token != NULL) {
                            delete token;
                            token = NULL;
                        }
                        // Redirect packet to all connected streams
                        mMixer->redirectDTMFPacket(timestamp, adu, getContentInfo().getAudioPayload()->getPayloadType(),
                                getContentInfo().getAudioPayload()->getClockRate());

                    } else {
                        // If we are connected the packet should
                        // be sent to the other stream.
                        redirectPacket(timestamp, adu, !isVideo);

                        if (getRecorder().isRecording()) {
                            getRecorder().handlePacket(adu, isVideo);
                        } else if (!hasVideo()) {
                            getRTPAudioHandler()->defaultPacketHandler(adu);
                        }
                    }
                }
            } else {
                JLogger::jniLogTrace(env, CLASSNAME, "No packet waiting on receiveData !");
            }
        }
    } catch (ost::Socket* error) {
        JLogger::jniLogError(env, CLASSNAME, "Call RTP Abandoned");
        onAbandoned();
        throw;
    } catch (ost::SockException const& error) {
        JLogger::jniLogError(env, CLASSNAME, "Call RTP Abandoned");
        onAbandoned();
        throw;
    }
}

void InboundSession::flushDataBeforeRecording(bool isVideo)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    JLogger::jniLogTrace(env, CLASSNAME, "flushDataBeforeRecording");
    uint32 timestamp(0);
    StreamRTPSession& session = isVideo ? getVideoSession() : getAudioSession();
    while (session.isWaiting()) {
        timestamp = session.getFirstTimestamp();
        std::auto_ptr<const ost::AppDataUnit> adu(session.getData(timestamp));
        if (adu.get() != 0) {
            if ((((int) adu->getType()) != session.getDTMFReceiver().getPayloadType()) &&
            //TODO: Should this check only apply to outbound calls ??
                    (((int) adu->getType()) != mDTMFPayloadType)) {
                if (!hasVideo()) {
                    JLogger::jniLogTrace(env, CLASSNAME, "flushDataBeforeRecording calling defaultPacketHandler");
                    getRTPAudioHandler()->defaultPacketHandler(adu);
                    ;
                }
            }
        }
    }
}

void InboundSession::redirectPacket(uint32 timestamp, std::auto_ptr<const AppDataUnit>& adu, bool isAudio)
{
    mMixer->redirectPacket(timestamp, adu, isAudio);
}

bool InboundSession::isOutbound()
{
    return false;
}

bool InboundSession::canSyncSessions()
{
    return getAudioSession().canBeSynchronizedWith(getVideoSession());
}

bool InboundSession::isComfortNoiseEnabled()
{
    return false;
}

ComfortNoiseGenerator& InboundSession::getComfortNoiseGenerator()
{
    return *mComfortNoise;
}

void InboundSession::attach(JNIEnv* env)
{
    getAudioSession().enableStack();
    if (hasVideo()) {
        getVideoSession().enableStack();
    }
    ProcessorGroup::instance().addSession(env, *this);
}

void InboundSession::onUnableToReceive(bool isVideo)
{
    // shut off the new session
    onAbandoned();
}

void InboundSession::onControlDataAvailable(bool isVideo, uint64 timeRef)
{
    mLastActivityTimeRef = timeRef;

    try {
        if (isVideo) {
            getVideoSession().receiveControlData();
        } else {
            getAudioSession().receiveControlData();
        }
    } catch (ost::Socket* error) {
        onAbandoned();
        throw;
    } catch (ost::SockException const& error) {
        onAbandoned();
        throw;
    }
}

void InboundSession::onDataAvailable(bool isVideo, uint64 timeRef)
{
    receiveData(isVideo, timeRef);
}

Recorder& InboundSession::getRecorder()
{
    return *mRecorder.get();
}

void InboundSession::shutdownAndDelete(JNIEnv* env, int requestId)
{
    joinLock();

    // Let the outbound session know that the inbound session is about to go down.
    OutboundSession* oSession = getOutboundSession();
    if (oSession != NULL) {
        if (!oSession->isInboundDeleted()) {
            oSession->setInboundDeleted(true);
        } else {
            JLogger::jniLogWarn(env, CLASSNAME, "Concurrency issue avoided when deleting inbound session %#x for outbound session %#x", this, oSession);
        }
    }

    stop(env, 0);
    SessionSupport::shutdownAndDelete(env, requestId);
}

StreamMixer& InboundSession::getStreamMixer()
{
    return *mMixer.get();
}

boost::ptr_vector<ReceptionAdapter>& InboundSession::getReceptionAdapters()
{
    if (mAdapters.empty()) {
        mAdapters.push_back(new DataReceptionAdapter(*this, false));
        mAdapters.push_back(new ControlReceptionAdapter(*this, false));

        if (hasVideo()) {
            mAdapters.push_back(new DataReceptionAdapter(*this, true));
            mAdapters.push_back(new ControlReceptionAdapter(*this, true));
        }
    }
    return mAdapters;
}

RTPAudioHandler *InboundSession::getRTPAudioHandler()
{
    return mRTPAudioHandler;
}

RTPVideoHandler *InboundSession::getRTPVideoHandler()
{
    return mRTPVideoHandler;
}

OutboundSession *InboundSession::getOutboundSession()
{
    return mOutboundSession;
}

void InboundSession::setOutboundSession(OutboundSession *outboundSession)
{
    mOutboundSession = outboundSession;
}
