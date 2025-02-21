/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "jlogger.h"
#include "jniutil.h"
#include "jnievtdispatcher.h"

#include "stackeventdispatcher.h"
#include "controltoken.h"

#include <iostream>

using namespace std;

static const char* CLASSNAME = "masjni.ccrtpadapter.StackEventDispatcher";

void StackEventDispatcher::playFinished(jobject eventNotifier, jobject callId, PlayFinishedEventCause cause,
        long cursor, JNIEnv* env)
{
    if (callId == NULL) {
        // Yes, callId might be NULL if the event is the result of an internal
        // method call. In this case, no event should be dispatched to 
        // Java-space. An example is when a record is issued with an initial
        // playjob. When the initial play has finished, the playFinished-event
        // should not be dispatched to Java-space.
        return;
    }

    try {
        JNIUtil::callVoidMethod(env, eventNotifier, JNIEvtDispatcher::getPlayFinishedMID(), callId, (int) cause,
                (jlong) cursor);

        JNIUtil::checkException(env, JNIEvtDispatcher::EVENT_PLAY_FINISHED_METHOD, false);
    } catch (exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "Failed to send event to Java-space: %s", e.what());
    }

    JNIUtil::deleteGlobalRef(env, callId);
    callId = NULL;
}

void StackEventDispatcher::recordFinished(jobject eventNotifier, jobject callId, RecordFinishedEventCause cause,
        JNIEnv* env)
{
    sendEvent(JNIEvtDispatcher::getRecordFinishedMID(), JNIEvtDispatcher::EVENT_RECORD_FINISHED_METHOD, eventNotifier,
            callId, (int) cause, env);
}

void StackEventDispatcher::playFailed(jobject eventNotifier, jobject callId, const char* message, JNIEnv* env)
{
    sendEvent(JNIEvtDispatcher::getPlayFailedMID(), JNIEvtDispatcher::EVENT_PLAY_FAILED_METHOD, eventNotifier, callId,
            message, env);
}

void StackEventDispatcher::recordFailed(jobject eventNotifier, jobject callId, RecordFailedEventCause cause,
        const char* message, JNIEnv* env)
{
    sendEvent(JNIEvtDispatcher::getRecordFailedMID(), JNIEvtDispatcher::EVENT_RECORD_FAILED_METHOD, eventNotifier,
            callId, (int) cause, message, env);
}

void StackEventDispatcher::returnFromCall(jobject eventNotifier, jobject callId, JNIEnv* env)
{
    sendEvent(JNIEvtDispatcher::getRetFromCallMID(), JNIEvtDispatcher::EVENT_RETURN_FROM_CALL_METHOD, eventNotifier,
            callId, env);
}

void StackEventDispatcher::sendEvent(jmethodID mID, const char* event, jobject eventNotifier, jobject callId, int cause,
        JNIEnv* env)
{
    sendEvent(mID, event, eventNotifier, callId, cause, NULL, env);
}
void StackEventDispatcher::sendEvent(jmethodID mID, const char* event, jobject eventNotifier, jobject callId,
        const char* message, JNIEnv* env)
{
    sendEvent(mID, event, eventNotifier, callId, -1, message, env);
}

void StackEventDispatcher::sendEvent(jmethodID mID, const char* event, jobject eventNotifier, jobject callId, int cause,
        const char* message, JNIEnv* env)
{

    if (callId == NULL) {
        // Yes, callId might be NULL if the event is the result of an internal
        // method call. In this case, no event should be dispatched to 
        // Java-space. An example is when a record is issued with an initial
        // playjob. When the initial play has finished, the playFinished-event
        // should not be dispatched to Java-space.
        return;
    }

    try {
        jstring messageStr;
        if (message) {
            messageStr = env->NewStringUTF(message);
        } else {
            messageStr = env->NewStringUTF("");
        }

        // Perform the method call
        JNIUtil::callVoidMethod(env, eventNotifier, mID, callId, cause, messageStr);
        JNIUtil::checkException(env, event, false);

        env->DeleteLocalRef(messageStr);
    } catch (exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "Failed to send event to Java-space: %s", e.what());
    }

    JNIUtil::deleteGlobalRef(env, callId);
    callId = NULL;
}

void StackEventDispatcher::sendEvent(jmethodID mID, const char* event, jobject eventNotifier, jobject callId,
        JNIEnv* env)
{
    try {
        // Perform the method call
        JNIUtil::callVoidMethod(env, eventNotifier, mID, callId);
        JNIUtil::checkException(env, event, false);
    } catch (exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "Failed to send event to Java-space: %s", e.what());
    }

    JNIUtil::deleteGlobalRef(env, callId);
    callId = NULL;
}

void StackEventDispatcher::streamAbandoned(jobject eventNotifier, jobject stream, JNIEnv* env)
{
    try {
        JNIUtil::callVoidMethod(env, eventNotifier, JNIEvtDispatcher::getStreamAbandonedMID(), stream);
        JNIUtil::checkException(env, JNIEvtDispatcher::EVENT_STREAM_ABANDONED_METHOD, false);
    } catch (exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "Failed to send event to Java-space: %s", e.what());
    }
}

void StackEventDispatcher::sendToken(jobject eventNotifier, const std::auto_ptr<ControlToken>& token, JNIEnv* env)
{
    try {
        // Perform the method call
        JNIUtil::callVoidMethod(env, eventNotifier, JNIEvtDispatcher::getSendTokenMID(), token->getDigit(),
                token->getVolume(), token->getDuration());

        JNIUtil::checkException(env, JNIEvtDispatcher::EVENT_SEND_TOKEN_METHOD, false);
    } catch (exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "Failed to send event to Java-space: %s", e.what());
    }
}
