/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef STACKEVENTDISPATCHER_H_
#define STACKEVENTDISPATCHER_H_

#include "jni.h"

#include <base_include.h>
#include <base_std.h>

class ControlToken;

/**
 * Responsible for dispatching events back to Java-space.
 * <p>
 * Note that this class should not throw any Java-exceptions. These methods
 * are used to dispatch asynchronous events to Java-space so there is no one
 * waiting to catch any Java-exceptions.
 */
class StackEventDispatcher
{
private:
    /**
     * Sends an event to Java-space and deletes the global reference to 
     * <code>callId</code>.
     * 
     * @param mID           Method ID that identifies the event.
     * @param signature     Event method signature
     * @param eventNotifier Java-object used to send events.
     * @param callId        Identifies the call that generated this event.
     */
    static void sendEvent(jmethodID mID, const char* signature, jobject eventNotifier, jobject callId, JNIEnv* env);

    /**
     * Sends an event to Java-space and deletes the global reference to 
     * <code>callId</code>.
     * 
     * @param mID           Method ID that identifies the event.
     * @param signature     Event method signature
     * @param eventNotifier Java-object used to send events.
     * @param callId        Identifies the call that generated this event.
     * @param message       Describes the cause of this event.
     */
    static void sendEvent(jmethodID mID, const char* signature, jobject eventNotifier, jobject callId,
            const char* message, JNIEnv* env);

    /**
     * Sends an event to Java-space and deletes the global reference to 
     * <code>callId</code>.
     * 
     * @param mID           Method ID that identifies the event.
     * @param signature     Event method signature
     * @param eventNotifier Java-object used to send events.
     * @param callId        Identifies the call that generated this event.
     * @param cause         The cause of this event.
     */
    static void sendEvent(jmethodID mID, const char* signature, jobject eventNotifier, jobject callId, int cause,
            JNIEnv* env);

    /**
     * Sends an event to Java-space and deletes the global reference to 
     * <code>callId</code>.
     * 
     * @param mID           Method ID that identifies the event.
     * @param signature     Event method signature
     * @param eventNotifier Java-object used to send events.
     * @param callId        Identifies the call that generated this event.
     * @param cause         The cause of this event.
     * @param message       Describes the cause of this event.
     */
    static void sendEvent(jmethodID mID, const char* signature, jobject eventNotifier, jobject callId, int cause,
            const char* message, JNIEnv* env);

public:
    enum PlayFinishedEventCause
    {
        /** The play has finished normally. */
        PLAY_FINISHED,
        /** stop has been called. */
        PLAY_STOPPED,
        /** cancel has been called. */
        PLAY_CANCELLED,
        /** The stream has been deleted. */
        PLAY_STREAM_DELETED,
        /** The stream has been joined. */
        PLAY_STREAM_JOINED
    };

    enum RecordFinishedEventCause
    {
        /** Max recording duration reached. */
        MAX_RECORDING_DURATION_REACHED = 0,
        /** stop has been called. */
        RECORDING_STOPPED,
        /** Maximum silence duration reached. NOT IMPLEMENTED YET! */
        MAX_SILENCE_DURATION_REACHED,
        /** Silence was detected. NOT IMPLEMENTED YET! */
        SILENCE_DETECTED,
        /** The stream has been deleted. */
        STREAM_DELETED,
        /** An "abandoned stream" timeout has been reached. */
        STREAM_ABANDONED
    };
    enum RecordFailedEventCause
    {
        /** An exception has occured. */
        EXCEPTION,
        /** Min recording duration was not reached. */
        MIN_RECORDING_DURATION
    };

    /**
     * Tells that a play-operation has finished and deletes the global
     * reference to <code>callId</code>.
     * 
     * @param eventNotifier Java-object used to send events.
     * @param callId        Identifies the call that generated this event.
     * @param cause         The operation has finished due to this cause.
     * @param cursor        Milliseconds of the media played so far.
     */
    static void playFinished(jobject eventNotifier, jobject callId, PlayFinishedEventCause cause, long cursor,
            JNIEnv* env);

    /**
     * Tells that a record-operation has finished and deletes the global
     * reference to <code>callId</code>.
     * 
     * @param eventNotifier Java-object used to send events.
     * @param callId        Identifies the call that generated this event.
     * @param cause         The operation has finished due to this cause.
     */
    static void recordFinished(jobject eventNotifier, jobject callId, RecordFinishedEventCause cause, JNIEnv* env);

    /**
     * Tells that a play-operation has failed and deletes the global
     * reference to <code>callId</code>.
     * 
     * @param eventNotifier Java-object used to send events.
     * @param callId        Identifies the call that generated this event.
     * @param message       Describes the cause of failure.
     */
    static void playFailed(jobject eventNotifier, jobject callId, const char* message, JNIEnv* env);

    /**
     * Tells that a record-operation has failed and deletes the global
     * reference to <code>callId</code>.
     * 
     * @param eventNotifier Java-object used to send events.
     * @param callId        Identifies the call that generated this event.
     * @param message       Describes the cause of failure.
     */
    static void recordFailed(jobject eventNotifier, jobject callId, RecordFailedEventCause cause, const char* message,
            JNIEnv* env);

    /**
     * Tells that a call should return. This does not necessarily mean that
     * the requested operation has finished. Also deletes the global
     * reference to <code>callId</code>.
     * 
     * @param eventNotifier Java-object used to send events.
     * @param callId        Identifies the call that generated this event.
     */
    static void returnFromCall(jobject eventNotifier, jobject callId, JNIEnv* env);

    /**
     * Tells that the stream should be considered as abandoned because
     * a configured timeout without RTP-packets has been reached.
     * 
     * @param eventNotifier Java-object used to send events.
     * @param callId        Identifies the call that generated this event.
     */
    static void streamAbandoned(jobject eventNotifier, jobject callId, JNIEnv* env);

    /**
     * Tells that a control token (DTMF) has been received on the stream.
     * 
     * @param eventNotifier Java-object used to send events.
     * @param token         Control token.
     */
    static void sendToken(jobject eventNotifier, const std::auto_ptr<ControlToken>& token, JNIEnv* env);
};

#endif /*STACKEVENTDISPATCHER_H_*/
