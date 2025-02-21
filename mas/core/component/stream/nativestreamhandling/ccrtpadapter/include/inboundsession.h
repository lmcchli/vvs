/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef INBOUNDSESSION_H_
#define INBOUNDSESSION_H_

#include "sessionsupport.h"
#include "comfortnoisegenerator.h"
#include "Recorder.h"
#include "recordjob.h"
#include "streamconnection.h"
#include "StreamMixer.h"

#include <boost/ptr_container/ptr_list.hpp>
#include <boost/ptr_container/ptr_vector.hpp>

namespace java {
class MediaObject;
};

class ReceptionAdapter;
class RecordingProperties;
class OutboundSession;

class RTPAudioHandler;
class RTPVideoHandler;

/**
 * This class implements the specific functionality needed for an 
 * inbound session.
 * <p>
 * For an inbound stream a StreamReceiverJob is created in the create-method.
 * To this job, RecordJobs can be added. See the StreamReceiverJob class for
 * motivation of its existance.
 * 
 * @author Jorgen Terner
 */
class InboundSession: public SessionSupport
{
private:
    InboundSession(InboundSession& rhs);
    InboundSession& operator=(const InboundSession &rhs);

    std::auto_ptr<ComfortNoiseGenerator> mComfortNoise;
    std::auto_ptr<Recorder> mRecorder;
    std::auto_ptr<StreamMixer> mMixer;

    boost::ptr_vector<ReceptionAdapter> mAdapters;

    uint64 mLastActivityTimeRef;

    /** RTP payload type for DTMF (dynamic type). */
    int mDTMFPayloadType;

    RTPAudioHandler *mRTPAudioHandler;
    RTPVideoHandler *mRTPVideoHandler;

    /** The calls outbound session. */
    OutboundSession *mOutboundSession;
public:
    virtual void onControlTick(uint64 timeref);
    void onAbandoned();
    /**
     * Creates the instance, no connections are established at this point.
     *
     * @param javaMediaStream Wraps the Java stream instance
     *                        that owns this instance.
     */
    InboundSession(JNIEnv* env, std::auto_ptr<JavaMediaStream>& javaMediaStream);

    /**
     * Creates session(s) for audio or audio+video depending on specified
     * MIME-types.
     * <p>
     * <strong>Note</strong> that this method is not synchronized!. Methods
     * calling this method must guarantee that only one thread at a time
     * calls this method. It should be reasonable that only one thread at a
     * time tries to create the sessions.
     *
     * @throws CreateSessionException If the local session could not be
     *                                created.
     * @throws StackException         If some other error occured.
     */
    void create(JNIEnv* env, std::auto_ptr<java::StreamContentInfo> contentInfo, jobject eventNotifier,
            int localAudioPort, int localVideoPort);

    virtual bool isComfortNoiseEnabled();

    virtual ComfortNoiseGenerator& getComfortNoiseGenerator();

    Recorder& getRecorder();

    /**
     * Records data into the given <code>mediaObject</code> according to
     * the properties in <code>properties</code>. This method will return as
     * soon as a possible, which most likely is before the recording has
     * finished.
     * <p>
     * <strong>Note</strong> that this method will take responsibility for
     * the given <code>MediaObject</code> and <code>properties</code>
     * instances and delete them when the operation is finished!
     *
     * @throws UnsupportedOperationException If a new record is issued when the
     *                                       stream is already recording.
     * @throws StackException                If some other error occured.
     */
    virtual void record(JNIEnv* env, jobject callId, std::auto_ptr<java::MediaObject>& playMediaObject,
            OutboundSession* outboundSession, std::auto_ptr<java::MediaObject>& recordMediaObject,
            std::auto_ptr<RecordingProperties>& properties);

    virtual void reNegotiatedSdp(JNIEnv* env, java::RTPPayload dtmfPayLoad);

    /**
     * Connects/joins an outbound session to this inbound session.
     *
     * @throws UnsupportedOperationException If the outbound session is already
     *                                       joined/connected.
     * @throws StackException                If some other error occured.
     */
    virtual void join(JNIEnv *env, bool handleDtmfAtInbound, SessionSupport* outboundSession,
            bool forwardDtmfToOutbound);

    /**
     * Disconnects/unjoins an outbound session from this inbound session.
     *
     * @throws StackException If some error occured.
     */
    virtual void unjoin(JNIEnv *env, SessionSupport* outboundSession);

    /**
     * The method <code>cleanUp</code> <strong>MUST</strong> be called
     * before deleting an instance.
     */
    virtual ~InboundSession();

    /* Documentation in baseclass. */
    long stop(JNIEnv*env, jobject callId);

    void connect(StreamConnection& connection, java::StreamContentInfo& contentInfo);

    void disconnect(StreamConnection& connection);

    bool canSyncSessions();

    virtual void attach(JNIEnv* env);

    void receiveData(bool isVideo, uint64 timeRef);
    void flushDataBeforeRecording(bool isVideo);

    void onUnableToReceive(bool isVideo);

    void onControlDataAvailable(bool isVideo, uint64 timeref);

    void onDataAvailable(bool isVideo, uint64 timeRef);

    /* Documentation in baseclass. */
    virtual bool isOutbound();

    virtual void shutdownAndDelete(JNIEnv* env, int requestId);

    StreamMixer& getStreamMixer();

    boost::ptr_vector<ReceptionAdapter>& getReceptionAdapters();

    RTPAudioHandler *getRTPAudioHandler();
    RTPVideoHandler *getRTPVideoHandler();

    OutboundSession *getOutboundSession();
    void setOutboundSession(OutboundSession *outboundSession);

protected:

    void redirectPacket(uint32 timestamp, std::auto_ptr<const AppDataUnit>& adu, bool isAudio);
};

#endif /*INBOUNDSESSION_H_*/
