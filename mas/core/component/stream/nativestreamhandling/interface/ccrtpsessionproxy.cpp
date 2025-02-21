/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 *
 * This purpose of this class is to hide as much as possible of the
 * JNI-details from the implementation.
 *
 * Some jobject references, however, must be passed to the implementation
 * class (SessionSupport) to provide the means for event dispatching to Java-space
 * for example.
 *
 * @author Jorgen Terner
 */
#include <iostream>
#include <vector>

#include "ccrtpsessionproxy.h"
#include "controltoken.h"
#include "sessionsupport.h"
#include "inboundsession.h"
#include "outboundsession.h"
#include "jniutil.h"
#include "rtppayload.h"
#include "Callback.h"
#include "mediaenvelope.h"
#include "java/mediaobject.h"
#include "mediahandler.h"
#include "recordingproperties.h"
#include "jlogger.h"
#include "streamconfiguration.h"
#include "streamcontentinfo.h"
#include "javamediastream.h"
#include "AutoGlobal.h"
#include "RTPHandlerFactory.h"
#include "RTPAudioHandler.h"
#include "RTPVideoHandler.h"
#include "MediaValidator.h"
#include "jnicontroltoken.h"
#include "jnimediaobject.h"
#include "jnimediastream.h"
#include "jnievtdispatcher.h"
#include "jnirecordingproperties.h"
#include "jnirtppayload.h"
#include "jnistreamcontentinfo.h"
#include "jnistreamconfiguration.h"

#include <base_include.h>

#include <mcheck.h>

//using namespace std;

static const char* CLASSNAME = "masjni.ccrtpadapter.ccrtpsessionproxy";

JNIEXPORT jlong JNICALL Java_com_mobeon_masp_stream_CCRTPSession_createInboundSession(JNIEnv* env, jobject,
        jobject stream)
{
    // This object is the responsibility of the session instance!
    // It is created here when there is an environment available.
    SessionSupport* rtpSession;
    jlong rtpSessionSupport = -1;
    try {
        std::auto_ptr<JavaMediaStream> mediaStream(JavaMediaStream::getInbound(stream, env));
        rtpSession = new InboundSession(env, mediaStream);
        rtpSessionSupport = (jlong) rtpSession;
        base::String sessionId = rtpSession->getCallSessionId(env, true);
        JLogger::jniLogInfo(env, CLASSNAME, "Created inbound RTP session for call: [%s]", sessionId.c_str());
    } catch (Exception& exception) {
        JLogger::jniLogWarn(env, CLASSNAME, "Caught exception while creating InboundSession: %s", exception.what());
    } catch (...) {
        // In this case, a StackException should already have been thrown
        // back to Java-space.
        JLogger::jniLogWarn(env, CLASSNAME, "Uknown exception while creating InboundSession.");
    }

    return rtpSessionSupport;
}

JNIEXPORT jlong JNICALL Java_com_mobeon_masp_stream_CCRTPSession_createOutboundSession(JNIEnv* env, jobject,
        jobject stream)
{
    // This object is the responsibility of the session instance!
    // It is created here when there is an environment available.
    SessionSupport* rtpSession;
    jlong rtpSessionSupport = -1;
    try {
        std::auto_ptr<JavaMediaStream> mediaStream(JavaMediaStream::getOutbound(stream, env));
        rtpSession = new OutboundSession(env, mediaStream);
        rtpSessionSupport = (jlong) rtpSession;
        base::String sessionId = rtpSession->getCallSessionId(env, true);
        JLogger::jniLogInfo(env, CLASSNAME, "Created outbound RTP session for call: [%s]", sessionId.c_str());
    } catch (Exception& exception) {
        JLogger::jniLogWarn(env, CLASSNAME, "Caught exception while creating OutboundSession: %s", exception.what());
    } catch (...) {
        // In this case, a StackException should already have been thrown
        // back to Java-space.
        JLogger::jniLogWarn(env, CLASSNAME, "Unknown exception while creating OutboundSession.");
    }

    return rtpSessionSupport;
}

JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_initConfiguration
(JNIEnv* env, jclass jobj, jobject configuration) {
    JLogger::jniLogInfo(env, CLASSNAME, "Initializing the Stream RTP configuration");
    java::StreamConfiguration::update(configuration, env);

    // Cannot call this method before the initial configuration has been
    // set (because of the initial thread pool size).
    try {
        SessionSupport::init(env);
    } catch (...) {
        JLogger::jniLogWarn(env, CLASSNAME, "SessionSupport init throwed an exception - check java logs");
    }
}

JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_updateConfiguration
(JNIEnv* env, jclass, jobject configuration) {
    JLogger::jniLogInfo(env, CLASSNAME, "Updating the Stream RTP configuration");
    java::StreamConfiguration::update(configuration, env);
}

// Create outbound stream
JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_create__Lcom_mobeon_masp_stream_StreamContentInfo_2Lcom_mobeon_masp_stream_StackEventNotifier_2IILjava_lang_String_2ILjava_lang_String_2IIJJ
(JNIEnv* env, jobject, jobject jContentInfo, jobject eventNotifier,
        jint localAudioPort, jint localVideoPort,
        jstring jAudioHost, jint remoteAudioPort,
        jstring jVideoHost, jint remoteVideoPort,
        jint mtu, jlong handle, jlong inboundSessionHandle) {

    JLogger::jniLogInfo(env, CLASSNAME, "Intializing outbound RTP session.");

    if (jAudioHost == NULL) {
        JLogger::jniLogWarn(env, CLASSNAME, "Can't create stream, audio host is null!");
        JNIUtil::throwStackException(
                StackException::STACK_EXCEPTION, "No audio host specified.", env);
    } else {
        const char* audioHost(NULL);
        const char* videoHost(NULL);

        try {
            audioHost = env->GetStringUTFChars(jAudioHost, 0);
            if (jVideoHost != NULL) {
                videoHost = env->GetStringUTFChars(jVideoHost, 0);
            }

            OutboundSession* session = (OutboundSession*)handle;
            base::String sessionId = session->getCallSessionId(env, true);

            JLogger::jniLogInfo(env, CLASSNAME, "Intializing outbound RTP session for call: [%s]", sessionId.c_str());

            if (inboundSessionHandle == -1) {
                JLogger::jniLogInfo(env, CLASSNAME, "Intializing outbound RTP session for call: inboundSessionHandle == -1");
                session->create(env, java::StreamContentInfo::getOutbound(jContentInfo, env),
                        (int)localAudioPort, (int)localVideoPort,
                        audioHost, (int)remoteAudioPort,
                        videoHost, (int)remoteVideoPort,
                        (int)mtu, NULL);
            }
            else {
                session->create(env, java::StreamContentInfo::getOutbound(jContentInfo, env),
                        (int)localAudioPort, (int)localVideoPort,
                        audioHost, (int)remoteAudioPort,
                        videoHost, (int)remoteVideoPort,
                        (int)mtu, (InboundSession*)inboundSessionHandle);
            }

        }
        catch (...) {
            JLogger::jniLogWarn(env, CLASSNAME, "Unexpected exception while creating StreamContentInfo instance.");
        }

        if (audioHost != NULL) {
            env->ReleaseStringUTFChars(jAudioHost, audioHost);
        }
        if (videoHost != NULL) {
            env->ReleaseStringUTFChars(jVideoHost, videoHost);
        }

        JLogger::jniLogTrace(env, CLASSNAME, "<-- creating output RTP session.");
    }

    return;
}

// Create inbound stream
JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_create__Lcom_mobeon_masp_stream_StreamContentInfo_2Lcom_mobeon_masp_stream_StackEventNotifier_2IIJ
(JNIEnv* env, jobject jobjt, jobject jContentInfo,
        jobject eventNotifier, jint localAudioPort, jint localVideoPort,
        jlong handle) {
    JLogger::jniLogInfo(env, CLASSNAME, "Intializing inbound RTP session.");

    // This object is the responsibility of the stack instance!
    // It is created here when there is an environment available.
    try {
        // This global reference is later deleted in the delete-method.
        jobject en = env->NewGlobalRef(eventNotifier);
        InboundSession* session = (InboundSession*)handle;

        JLogger::jniLogInfo(env, CLASSNAME, "Issuing CREATE: [%s]", session->getCallSessionId(env, true).c_str());
        session->create(env, java::StreamContentInfo::getInbound(jContentInfo, env), en, (int)localAudioPort, (int)localVideoPort);
    }
    catch (...) {
        // In this case, a StackException should already have been thrown
        // back to Java-space.
        JLogger::jniLogWarn(env, CLASSNAME, "Unexpected exception while creating StreamContentInfo instance.");
        return;
    }
}

JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_play
(JNIEnv* env, jobject obj, jint requestId, jobject jMediaEnvelope,
        jint playOption, jlong jCursor, jlong handle) {
    JLogger::jniLogInfo(env, CLASSNAME, "Issuing a PLAY request.");

    // Pre parsing the media object ...
    // 1) Get a media parser
    // 2) Retreive audio and video (if any) and store it in a RtpBlockHandler
    // 3) Issue a play with the RtpBlockHandler in stead of the MediaEnvelope

    // TODO: Well ... why not set up the stream and media object here?
    //       And just let the play job handle putting packages on the
    //       queue? Uh, since the code is there why not just reuse
    //       it, its simpler I think. Don't overdo it ...

    OutboundSession* outbound(0);
    try {
        SessionSupport* session = (SessionSupport*)handle;
        if(!(session != 0 && session->isOutbound())) {
            JLogger::jniLogError(env, CLASSNAME, "Can not play since session is invalid");
            return;
        }

        outbound = static_cast<OutboundSession*>(session);

        java::MediaObject javaMediaEnvelope(env, jMediaEnvelope);
        java::StreamContentInfo& contentInfo(session->getContentInfo());
        java::RTPPayload* audioPayload;
        java::RTPPayload* videoPayload;

        boost::ptr_list<MediaValidator> mediaValidators;
        RTPAudioHandler *audioHandler = RTPHandlerFactory::createAudioHandler(env, *outbound);
        if (audioHandler != 0)
        mediaValidators.push_back(audioHandler);

        RTPVideoHandler *videoHandler = RTPHandlerFactory::createVideoHandler(env, *outbound);
        if (videoHandler != 0)
        mediaValidators.push_back(videoHandler);

        MediaHandler mediaHandler(javaMediaEnvelope,
                contentInfo.getPTime(),
                contentInfo.getMaxPTime(),
                outbound->getMtu());

        JLogger::jniLogTrace(env, CLASSNAME, "Cursor : %l", (long)jCursor);

        mediaHandler.parse(mediaValidators, (long)jCursor);
        if (mediaHandler.isOk()) {
            std::auto_ptr<MediaEnvelope> mediaObject(mediaHandler.getMediaObject());

            JLogger::jniLogTrace(env, CLASSNAME, "Audio codec : %s", mediaHandler.getAudioCodec().c_str());
            if ( (audioPayload = contentInfo.getPayload(mediaHandler.getAudioCodec())) ) {
                JLogger::jniLogTrace(env, CLASSNAME, "Audio payload type : %d", audioPayload->getPayloadType());
                JLogger::jniLogTrace(env, CLASSNAME, "Audio clockrate : %lu", audioPayload->getClockRate());
                mediaObject->getSessionDescription().setAudioPayload(
                        audioPayload->getPayloadType(),
                        audioPayload->getClockRate());
            } else {
                if(contentInfo.getAudioPayload() && mediaHandler.getAudioCodec()!="") {
                    JLogger::jniLogWarn(env, CLASSNAME, "Attempted to play media of type %s but we can only play %s",
                            contentInfo.getAudioPayload()->getCodec().c_str(),
                            mediaHandler.getAudioCodec().c_str());
                }
            }

            JLogger::jniLogTrace(env, CLASSNAME, "Video codec : %s", mediaHandler.getVideoCodec().c_str());

            if ( (videoPayload = contentInfo.getPayload(mediaHandler.getVideoCodec())) ) {
                JLogger::jniLogTrace(env, CLASSNAME, "Video payload type : %d", videoPayload->getPayloadType());
                JLogger::jniLogTrace(env, CLASSNAME, "Video clockrate : %lu", videoPayload->getClockRate());
                mediaObject->getSessionDescription().setVideoPayload(
                        videoPayload->getPayloadType(),
                        videoPayload->getClockRate());
            } else {
                if(contentInfo.getVideoPayload() && mediaHandler.getVideoCodec()!="") {
                    JLogger::jniLogWarn(env, CLASSNAME, "Attempted to play media of type %ud but we can only play %s",
                            contentInfo.getVideoPayload()->getCodec().c_str(),
                            mediaHandler.getVideoCodec().c_str());
                }
            }

            mediaObject->getSessionDescription().setDtmfPayload(
                    contentInfo.getDTMFPayload().getPayloadType(),
                    contentInfo.getDTMFPayload().getClockRate());

            JLogger::jniLogInfo(env, CLASSNAME, "Issuing PLAY: [%lu:%s]", unsigned(requestId), session->getCallSessionId(env, true).c_str());

            outbound->play(env, (unsigned)requestId, mediaObject, (int)playOption, mediaHandler.getAdjustedCursor());
        } else {
            JLogger::jniLogWarn(env, CLASSNAME, "Failed to issue play due to media error! [%lu:%s]", unsigned(requestId), session->getCallSessionId(env, true).c_str());
            outbound->postCallback(new Callback(env, requestId, Callback::PLAY_COMMAND, Callback::FAILED));
        }
    }
    catch (Exception& e) {
        JLogger::jniLogWarn(env, CLASSNAME, "Caught exception while initiating media object play: %s", e.what());

        if (outbound != 0) {
            outbound->postCallback(new Callback(env, requestId, Callback::PLAY_COMMAND, Callback::FAILED));
        }
    }
    catch (std::exception& e) {
        JLogger::jniLogWarn(env, CLASSNAME, "Caught exception while initiating media object play: %s", e.what());

        if (outbound != 0) {
            outbound->postCallback(new Callback(env, requestId, Callback::PLAY_COMMAND, Callback::FAILED));
        }
    }
    catch (...) {
        // In this case, a StackException should already have been thrown
        // back to Java-space.
        JLogger::jniLogWarn(env, CLASSNAME, "Caught unknown exception while creating a initiating media object play.");
    }
}

JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_play__Ljava_lang_Object_2_3Lcom_mobeon_masp_mediaobject_IMediaEnvelope_2IJI
(JNIEnv *env, jobject obj, jobject callId, jobjectArray jMediaEnvelopes,
        jint playOption, jlong jCursor, jlong handle)
{
    JLogger::jniLogError(env, CLASSNAME, "Multi play is NOT implemented in C++!");
}

JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_record
(JNIEnv* env, jobject obj, jobject callId, jobject playMediaEnvelope,
        jlong outboundStreamHandle, jobject recordMediaEnvelope,
        jobject properties, jlong handle) {
    JLogger::jniLogInfo(env, CLASSNAME, "Issuing a RECORD request.");

    // These objects are later the responsibility of the stack instance!
    // They are created here when there is an environment available.
    std::auto_ptr<java::MediaObject> playMo(NULL);
    try {
        std::auto_ptr<java::MediaObject> recordMo(new java::MediaObject(env, recordMediaEnvelope));
        if (playMediaEnvelope != NULL) {
            playMo.reset(new java::MediaObject(env, playMediaEnvelope));
        } 

        // This object is the responsibility of the stack instance!
        // It is created here when there is an environment available.
        try {
            std::auto_ptr<RecordingProperties> prop(new RecordingProperties(env, properties));

            // This global reference is later deleted when an event is
            // sent to Java-space, or if an exception occurs.
            jobject ci = env->NewGlobalRef(callId);

            SessionSupport* session = (SessionSupport*)handle;
            if(!session->isOutbound()) {
                JLogger::jniLogInfo(env, CLASSNAME, "Issuing RECORD: [%s]", session->getCallSessionId(env).c_str());
                InboundSession *inSession = static_cast<InboundSession*>(session);
                inSession->record(env, ci, playMo, (OutboundSession*)outboundStreamHandle, recordMo, prop);
            }
        }
        catch (...) {
            // In this case, a StackException should already have been thrown
            // back to Java-space.
            JLogger::jniLogWarn(env, CLASSNAME, "Unexpected exception while creating a RecordingProperties instance.");
            return;
        }
    }
    catch (...) {
        // In this case, a StackException should already have been thrown
        // back to Java-space.
        JLogger::jniLogWarn(env, CLASSNAME, "Unexpected exception while creating a MediaEnvelope instance.");
        return;
    }
}

JNIEXPORT jlong JNICALL Java_com_mobeon_masp_stream_CCRTPSession_stop(JNIEnv* env, jobject obj, jobject callInfo,
        jlong handle)
{
    jlong stopRes;
    JLogger::jniLogInfo(env, CLASSNAME, "Issuing a STOP request.");
    // This opertion is a synchronous one, therefore there is no need
    // to create global references.

    //TODO: We still need global refs since we're not
    //doing _everything_ in the same thread. But we
    //should be able to create a simple class, in some
    //way similar to a MutexLock for taking care about
    //these cases... maybe like this:
    AutoGlobal ref(callInfo, env);
    stopRes = ((SessionSupport*) handle)->stop(env, *ref);

    return stopRes;
}

JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_cancel
(JNIEnv *env, jobject callInfo, jlong handle) {
    JLogger::jniLogInfo(env, CLASSNAME, "Issuing a CANCEL request.");

    SessionSupport* session = (SessionSupport*)handle;
    AutoGlobal ref(callInfo, env);
    if(session->isOutbound()) {
        static_cast<OutboundSession*>(session)->cancel(env, *ref);
    }
}

JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_setSkew
(JNIEnv* env, jobject, jint method, jlong skew, jlong handle) {
    JLogger::jniLogInfo(env, CLASSNAME, "Setting skew");
    ((SessionSupport*)handle)->setSkew((int)method, (long)skew);
}

JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_delete
(JNIEnv* env, jobject jobj, jlong handle, jint requestId) {
    JLogger::jniLogInfo(env, CLASSNAME, "Issuing a DELETE request: [%d]", requestId);
    SessionSupport* stack = (SessionSupport*)handle;
    stack->shutdownAndDelete(env, requestId);
}

JNIEXPORT jint JNICALL Java_com_mobeon_masp_stream_CCRTPSession_getCumulativePacketLost(JNIEnv *env, jobject,
        jlong handle)
{
    jint cumulPktLost;
    JLogger::jniLogInfo(env, CLASSNAME, "Getting RTCP, Cumulative Packet Lost");
    cumulPktLost = ((SessionSupport*) handle)->getCumulativePacketLost();
    return cumulPktLost;
}

JNIEXPORT jshort JNICALL Java_com_mobeon_masp_stream_CCRTPSession_getFractionLost(JNIEnv *env, jobject, jlong handle)
{
    jshort fraLost;
    JLogger::jniLogInfo(env, CLASSNAME, "Getting RTCP, Fraction Lost");
    fraLost = ((SessionSupport*) handle)->getFractionLost();

    return fraLost;
}

JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_send
(JNIEnv* env, jobject, jobjectArray tokens, jint nrOfTokens, jlong handle) {
    JLogger::jniLogInfo(env, CLASSNAME, "Sending control token(s), DTMF");
    int size = (int)nrOfTokens;

    std::auto_ptr<boost::ptr_list<ControlToken> > tokenList(new boost::ptr_list<ControlToken>());
    try {
        for (int i = 0; i < size; i++) {
            jobject elem = env->GetObjectArrayElement(tokens, i);
            // The ControlToken instances is owned by the stack,
            // they are not deleted here.
            tokenList->push_back(new ControlToken(elem, env));

            JLogger::jniLogTrace(env, CLASSNAME, "Added ControlToken");

            JNIUtil::deleteLocalRef(env, elem);
        }
        SessionSupport* session = (SessionSupport*)handle;
        if(session->isOutbound()) {
            static_cast<OutboundSession*>(session)->send(env, tokenList);
        }
    }
    catch (...) {
        JLogger::jniLogWarn(env, CLASSNAME, "Unexpected exception while creating token list or executing send.");
    }
}

JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_join
(JNIEnv *env, jobject, jlong outboundHandle, jlong inboundHandle,
        jboolean handleDtmfAtInbound, jboolean forwardDtmfToOutbound) {
    JLogger::jniLogInfo(env, CLASSNAME, "Issuing a JOIN request.");
    SessionSupport* outboundStack((SessionSupport*)outboundHandle);
    SessionSupport* inboundStack((SessionSupport*)inboundHandle);

    if( !inboundStack->isOutbound() && outboundStack->isOutbound()) {
        static_cast<InboundSession*>(inboundStack)->join(env,
                handleDtmfAtInbound==JNI_TRUE?true:false,
                outboundStack,
                forwardDtmfToOutbound==JNI_TRUE?true:false);
    } else {
        if (inboundStack->isOutbound()) {
            JLogger::jniLogWarn(env, CLASSNAME, "Inbound stack is outbound");
        }
        if (!outboundStack->isOutbound()) {
            JLogger::jniLogWarn(env, CLASSNAME, "Outbound stack is NOT outbound");
        }
    }
}

JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_unjoin
(JNIEnv *env, jobject, jlong outboundHandle, jlong inboundHandle) {
    JLogger::jniLogInfo(env, CLASSNAME, "Issuing an UNJOIN request.");
    SessionSupport* outboundStack((SessionSupport*)outboundHandle);
    SessionSupport* inboundStack((SessionSupport*)inboundHandle);

    if( !inboundStack->isOutbound() && outboundStack->isOutbound()) {
        static_cast<InboundSession*>(inboundStack)->unjoin(env, outboundStack);
    }
}

/**
 * Sets the reference to the Java Virtual Machine and initialized the stack.
 * Note that this method is only called once when the native library is loaded.
 * Thus, no synchronization is needed.
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
    JNIEnvProxy::init(jvm);
    JNI_LoggerOnLoad(reserved);
    JNI_MediaObjectOnLoad(reserved);
    JNI_MediaStreamOnLoad(reserved);
    JNI_EvtDispatcherOnLoad(reserved);
    JNI_ControlTokenOnLoad(reserved);
    JNI_RecordingPropertiesOnLoad(reserved);
    JNI_RtpPayloadOnLoad(reserved);
    JNI_StreamContentInfoOnLoad(reserved);
    JNI_StreamConfigurationOnLoad(reserved);

    java::StreamConfiguration::init();
    return JNI_VERSION_1_6;
}

/**
 * Tells the stack to release its resources. Note that this method might
 * be called asynchronously by the jvm so all operations must be thread-safe.
 */
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *jvm, void *reserved) {
    SessionSupport::shutdown();
    java::StreamConfiguration::cleanUp();

    JNI_LoggerOnUnload(reserved);
}

/**
 * Returns SSRC of media source
 */
JNIEXPORT jint JNICALL Java_com_mobeon_masp_stream_CCRTPSession_getSenderSSRC(JNIEnv *env, jobject, jlong inboundHandle)
{
    SessionSupport* inboundStack((SessionSupport*) inboundHandle);
    int ssrc = 0;
    if (inboundStack) {
        StreamRTPSession& inSession = inboundStack->getVideoSession();
        ssrc = inSession.getSSRC(env);
    } else {
        JLogger::jniLogWarn(env, CLASSNAME, "No inboundStack existed while trying to get SSRC!");
    }

    return ssrc;
}

/**
 * Sends a Full Intra-frame request (FIR) as RTCP feedback, for the specified SSRC
 */
JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_sendPictureFastUpdate
(JNIEnv *env, jobject, jlong outboundHandle, jint ssrc) {
    SessionSupport* outboundStack((SessionSupport*)outboundHandle);
    if (outboundStack) {
        StreamRTPSession& outSession = outboundStack->getVideoSession();
        outSession.sendPictureFastUpdateRequest(env, ssrc);
    }
    else {
        JLogger::jniLogWarn(env, CLASSNAME, "No outboundStack existed while trying to send FIR!");
    }
}

/*
 * Class:     com_mobeon_masp_stream_CCRTPSession
 * Method:    reNegotiatedSdp
 * Signature: (Lcom/mobeon/masp/stream/RTPPayload;)V
 */
JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_CCRTPSession_reNegotiatedSdp
(JNIEnv* env, jobject obj, jobject dtmfPayLoad, jlong handle) {
    try {
        java::RTPPayload javaDTMFPayload(dtmfPayLoad, env);

        SessionSupport* session = (SessionSupport*)handle;
        if(!session->isOutbound()) {
            JLogger::jniLogInfo(env, CLASSNAME, "Issuing RECORD: [%s]", session->getCallSessionId(env).c_str());
            InboundSession *inSession = static_cast<InboundSession*>(session);
            inSession->reNegotiatedSdp(env, javaDTMFPayload);
        }
    } catch (...) {
        JLogger::jniLogError(env, CLASSNAME, "Unable to create RTPPayload");

        JNIUtil::throwStackException(
                StackException::STACK_EXCEPTION, "Unable to create RTPPayload", env);
    }
}
