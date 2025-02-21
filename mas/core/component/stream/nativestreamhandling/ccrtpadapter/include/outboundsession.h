/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef OUTBOUNDSESSION_H_
#define OUTBOUNDSESSION_H_

#include <base_std.h>
#include <boost/ptr_container/ptr_vector.hpp>

#include "sessionsupport.h"
#include "Player.h"
#include "dtmfsender.h"
#include "streamconnection.h"

class PlayJob;
class InboundSession;
class MediaObject;
class CallbackQueue;
class Callback;

namespace java {
class MediaObject;
};

/**
 * This class implements the specific functionality needed for an 
 * outbound session.
 * <p>
 * For an outbound stream a StreamSenderJob is created in the create-method. 
 * To this job, PlayJobs can be added. See the StreamSenderJob class for 
 * motivation of its existance.
 * 
 * @author Jorgen Terner
 */
class OutboundSession: public SessionSupport
{
private:
    OutboundSession(OutboundSession& rhs);
    OutboundSession& operator=(const OutboundSession& rhs);

    std::auto_ptr<DTMFSender> mDtmfSender;
    std::auto_ptr<Player> mPlayer;

    InboundSession* mSessionToJoin;
    CallbackQueue* mCallbackQueue;

    std::auto_ptr<ComfortNoiseGenerator> mComfortNoise;

    size_t mJoinHandle;

    bool mHandleDtmfAtInbound;
    bool mForwardDtmfToOutbound;

    /**
     * Maximum Transmission Unit. If this value is <= 0, it is read from the
     * configuration instead
     */
    int mMtu;

    bool mIsAttached;

    /**
     * true if the referenced inbound stream is deleted.
     */
    bool mIsInboundDeleted;

    /**
     * Mutex used for synchronization when updating mIsInboundDeleted.
     */
    ost::Mutex mInboundDeletedMutex;

    InboundSession *mInboundSession;

public:
    /**
     * Creates the instance, no connections are established at this point.
     * 
     * @param javaMediaStream Wraps the Java stream instance
     *                        that owns this instance.
     */
    OutboundSession(JNIEnv* env, std::auto_ptr<JavaMediaStream>& javaMediaStream);

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
    virtual void create(JNIEnv* env, std::auto_ptr<java::StreamContentInfo> contentInfo, int localAudioPort,
            int localVideoPort, const char* audioHost, int remoteAudioPort, const char* videoHost,
            int remoteVideoPort, int mtu, InboundSession* inboundSession);

    /**
     * Sends the given data according to <code>playOption</code>. This method
     * will return as soon as a possible, which most likely is before the play
     * has finished.
     * <p>
     * <strong>Note</strong> that this method will take responsibility for
     * the given <code>MediaObject</code> instance and delete it when the
     * operation is finished! 
     * 
     * @throws UnsupportedOperationException If a new play is issued when the
     *                                       stream is already playing.
     * @throws StackException                If some other error occured.
     */
    virtual void play(JNIEnv* env, unsigned requestId, std::auto_ptr<MediaEnvelope>& mediaObject, int playOption,
            long cursor);

    /**
     * Cancels all ongoing outgoing jobs (play jobs).
     */
    virtual void cancel(JNIEnv* env, jobject callId);

    /**
     * Sends the given control tokens.
     * <p>
     * <strong>Note</strong> that this method will take responsibility for
     * the given list instance and delete it when the operation is finished! 
     * 
     * @param tokens List of tokens to send.
     */
    virtual void send(JNIEnv* env, std::auto_ptr<boost::ptr_list<ControlToken> >& tokens);

    virtual long stop(JNIEnv* env, jobject callId);

    /**
     * Is called before a join-operation.
     * <p>
     * Stops all ongoing outgoing jobs (play jobs).
     */
    void beforeJoin();

    /**
     * The method <code>cleanUp</code> <strong>MUST</strong> be called 
     * before deleting an instance.
     */
    virtual ~OutboundSession();

    virtual bool isComfortNoiseEnabled();

    virtual void attach(JNIEnv *env);

    bool isAttached();

    void detach();

    virtual ComfortNoiseGenerator& getComfortNoiseGenerator();

    Player& getPlayer();

    DTMFSender& getDtmfSender();

    size_t getJoinHandle();

    /* Documentation in baseclass. */
    virtual bool isOutbound();

    virtual void shutdownAndDelete(JNIEnv* env, int requestId);

    void performJoin();

    void prepareJoin(InboundSession& sessionToJoin, bool handleDtmfAtInbound, bool forwardDtmfToOutbound);

    void performUnjoin();

    void setCallbackQueue(CallbackQueue* queue);
    void postCallback(Callback* callback);

    int getMtu();

    bool isInboundDeleted();
    void setInboundDeleted(bool value);

    virtual void onControlTick(uint64 timeref);
};
#endif /*OUTBOUNDSESSION_H_*/
