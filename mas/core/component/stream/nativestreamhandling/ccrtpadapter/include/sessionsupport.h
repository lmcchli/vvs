/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef SESSIONSUPPORT_H_
#define SESSIONSUPPORT_H_

#ifdef WIN32
#include <config.h> // F�r att __EXPORT ska vara definierad (Pointer.h)
#else
#include <cc++/config.h>
#endif

#include <cc++/thread.h>
#include <ccrtp/rtp.h>
#include <boost/scoped_ptr.hpp>
#include <base_std.h>

#include "jni.h"
#include "streamconfiguration.h"
#include "streamrtpsession.h"
#include "javamediastream.h"

class Processor;

class RecordingProperties;

namespace java {
class MediaObject;
class StreamContentInfo;
};

class ControlToken;
class ComfortNoiseGenerator;

#define EVENT_ANY 0xffffffff
#define EVENT_DELETED 1
#define EVENT_JOINED (1<<1)

/**
 * This class contains common functionality for inbound and outbound sessions
 * as well as stub-implementations for the specific functionality. The stub-
 * implementation exists so that the proxy-class does not need to be aware
 * of session types.
 * 
 * @author J�rgen Terner
 */
using namespace ost;

class SessionSupport
{
protected:
    ost::Semaphore mJoinLock;

    /** Used to send stack events for all calls to this stack instance. */
    jobject mEventNotifier;

    int mVideoPort;
    int mAudioPort;

private:
    /** 
     * Content information for media on this stream (RTP payloads, 
     * content type and file extension). This object is created
     * outside of this instance but is still the responsibility of this 
     * instance.
     */
    std::auto_ptr<java::StreamContentInfo> mContentInfo;

    /**
     * Wrapped Java stream instance. This is used to call methods on
     * the Java instance and to pass the stream as parameter to certain
     * events.
     */
    boost::scoped_ptr<JavaMediaStream> mJavaMediaStream;

    /** Current configuration. */
    java::StreamConfiguration mConfig;

    /** 
     * Packets that are ready to be sent within this time 
     * (in milliseconds) will be sent immediately. 
     */
    uint32 mDeltaTimeMs;

    timeval mTimeout;

    std::auto_ptr<ost::RTPApplication> mAppl;

    StreamRTPSession::SkewMethod mSkewMethod;
    long mSkew;

    std::auto_ptr<StreamRTPSession> mAudioSession;
    std::auto_ptr<StreamRTPSession> mVideoSession;

    uint32 mState;

    bool mHandleDtmf;

    base::String mCallSessionId;
    int mRequestId;

    /**
     * Creates CNAME on the format: &ltuser&gt@&lthost&gt.
     * 
     * @param cname     The resulting cname destination.
     */
    void createCNAME(std::ostringstream& cname);

    /**
     * Finds the username.
     */
    void findUserName(base::String& username);

    /**
     * @param method Integer representation of the skew method.
     * 
     * @return SkewMethod representation of the skew method.
     */
    StreamRTPSession::SkewMethod intToSkewMethod(int method);

    SessionSupport(SessionSupport &rhs);
    const SessionSupport& operator=(const SessionSupport &rhs);

protected:
    /**
     * Creates the local sessions for audio and video.
     */
    void createLocalSessions(JNIEnv* env, int audioPort, int videoPort, base::String& cname);

    void createLocalSessions(JNIEnv* env, int audioPort, int videoPort, base::String& cname, uint32 audio_ssrc,
            uint32 video_ssrc, StreamRTPSession* audio_stream, StreamRTPSession* video_stream);

    /**
     * Adds information from the given parameters to the message.
     * 
     * @param message  Resulting error message destination.
     * @param error    Error containing additional information. Is omitted
     *                 if <code>NULL</code>.
     * @param host     Name of host to include in message. Is omitted if
     *                 <code>NULL</code>.
     * @param rtpPort  Portnumber that should be included in the message.
     * @param rtcpPort Portnumber that should be included in the message.
     */
    void buildErrorMessage(std::ostringstream&, ost::Socket*, const char*, int, int);

    void buildErrorMessage(std::ostringstream&, ost::SockException, const char*, int, int);

public:

    /**
     * @return <code>true</code> if this is an outbound stream,
     *         <code>false</code> if this is an inbound stream.
     */
    virtual bool isOutbound() = 0;

    virtual bool isComfortNoiseEnabled() = 0;

    virtual ComfortNoiseGenerator& getComfortNoiseGenerator() = 0;

    virtual void attach(JNIEnv* env) = 0;

    /**
     * Creates the instance, no connections are established at this point.
     * 
     * @param javaMediaStream Wraps the Java stream instance
     *                        that owns this instance.
     */
    SessionSupport(JNIEnv* env, std::auto_ptr<JavaMediaStream>& javaMediaStream);

    /**
     * Stops the current ongoing operation.
     * <p>
     * In the case of a <code>play</code>-operation, the media stream retains
     * all internal data of the media object in case a new <code>play</code>
     * with the same media object is issued.
     *
     * @param callId Identifies the call that initiated the operation that
     *               shall be stopped. May not be <code>null</code>.
     *                    
     * @return In the case of a <code>play</code>-operation, the current cursor
     *         in milliseconds is returned. In the case of a
     *         <code>recording</code>-operation, <code>0</code> is always
     *         returned.
     *         
     * @throws StackException If some error occured.
     */
    virtual long stop(JNIEnv* env, jobject callId);

    /**
     * The method <code>cleanUp</code> <strong>MUST</strong> be called 
     * before deleting an instance.
     */
    virtual ~SessionSupport();

    /**
     * Closes the application, closes all sessions, stops all jobs and 
     * cleans up JNI-related resources. Call this method before deleting 
     * an instance to save the destructor from the work of attaching itself 
     * to Java before cleaning up. It is also important that all jobs are
     * stopped before these resources are deleted.
     * <p>
     * The stack will send a BYE packet to every destination when a session is 
     * closed.
     * 
     * @param env Reference to Java environment used to clean up resources.
     */

    /**
     * @return Current number of lost packets.
     */
    uint32 getCumulativePacketLost();

    /**
     * The loss fraction is defined as the number of packets lost in the
     * current reportinginterval, divided by the number expected. It is 
     * expressed as the integer part after multiplying the loss fraction
     * by 256. Possible values are 0-255. If duplicates exists and the 
     * number of received packets are greater than the number expected, 
     * the loss fraction is set to zero.
     * <p>
     * Example: If 1/4 of the packets were lost, the loss fraction would be
     * 1/4*256=64.
     * 
     * @return Current loss fraction.
     */
    uint8 getFractionLost();

    /**
     * Sets the skew between the audio and video streams for a stream object.
     * <p>
     * The skew is the number of milliseconds the audio is ahead of the video.
     * If the video is ahead of the audio, this is a negative value.
     * <p>
     * <strong>Note</strong> that this method is allowed to be called before
     * the create-method.
     *
     * @param method Defines how the skew will be used.
     * @param skew   The number of milliseconds the audio is ahead of the
     *               video.
     */
    void setSkew(int method, long skew);

    void getSkew(StreamRTPSession::SkewMethod& method, long& skew);

    /**
     * Performs class initiation, which includes creating the thread pool.
     */
    static void init(JNIEnv* env);

    /**
     * Cleans up statically allocated resources.
     */
    static void shutdown();

    virtual void shutdownAndDelete(JNIEnv* env, int requestId);

    virtual void onControlTick(uint64 timeref);

    /**
     * @return Audio session.
     */
    StreamRTPSession& getAudioSession();

    /**
     * @return Video session.
     */
    StreamRTPSession& getVideoSession();

    java::StreamConfiguration& getConfiguration();

    java::StreamContentInfo& getContentInfo();

    JavaMediaStream& getJavaMediaStream();

    RTPApplication& getRTPApplication();

    jobject getEventNotifier();

    void setContentInfo(std::auto_ptr<java::StreamContentInfo> contentInfo);

    void setEventNotifier(jobject eventNotifier);

    bool hasVideo();

    std::vector<SOCKET> getAllSockets();

    uint32 setEvent(uint32 mask);
    uint32 setEvent(uint32 mask, int requestId);

    bool pendingEvent(uint32 mask);

    void clearEvent(uint32 mask);

    void joinLock();

    void joinUnlock();

    void setHandleDtmf(bool value);
    bool getHandleDtmf();

    const base::String& getCallSessionId(JNIEnv* env, bool reread = false);
    int getRequestId();

    void deleteJavaStream(JNIEnv* env);

    int getAudioPort();
    int getVideoPort();
};

#endif /*SESSIONSUPPORT_H_*/

